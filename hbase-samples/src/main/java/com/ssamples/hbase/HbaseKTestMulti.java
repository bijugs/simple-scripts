package com.ssample.hbase;

import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.UserGroupInformation;
import java.security.PrivilegedExceptionAction;
import java.io.IOException;
import java.util.Map;
import java.util.Iterator;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.hadoop.hbase.util.Bytes;

public class HbaseKTestMulti implements Runnable {

  static HConnection conn = null;
  String threadName;

  HbaseKTestMulti(String name) {
    threadName = name;
  }

  public void run(){
    HTableInterface table = null;
    try {
      table = conn.getTable("tbn");
      while (true){
        System.out.println("***** Starting table operation thread in "+ threadName);
        java.util.Date date= new java.util.Date();
        Get g = new Get(Bytes.toBytes("r1"));
        Result r = table.get(g);
        Thread.sleep(60000);
        System.out.println("***** Completed table operation in thread "+threadName +" "+new Timestamp(date.getTime())+" "+new String(r.value()));
      }
    } catch(Exception e) {
      e.printStackTrace();
      return;
    }
  }

  public static void main(String args[]) throws IOException{
    try {
       Configuration config = HBaseConfiguration.create();
       if (args.length > 2)
           config.set("hbase.zookeeper.quorum", args[2]);
       System.out.println("Hbase zookeeper "+config.get("hbase.zookeeper.quorum"));
       config.set("hadoop.security.authentication", "Kerberos");
       UserGroupInformation.setConfiguration(config);
       if (args.length > 3)
         UserGroupInformation.loginUserFromKeytab(args[3], args[4]);
       HBaseAdmin hba = new HBaseAdmin(config);
       System.out.println("Cluster Id "+hba.getClusterStatus().getClusterId());
       conn = HConnectionManager.createConnection(config);
       System.out.println("************ Connected to HBase *************");
       Thread t1 = new Thread(new HbaseKTestMulti("1"));
       t1.start();
       Thread t2 = new Thread(new HbaseKTestMulti("2"));
       t2.start();
       t1.join();
       t2.join();
    }catch (Exception e) {
       e.printStackTrace();
       if (conn != null)
         conn.close();
    }
  }
}
