package com.ssamples.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class PhoenixCursor1 {
	
    public static void main( String[] args ) throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty("zookeeper.znode.parent","hbase");
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        System.out.println("getting connection");
        ResultSet rset = null;
        Connection con = DriverManager.getConnection("jdbc:phoenix:localhost:2181:/hbase");
        System.out.println("connected: " + con.toString());
        PreparedStatement statement = con.prepareStatement("DECLARE testCursor CURSOR FOR SELECT * FROM TBL ORDER BY TICKER DESC");
        //PreparedStatement statement = con.prepareStatement("DECLARE testCursor CURSOR FOR select location, count(1) as count from details group by location order by location desc");
        statement.execute();
        /*if (statement.execute())
        	System.out.println("Cursor created successfully");
        else {
        	System.out.println("Cursor creation failed");
        	throw new Exception("Cursor creation exception");
        }*/
        statement = con.prepareStatement("OPEN testCursor");
        statement.execute();
        statement = con.prepareStatement("FETCH NEXT 1 ROWS FROM testCursor");
        rset = statement.executeQuery();
        System.out.println("**** First executeQuery()");
        while (rset.next()) {
            System.out.println(rset.getString("ticker"));
            System.out.println(rset.getFloat("price"));
        }
    	Thread.sleep(10000);
        System.out.println("**** Second executeQuery()");
        rset = statement.executeQuery();
        while  (rset.next()) {
            System.out.println(rset.getString("ticker"));
            System.out.println(rset.getFloat("price"));
        }
        System.out.println("**** Third executeQuery()");
        rset = statement.executeQuery();
        while  (rset.next()) {
            System.out.println(rset.getString("ticker"));
            System.out.println(rset.getFloat("price"));
        }
        System.out.println("**** last executeQuery()");
        rset = statement.executeQuery();
        while  (rset.next()) {
            System.out.println(rset.getString("ticker"));
            System.out.println(rset.getFloat("price"));
        }
        statement.close();
        con.close();
    }

}
