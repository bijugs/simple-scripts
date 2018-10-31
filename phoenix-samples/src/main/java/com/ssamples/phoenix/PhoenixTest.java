package com.ssamples.phoenix;

import org.apache.commons.cli.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.*;

public class PhoenixTest {

    public static void usage(Options options) {
        System.out.println("ZK Quorum  and port is required");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "PhoenixTest", options );
        return;
    }

    public static void main( String[] args ) throws Exception
    {
        boolean isSecure = false;
        String zkQuorum = null;
        String zkPort = null;
        String userName = null;
        String keyTab = null;
        String krbRealm = null;
        Connection con = null;
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption( "z", "zkquorum", true, "Comma seperated list of ZK nodes" );
        options.addOption( "p", "zkport", true, "ZooKeeper port" );
        options.addOption( "k", "keytab", true, "Keytab file path" );
        options.addOption( "u", "username", true, "Principal for the keytab" );
        options.addOption( "r", "krbrealm", true, "Kerberos realm" );
        options.addOption( "s", "issecure", false, "Is cluster kerberized" );
        if (args.length < 2) {
           PhoenixTest.usage(options);
           return;
        }
        CommandLine line = parser.parse( options, args );
        if( line.hasOption( "issecure" ) ) {
            // print whether the cluster is secure
            isSecure = true;
            System.out.println("Cluster is Kerberized");
        }
        if( line.hasOption( "username" ) ) {
            // print the username for the keytab passed
            userName = line.getOptionValue("username");
            System.out.println("Will user user name "+userName);
        }
        if( line.hasOption( "keytab" ) ) {
            // print the keytab file passed
            keyTab = line.getOptionValue("keytab");
            System.out.println("Will user key tab "+keyTab);
        }
        if( line.hasOption( "krbrealm" ) ) {
            // print the KRB realm passed
            krbRealm = line.getOptionValue("krbrealm");
            System.out.println("Will user KRB realm "+krbRealm);
        }
        if( line.hasOption( "zkquorum" ) ) {
            // print the ZK Quorum provided
            zkQuorum = line.getOptionValue("zkquorum");
            System.out.println("Will use ZK nodes "+zkQuorum);
        }
        if( line.hasOption( "zkport" ) ) {
            // print the ZK port provided
            zkPort = line.getOptionValue("zkport");
            System.out.println("Will use ZK nodes "+zkPort);
        }
        Properties prop = new Properties();
        prop.setProperty("zookeeper.znode.parent","hbase");
        if (isSecure) {
            prop.setProperty("hadoop.security.authentication","Kerberos");
            prop.setProperty("hbase.security.authentication","Kerberos");
            prop.setProperty("hbase.master.kerberos.principal","hbase/_HOST@"+krbRealm);
            prop.setProperty("hbase.regionserver.kerberos.principal","hbase/_HOST@"+krbRealm);
        }
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        System.out.println("getting connection");
        Statement stmt = null;
        ResultSet rset = null;
        if (zkQuorum == null || zkPort == null) {
            PhoenixTest.usage(options);
            return;
        }
        while (true) {
            try {
                if (isSecure && userName != null && keyTab != null)
                    con = DriverManager.getConnection("jdbc:phoenix:"+zkQuorum+":"+zkPort+":/hbase:"+userName+":"+keyTab, prop);
                else //Works with KRB tickets of executing user?
                    con = DriverManager.getConnection("jdbc:phoenix:"+zkQuorum+":"+zkPort+":/hbase", prop);
                break;
            } catch (SQLException ex) {
                 System.out.println("SQLState = "+ex.getSQLState());
                 System.out.println("SQLErrCode = "+ex.getErrorCode());
                 if (ex.getErrorCode() == 726 && ex.getSQLState().equalsIgnoreCase("43M10"))
                     prop.setProperty("phoenix.schema.isNamespaceMappingEnabled","true");
                 else
                     throw ex;
            }
        }
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
