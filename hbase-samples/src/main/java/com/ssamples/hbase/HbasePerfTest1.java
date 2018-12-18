package com.ssamples.hbase;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.UniformReservoir;
import com.codahale.metrics.Snapshot;

import java.lang.Long;
import java.util.ArrayList;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;

public class HbasePerfTest1 extends HbasePerfTester {

  Random rand = new Random();

  public HbasePerfTest1(Configuration conf) {
    super(conf);
  }

  public boolean execute(){
    if (createStatsCollectors(1)){
      System.out.println("Implement code to run multiple threads creating a single connection");
    }
    for (int i = 0; i < 10000; i++)
      addLatency(rand.nextInt()%100,0);
    return true;
  }

}
