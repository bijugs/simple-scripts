package com.ssamples.hbase;

import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HConstants;

public class TryHBaseTestUtility {

    public static void main(String args[]) throws Exception{
        HBaseTestingUtility htu1 = new HBaseTestingUtility();
        htu1.getConfiguration().set(HConstants.ZOOKEEPER_ZNODE_PARENT, "/hbase1");
        htu1.getConfiguration().set(HConstants.ZOOKEEPER_CLIENT_PORT, "64410");
        htu1.getConfiguration().set(HConstants.MASTER_INFO_PORT, "64310");
        try{
            htu1.startMiniCluster();
            System.out.println("Start of cluster complete.. going to sleep");
            Thread.sleep(600000);
            System.out.println("I am awake now");
            Thread.sleep(600000);
            System.out.println("Shutting down");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            htu1.shutdownMiniCluster();
        }
    }
}