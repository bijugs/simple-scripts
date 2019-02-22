package com.ssamples.hbase;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.UniformReservoir;
import com.codahale.metrics.Snapshot;

import java.lang.Long;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;

public abstract class HbasePerfTester {

  private static ArrayList<Histogram> latencyHist;
  private static ArrayList<Long> totalTime;
  private static ArrayList<Long> totalRows;
  private static Configuration conf;

  public HbasePerfTester(Configuration conf) {
    System.out.println("Setting up the configuration");
    this.conf = conf;
  }

  public HbasePerfTester(Configuration conf, int i) {
    System.out.println("Setting up the configuration");
    this.conf = conf;
  }

  public static boolean createStatsCollectors(int threadCount) {
    UniformReservoir r;
    Histogram hist;
    latencyHist = new ArrayList<Histogram>(threadCount);
    for (int i = 0; i < threadCount; i++) {
      r = new UniformReservoir();
      hist = new Histogram(r);
      latencyHist.add(hist);
    }
    totalTime = new ArrayList<Long>(threadCount);
    totalRows = new ArrayList<Long>(threadCount);
    return true;
  } 

  public static boolean addLatency(int latency, int threadNum) {
    Histogram hist = latencyHist.get(threadNum);
    hist.update(latency);
    return true;
  }

  public static boolean generateStats() {
    for (int i = 0; i < latencyHist.size(); i++) {
      System.out.println("Generating stats for thread "+ i);
      Snapshot s = latencyHist.get(i).getSnapshot();
      System.out.println("Min "+s.getMin());
      System.out.println("Max "+s.getMax());
      System.out.println("Mean "+s.getMean());
      System.out.println("Median "+s.getMedian());
      System.out.println("95th percentile "+s.get95thPercentile());
    }
    return true;
  }

  abstract public boolean execute();

}
