package com.ssamples.hbase;

import java.security.PrivilegedExceptionAction;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

public class HbaseRegionKeys {
  public static void main(String args[]) throws IOException{
    org.apache.log4j.BasicConfigurator.configure();
    Connection conn = null;
    try {
       /**
        * It is assumed that the hbase-site.xml of the target cluster is avaliable in the CLASSPATH
        */
       Configuration config = HBaseConfiguration.create();
       config.set("hbase.zookeeper.quorum", args[0]);
       System.out.println("Hbase zookeeper "+config.get("hbase.zookeeper.quorum"));
       config.set("hadoop.security.authentication", "Kerberos");
       config.set("hbase.security.authentication", "Kerberos");
       config.set("hbase.master.kerberos.principal", "hbase/_HOST@REALM");
       config.set("hbase.regionserver.kerberos.principal", "hbase/_HOST@REALM");
       UserGroupInformation.setConfiguration(config);
       if (args.length > 1)
         UserGroupInformation.loginUserFromKeytab(args[1], args[2]);
       conn = ConnectionFactory.createConnection(config);
       RegionLocator rLoc = conn.getRegionLocator(TableName.valueOf("tbn"));
       Pair<byte[][],byte[][]> startEnd = rLoc.getStartEndKeys();
       System.out.println("Number of start end keys = "+startEnd.getFirst().length);
       HRegionLocation hrl = rLoc.getRegionLocation(Bytes.toBytes("043c559fb8e8021001b200000aca"));
       if (hrl != null)
          System.out.println("Region server hostname "+ hrl.getHostname());
       conn.close();
    }catch (Exception e) {
       e.printStackTrace();
       conn.close();
    }
  }
}
