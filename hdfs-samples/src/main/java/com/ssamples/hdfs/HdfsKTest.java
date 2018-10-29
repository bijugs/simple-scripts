package com.ssamples.hdfs;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.cli.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.ipc.StandbyException;
import org.apache.hadoop.security.UserGroupInformation;

public class HdfsKTest {

    public static void usage(Options options) {
        System.out.println("Namenode and hdfs path are required");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "HdfsKTest", options );
        return;
    }

    public static void main(String args[]) {
        String[] nameNodes = new String[2];
        String userName = null;
        String keyTab = null;
        String hdfsPath = null;
        boolean isSecure = false;
        FileSystem fs = null;
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption( "s", "secure", false, "secure cluster" );
        options.addOption( "n1", "namenode1", true, "Namenode host:port" );
        options.addOption( "n2", "namenode2", true, "Namenode host:port" );
        options.addOption( "u", "username", true, "User name in Keytab" );
        options.addOption( "k", "keytab", true, "Keytab file path" );
        options.addOption( "p", "path", true, "hdfs path to be used as root" );
        if (args.length < 2) {
           HdfsKTest.usage(options);
           return;
        }

        try {
              CommandLine line = parser.parse( options, args );
              if( line.hasOption( "secure" ) ) {
                  // print whether the HDFS cluster is Secure
                  System.out.println("The cluster is secure");
                  isSecure = true;
              }
              if( line.hasOption( "namenode1" ) ) {
                  // print the namenode host:port for the cluster
                  nameNodes[0] = line.getOptionValue("namenode1");
                  System.out.println("Will connect to NN "+nameNodes[0]);
              }
              if( line.hasOption( "namenode2" ) ) {
                  // print the namenode host:port for the cluster
                  nameNodes[1] = line.getOptionValue("namenode2");
                  System.out.println("Will connect to NN "+nameNodes[1]);
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
              if( line.hasOption( "path" ) ) {
                  // print the hdfs path passed
                  hdfsPath = line.getOptionValue("path");
                  System.out.println("Will use HDFS directory "+hdfsPath);
              }
              if (nameNodes[0] == null || hdfsPath == null) {
                  HdfsKTest.usage(options);
                  return;
              }

              Configuration conf = new Configuration();
              conf.set("fs.hdfs.impl",org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
              conf.set("fs.file.impl",org.apache.hadoop.fs.LocalFileSystem.class.getName());
              if (isSecure) {
                  System.out.println("Performing UGI login since the cluster is secure");
                  conf.set("hadoop.security.authentication", "Kerberos");
                  UserGroupInformation.setConfiguration(conf);
                  if (userName != null && keyTab != null) {
                      System.out.println("Performing UGI login from keyTab");
                      UserGroupInformation.loginUserFromKeytab(userName, keyTab);
                  } else {
                      System.out.println("Performing UGI login using current user");
                      UserGroupInformation.loginUserFromSubject(null);
                 }
              }
              for (String nameNode : nameNodes) {
                  try {
                      System.out.println("Namenode trying "+nameNode);
                      if (nameNode == null)
                         break;
                      conf.set("fs.defaultFS", "hdfs://"+nameNode);
                      fs = FileSystem.get(conf);

                      fs.createNewFile(new Path(hdfsPath+"/test"));

                      if (!fs.exists(new Path(hdfsPath+"/data.txt"))) {
                          FSDataOutputStream outStream = fs.create(new Path(hdfsPath+"/data.txt"),false);
                          BufferedWriter bw = new BufferedWriter( new OutputStreamWriter( outStream, "UTF-8" ) );   
                          bw.write("1,Ant\n");
                          bw.write("2,Bat");
                          bw.close();
                      } else {
                          FSDataOutputStream outStream = fs.append(new Path(hdfsPath+"/data.txt"));
                          BufferedWriter bw = new BufferedWriter( new OutputStreamWriter( outStream, "UTF-8" ) );   
                          bw.write("1,Ant\n");
                          bw.write("2,Bat");
                          bw.close();
                      }

                      FileStatus[] status = fs.listStatus(new Path(hdfsPath));
                      for(int i=0;i<status.length;i++){
                          System.out.println(status[i].getPath());
                      }
                      fs.delete(new Path(hdfsPath+"/test"));
                      break;
                  } catch (RemoteException rEx) {
                       if (rEx.getClassName().equals("org.apache.hadoop.ipc.StandbyException"))
                           System.out.println("Caught StandbyException "+rEx.getClassName());             
                       else
                          throw rEx;
                  }
              }
              fs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
