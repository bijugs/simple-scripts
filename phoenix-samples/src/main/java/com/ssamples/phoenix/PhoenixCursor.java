package com.ssamples.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class PhoenixCursor {
	
    public static void main( String[] args ) throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty("zookeeper.znode.parent","hbase");
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        System.out.println("getting connection");
        ResultSet rset = null;
        Connection con = DriverManager.getConnection("jdbc:phoenix:localhost:2181:/hbase");
        System.out.println("connected: " + con.toString());
        PreparedStatement statement = con.prepareStatement("DECLARE testCursor CURSOR FOR SELECT * FROM TBL");
        statement.execute();
        /*if (statement.execute())
        	System.out.println("Cursor created successfully");
        else {
        	System.out.println("Cursor creation failed");
        	throw new Exception("Cursor creation exception");
        }*/
        statement = con.prepareStatement("OPEN testCursor");
        statement.execute();
        statement = con.prepareStatement("FETCH NEXT FROM testCursor");
        rset = statement.executeQuery();
        //while (rset.next()) {
            rset.next();
            System.out.println(rset.getString("ticker"));
            System.out.println(rset.getString("price"));
            rset = statement.executeQuery();
            rset.next();
            System.out.println(rset.getString("ticker"));
            System.out.println(rset.getString("price"));
        //}
        statement.close();
        con.close();
    }

}
