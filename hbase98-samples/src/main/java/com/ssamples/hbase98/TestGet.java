package com.ssamples.hbase98;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

public class TestGet {

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

		LOG.info(">>>>>> Was able to create a connection");

		HTableInterface table = connection.getTable("tbn");

		Get g = new Get(Bytes.toBytes("r1"));
		for (int i = 0; i < 10; i++) {
			g = new Get(Bytes.toBytes("r"+i));
			Result r = table.get(g);
			System.out.println(">>>>>> Data from table " + new String(r.value()));
			Thread.sleep(1000);
		}

		table.close();
		connection.close();
	}

}
