package com.ssamples.hbase;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.UniformReservoir;
import com.codahale.metrics.Snapshot;

import java.lang.Long;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;

public abstract class HbasePerfTester {

  private ArrayList<Histogram> latencyHist;
  private ArrayList<Long> totalTime;
  private ArrayList<Long> totalRows;
  private Configuration conf;

  public HbasePerfTester(Configuration conf) {
    System.out.println("Setting up the configuration");
    this.conf = conf;
  }

  public boolean createStatsCollectors(int threadCount) {
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

  public boolean addLatency(int latency, int threadNum) {
    Histogram hist = latencyHist.get(threadNum);
    hist.update(latency);
    return true;
  }

  public boolean generateStats() {
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
