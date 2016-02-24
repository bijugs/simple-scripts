package com.ssamples.hbase.hbase98;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

public class TestDelete {

	static Logger LOG = Logger.getLogger(TestDelete.class);

	public static void main(String[] args) throws Exception {

		Configuration config = HBaseConfiguration.create();

		if (args.length > 0)
			config.set("hbase.zookeeper.quorum", args[0]);

		System.out.println(">>>>>> hbase.zookeeper.quorum = " + config.get("hbase.zookeeper.quorum"));

		if (config.get("hbase.security.authentication").equalsIgnoreCase("kerberos")) {
			config.set("hadoop.security.authentication", "Kerberos");
			UserGroupInformation.setConfiguration(config);
		}

		final HConnection connection = HConnectionManager.createConnection(config);

		System.out.println(">>>>>> Was able to create a connection");

		HTableInterface table = connection.getTable("tbn");

		for (int i = 1; i < 5; i++) {
			Delete d = new Delete(Bytes.toBytes("r" + i));
			table.delete(d);
			Thread.sleep(1000);
		}

		table.close();
		connection.close();
	}
}