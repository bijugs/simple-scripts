package com.ssamples.hbase;

import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.UserGroupInformation;
import java.security.PrivilegedExceptionAction;
import java.io.IOException;
import java.util.Map;
import java.util.Iterator;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.hadoop.hbase.util.Bytes;

public class HbaseMultiUGI implements Runnable {

  String threadName;
  Connection conn;

  HbaseMultiUGI(String name, Connection conn) {
    this.threadName = name;
    this.conn = conn;
  }

  public void run(){
    Table table = null;
    try {
      table = conn.getTable(TableName.valueOf("tbn"));
      while (true){
        System.out.println("***** Starting table operation thread in "+ threadName);
        java.util.Date date= new java.util.Date();
        Get g = new Get(Bytes.toBytes("r1"));
        Result r = table.get(g);
        Thread.sleep(600000);
        System.out.println("***** Completed table operation in thread "+threadName +" "+new Timestamp(date.getTime())+" "+new String(r.value()));
      }
    } catch(Exception e) {
      e.printStackTrace();
      return;
    }
  }

  public static void main(String args[]) throws IOException{
    try {
       for (int i = 0; i < args.length; i++)
          System.out.println(" arg "+i+" "+args[i]);
       if (args.length < 5) {
          System.out.println("Expected params : krbRealm, zkCluster1, zkCluster2, principal, keyTab");
          return;
       }
       String krbRealm = args[0];
       System.out.println("krbRealm = "+args[0]);
       Configuration config1 = HBaseConfiguration.create();
       config1.set("hbase.zookeeper.quorum", args[1]);
       System.out.println("Hbase zookeeper "+config1.get("hbase.zookeeper.quorum"));
       config1.set("hadoop.security.authentication", "Kerberos");
       config1.set("hbase.security.authentication", "hbase/_HOST@"+krbRealm);
       config1.set("hbase.master.kerberos.principal", "Kerberos");
       config1.set("hbase.regionserver.kerberos.principal", "Kerberos");
       MultiUserGroupInformation ugi1 = new MultiUserGroupInformation(config1, args[3], args[4]);
       Connection conn1 = ConnectionFactory.createConnection(config1);
       System.out.println("************ Connected to HBase *************");
       Thread t1 = new Thread(new HbaseMultiUGI("1",conn1));
       t1.start();

       Configuration config2 = HBaseConfiguration.create();
       config2.set("hbase.zookeeper.quorum", args[2]);
       System.out.println("Hbase zookeeper "+config2.get("hbase.zookeeper.quorum"));
       config2.set("hadoop.security.authentication", "Kerberos");
       config2.set("hbase.security.authentication", "hbase/_HOST@"+krbRealm);
       config2.set("hbase.master.kerberos.principal", "Kerberos");
       config2.set("hbase.regionserver.kerberos.principal", "Kerberos");
       MultiUserGroupInformation ugi2 = new MultiUserGroupInformation(config1, args[3], args[4]);
       Connection conn2 = ConnectionFactory.createConnection(config2);
       Thread t2 = new Thread(new HbaseMultiUGI("2",conn2));
       t2.start();
       t1.join();
       t2.join();
    } catch (Exception e) {
       e.printStackTrace();
    }
  }
}
