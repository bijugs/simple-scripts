#
# Python script to calculate the values to enable HBase Bucket cache
# 
def is_number(val):
    try:
        float(val)
        return True
    except ValueError:
        return False

tot = raw_input("Enter total physical memory available for HBase JVM (MB) :")
while not is_number(tot):
    tot = raw_input("Value error : Enter total physical memory available for HBase JVM (MB) :")
msz = raw_input("Enter size memory to be allocated to memstore (MB) :")
while not is_number(msz):
    msz = raw_input("Value error : Enter size memory to be allocated to memstore (MB) :")
l1sz = raw_input("Enter size of memory to be allocated to block cache (MB) :")
while not is_number(l1sz):
    l1sz = raw_input("Value error : Enter size of memory to be allocated to block cache (MB) :")
jhsz = raw_input("Enter size of memory to be allocated to JVM components (MB) :")
while not is_number(jhsz):
    jhsz = raw_input("Value error : Enter size of memory to be allocated to JVM components (MB) :")

dmem = float(tot) - float(msz) - float(l1sz) - float(jhsz)
xmx = float(msz) + float(l1sz) + float(jhsz)
ulim = float(msz)/xmx
blksz = 0.8 - ulim
bucsz = dmem + (blksz * xmx)
ccsz = 1 - ((blksz * xmx)/bucsz)

print "*************************************************************"
print "Set XX:MaxDirectMemorySize parameter in hbase-env.sh to %f MB" % dmem
print "Set Xmx parameter in hbase-env.sh to %f MB" % xmx
print "*************************************************************"
print "Set hbase.regionserver.global.memstore.upperLimit property in hbase-site.xml to %f " % ulim
print "Set hfile.block.cache.size property in hbase-site.xml to %f " % blksz
print "Set hbase.bucketcache.size property in hbase-site.xml to %f " % bucsz
print "Set hbase.bucketcache.percentage.in.combinedcache property in hbase-site.xml to %f " % ccsz
print "Set hbase.bucketcache.ioengine property in hbase-site.xml to offheap or file:/localfile"
print "*************************************************************"
print "Restart the HBase regionservers in all the nodes"
