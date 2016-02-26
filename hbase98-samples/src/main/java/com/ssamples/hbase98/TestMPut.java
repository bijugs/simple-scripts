package com.ssamples.hbase98;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;

public class TestMPut {

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

		List<Put> putList = new ArrayList<Put>();

		for (int i = 1; i < 130; i++) {
			Put p = new Put(Bytes.toBytes("r" + i));
			p.add(Bytes.toBytes("cf1"), Bytes.toBytes("c1"), Bytes.toBytes("v" + i));
			putList.add(p);
		}
		table.put(putList);
		table.close();
		connection.close();
	}
}
