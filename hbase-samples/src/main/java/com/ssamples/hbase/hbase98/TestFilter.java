package com.ssamples.hbase.hbase98;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.log4j.Logger;

public class TestFilter {

	static Logger LOG = Logger.getLogger(TestFilter.class);

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
		Filter filter = new SingleColumnValueFilter(Bytes.toBytes("cf1"), Bytes.toBytes("c1"), CompareOp.EQUAL,
				Bytes.toBytes("v11"));
		scan.setFilter(filter);
		ResultScanner scanner = table.getScanner(scan);
		for (Result result = scanner.next(); result != null; result = scanner.next()) {
			System.out.println(">>>>>> Data from single col val filter " + new String(result.value()));
		}
		filter = new PrefixFilter(Bytes.toBytes("r"));
		scan.setFilter(filter);
		scanner = table.getScanner(scan);
		for (Result result = scanner.next(); result != null; result = scanner.next())
			System.out.println(">>>>>> Data from prefix filter " + new String(result.value()));
		scanner.close();
		table.close();
		connection.close();
	}

}