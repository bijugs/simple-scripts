package com.ssamples.phoenix;

import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Properties;

public class PhoenixPreparedStmt {
        
        static Logger log = Logger.getLogger(PhoenixPreparedStmt.class.getName());
	
	public static void main(String args[]) throws Exception {
                log.debug("Log4j is enabled");
		final String sql = "upsert into TBN values (?,?)";
		int rows = 0;
		String key = null;
		StringBuffer value = new StringBuffer();
		ArrayList<Long> stats = null;
		int valueSize = 10;
		int numRows = 100;
		int commitSize = 50;
                boolean isSecure = false;
                String principal = null;
                String keyTab = null;
                String zkQuorum = "localhost";
                boolean autoCommit = false;
		Properties prop = new Properties();
		prop.setProperty("phoenix.mutate.batchSize", "5000");
		Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
                Connection con = null;
		PreparedStatement stmt = null;
		for (int i = 0; i < args.length; i++) {
			if (i == 0)
				zkQuorum = args[i];
			if (i == 1) {
                                if (args[i].equalsIgnoreCase("true"))
				    autoCommit = true;
                        }
			if (i == 2)
				numRows = Integer.parseInt(args[i]);
			if (i == 3)
				commitSize = Integer.parseInt(args[i]);
			if (i == 4)
				valueSize = Integer.parseInt(args[i]);
			if (i == 5) {
				if (args[i].equalsIgnoreCase("secure"))
                                    isSecure = true;
                        }
			if (i == 6)
				principal = args[i];
			if (i == 7)
				keyTab = args[i];

		}
		System.out.println("getting connection zkQuorum " +zkQuorum);
                if (isSecure && (principal != null) && (keyTab != null))
		    con = DriverManager.getConnection("jdbc:phoenix:"+zkQuorum+":2181:/hbase:"+principal+":"+keyTab,prop);
                else
		    con = DriverManager.getConnection("jdbc:phoenix:"+zkQuorum+":2181:/hbase",prop);
		if (autoCommit) {
			System.out.println("Auto commit is set");
			con.setAutoCommit(true);
		} else {
			System.out.println("Auto commit is false");
			con.setAutoCommit(false);
                }
		System.out.println("connected: " + con.toString());
		stmt = con.prepareStatement(sql);
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
		System.out.println("Completed batch execute for "+numRows+" rows "+" commit size "+commitSize+" and val size "+valueSize+" autoCommit "+args[1]);
		System.out.println("Stats avg "+getAvg(stats)+" max "+getMax(stats)+" min "+getMin(stats)+" Tot "+getTotal(stats));
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


	static long getTotal(ArrayList<Long> in) {
		Long out = 0L;
		for (int i = 0; i <= in.size() - 1; i++) {
			out += in.get(i);
		}
		return out;
	}
}
