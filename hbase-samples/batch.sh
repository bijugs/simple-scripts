export CLASSPATH=./target/hbase-samples-1.0-SNAPSHOT-jar-with-dependencies.jar
#java -Djava.security.auth.login.config=/home/bnair10/hb-jaas.conf com.ssamples.hbase.HbaseKTest
java com.ssamples.hbase.HBaseAdminTool
#java com.ssamples.hbase.BatchPutData dob2-bach-r1n11.bloomberg.com 200000 10000 1024 secure
#java com.ssamples.hbase.BuffMutator
#java com.ssamples.hbase.TryHBaseTestUtility
#java com.ssamples.hbase.HbaseDataCompare ./src/main/resources/hbase-compare.xml
#java com.ssamples.hbase.ReadJMXStats
#java com.ssamples.hbase.HBaseDmlTest
