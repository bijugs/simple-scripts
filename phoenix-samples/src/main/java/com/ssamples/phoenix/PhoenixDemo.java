package com.ssamples.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;

import java.util.*;

public class PhoenixDemo
{
    public static void main( String[] args ) throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty("zookeeper.znode.parent","hbase");
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        System.out.println("getting connection");
        Statement stmt = null;
        ResultSet rset = null;
        Connection con = DriverManager.getConnection("jdbc:phoenix:localhost:2181:/hbase");
        System.out.println("connected: " + con.toString());
        stmt = con.createStatement();
        stmt.executeUpdate("upsert into TBL values ('IBM',100)");
        stmt.executeUpdate("upsert into TBL values ('AAPL',500)");
        stmt.executeUpdate("upsert into TBL values ('GOOG',5.5)");
        stmt.executeUpdate("delete from TBL where ticker='IBM'");
        con.commit();
        PreparedStatement statement = con.prepareStatement("select location, count(1) as count from details where ticker in (select ticker from tbl) group by location order by location desc");
        rset = statement.executeQuery();
        while (rset.next()) {
            System.out.println(rset.getString("location"));
            System.out.println(rset.getString("count"));
        }
        statement.close();
        con.close();
    }
}
