package com.ssamples.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;

import java.util.*;

public class PhoenixTraceDemo
{
    public static void main( String[] args ) throws Exception
    {
        Properties prop = new Properties();
        // Enable tracing on every request
        // Tracing is done every 10 secs
        prop.setProperty("phoenix.trace.frequency", "always");

        // Enable tracing on 50% of requests
        //prop.setProperty("phoenix.trace.frequency", "probability");
        //prop.setProperty("phoenix.trace.probability.threshold", 0.5)
        prop.setProperty("zookeeper.znode.parent","hbase");
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        System.out.println("getting connection");
        Statement stmt = null;
        ResultSet rset = null;
        Connection con = DriverManager.getConnection("jdbc:phoenix:localhost:2181:/hbase",prop);
        System.out.println("connected: " + con.toString());
        stmt = con.createStatement();
        for (int i = 0; i < 1000; i++) {
          stmt.executeUpdate("upsert into TBL values ('IBM',100+i)");
          stmt.executeUpdate("upsert into TBL values ('AAPL',500+i)");
          stmt.executeUpdate("upsert into TBL values ('GOOG',5.5+i)");
          stmt.executeUpdate("delete from TBL where name='IBM'");
          con.commit();
          Thread.sleep(1000);
        }
        PreparedStatement statement = con.prepareStatement("select * from TBL");
        rset = statement.executeQuery();
        while (rset.next()) {
                System.out.println(rset.getString("name"));
                System.out.println(rset.getString("price"));
        }
        statement.close();
        con.close();
    }
}
