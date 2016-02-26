package com.ssamples.hbase98;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

public class TestPut {

	static Logger LOG = Logger.getLogger(TestGet.class);

	public static void main(String[] args) throws Exception {

		Configuration config = HBaseConfiguration.create();

		if (args.length > 0)
			config.set("hbase.zookeeper.quorum", args[0]);

		LOG.info(">>>>>> hbase.zookeeper.quorum: " + config.get("hbase.zookeeper.quorum"));

		if (config.get("hbase.security.authentication").equalsIgnoreCase("kerberos")) {
			config.set("hbase.security.authentication", "Kerberos");
			config.set("hbase.master.kerberos.principal", "hbase/_HOST@REALM.COM");
			config.set("hbase.regionserver.kerberos.principal", "hbase/_HOST@REALM.COM");
			UserGroupInformation.setConfiguration(config);
		}

		final HConnection connection = HConnectionManager.createConnection(config);

		HTableInterface table = connection.getTable("tbn");

		for (int i = 31; i < 40; i++) {
			Put p = new Put(Bytes.toBytes("r" + i));
			p.add(Bytes.toBytes("cf1"), Bytes.toBytes("c1"), Bytes.toBytes("v" + i));
			table.put(p);
			Thread.sleep(1000);
		}

		table.close();
		connection.close();
	}
}
