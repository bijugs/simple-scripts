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

# Return an array of primary region replicas
#
# tableName - table to return regions for
#
def getPrimaryRegionEncodedNames(tableName)
  c = HBaseConfiguration.new()
  tableNameObj = TableName.valueOf(tableName)
  t = HTable.new(c, tableNameObj)
  regions = t.getRegionsInRange(t.getStartKeys[0],
                                t.getEndKeys[t.getEndKeys.size-1])
  priRegions = Array.new
  regions.each do |r|
    priRegions << r.getRegionInfo().getEncodedName()
  end
  priRegions
end

#
# Distribute the regions in the array passed uniformly across
# the server array provided
#
def distributePrimaryRegions(priRegions)
  c = HBaseConfiguration.new()
  admin = HBaseAdmin.new(c)
  servers = Array.new()
  serv = admin.getClusterStatus.getServers()
  serv.each do |s|
    servers << s.getServerName()
  end
  count=0
  priRegions.each do |r|
    puts r+" will move to "+servers[count%13]
    move r,servers[count%13]
    count+=1
  end
end

#
# Check the allocations of primary region replication
# in the region servers in the cluster
#
def getPrimaryDistribution(tableName)
  c = HBaseConfiguration.new()
  tableNameObj = TableName.valueOf(tableName)
  t = HTable.new(c, tableNameObj)
  regions = t.getRegionsInRange(t.getStartKeys[0],
                                t.getEndKeys[t.getEndKeys.size-1])

  count = Hash.new(0)
  regions.each do |r|
    #puts r.getRegionInfo().getRegionNameAsString()+" id "+r.getRegionInfo().getReplicaId().to_s()+" enc name "+r.getRegionInfo().getEncodedName()+" server name "+r.getServerName().getHostname()
    z = count[r.getServerName().getHostname()]
    count[r.getServerName().getHostname()]=z+1
  end
  count.each do |r,c|
    puts r.to_s()+" "+c.to_s()
  end
end

#
# Currently expects the table name as the first argument
# >hbase shell hbase_mv_regions.rb table_name
# Will list the region servers and the number of primar regions in each
# If user want to distribute primary regions in a round robin basis
# >hbase shell hbase_mv_regions.rb table_name dist
#
if ARGV.length() < 1
  puts "Expecting atleast one argument: table name"
else
  c = HBaseConfiguration.new()
  admin = HBaseAdmin.new(c)
  tables = admin.getTableNames()
  tables.each do |tableName|
    if tableName == ARGV[0]
      puts "****************************"
      puts "Requested table :"+tableName
      puts "****************************"
      begin
          priRegions = getPrimaryRegionEncodedNames(tableName)
          if (ARGV.length() > 1 && ARGV[1] == 'dist')
            puts "Distribute table primary regions"
            distributePrimaryRegions(priRegions)
            sleep 30
          else
            getPrimaryDistribution(tableName)
          end
      rescue org.apache.hadoop.hbase.TableNotFoundException
        true
      end
    end
  end
end
exit
