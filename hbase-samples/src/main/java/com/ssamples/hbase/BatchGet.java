package com.ssamples.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class BatchGet {

	public static void main(String[] args) throws IOException, InterruptedException {
		String zkQuorum = "localhost";

		Configuration config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.quorum", zkQuorum);
		System.out.println("Hbase zookeeper " + config.get("hbase.zookeeper.quorum"));
		Connection conn = ConnectionFactory.createConnection(config);
		TableName tblName = TableName.valueOf("t1");
		Table tbl = conn.getTable(tblName);
		tbl.get(new Get(Bytes.toBytes("A99")));
		int rows = 0;
		ArrayList<Get> gets = new ArrayList<Get>(10);
		while (rows < 10) {
			rows += 1;
			String key = "A" + rows;
			Get g = new Get(Bytes.toBytes(key));
			gets.add(g);
		}
        Object[] result = new Object[10];
		tbl.get(gets);
		for (int i = 0; i < 10; i++) {
		  tbl.batch(gets, result);
		}
		tbl.close();
		conn.close();
	}

}
