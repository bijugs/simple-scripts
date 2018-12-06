package com.ssamples.hbase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.PerformanceEvaluation;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.ToolRunner;

public class ClientPerformanceEvaluation extends PerformanceEvaluation {
	static boolean isClientMode = false;
	static boolean isSecure = false;
	static boolean nomapred = false;
	static String zkQuorum = null;
	static String krbPrincipal = null;
	static String krbKeytab = null;
	static String krbRealm = null;

	public ClientPerformanceEvaluation(Configuration conf) {
            super(conf);
	}
	
	static String[] checkSecurity(Queue<String> args) {
            ArrayList<String> argm = new ArrayList<String>();
	    String cmd = null;
	    while ((cmd = args.poll()) != null) {
	      final String clientMode = "--clientmode";
	      if (cmd.startsWith(clientMode)) {
	        isClientMode = true;
	        continue;
	      }
	      final String secure = "--secure";
	      if (cmd.startsWith(secure)) {
	        isSecure = true;
	        continue;
	      }
	      final String zkNodes = "--zkquorum=";
	      if (cmd.startsWith(zkNodes)) {
	        zkQuorum = cmd.substring(zkNodes.length());
	        continue;
	      }
	      final String principal = "--principal=";
	      if (cmd.startsWith(principal)) {
	        krbPrincipal = cmd.substring(principal.length());
	        continue;
	      }
	      final String keytab = "--keytab=";
	      if (cmd.startsWith(keytab)) {
	        krbKeytab = cmd.substring(keytab.length());
	        continue;
	      }
              final String nmr = "--nomapred";
              if (cmd.startsWith(nmr)) {
                nomapred = true;
                continue;
              }
              final String realm = "--realm=";
              if (cmd.startsWith(realm)) {
                krbRealm = cmd.substring(realm.length());
                continue;
              }
              argm.add(cmd);
           }		
           String[] peArgs = new String[argm.size()];
	   int i = 0;
	   for (String arg : argm) {
	       peArgs[i] = arg;
	       i++;
	   }
	   return peArgs;
	}
	
	public static void main(final String[] args) throws Exception {
	    Queue<String> argv = new LinkedList<>();
	    argv.addAll(Arrays.asList(args));
            String[] peArgs = checkSecurity(argv);
	    System.out.println("ClientMode "+isClientMode+" zkQuorum "+zkQuorum+" principal "+krbPrincipal+" keytab "+krbKeytab);
            Configuration config = HBaseConfiguration.create();
            if (isClientMode && !nomapred) {
              System.out.println("For client mode can only run as nomapred");
              System.exit(1);
            } else if (zkQuorum != null) {
              config.set("hadoop.zookeeper.quorum", zkQuorum);
            }
            if (isSecure) {
              config.set("hadoop.security.authentication", "Kerberos");
              config.set("hbase.security.authentication", "Kerberos");
              config.set("hbase.master.kerberos.principal", "hbase/_HOST@"+krbRealm);
              config.set("hbase.regionserver.kerberos.principal", "hbase/_HOST@"+krbRealm);
              UserGroupInformation.setConfiguration(config);
              if (krbPrincipal != null && krbKeytab != null) {
                UserGroupInformation.loginUserFromKeytab(krbPrincipal, krbKeytab);
              }
            }
	    int res = ToolRunner.run(new PerformanceEvaluation(HBaseConfiguration.create()), peArgs);
	    System.exit(res);
        }
}
