package com.ssamples.hive;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;

import org.apache.commons.cli.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.security.UserGroupInformation;

public class HiveJdbcClient {
  private static String driverName = "org.apache.hive.jdbc.HiveDriver";

  public static void usage(Options options) {
    System.out.println("Hive Server and port are required");
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "HdfsKTest", options );
    return;
  }

  public static void main(String[] args) throws SQLException {
    Connection con = null;
    String hiveServer = null;
    String hivePort = null;
    String userName = null;
    String keyTab = null;
    String krbRealm = null;
    String hiveDB = "default";
    boolean isSecure = false;
    CommandLineParser parser = new GnuParser();
    Options options = new Options();
    options.addOption( "s", "secure", false, "secure cluster" );
    options.addOption( "h", "hiveserver", true, "Hiveserver host" );
    options.addOption( "p", "hiveport", true, "Hiveserver port" );
    options.addOption( "r", "krbrealm", true, "Kerberos realm" );
    options.addOption( "u", "username", true, "User name in Keytab" );
    options.addOption( "k", "keytab", true, "Keytab file path" );
    options.addOption( "d", "database", true, "Hive database, default:default" );
    if (args.length < 2) {
       HiveJdbcClient.usage(options);
       return;
    }
    try {
      CommandLine line = parser.parse( options, args );
      if( line.hasOption( "secure" ) ) {
          // print whether the HDFS cluster is Secure
          System.out.println("The cluster is secure");
          isSecure = true;
      }
      if( line.hasOption( "hiveserver" ) ) {
          // print the Hive server host for the cluster
          hiveServer = line.getOptionValue("hiveserver");
          System.out.println("Will connect to HS2 "+hiveServer);
      }
      if( line.hasOption( "hiveport" ) ) {
          // print the Hive port for the cluster
          hivePort = line.getOptionValue("hiveport");
          System.out.println("Will connect to HS2 "+hivePort);
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
      if( line.hasOption( "krbrealm" ) ) {
          // print the Kerberos realms to be used
          krbRealm = line.getOptionValue("krbrealm");
          System.out.println("Will user Kerberos realmn  "+krbRealm);
      }
      if( line.hasOption( "database" ) ) {
          // print the database name to be used
          hiveDB = line.getOptionValue("database");
          System.out.println("Will use Hive DB"+hiveDB);
      }
      if (hiveServer == null || hivePort == null) {
          HiveJdbcClient.usage(options);
          return;
      }
      Class.forName(driverName);
      Configuration conf = new Configuration();
      if (isSecure) {
          if (krbRealm == null) {
              System.out.println("For secure Hive need realm to connect");
              HiveJdbcClient.usage(options);
              return;
          }
          conf.set("hadoop.security.authentication", "Kerberos");
          UserGroupInformation.setConfiguration(conf);
          if (userName == null || keyTab == null) {
              UserGroupInformation.loginUserFromSubject(null);
          } else {
              UserGroupInformation.loginUserFromKeytab(userName, keyTab);
          }
      }
      System.out.println("jdbc:hive2://"+hiveServer+":"+hivePort+"/"+hiveDB+";principal=hive/"+hiveServer+"@"+krbRealm);
      con = DriverManager.getConnection("jdbc:hive2://"+hiveServer+":"+hivePort+"/"+hiveDB+";principal=hive/"+hiveServer+"@"+krbRealm, "", "");
      System.out.println("Connection successful");
      Statement stmt = con.createStatement();
      String tableName = "testHiveNonTableBN";
      stmt.execute("drop table " + tableName);
    } catch (ClassNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          System.exit(1);
    } catch (Exception ex) {
          ex.printStackTrace();
    } finally {
          con.close();
    }
  }
}
