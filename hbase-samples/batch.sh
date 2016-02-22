export CLASSPATH=./target/hbase-samples-1.0-SNAPSHOT-jar-with-dependencies.jar
#java -Djava.security.auth.login.config=/home/bnair10/hb-jaas.conf com.ssamples.hbase.HbaseKTest
java com.ssamples.hbase.HBaseAdminTool
#java com.ssamples.hbase.BuffMutator
#java com.ssamples.hbase.TryHBaseTestUtility
#java com.ssamples.hbase.HbaseDataCompare ./src/main/resources/hbase-compare.xml
#java com.ssamples.hbase.ReadJMXStats
