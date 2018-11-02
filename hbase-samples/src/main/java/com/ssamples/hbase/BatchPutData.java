package com.ssamples.hbase;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
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
                String zkQuorum = "localhost";
                String principal = null;
                String keyTab = null;
                boolean isSecure = false;
		Configuration config = HBaseConfiguration.create();
                for (int i = 0; i < args.length; i++) {
                        if (i == 0)
                                zkQuorum = args[i];
                        if (i == 1)
                                numRows = Integer.parseInt(args[i]);
                        if (i == 2)
                                commitSize = Integer.parseInt(args[i]);
                        if (i == 3)
                                valueSize = Integer.parseInt(args[i]);
                        if (i == 4) {
                                if (args[i].equalsIgnoreCase("secure"))
                                    isSecure = true;
                        }
                        if (i == 5)
                                principal = args[i];
                        if (i == 6)
                                keyTab = args[i];

                }
                config.set("hbase.zookeeper.quorum", zkQuorum);
                System.out.println("Hbase zookeeper "+config.get("hbase.zookeeper.quorum"));
                if (isSecure) {
                    config.set("hadoop.security.authentication", "Kerberos");
                    config.set("hbase.security.authentication", "Kerberos");
                    config.set("hbase.master.kerberos.principal", "hbase/_HOST@REALM");
                    config.set("hbase.regionserver.kerberos.principal", "hbase/_HOST@REALM");
                    UserGroupInformation.setConfiguration(config);
                    if (principal != null && keyTab != null)
                       UserGroupInformation.loginUserFromKeytab(principal,keyTab);
                }
		Connection conn = ConnectionFactory.createConnection(config);
		TableName tblName = TableName.valueOf("t1"); 
		Table tbl = conn.getTable(tblName);
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
