package com.ssamples.hbase;

import java.io.IOException;
import java.util.List;
import java.util.Iterator;

import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;

public class ScanData {

	public static void main(String[] args) throws Exception {
		Connection conn = null;
		Table tbl = null;
		try {
			// Instantiating configuration object
			Configuration conf = HBaseConfiguration.create();
			conf.set("hbase.zookeeper.quorum", "localhost");
			//conf.set("hbase.client.scanner.timeout.period", "70000");
			// Creating a connection object
			conn = ConnectionFactory.createConnection(conf);
			TableName tblName = TableName.valueOf("t1");
			tbl = conn.getTable(tblName);
			Scan s = new Scan();
                        s.setStartRow(Bytes.toBytes("r1"));
                        s.setStopRow(Bytes.toBytes("r4"));
                        ResultScanner resultScanner = tbl.getScanner(s);
                        Iterator<Result> it = resultScanner.iterator();
                        while (it.hasNext()) {
                           System.out.println(new String(it.next().getRow()));
                        }
                        System.out.println("**** Reverse scanning ****");
			s = new Scan();
                        byte[] start = Bytes.toBytes("r1");
                        int len = start.length;
                        Byte a = (byte)(start[len-1] - 1);
                        start[len - 1] = a;
                        s.setStartRow(Bytes.toBytes("r4"));
                        //s.setStopRow(Bytes.toBytes("r1"));
                        s.setStopRow(start);
                        s.setReversed(true);
                        resultScanner = tbl.getScanner(s);
                        it = resultScanner.iterator();
                        while (it.hasNext()) {
                           System.out.println(new String(it.next().getRow()));
                           Thread.sleep(3);
                        }
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Exception creating connection or table object");
		} finally {
			if (tbl != null)
				tbl.close();
			if (conn != null)
				conn.close();
		}
	}

}
