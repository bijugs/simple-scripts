package com.ssamples.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class PhoenixQuery {

  private static final String TABLE_A = "tableA";
  private static final String TABLE_B = "tableB";

  private static void executeQuery(Connection conn, String query) throws SQLException {
    int count = 0;
    try (Statement stmt = conn.createStatement()) {
      long start = System.currentTimeMillis();
      try (ResultSet results = stmt.executeQuery(query)) {
        ResultSetMetaData rsmd = results.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        while (results.next()) {
          System.out.println(count+": Execution time: " + (System.currentTimeMillis() - start) + "ms");
          /*for (int i = 1; i <= columnsNumber; i++) {
            if (i > 1) System.out.print(",  ");
            String columnValue = results.getString(i);
            System.out.print(columnValue + " " + rsmd.getColumnName(i));
          }*/
          System.out.println("");
          count++;
          if (count == 3)
            break;
        }
      }
      System.out.println("Execution time: " + (System.currentTimeMillis() - start) + "ms");
    }
  }

  public static void main(String[] args) throws Exception {
    final String QUERY = "SELECT /*+USE_SORT_MERGE_JOIN*/ ...";
    Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
    try (Connection conn = DriverManager.getConnection("jdbc:phoenix:zk1,zk2,zk3:2181:/hbase")) {
      //System.out.println("Explain Query");
      //executeQuery(conn, "EXPLAIN " + QUERY);
      for (int i = 0; i < 5; i++) {
        System.out.println("Data Query");
        executeQuery(conn, QUERY);
      }
    }
  }
}
