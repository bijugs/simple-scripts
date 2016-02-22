package com.ssamples.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.BufferedMutatorParams;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException;
import org.apache.hadoop.hbase.util.Bytes;

public class BuffMutator implements Callable<Boolean> {

	static Connection conn = null;
	int id;
	
	BuffMutator(int id) {
		this.id = id;
	}
	
	BufferedMutator.ExceptionListener bmListener = new BufferedMutator.ExceptionListener() {
		
		@Override
		public void onException(RetriesExhaustedWithDetailsException exp,BufferedMutator bm)
				throws RetriesExhaustedWithDetailsException {
			for (int i = 0; i < exp.getNumExceptions(); i++) {
			    System.out.println(exp.getRow(i) +" "+ exp.getHostnamePort(i)+" "+exp.getExhaustiveDescription());
			}
		}
	};
	
	@Override
	public Boolean call() throws Exception {
		System.out.println("Starting thread "+id);
        BufferedMutatorParams bmParams = new BufferedMutatorParams(TableName.valueOf("test"));
        bmParams.listener(bmListener);
    	BufferedMutator bm = conn.getBufferedMutator(bmParams);
		for (int i = 0; i < 10000; i++) {
			int j = new Random().nextInt(10000);
			Put p = new Put(Bytes.toBytes("r"+j));
			p.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("c1"), Bytes.toBytes("v"+j));
			bm.mutate(p);
		}
		System.out.println("Ending thread "+id);
		bm.close();
		return true;
	}
	
	public static void main(String args[]) {
		try {
			Configuration conf = HBaseConfiguration.create();
			conf.set("hbase.zookeeper.quorum", "localhost");
			conn = ConnectionFactory.createConnection(conf);
			ExecutorService pool = Executors.newFixedThreadPool(10);
			Collection<Callable<Boolean>> callableList = new ArrayList<Callable<Boolean>>(10);
			for (int i = 0; i < 10; i++) {
				callableList.add(new BuffMutator(i));
			}
			pool.invokeAll(callableList);
			pool.shutdown();
		} catch (IOException e) {
			System.out.println("Got an IOException in main");
		} catch (InterruptedException e) {
			System.out.println("Interrupted exception in main");
		}
	}

}
