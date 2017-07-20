package com.ssamples.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class BatchPut {
	  private final static byte[] ROW1 = Bytes.toBytes("row1");
	  private final static byte[] ROW2 = Bytes.toBytes("row2");
	  private final static byte[] ROW3 = Bytes.toBytes("row3");
	  private final static byte[] ROW4 = Bytes.toBytes("row4");
	  private final static byte[] CF1 = Bytes.toBytes("cf1");
	  private final static byte[] CF2 = Bytes.toBytes("cf2");
	  private final static byte[] COL1 = Bytes.toBytes("col1");
	  private final static byte[] COL2 = Bytes.toBytes("col2");

	  public static void main(String[] args) throws IOException, InterruptedException {
	    Configuration conf = HBaseConfiguration.create();
	    conf.set("hbase.zookeeper.quorum", "localhost");
	    System.out.println("Before batch call...");
	    Connection connection = ConnectionFactory.createConnection(conf);
	    Table table = connection.getTable(TableName.valueOf("testtable"));

	    // vv BatchSameRowExample
	    List<Row> batch = new ArrayList<Row>();

	    Put put = new Put(ROW1);
	    put.add(CF1, COL1, 2L, Bytes.toBytes("val1"));
	    batch.add(put);

	    Put put1 = new Put(ROW2);
	    put1.add(CF1, COL1, 2L, Bytes.toBytes("val2"));
	    batch.add(put1);

	    Put put2 = new Put(ROW3);
	    put2.add(CF1, COL1, 2L, Bytes.toBytes("val3"));
	    batch.add(put2);

	    Increment inc3 = new Increment(ROW3);
	    inc3.addColumn(CF1, COL2, 1L);
	    batch.add(inc3);

	    Increment inc4 = new Increment(ROW3);
	    inc4.addColumn(CF1, COL2, 1L);
	    batch.add(inc4);

	    Put put4 = new Put(ROW4);
	    put4.add(CF1, COL1, 2L, Bytes.toBytes("val4"));
	    batch.add(put4);

	    Object[] results = new Object[batch.size()];
            for (int i = 0; i < results.length; i++)
               System.out.println(results[i]);

	    System.out.println("*** Batch size "+ batch.size());
	    try {
	      table.batch(batch, results);
	    } catch (Exception e) {
	      System.err.println("Error: " + e);
	    }
	    // ^^ BatchSameRowExample
	    System.out.println("*** result length "+ results.length);
	    for (int i = 0; i < results.length; i++) {
	      System.out.println("Result[" + i + "]: type = " +
	        results[i].getClass().getSimpleName() + "; " + results[i]);
	    }
	    table.close();
	    connection.close();
	    System.out.println("After batch call...");
	  }

}
