package com.ssamples.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;

import java.util.*;

public class PhoenixDemo1
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
        Connection con1 = DriverManager.getConnection("jdbc:phoenix:localhost:2181:/hbase");
        con.close();
        con1.close();
    }
}
