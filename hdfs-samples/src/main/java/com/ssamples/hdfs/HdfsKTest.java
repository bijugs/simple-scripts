package com.ssamples.hdfs;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;

public class HdfsKTest {

    public static void main(String args[]) {

        try {
              Configuration conf = new Configuration();
              conf.set("fs.defaultFS", "hdfs://NN:PORT/user/hbase");
              conf.set("hadoop.security.authentication", "Kerberos");
              UserGroupInformation.setConfiguration(conf);
              UserGroupInformation.loginUserFromKeytab("ubuntu/hostname@REALM.EXAMPLE.COM", "/home/ubuntu/ubuntu.keytab");
              FileSystem fs = FileSystem.get(conf);

              fs.createNewFile(new Path("/user/hbase/test"));

              FileStatus[] status = fs.listStatus(new Path("/user/hbase"));
              for(int i=0;i<status.length;i++){
                  System.out.println(status[i].getPath());
              }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}