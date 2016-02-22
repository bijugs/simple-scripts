package com.ssamples.hbase;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class GetData {

	public static void main(String[] args) throws IOException {
		Connection conn = null;
		Table tbl = null;
		try {
			// Instantiating configuration object
			Configuration conf = HBaseConfiguration.create();
			conf.set("hbase.zookeeper.quorum", "localhost");
			// Creating a connection object
			conn = ConnectionFactory.createConnection(conf);
			TableName tblName = TableName.valueOf("emp5");
			tbl = conn.getTable(tblName);
			Get g = new Get(Bytes.toBytes("r1"));
			g.setMaxVersions(3);
			Result r = tbl.get(g);
			Cell data = r.getColumnLatestCell(Bytes.toBytes("corp"), Bytes.toBytes("name"));
			byte[] val = r.getValue(Bytes.toBytes("corp"), Bytes.toBytes("name"));
			System.out.println("Cell data " + Bytes.toString(CellUtil.cloneValue(data)) + " " + Bytes.toString(val));
			List<Cell> vals = r.listCells();
			for (Cell c : vals) {
				System.out.println(Bytes.toString(CellUtil.cloneFamily(c)) + " "
						+ Bytes.toString(CellUtil.cloneQualifier(c)) + " " + Bytes.toString(CellUtil.cloneValue(c)));
			}
			List<Cell> hc = r.getColumnCells(Bytes.toBytes("corp"), Bytes.toBytes("headcount"));
			for (Cell h : hc) {
				System.out.println(Bytes.toString(CellUtil.cloneValue(h)));
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
