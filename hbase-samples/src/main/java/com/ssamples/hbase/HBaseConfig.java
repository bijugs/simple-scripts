package com.ssamples.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

public class HBaseConfig {

	public static void main(String args[]){
		Configuration conf = HBaseConfiguration.create();
		System.out.println(conf.get("hadoop.security.authentication"));
	}
}
