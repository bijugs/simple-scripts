package com.ssamples.hbase;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

public class HTablePool {

  private ArrayBlockingQueue<Table> tablePool;
  private Connection conn;
  private String tableName;
  private int maxSize = 0;

  HTablePool(Connection conn, int maxSize, String tableName) throws IOException{

    if (maxSize < 1) {
      throw new IllegalArgumentException("Invalid table pool size parameters");
    }
    this.maxSize = maxSize;
    this.conn = conn;
    this.tableName = tableName;
    tablePool = new ArrayBlockingQueue<Table>(maxSize);
    initPool();
  }

  private void initPool() throws IOException{
    for(int i = 0; i < maxSize; i++) {
      tablePool.offer(conn.getTable(TableName.valueOf(tableName)));
    }
  }

  public synchronized Table getTable() throws IOException, InterruptedException{
    return tablePool.take();
  }

  public void returnTable(Table t) throws InterruptedException{
    if (!(t instanceof Table))
      return;
    tablePool.offer(t);
  }

  public void close() throws IOException, InterruptedException {
    for (int i = 0; i < maxSize; i++){
      tablePool.take().close();
    }
  }
}
