import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.HTable
c = HBaseConfiguration.new()
tableNameObj = TableName.valueOf('HBASE_TABLE')
t = HTable.new(c, tableNameObj)
regions = t.getRegionsInRange(t.getStartKeys[0],
                              t.getEndKeys[t.getEndKeys.size-1])
priRegions = Array.new
regions.each do |r|
  if (r.getRegionInfo().getReplicaId() == 0)
    priRegions << r.getRegionInfo().getEncodedName()
  end
end
#execute merge_region command-
priRegions.each_slice(2) { |a| cmd="merge_region '#{a.at(0)}', '#{a.at(1)}'"; eval (cmd) }
