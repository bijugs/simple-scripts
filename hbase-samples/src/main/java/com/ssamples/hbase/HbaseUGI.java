package com.ssamples.hbase;

import org.apache.hadoop.security.UserGroupInformation;
import java.security.PrivilegedExceptionAction;
import java.io.IOException;
import java.util.Map;
import java.util.Iterator;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.hadoop.hbase.util.Bytes;

public class HbaseUGI implements Runnable {

  String threadName;
  Connection conn;

  HbaseUGI(String name, Connection conn) {
    this.threadName = name;
    this.conn = conn;
  }

  public void run(){
    Table table = null;
    try {
      System.out.println("***** Starting table operation thread in "+ threadName +" "+conn+" "+conn.isClosed());
      table = conn.getTable(TableName.valueOf("tbn"));
      HTableDescriptor desc = table.getTableDescriptor();
      System.out.println("Number of column families "+ desc.getColumnFamilies());
      while (true){
        java.util.Date date= new java.util.Date();
        Get g = new Get(Bytes.toBytes("r1"));
        Result r = table.get(g);
        Thread.sleep(100);
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
       String krbRealm = args[0];
       System.out.println("krbRealm = "+args[0]);
       Configuration config1 = HBaseConfiguration.create();
       config1.set("hbase.zookeeper.quorum", args[1]);
       System.out.println("Hbase zookeeper "+config1.get("hbase.zookeeper.quorum"));
       config1.set("hadoop.security.authentication", "Kerberos");
       config1.set("hbase.security.authentication", "Kerberos");
       config1.set("hbase.master.kerberos.principal", "hbase/_HOST@"+krbRealm);
       config1.set("hbase.regionserver.kerberos.principal", "hbase/_HOST@"+krbRealm);
       UserGroupInformation.setConfiguration(config1);
       if (args.length == 4) {
         UserGroupInformation.loginUserFromKeytab(args[2], args[3]);
       }
       Connection conn1 = ConnectionFactory.createConnection(config1);
       System.out.println("************ Connected to HBase *************");
       /*Table table = conn1.getTable(TableName.valueOf("tbn"));
       System.out.println("******* Done getTable ******");
       HTableDescriptor desc = table.getTableDescriptor();
       System.out.println("******* Done getTableDescriptor ******");
       Get g = new Get(Bytes.toBytes("r1"));
       Result r = table.get(g);
       System.out.println("***** Completed table operation in thread "+new String(r.value()));
       conn1.close();*/
       Thread t1 = new Thread(new HbaseUGI("1",conn1));
       t1.start();
       t1.join();
    } catch (Exception e) {
       e.printStackTrace();
    }
  }
}
