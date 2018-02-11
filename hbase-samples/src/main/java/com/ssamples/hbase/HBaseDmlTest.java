package com.ssamples.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeepDeletedCells;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
//import org.junit.Test;

public class HBaseDmlTest {

    public static void print(final Connection connection, final TableName tableName) throws Exception {
        System.out.println("Scan printout =>");
        try (final Table table = connection.getTable(tableName);) {
            final Scan scan = new Scan();
            // scan.setRaw(true);
            try (final ResultScanner scanner = table.getScanner(scan);) {
                for (final Result result : scanner) {
                    for (final Cell c : result.rawCells()) {
                        final String msg =
                            String.format("  Row: '%s', Timestamp: '%s', Family: '%s', Qualifier: '%s', Value: '%s'", //
                                Bytes.toString(CellUtil.cloneRow(c)), //
                                c.getTimestamp(), //
                                Bytes.toString(CellUtil.cloneFamily(c)), //
                                Bytes.toString(CellUtil.cloneQualifier(c)), //
                                Bytes.toString(CellUtil.cloneValue(c))//
                            );
                        System.out.println(msg);
                    }
                }
            }
        }
    }

    public static void put(final Connection connection, final TableName tableName, final byte[] row, final byte[] family,
        final byte[] qualifier, final long ts, final byte[] value) throws Exception {

        final String msg = String.format("Put row: '%s' with family: '%s' with qualifier: '%s' with timestamp: '%s'", //
            Bytes.toString(row), //
            Bytes.toString(family), //
            Bytes.toString(qualifier), //
            ts);

        System.out.println(msg);

        final Put put = new Put(row);
        put.addColumn(family, qualifier, ts, value);
        try (final Table table = connection.getTable(tableName)) {
            table.put(put);
        }
    }

    //@Test
    public static void main(String args[]) throws Exception {

        final Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "localhost");
        config.set("hbase.zookeeper.property.clientPort", "2181");

        final Connection connection = ConnectionFactory.createConnection(config);

        final Admin admin = connection.getAdmin();

        final TableName tableName = TableName.valueOf("test_dml");
        final HColumnDescriptor family = new HColumnDescriptor("myFamily");

        if (admin.tableExists(tableName)) {
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
        }

        final HTableDescriptor desc = new HTableDescriptor(tableName);

        //family.setKeepDeletedCells(KeepDeletedCells.TRUE);
        //family.setVersions(1, 99);
        family.setMinVersions(1);
        family.setMaxVersions(99);
        desc.addFamily(family);
        admin.createTable(desc);

        admin.close();

        // Put row with family and qualifier (timestamp 1).
        put(connection, tableName, Bytes.toBytes("myRow"), Bytes.toBytes("myFamily"), Bytes.toBytes("myQualifier"), 1,
            Bytes.toBytes("myValue"));
        print(connection, tableName);

        // Delete entire row (timestamp 2).
        System.out.println("Delete row: 'myRow'");
        try (final Table table = connection.getTable(tableName)) {
            table.delete(new Delete(Bytes.toBytes("myRow"), 1));
        }
        print(connection, tableName);

        // Put same row again with family without qualifier (timestamp 3).
        put(connection, tableName, Bytes.toBytes("myRow"), Bytes.toBytes("myFamily"), null, 3, Bytes.toBytes("myValue"));
        print(connection, tableName);

        // Put the same row again with family without qualifier (timestamp 4).
        put(connection, tableName, Bytes.toBytes("myRow"), Bytes.toBytes("myFamily"), null, 4, Bytes.toBytes("myValue"));
        print(connection, tableName);

        connection.close();
    }

}
