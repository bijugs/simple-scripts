package com.ssamples.hbase98;

import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;

public class TestMGet {

	static Logger LOG = Logger.getLogger(TestMGet.class);

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
		List<Get> queryRowList = new ArrayList<Get>();
		queryRowList.add(new Get(Bytes.toBytes("r11")));
		queryRowList.add(new Get(Bytes.toBytes("r12")));
		queryRowList.add(new Get(Bytes.toBytes("r13")));
		Result[] results = table.get(queryRowList);
		System.out.println(">>>>>> Number of results " + results.length);
		for (int i = 0; i < results.length; i++)
			System.out.println(">>>>>> Data from table " + new String(results[i].value()));

		table.close();
		connection.close();
	}

}
