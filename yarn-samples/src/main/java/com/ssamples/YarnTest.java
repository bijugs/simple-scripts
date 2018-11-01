package com.ssamples.yarn;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;

import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.api.records.YarnClusterMetrics;

public class YarnTest {

    public static void usage(Options options) {
        System.out.println("RM host and port are required");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "YarnTest", options );
        return;
    }

    public static void main(String args[]) throws Exception {
        YarnClient yarnClient = null;
        String[] rmNodes = new String[2];
        String userName = null;
        String keyTab = null;
        String krbRealm = null;
        boolean isSecure = false;
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption( "s", "secure", false, "secure cluster" );
        options.addOption( "r1", "yarnrm1", true, "Yarn resource manager host:port" );
        options.addOption( "r2", "yarnrm2", true, "Yarn resource manager host:port" );
        options.addOption( "u", "username", true, "User name in Keytab" );
        options.addOption( "k", "keytab", true, "Keytab file path" );
        options.addOption( "r", "realm", true, "Kerberos realm" );
        if (args.length < 2) {
           YarnTest.usage(options);
           return;
        }

        CommandLine line = parser.parse( options, args );
        if( line.hasOption( "secure" ) ) {
            // print whether the HDFS cluster is Secure
            System.out.println("The cluster is secure");
            isSecure = true;
        }
        if( line.hasOption( "yarnrm1" ) ) {
            // print the Yarn RM host:port for the cluster
            rmNodes[0] = line.getOptionValue("yarnrm1");
            System.out.println("Will connect to RM "+rmNodes[0]);
        }
        if( line.hasOption( "yarnrm2" ) ) {
            // print the Yarn RM host:port for the cluster
            rmNodes[1] = line.getOptionValue("yarnrm2");
            System.out.println("Will connect to RM "+rmNodes[1]);
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
        if( line.hasOption( "realm" ) ) {
            // print the realm passed
            krbRealm = line.getOptionValue("realm");
            System.out.println("Will user realm "+krbRealm);
        }
        if (rmNodes[0] == null) {
            YarnTest.usage(options);
            return;
        }
        try {
            Configuration conf = new YarnConfiguration();
            conf.setLong(YarnConfiguration.RESOURCEMANAGER_CONNECT_MAX_WAIT_MS, 5);
            conf.unset(YarnConfiguration.CLIENT_FAILOVER_MAX_ATTEMPTS);
            //conf.setLong(YarnConfiguration.RESOURCEMANAGER_CONNECT_RETRY_INTERVAL_MS, 2);
            if (isSecure) {
                if (krbRealm == null){
                   System.out.println("Realm is required to connect to a secure cluster");
                   YarnTest.usage(options);
                   return;
                }
                conf.set("hadoop.security.authentication", "Kerberos");
                conf.set(YarnConfiguration.RM_PRINCIPAL, "yarn/_HOST@"+krbRealm);
                UserGroupInformation.setConfiguration(conf);
                if (userName != null && keyTab != null)
                    UserGroupInformation.loginUserFromKeytab(userName, keyTab);
                else
                    UserGroupInformation.loginUserFromSubject(null);
            }
            for (String rmNode : rmNodes) {
                if (rmNode == null)
                   break;
                try {
                    conf.set(YarnConfiguration.RM_ADDRESS, rmNode);
                    yarnClient = YarnClient.createYarnClient();
                    yarnClient.init(conf);
                    yarnClient.start();
                    YarnClusterMetrics clusterMetrics = yarnClient.getYarnClusterMetrics();
                    System.out.println("Able to start yarn client "+clusterMetrics.getNumNodeManagers());
                    yarnClient.stop();
                } catch(ConnectException ex) {
                    System.out.println("Connection refused. Non active RM possible");
                    break;
                }
           }
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            if (yarnClient != null)
                yarnClient.stop();
        }
    }
}
