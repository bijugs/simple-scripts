package com.ssamples.hdfs;

import org.apache.commons.cli.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;

public class HdfsKTest {

    public static void usage(Options options) {
        System.out.println("Namenode and hdfs path are required");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "HdfsKTest", options );
        return;
    }


    public static void main(String args[]) {
        String nameNode = null;
        String userName = null;
        String keyTab = null;
        String hdfsPath = null;
        boolean isSecure = false;
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption( "s", "secure", false, "secure cluster" );
        options.addOption( "n", "namenode", true, "Namenode host:port" );
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
              if( line.hasOption( "namenode" ) ) {
                  // print the namenode host:port for the cluster
                  nameNode = line.getOptionValue("namenode");
                  System.out.println("Will connect to NN "+nameNode);
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
              if (nameNode == null || hdfsPath == null) {
                  HdfsKTest.usage(options);
                  return;
              }

              Configuration conf = new Configuration();
              conf.set("fs.hdfs.impl",org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
              conf.set("fs.file.impl",org.apache.hadoop.fs.LocalFileSystem.class.getName());
              conf.set("fs.defaultFS", "hdfs://"+nameNode);
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
              FileSystem fs = FileSystem.get(conf);

              fs.createNewFile(new Path(hdfsPath+"/test"));

              FileStatus[] status = fs.listStatus(new Path(hdfsPath));
              for(int i=0;i<status.length;i++){
                  System.out.println(status[i].getPath());
              }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
