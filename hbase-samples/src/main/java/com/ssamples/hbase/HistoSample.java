package com.ssamples.hbase;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.UniformReservoir;
import com.codahale.metrics.Snapshot;
import java.lang.reflect.Constructor;
import java.util.Random;

public class HistoSample {

  public static void main(String args[]){
    Random rand = new Random();
    try {
      UniformReservoir r = new UniformReservoir();
      Histogram hist = new Histogram(r);
      for (int i = 0; i < 10000; i++)
        hist.update(rand.nextInt()%100);
      Snapshot s = hist.getSnapshot();
      System.out.println("Min "+s.getMin());
      System.out.println("Max "+s.getMax());
      System.out.println("Mean "+s.getMean());
      System.out.println("Median "+s.getMedian());
      System.out.println("95th percentile "+s.get95thPercentile());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
