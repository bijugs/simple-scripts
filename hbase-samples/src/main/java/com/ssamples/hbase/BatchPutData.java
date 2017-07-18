package com.ssamples.hbase;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class BatchPutData {
	public static void main(String[] args) throws IOException {

		// Instantiating configuration class
		int valueSize = 10;
		int numRows = 100;
		int commitSize = 50;
		int rows = 0;
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "localhost");
		// Creating a connection
		Connection conn = ConnectionFactory.createConnection(conf);
		TableName tblName = TableName.valueOf("t1"); 
		Table tbl = conn.getTable(tblName);
		for (int i = 0; i < args.length; i++) {
			if (i == 0)
				numRows = Integer.parseInt(args[0]);
			if (i == 1)
				commitSize = Integer.parseInt(args[1]);
			if (i == 2)
				valueSize = Integer.parseInt(args[2]);

		}
		ArrayList<Long>  stats = new ArrayList<Long>(numRows/commitSize);
		StringBuffer value = new StringBuffer();
		for (int i = 0; i < valueSize; i++) {
			value = value.append('a');
		}
		long startTime;
		String key = null;
		ArrayList<Put> puts = new ArrayList<Put>(5000);
		while (rows < numRows) {
			for (int i = 0; i < commitSize; i++) {
				rows += 1;
				key = "A" + rows;
				Put p = new Put(Bytes.toBytes(key));
				p.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("c1"), Bytes.toBytes(value.toString()));
				puts.add(p);
			}
			startTime = System.currentTimeMillis();
			tbl.put(puts);
			stats.add(System.currentTimeMillis() - startTime);
			System.out.println("Committed "+ (System.currentTimeMillis() - startTime));
		}
		tbl.close();
		conn.close();
		System.out.println("Completed batch execute for "+numRows+" rows "+" commit size "+commitSize+" and val size "+valueSize);
		System.out.println("Stats avg "+getAvg(stats)+" max "+getMax(stats)+" min "+getMin(stats)+" Tot "+getTot(stats));
		System.out.println("Put complete");
	}
	
	static long getMax(ArrayList<Long> in) {
		Long out = in.get(0);
		for (int i = 1; i <= in.size() - 1; i++) {
			if (out < in.get(i))
				out = in.get(i);
		}
		return out;
	}
	
	static long getMin(ArrayList<Long> in) {
		Long out = in.get(0);
		for (int i = 1; i <= in.size() - 1; i++) {
			if (in.get(i) < out)
				out = in.get(i);
		}
		return out;
	}
	
	static long getAvg(ArrayList<Long> in) {
		Long out = 0L;
		for (int i = 0; i <= in.size() - 1; i++) {
			out += in.get(i);
		}
		return out/in.size();
	}
	
	static long getTot(ArrayList<Long> in) {
		Long out = 0L;
		for (int i = 0; i <= in.size() - 1; i++) {
			out += in.get(i);
		}
		return out;
	}
}
