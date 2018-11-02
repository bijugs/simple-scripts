echo $1 > oozieId.log
hdfs dfs -moveFromLocal -f oozieId.log $2
