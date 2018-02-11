package com.ssamples.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class PhoenixCursorCount{
	
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
        PreparedStatement statement = con.prepareStatement("DECLARE testCursor CURSOR FOR select d.location as location, count(1) as count from details as d group by location order by count desc");
        statement.execute();
        statement = con.prepareStatement("OPEN testCursor");
        statement.execute();
        statement = con.prepareStatement("FETCH NEXT ROW FROM testCursor");
        rset = statement.executeQuery();
        System.out.println("**** First executeQuery()");
        while (rset.next()) {
            System.out.println(rset.getString("location"));
            System.out.println(rset.getFloat("count"));
            Thread.sleep(1000);
        }
        rset = statement.executeQuery();
        System.out.println("**** Second executeQuery()");
        while  (rset.next()) {
            System.out.println(rset.getString("location"));
            System.out.println(rset.getFloat("count"));
        }
        rset = statement.executeQuery();
        System.out.println("**** Third executeQuery()");
        while  (rset.next()) {
            System.out.println(rset.getString("location"));
            System.out.println(rset.getFloat("count"));
        }
        rset = statement.executeQuery();
        System.out.println("**** Fourth executeQuery()");
        while  (rset.next()) {
            System.out.println(rset.getString("location"));
            System.out.println(rset.getFloat("count"));
        }
        rset = statement.executeQuery();
        System.out.println("**** Fifth executeQuery()");
        while  (rset.next()) {
            System.out.println(rset.getString("location"));
            System.out.println(rset.getFloat("count"));
        }
        /*System.out.println("**** last executeQuery()");
        rset = statement.executeQuery();
        while  (rset.next()) {
            System.out.println(rset.getString("location"));
            System.out.println(rset.getFloat("count"));
        }*/
        statement = con.prepareStatement("FETCH PRIOR ROW FROM testCursor");
        rset = statement.executeQuery();
        System.out.println("**** FETCH PRIOR executeQuery()");
        while (rset.next()) {
            System.out.println(rset.getString("location"));
            System.out.println(rset.getFloat("count"));
            rset = statement.executeQuery();
        }
        statement.close();
        con.close();
    }
}
