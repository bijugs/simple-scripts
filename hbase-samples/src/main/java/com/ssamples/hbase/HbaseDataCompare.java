package com.ssamples.hbase;

/*
 * Code to compare HBase table data
 * At this time the execution stops at the first mismatch
 * Usage() method has the details about how to use it
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.hadoop.hbase.util.Bytes;

import org.apache.log4j.Logger;


public class HbaseDataCompare implements Callable<Boolean> {

  private static final Logger logger = Logger.getLogger(HbaseDataCompare.class);
  private static Configuration compareConf = null;
  private static Connection conn1 = null;
  private static Connection conn2 = null;
  private static TableName tblName1 = null;
  private static TableName tblName2 = null;
  private static int numThreads = 10;
  private static int totErrorCount = 5;
  private static final AtomicInteger errorCount = new AtomicInteger(0);
  private static final AtomicInteger totalRows = new AtomicInteger(0);
  private static ExecutorService pool;
  private static int numVersions = 0;
  private Table table1 = null;
  private Table table2 = null;
  private String keys = null;
  private int threadId = 0;
  private int counter = 0;
  /*
   * Main method 
   * Need some more work on the result output
   */
  public static void main(String[] args) throws IOException {

    if (args.length < 1) {
      usage();
      return;
    }
    Path configPath = new Path(args[0]);
    compareConf = HbaseCompareUtil.getConfig(configPath);
    String tbl1 = compareConf.get("hbase.compare.table1");
    String tbl2 = compareConf.get("hbase.compare.table2");

    String zkQuorum1 = compareConf.get("hbase.zookeeper.quorum.compare.cluster1");
    String zkQuorum2 = compareConf.get("hbase.zookeeper.quorum.compare.cluster2");

    if (zkQuorum2 == null)
      zkQuorum2 = zkQuorum1;

    if (tbl1 == null || tbl2 == null) {
      logger.error("Two table names need to be provided to perform comparison");
      System.exit(1);
    } else if (zkQuorum1 == null && zkQuorum2 == null) {
      logger.error("Need to provide the quorum details of atleast one HBase cluster");
      System.exit(1);
    } else if (zkQuorum1.equals(zkQuorum2) && tbl1.equals(tbl2)) {
      logger.error("Trying to compare the same table");
      System.exit(1);
    }

    tblName1 = TableName.valueOf(tbl1);
    tblName2 = TableName.valueOf(tbl2);

    numVersions = compareConf.getInt("hbase.compare.versions",0);

    long start = System.currentTimeMillis();
    try {
      Configuration confCluster1 = HbaseCompareUtil.getConfig(compareConf, zkQuorum1, "1");
      Configuration confCluster2 = HbaseCompareUtil.getConfig(compareConf, zkQuorum2, "2");
      conn1 = ConnectionFactory.createConnection(confCluster1);
      conn2 = ConnectionFactory.createConnection(confCluster2);

      logger.info("Connections to HBase were taken");

      String keyProp = compareConf.get("hbase.startstop.key.compare");
      Collection<Callable<Boolean>> compCallable = null;

      int count = 0;
      numThreads = compareConf.getInt("hbase.max.threads.compare",10);
      totErrorCount = compareConf.getInt("hbase.error.count.compare",5);
      if (keyProp == null) {
        RegionLocator rLoc = conn1.getRegionLocator(tblName1);
        byte[][] startKeys = rLoc.getStartKeys();
        int numRegions = startKeys.length;
        int incr = 1;

        if (numRegions < numThreads) {
          numThreads = startKeys.length;
        } else {
          incr = numRegions/numThreads;
          if (numRegions%numThreads != 0)
            incr++;
        }

        int i;
        if (numRegions == 1) {
          pool = Executors.newFixedThreadPool(1);     
          compCallable = new ArrayList<Callable<Boolean>>(1);
          compCallable.add(new HbaseDataCompare(null, ++count));
        }
        else {
          pool = Executors.newFixedThreadPool(numThreads);     
          compCallable = new ArrayList<Callable<Boolean>>(numThreads); 
          for (i=incr; i < numRegions; i+=incr) {
            count++;
            if (i == incr){
              compCallable.add(new HbaseDataCompare(":"+Bytes.toString(startKeys[incr]), count));
            } else {
              compCallable.add(new HbaseDataCompare(Bytes.toString(startKeys[i-incr])+":"+Bytes.toString(startKeys[i]), count));
            }
          }
          compCallable.add(new HbaseDataCompare(Bytes.toString(startKeys[i-incr])+":", ++count));
        }
      } else {
        String[] keyArray = compareConf.get("hbase.startstop.key.compare").split(",");
        pool = Executors.newFixedThreadPool(keyArray.length);     
        compCallable = new ArrayList<Callable<Boolean>>(keyArray.length); 
        for (String keys : keyArray) {
          count += 1;
          compCallable.add(new HbaseDataCompare(keys, count));
        }
      }
      pool.invokeAll(compCallable);
      pool.shutdown();

      if (errorCount.intValue() > 0)
        logger.info("Mismatches found during table data compare ");
      logger.info("Total number of rows compared "+totalRows.toString());

    } catch (Exception e) {
      logger.error(" Failed to perform table comparison");
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      conn1.close();
      conn2.close();
      logger.info("Total time: "+(System.currentTimeMillis() - start)+" ms");
    }
  }

  HbaseDataCompare(String keys, int id) throws Exception{
    this.keys = keys;
    threadId = id;
    try {
      table1 = conn1.getTable(tblName1);
      table2 = conn2.getTable(tblName2);
    } catch (Exception e) {
      logger.error("ID:"+threadId+" Not able to create table object");
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      if (table1 != null)
        table1.close();
      if (table2 != null)
        table2.close();
    }
  }

  public Boolean call() {
    try{
      if (keys != null) {
        String[] keyArray = keys.split(":");
        String startKey = keys.split(":")[0];
        String stopKey = null;
        if (keyArray.length > 1)
          stopKey = keys.split(":")[1];
        compareTable(table1, table2,startKey, stopKey);
      } else {
        compareTable(table1, table2, null, null);
      }
      totalRows.addAndGet(counter);
    } catch (Exception e) {
      logger.error("ID:"+threadId+" Not able to perform table comparison");
      e.printStackTrace();
    }
    return true;
  }

  private void compareTable(Table t1, Table t2, String startKey, String stopKey) throws IOException {
    Scan s = new Scan();
    if (numVersions > 0)
      s.setMaxVersions(numVersions);
    else
      s.setMaxVersions();

    if (startKey != null) {
      s.setStartRow(startKey.getBytes());
    }

    if (stopKey != null) {
      s.setStopRow(stopKey.getBytes());
    }

    ResultScanner scan1 = t1.getScanner(s);
    ResultScanner scan2 = t2.getScanner(s);

    try {
      do {
        Result r1 = scan1.next();
    Result r2 = scan2.next();

        while(r1 != null && r2 != null && Bytes.compareTo(r1.getRow(), r2.getRow()) != 0 && errorCount.intValue() < totErrorCount) {
          if (Bytes.compareTo(r1.getRow(), r2.getRow()) > 0){
            logger.info("ID:"+threadId+" Table 2 rowid "+ Bytes.toString(r2.getRow()) +" not in table 1");
            errorCount.getAndIncrement();
            r2 = scan2.next();
          } else {
            logger.info("ID:"+threadId+" Table 1 rowid "+ Bytes.toString(r1.getRow()) +" not in table 2");
            errorCount.getAndIncrement();
            r1 = scan1.next();
          }
        }

        if (errorCount.intValue() >= totErrorCount)
          break;

    if (r1 == null && r2 == null) {
      break;
    }

    if (r1 != null && r2 != null) {
          if (!compareRow(r1, r2)) {
            errorCount.getAndIncrement();
      }
    } else {
      if (r1 != null) {
        logger.info("ID:"+threadId+" Completed comparing: Comparison failed since table 1 had more rows " +HbaseCompareUtil.getRow(r1));
        logger.info("ID:"+threadId+" Current row counter is " + counter);
      } else {
        logger.info("ID:"+threadId+" Completed comparing: Comparison failed since table 2 had more rows " +HbaseCompareUtil.getRow(r2));
        logger.info("ID:"+threadId+" Current row counter is " + counter);
          }
      break;
    }
    if (++counter % 1000 == 0) {
      System.out.print(".");
    }
      } while (errorCount.intValue() < totErrorCount);
      logger.info("ID:"+threadId+" Completed comparing " + counter + " rows");
    } finally {
      scan1.close();
      scan2.close();
    }
  }

  private boolean compareRow(Result r1, Result r2) throws IOException {
    CellScanner c1 = r1.cellScanner();
    CellScanner c2 = r2.cellScanner();
    if (!compare(r1, "row", null, r1.getRow(), r2.getRow())) {
      return false;
    }
    while (c1.advance()) {
      if (!c2.advance()) {
        logger.info("ID:"+threadId+" Second table does not have enough cells: " + HbaseCompareUtil.getRow(r2));
    return false;
      }
      Cell cell1 = c1.current();
      Cell cell2 = c2.current();
      if (!compare(r1, "family", null, CellUtil.cloneFamily(cell1),CellUtil.cloneFamily(cell2))) {
        return false;
      }
      if (!compare(r1, "qualifier", null, CellUtil.cloneQualifier(cell1),CellUtil.cloneQualifier(cell2))) {
        return false;
      }
      if (!compare(r1, "value", CellUtil.cloneQualifier(cell1),CellUtil.cloneValue(cell1), CellUtil.cloneValue(cell2))) {
        return false;
      }
      if (compareConf.getBoolean("hbase.table.timestamp.compare",true) && (cell1.getTimestamp() != cell2.getTimestamp())) {
        logger.info("ID:"+threadId+" Cell timestamps does not match for "+ HbaseCompareUtil.getRow(r1) + " in table1");
        return false;
      }
    }
    if (c2.advance()) {
      logger.info("ID:"+threadId+" First table does not have enough cells: "+ HbaseCompareUtil.getRow(r1));
      return false;
    }

    return true;
  }

  private boolean compare(Result row, String type, byte[] t,byte[] b1, byte[] b2) {
    if (Bytes.compareTo(b1, b2) != 0) {
      logger.info("ID:"+threadId+" "+type + " does not match for " + HbaseCompareUtil.getRow(row) +" in table1");
      if (t != null) {
        logger.info("qualifier: " + Bytes.toStringBinary(t)+" values "+Bytes.toStringBinary(b1)+" "+Bytes.toStringBinary(b2));
      }
      return false;
    }
    return true;
  }

  private static void usage() {
    System.out.println("Usage:");
    System.out.println("java com.bloomberg.hbase.sample.HbaseDataCompare hbase-compare.xml");
    System.out.println("**************************************************************");
    System.out.println("the following properties in hbase-compare.xml will be used");
    System.out.println("hbase.compare.table1 & 2: Names of tables to be compared");
    System.out.println("hbase.zookeeper.quorum.compare.cluster1 & 2: ZK quorum of the HBase clusters where the two tables are located");
    System.out.println("hbase.security.authentication.compare.cluster1 & 2: set to Kerberos if the cluster is secured");
    System.out.println("hbase.master.kerberos.principal.compare.cluster1 & 2: Set to the HBase master principal for the cluster if secured");
    System.out.println("hbase.regionserver.kerberos.principal.compare.cluster1 & 2: Set to the regionserver principal for the cluster if secured");
    System.out.println("hbase.kerberos.keytab.compare.cluster1 & 2: Set to the keytab file name if Kerberos keytab need to be used to login");
    System.out.println("hbase.kerberos.principal.compare.cluster1 & 2: Set to the principal name in the keytab which need to be used");
    System.out.println("hbase.table.timestamp.compare: Set it to false if mismatch in row timestamps are acceptable");
    System.out.println("hbase.startstop.key.compare: Specify the start and stop keys in the format a:b to compare");
    System.out.println("hbase.max.threads.compare: Specify the max number of thrads to do compare; default 10");
    System.out.println("hbase.compare.versions: Specify the max number of data versions to compare; default max");
    System.out.println("hbase.error.count.compare: Specify the max number of mismatches after which compare stops; default 5");
  }
}