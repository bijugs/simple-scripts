package com.ssample.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class DBAccessorThread implements Runnable {

  String query;
  String zk;
  Connection conn;
  public DBAccessorThread(String q, String c) throws Exception {
      this.query = q;
      this.zk = c;
  }

  public void run(){
      try {
          Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
          conn = DriverManager.getConnection("jdbc:phoenix:"+zk+":2181:/hbase");
          executeQuery();
          conn.close();
      } catch (Exception ex) {
        System.out.println("Something happened with the query execution");
        ex.printStackTrace();
        return;
      }
  }

  private void executeQuery() throws SQLException {
      int count = 0;
      long start = 0;
      try  {
        Statement stmt = conn.createStatement();
        start = System.currentTimeMillis();
        ResultSet results = stmt.executeQuery(query);
        ResultSetMetaData rsmd = results.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        while (results.next()) {
          System.out.println(count+": Execution time: " + (System.currentTimeMillis() - start) + "ms");
          System.out.println("");
          count++;
          if (count == 3)
            break;
        }
      } finally {
      
      }
      System.out.println("Execution time: " + (System.currentTimeMillis() - start) + "ms");
  }
}

public class PhoenixQueryMulti {

  public static void main(String[] args) throws Exception {

    String zk = "localhost";
    if (args.length < 3){
        System.out.println("Enter date, start-time, end-time");
        return;
    }
    String[] prefix = {"C","MU","MT"};
    String PRE = null;

    System.out.println("Connecting to ZK:"+zk);
    //Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
    ExecutorService executor = Executors.newFixedThreadPool(prefix.length);
    for (int i = 0; i < prefix.length; i++) {
        PRE = prefix[i];
        String query = "SELECT * FROM "+PRE+"_TABLE_A";
        System.out.println("Query "+i+" = "+query);
        Runnable reader = new DBAccessorThread(query,zk);
        executor.execute(reader);
    }
    while (!executor.isTerminated()){
    }
    executor.shutdown();
  }
}
