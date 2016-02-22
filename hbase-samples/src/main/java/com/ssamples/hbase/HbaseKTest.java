package com.ssamples.hbase;

import org.apache.hadoop.security.UserGroupInformation;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

public class HbaseKTest {
  public static void main(String args[]){
    try {
       Configuration config = HBaseConfiguration.create();
       System.out.println("Hbase zookeeper "+config.get("hbase.zookeeper.quorum"));
       config.set("hadoop.security.authentication", "Kerberos");
       config.set("hbase.auth.token.max.lifetime", "300000");
       UserGroupInformation.setConfiguration(config);
       UserGroupInformation.loginUserFromKeytab("ubuntu/ll22-bcpc-r1n7.ll22.fqdn.com@BCPC.EXAMPLE.COM", "/home/ubuntu/ubuntu.keytab");
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
