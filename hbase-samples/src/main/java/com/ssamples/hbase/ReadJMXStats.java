package com.ssamples.hbase;

import java.lang.Object;
import java.lang.StringBuffer;
import java.lang.Integer;

import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;

public class ReadJMXStats implements Callable<Boolean> {

    static String[] attr = {"memStoreSize", "storeFileCount", "flushQueueLength", "compactionQueueLength"};
    String serverName = null;
    static String[] servers = {"rs1", "rs2"};
    static int samplingInterval = 10;      // In seconds
    static int totalRuntime = 2;           // In minutes
    static int totalRun;
    static int sleepTime;

    public ReadJMXStats(String server) {
        serverName = server;
    }

    public Boolean call() {
        int count = 0;
        try {
            PrintWriter out = new PrintWriter(new FileWriter(serverName+".txt"));
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + serverName + ":10102/jmxrmi");
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
            while (count < totalRun) {
                StringBuffer data = new StringBuffer();
                data.append(serverName);
                data.append(" ");
                data.append(new Timestamp(new Date().getTime()));
                for (int i = 0; i < attr.length; i++) {
                    Object val = mbsc.getAttribute(new ObjectName("Hadoop:service=HBase,name=RegionServer,sub=Server"), attr[i]);
                    data.append(" ");
                    data.append(attr[i]);
                    data.append(" ");
                    data.append(val);
                }
                out.println(data);
                count++;
                Thread.sleep(sleepTime);
            }
            out.close();
        } catch (MalformedURLException e) {
            System.out.println(serverName +" Invalid URL provided to create JMXServiceURL");
            return false;
        } catch (IOException e) {
            System.out.println(serverName +" IOException reading JMX Data");
            return false;
        } catch (Exception e) {
            System.out.println(serverName +" MBean exception");
            return false;
        }
        return true;
    }

    public static void main(String args[]) {
        int idx = 0;
        if (args.length > 0) {
           while (args[idx].startsWith("-")) {
              if (args[idx].compareTo("-i")==0)
                 samplingInterval = Integer.parseInt(args[++idx]);

              if (args[idx].compareTo("-t")==0)
                 totalRuntime = Integer.parseInt(args[++idx]);

              if (args[idx].compareTo("-s")==0)
                 servers = args[++idx].split(",");

              if (args[idx].compareTo("-a")==0)
                 attr = args[++idx].split(",");

              idx++;
              if (idx >= args.length)
                 break;
           }
        }
        totalRun = totalRuntime * 60 / samplingInterval;
        sleepTime = samplingInterval * 1000;

        try {
            ExecutorService pool = Executors.newFixedThreadPool(servers.length);
            Collection<Callable<Boolean>> calls = new ArrayList<Callable<Boolean>>(servers.length);
            for (int i = 0; i < servers.length; i++) {
               calls.add(new ReadJMXStats(servers[i]));
            }
            System.out.println(new Timestamp(new Date().getTime())+" Starting JMX stats collection at "+samplingInterval+" sec interval for "+ totalRuntime + "minutes");
            pool.invokeAll(calls);
            pool.shutdown();
            System.out.println(new Timestamp(new Date().getTime())+" JMX stats collection completed");
        } catch (InterruptedException e) {
            System.out.println("Interrupted exception from main");
        }
    }
}
