# simple-scripts


The following is a brief description of files in the repo

``hbase-bucketcache.py`` - python script to calculate HBase configuration parameter values when configuring offheap bucketcache.
``hdfsdir_resource.rb`` - Chef LWRP resource definition for HDFS directory. See next item for the corresponding provider.
``hdfsdir_provider.rb`` - Chef LWRP provider for HDFS directory which can be used with the resource definition above.
``install_kafka_using_chef_solo.sh`` - Script to install a single node ``Kafka`` using ``Chef Solo``.
