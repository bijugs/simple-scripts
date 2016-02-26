package com.ssamples.hbase98;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import java.util.List;
import java.util.ArrayList;
import org.apache.log4j.Logger;

public class TestExists {

	static Logger LOG = Logger.getLogger(TestExists.class);

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

		Get g = new Get(Bytes.toBytes("r1"));
		System.out.println(">>>>>> Data for row key r1 exists? " + table.exists(g));
		List<Get> rowList = new ArrayList<Get>();
		for (int i = 0; i < 10; i++)
			rowList.add(new Get(Bytes.toBytes("r" + i)));

		Boolean[] res = table.exists(rowList);
		for (int i = 0; i < 10; i++)
			System.out.println(">>>>> Data for row key r" + i + " exists? " + res[i]);

		table.close();
		connection.close();
	}

}