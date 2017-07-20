export CLASSPATH=./target/phoenix-samples-1.0-SNAPSHOT-jar-with-dependencies.jar:/home/bnair10/jars/log4j.properties:/home/bnair10/jars/hbase-site.xml:/home/bnair10/jars/core-site.xml:/home/bnair10/jars/hdfs-site.xml:/home/bnair10/jars/*
echo $CLASSPATH
java com.ssamples.phoenix.PhoenixPreparedStmt dob2-bach-r1n08.bloomberg.com false 2000000 10000 1024 secure
