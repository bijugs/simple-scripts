package com.ssamples.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class PhoenixCursor3 {
	
    public static void main( String[] args ) throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty("zookeeper.znode.parent","hbase");
        prop.setProperty("hbase.client.scanner.timeout.period","1000");
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        System.out.println("getting connection");
        ResultSet rset = null;
        Connection con = DriverManager.getConnection("jdbc:phoenix:localhost:2181:/hbase",prop);
        System.out.println("connected: " + con.toString());
        PreparedStatement statement = con.prepareStatement("DECLARE testCursor CURSOR FOR select price, count(1) from tbl group by price having count(1) > 0");
        statement.execute();
        statement = con.prepareStatement("OPEN testCursor");
        statement.execute();
        statement = con.prepareStatement("FETCH NEXT 2 ROWS FROM testCursor");
        for (int i = 0; i < 100000; i++) {
        	rset = statement.executeQuery();
        	System.out.println("**** First executeQuery()");
        	while (rset.next()) {
        		System.out.println(rset.getString("price"));
        	}
        	Thread.sleep(10000);
        }
        statement.close();
        con.close();
    }

}

