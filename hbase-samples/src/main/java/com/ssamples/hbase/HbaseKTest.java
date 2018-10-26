package com.ssamples.hbase;

import org.apache.hadoop.security.UserGroupInformation;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.cli.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

public class HbaseKTest {

  public static void usage(Options options) {
    System.out.println("ZK Quorum is required");
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "HbaseKTest", options );
    return;
  }

  public static void main(String args[]){

    String zkQuorum = null;
    String zkPort = "2181";
    String userName = null;
    String keyTab = null;
    String krbRealm = null;
    boolean isSecure = false;
    CommandLineParser parser = new GnuParser();
    Options options = new Options();
    options.addOption( "s", "secure", false, "secure cluster" );
    options.addOption( "z", "zkquorum", true, "ZooKeeper quorum" );
    options.addOption( "p", "zkport", true, "ZooKeeper port" );
    options.addOption( "k", "keytab", true, "Keytab file path" );
    options.addOption( "u", "username", true, "Principal for the keytab" );
    options.addOption( "r", "krbrealm", true, "Kerberos realm to be used" );
    if (args.length < 1) {
       HbaseKTest.usage(options);
       return;
    }

    try {
        CommandLine line = parser.parse( options, args );
        if( line.hasOption( "secure" ) ) {
            // print whether the HDFS cluster is Secure
            System.out.println("The cluster is secure");
            isSecure = true;
        }
        if( line.hasOption( "zkquorum" ) ) {
            // print the ZK Quorum for the cluster
            zkQuorum = line.getOptionValue("zkquorum");
            System.out.println("Will connect to ZK "+zkQuorum);
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
        if( line.hasOption( "zkport" ) ) {
            // print the ZK port passed
            zkPort = line.getOptionValue("zkport");
            System.out.println("Will use ZK port "+zkPort);
        }
        if( line.hasOption( "krbrealm" ) ) {
            // print the KRB Realm passed
            krbRealm = line.getOptionValue("krbrealm");
            System.out.println("Will use realm "+krbRealm);
        }
        if (zkQuorum == null) {
            HbaseKTest.usage(options);
            return;
        }
        if (isSecure && krbRealm == null) {
            System.out.println("Need Kerberos REALM for secure clusters");
            HbaseKTest.usage(options);
            return;
       }
       Configuration config = HBaseConfiguration.create();
       System.out.println("Hbase zookeeper "+config.get("hbase.zookeeper.quorum"));
       config.set("hbase.zookeeper.quorum", zkQuorum);
       config.set("hbase.auth.token.max.lifetime", "300000");
       System.out.println("Hbase zookeeper "+config.get("hbase.zookeeper.quorum"));
       if (isSecure) {
         config.set("hadoop.security.authentication", "Kerberos");
         config.set("hbase.security.authentication", "Kerberos");
         config.set("hbase.master.kerberos.principal", "hbase/_HOST@"+krbRealm);
         config.set("hbase.regionserver.kerberos.principal", "hbase/_HOST@"+krbRealm);
         config.set("hadoop.security.authentication", "Kerberos");
         UserGroupInformation.setConfiguration(config);
         if (keyTab != null && userName != null) {
           UserGroupInformation.loginUserFromKeytab(userName, keyTab);
         }
       }
       Connection conn = ConnectionFactory.createConnection(config);
       Iterator<Map.Entry<String, String>> ite = config.iterator();
       while(ite.hasNext()) {
         Map.Entry<String,String> ent = ite.next();
         System.out.println(ent.getKey() + " " +ent.getValue());
       }
       TableName[] tables = conn.getAdmin().listTableNames();
       for (int i = 0; i < tables.length; i++)
         System.out.println("Table "+tables[i].getNameAsString());
       conn.close();
    }catch (Exception e) {
       e.printStackTrace();
    }
  }
}
