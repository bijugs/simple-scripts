package com.ssamples.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class PhoenixCursor2 {

	public static void main(String[] args) throws Exception {
		Properties prop = new Properties();
		prop.setProperty("zookeeper.znode.parent", "hbase");
		Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
		prop.setProperty("hbase.client.scanner.timeout.period","1000");
		System.out.println("getting connection");
		ResultSet rset = null;
		Connection con = DriverManager.getConnection("jdbc:phoenix:localhost:2181:/hbase");
		System.out.println("connected: " + con.toString());
		PreparedStatement statement = con
				.prepareStatement("DECLARE testCursor CURSOR FOR SELECT * FROM TBL");
		statement.execute();
		statement = con.prepareStatement("OPEN testCursor");
		statement.execute();
		statement = con.prepareStatement("FETCH NEXT 1 ROWS FROM testCursor");
		for (int i = 0; i < 100001; i++) {
			rset = statement.executeQuery();
			System.out.println("**** First executeQuery()");
			while (rset.next()) {
				System.out.println(rset.getString("ticker"));
				System.out.println(rset.getFloat("price"));
			}
			Thread.sleep(1000);
		}
		statement.close();
		con.close();
	}
}
