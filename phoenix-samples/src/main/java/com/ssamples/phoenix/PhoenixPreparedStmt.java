package com.ssamples.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Properties;

public class PhoenixPreparedStmt {
	
	public static void main(String args[]) throws Exception {
		final String sql = "upsert into TBL values (?,?)";
		int rows = 0;
		String key = null;
		StringBuffer value = new StringBuffer();
		ArrayList<Long> stats = null;
		int valueSize = 10;
		int numRows = 100;
		int commitSize = 50;
		Properties prop = new Properties();
		prop.setProperty("zookeeper.znode.parent", "hbase");
		Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
		System.out.println("getting connection");
		PreparedStatement stmt = null;
		Connection con = DriverManager.getConnection("jdbc:phoenix:localhost:2181:/hbase");
		if (!con.getAutoCommit()) {
			System.out.println("Auto commit is off");
			con.setAutoCommit(true);
		}
		System.out.println("connected: " + con.toString());
		stmt = con.prepareStatement(sql);
		for (int i = 0; i < args.length; i++) {
			if (i == 0)
				numRows = Integer.parseInt(args[0]);
			if (i == 1)
				commitSize = Integer.parseInt(args[1]);
			if (i == 2)
				valueSize = Integer.parseInt(args[2]);

		}
		stats = new ArrayList<Long>(numRows/commitSize);
		for (int i = 0; i < valueSize; i++) {
			value = value.append('a');
		}
		long startTime;
		while (rows < numRows) {
			for (int i = 0; i < commitSize; i++) {
				rows = rows + 1;
				key = "A" + rows;
				stmt.setString(1, key);
				stmt.setString(2, value.toString());
				stmt.addBatch();
			}
			startTime = System.currentTimeMillis();
			stmt.executeBatch();
			con.commit();
			stats.add(System.currentTimeMillis() - startTime);
			System.out.println("Committed "+ (System.currentTimeMillis() - startTime));
		}
		System.out.println("Completed batch execute for "+numRows+" rows "+" commit size "+commitSize+" and val size "+valueSize);
		System.out.println("Stats avg "+getAvg(stats)+" max "+getMax(stats)+" min "+getMin(stats));
		Thread.sleep(6000);
		stmt.close();
		con.close();
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
}
