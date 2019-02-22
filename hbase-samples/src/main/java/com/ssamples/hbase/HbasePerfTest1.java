package com.ssamples.hbase;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.UniformReservoir;
import com.codahale.metrics.Snapshot;

import java.lang.Long;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;

public class HbasePerfTest1 extends HbasePerfTester {

  Random rand = new Random();
  int threadNum;

  public HbasePerfTest1(Configuration conf) {
    super(conf);
  }

  public HbasePerfTest1(Configuration conf,int i) {
    super(conf,i);
  }

  public boolean execute(){
    if (createStatsCollectors(2)){
      System.out.println("Implement code to run multiple threads creating a single connection");
    }
    ExecutorService executor = Executors.newFixedThreadPool(2);
    for (int i = 0; i < 2; i++) {
      Runnable app = new AppPerformance(i);
      executor.execute(app);
    }
    executor.shutdown();
    while (!executor.isTerminated()){

    }
    return true;
  }

  class AppPerformance implements Runnable {
    private int threadNum;

    public AppPerformance(int num) {
      this.threadNum = num;
    }

    public void run() {
      System.out.println("Running thread "+threadNum);
      for (int i = 0; i < 10000; i++)
        addLatency(rand.nextInt()%100,threadNum);
      System.out.println("Completed running thread "+threadNum);
    }

  }

}
