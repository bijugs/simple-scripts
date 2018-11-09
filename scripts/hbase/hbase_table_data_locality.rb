#
# Originally code from http://clayb.net/blog/finding-hbase-region-locations/
# Minor modifications made for ease of use
#
require 'set'
include Java
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.HColumnDescriptor
import org.apache.hadoop.hbase.HConstants
import org.apache.hadoop.hbase.HTableDescriptor
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.io.Text

import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import java.util.NoSuchElementException
import java.io.FileNotFoundException

# Return a Hash of region UUIDs to hostnames with column family stubs
#
# tableName - table to return regions for
#
# Example
# getRegionUUIDs "TestTable"
# # => {"3fe594363a2c13a3550f752db147194b"=>{"host" => "r1n1.example.com", "cfs" => {"f1" => {}, "f2" => {}},
#       "da19a80cc403daa9a8f82ac9a1253e9d"=>{"host" => "r1n2.example.com", "cfs" => {"f1" => {}, "f2" => {}}}}
#
def getRegionUUIDs(tableName)
  c = HBaseConfiguration.new()
  tableNameObj = TableName.valueOf(tableName)
  t = HTable.new(c, tableNameObj)
  regions = t.getRegionsInRange(t.getStartKeys[0],
                                t.getEndKeys[t.getEndKeys.size-1])
  count = Hash.new(0)
  regions.each do |r|
    z = count[r.getServerName().getHostname()]
    count[r.getServerName().getHostname()]=z+1
  end
  # get all column families -- XXX do all regions have to host all CF's?
  cfs = HTable.new(c, tableNameObj).getTableDescriptor.getFamilies().map{ |cf| cf.getNameAsString() }

  r_to_host = regions.map{|r| [r.getRegionInfo().getEncodedName(), Hash["host" => r.getHostname(), "cfs" => Hash[cfs.map{|cf| [cf, Hash.new()] }]]] }

  Hash[r_to_host]
end

def findHDFSBlocks(regions, tableName)
  # augment regions with HDFS block locations
  augmented = regions.clone
  c = HBaseConfiguration.new()
  fs = FileSystem.newInstance(c)
  hbase_rootdir = c.select{|r| r.getKey() == "hbase.rootdir"}.first.getValue
  tableNameObj = TableName.valueOf(tableName)
  nameSpace = tableNameObj.getNamespaceAsString
  baseTableName = tableNameObj.getQualifierAsString
  # use the default namespace if nongiven
  nameSpace = "default" if nameSpace == tableName

  regions.each do |r, values|
    values["cfs"].keys().each do |cf|
      rPath = Path.new(Pathname.new(hbase_rootdir).join("data", nameSpace, baseTableName, r, cf).to_s)
      begin
        files = fs.listFiles(rPath, true)
      rescue java.io.FileNotFoundException
        next
      end

      begin
        begin
          fStatus = files.next()
          hosts = fStatus.getBlockLocations().map { |block| Set.new(block.getHosts().to_a) }
          augmented[r]["cfs"][cf][File.basename(fStatus.getPath().toString())] = hosts
        rescue NativeException, java.util.NoSuchElementException
          fStatus = false
        end
      end until fStatus == false
    end
  end
  augmented
end

def computeLocalityByBlock(regions)
  non_local_blocks = []
  regions.each do |r, values|
    values["cfs"].each do |cf, hFiles|
      hFiles.each do |id, blocks|
        blocks.each_index do |idx|
          non_local_blocks.push(Pathname.new(r).join(cf, id, idx.to_s).to_s) unless blocks[idx].include?(values["host"])
        end
      end
    end
  end
  non_local_blocks
end

def totalBlocks(regions)
  regions.map do |r, values|
    values["cfs"].map do |cf, hFiles|
      hFiles.map do |id, blocks|
        blocks.count
      end
    end
  end.flatten().reduce(0, :+)
end

#
# Expects one argument : hbase table name
# >hbase shell hbase_table_data_locality.rb table_name
#
if ARGV.length() < 1
  puts "Expecting atleast one argument: table name"
else
  c = HBaseConfiguration.new()
  admin = HBaseAdmin.new(c)
  tables = admin.getTableNames()

  tables.each do |tableName|
    if tableName == ARGV[0]
      puts "***************************"
      puts "Stats for table "+tableName
      puts "***************************"
      begin
        regions = getRegionUUIDs(tableName)
        hdfs_blocks_by_region = findHDFSBlocks(regions, tableName)
        non_local_blocks = computeLocalityByBlock(hdfs_blocks_by_region)
        total_blocks = totalBlocks(hdfs_blocks_by_region)
        puts "Non local blocks: "+non_local_blocks.length().to_s+" total_blocks "+total_blocks.to_s
        puts "Percentage of non local blocks :"
        puts non_local_blocks.length().to_f/total_blocks if total_blocks > 0 # e.g. if table not empty or disabled
      rescue org.apache.hadoop.hbase.TableNotFoundException
        true
      end
    end
  end
end
exit

