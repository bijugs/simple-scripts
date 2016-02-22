package com.ssamples.hbase;

/*
 * Common functions which are used for HBase data comparison
 */
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import org.apache.hadoop.security.UserGroupInformation;

import org.apache.log4j.Logger;

public class HbaseCompareUtil {

  private static final Logger logger = Logger.getLogger(HbaseCompareUtil.class);

  /*
   * Method to create configuration object for HBase clusters based on hbase compare utility configuration
   */
  public static Configuration getConfig(Configuration compareConf, String zkQuorum, String clusterId) throws Exception {

    Configuration confCluster = HBaseConfiguration.create();
    confCluster.set("hbase.zookeeper.quorum", zkQuorum);

    if ("kerberos".equalsIgnoreCase(compareConf.get("hbase.security.authentication.compare.cluster"+clusterId))) {
      confCluster.set("hadoop.security.authentication", "Kerberos");
      confCluster.set("hbase.security.authentication", "Kerberos");
      String masterPrincipal = compareConf.get("hbase.master.kerberos.principal.compare.cluster"+clusterId);
      if (masterPrincipal == null) {
        logger.error("Error: Need to provide HBase master Kerberos principal to connect to table "+clusterId);
        throw new Exception("Error: Need to provide HBase master Kerberos principal to connect to hbase");
      }
      else
        confCluster.set("hbase.master.kerberos.principal",masterPrincipal);
      String rsPrincipal = compareConf.get("hbase.regionserver.kerberos.principal.compare.cluster"+clusterId);

      if (rsPrincipal == null) {
        logger.error("Error: Need to provide HBase regionserver Kerberos principal to connect to table "+clusterId);
        throw new Exception("Error: Need to provide HBase master Kerberos principal to connect to hbase");
      }
      else
        confCluster.set("hbase.regionserver.kerberos.principal",rsPrincipal);

      String krbPrincipal = compareConf.get("hbase.kerberos.principal.compare.cluster"+clusterId);
      String krbKeyTab = compareConf.get("hbase.kerberos.keytab.compare.cluster"+clusterId);

      if (krbPrincipal != null && krbKeyTab != null) {
        try {
          UserGroupInformation ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(krbPrincipal,krbKeyTab);
          UserGroupInformation.setLoginUser(ugi);
        } catch (IOException e) {
          logger.error("Not able to login using the principal and keytab provided for table "+clusterId);
          throw new Exception(e);
        }
      }
      UserGroupInformation.setConfiguration(confCluster);
    }
    return confCluster;
  }

  /*
   * Method to create a configuration object for the site.xml file passed
   */
  public static Configuration getConfig(Path path) {
    Configuration conf = new Configuration();
    conf.addResource(path);
    return conf;
  }

  public static String getRow(Result r) {
    return Bytes.toStringBinary(r.getRow());
  }
}
