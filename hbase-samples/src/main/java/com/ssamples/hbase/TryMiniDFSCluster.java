package com.ssamples.hbase;

import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.HdfsConfiguration;

public class TryMiniDFSCluster {

  public static void main(String args[]) throws Exception{
    Configuration conf = new HdfsConfiguration();
    MiniDFSCluster mdc = new MiniDFSCluster.Builder(conf).build();
  }

}
