export CLASSPATH=./target/phoenix-samples-1.0-SNAPSHOT-jar-with-dependencies.jar:/home/bjugs/jars/log4j.properties:/home/bijugs/jars/hbase-site.xml:/home/bijugs/jars/core-site.xml:/home/bijugs/jars/hdfs-site.xml:/home/bijugs/jars/*
echo $CLASSPATH
java com.ssamples.phoenix.PhoenixPreparedStmt zkhost false 2000000 10000 1024 secure
