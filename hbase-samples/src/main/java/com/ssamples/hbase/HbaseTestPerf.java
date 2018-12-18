package com.ssamples.hbase;

import org.apache.hadoop.conf.Configuration;

public class HbaseTestPerf{

  public static void main(String args[]) throws Exception {
    Configuration conf = new Configuration();
    HbasePerfTester tester = (HbasePerfTester) Class.forName("com.ssamples.hbase.HbasePerfTest1").getConstructor(Configuration.class).newInstance(conf);
    //HbasePerfTester tester = (HbasePerfTester) Class.forName("com.ssamples.hbase.HbasePerfTest1").newInstance();
    tester.execute();
    tester.generateStats();
  }

}
