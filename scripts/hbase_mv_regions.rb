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
    if (r.getRegionInfo().getReplicaId() == 0)
      priRegions << r.getRegionInfo().getEncodedName()
    end
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
  dServers = Array.new()
  dServers = admin.getClusterStatus.getDeadServerNames()
  serv = admin.getClusterStatus.getServers()
  serv.each do |s|
    if (!dServers.include?(s))
      servers << s.getServerName()
    end
  end
  count=0
  totRS = servers.size()
  priRegions.each do |r|
    puts r+" will move to "+servers[count%totRS]
    move r,servers[count%totRS]
    count+=1
  end
  puts priRegions.size().to_s() + "primary regions moved"
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

