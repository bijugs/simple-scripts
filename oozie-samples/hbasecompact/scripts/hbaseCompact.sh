#!/bin/ksh
echo "Starting HBase major compaction on table $1 - $3" > majorcompact.log
echo "major_compact \"$1\"" | /usr/bin/hbase shell 2>&1 >> majorcompact.log
echo "HBase major compaction request complete on table $1" >> majorcompact.log
hdfs dfs -moveFromLocal -f majorcompact.log $2
