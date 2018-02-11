package com.ssamples.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class PhoenixCursor6 {
	
    public static void main( String[] args ) throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty("zookeeper.znode.parent","hbase");
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        System.out.println("getting connection");
        ResultSet rset = null;
        Connection con = DriverManager.getConnection("jdbc:phoenix:localhost:2181:/hbase");
        System.out.println("connected: " + con.toString());
        PreparedStatement statement = con.prepareStatement("DECLARE testCursor CURSOR FOR SELECT * FROM MARKET");
        statement.execute();
        statement = con.prepareStatement("OPEN testCursor");
        statement.execute();
        statement = con.prepareStatement("FETCH NEXT 1 ROWS FROM testCursor");
        rset = statement.executeQuery();
        System.out.println("**** First executeQuery()");
        while (rset.next()) {
            System.out.println(rset.getInt("ticker"));
            System.out.println(rset.getFloat("price"));
        }
        System.out.println("**** Second executeQuery()");
        rset = statement.executeQuery();
        while  (rset.next()) {
            System.out.println(rset.getInt("ticker"));
            System.out.println(rset.getFloat("price"));
        }
        statement = con.prepareStatement("FETCH PRIOR 1 ROWS FROM testCursor");
        System.out.println("**** Third executeQuery()");
        rset = statement.executeQuery();
        while  (rset.next()) {
            System.out.println(rset.getInt("ticker"));
            System.out.println(rset.getFloat("price"));
        }
        System.out.println("**** Fourth executeQuery()");
        rset = statement.executeQuery();
        while  (rset.next()) {
            System.out.println(rset.getInt("ticker"));
            System.out.println(rset.getFloat("price"));
        }
        System.out.println("**** Last executeQuery()");
        rset = statement.executeQuery();
        while  (rset.next()) {
            System.out.println(rset.getInt("ticker"));
            System.out.println(rset.getFloat("price"));
        }
        statement.close();
        con.close();
    }

}
