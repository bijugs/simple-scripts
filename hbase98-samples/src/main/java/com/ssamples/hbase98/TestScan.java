package com.ssamples.hbase98;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

public class TestScan {

	static Logger LOG = Logger.getLogger(TestGet.class);

	public static void main(String[] args) throws Exception {

		Configuration config = HBaseConfiguration.create();

		if (args.length > 0)
			config.set("hbase.zookeeper.quorum", args[0]);

		LOG.info(">>>>>> hbase.zookeeper.quorum: " + config.get("hbase.zookeeper.quorum"));

		if (config.get("hbase.security.authentication").equalsIgnoreCase("kerberos")) {
			config.set("hadoop.security.authentication", "Kerberos");
			UserGroupInformation.setConfiguration(config);
		}

		final HConnection connection = HConnectionManager.createConnection(config);

		HTableInterface table = connection.getTable("tbn");
		Scan scan = new Scan();
		scan.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("c1"));
		ResultScanner scanner = table.getScanner(scan);
		for (Result result = scanner.next(); result != null; result = scanner.next()) {
			System.out.println(">>>>>> Data from table " + new String(result.value()));
		}
		scanner.close();
		table.close();
		connection.close();
	}

}