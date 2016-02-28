package com.ssamples.zookeeper;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZKAdmin implements Watcher {
	
	ZooKeeper zk;
	String hostPort;
	
	public ZKAdmin(String hp){
		hostPort = hp;
	}

	void startZK() throws IOException {
		zk = new ZooKeeper(hostPort, 15000, this);
	}

	public void process(WatchedEvent event) {
		System.out.println(event);
	}
	
	void listNode(String parent) throws KeeperException, InterruptedException {
		List<String> nodes = zk.getChildren(parent, false);
		for (String node : nodes) {
			System.out.println(node);
		}
	}
	
	void listRecurNode(String parent) throws KeeperException, InterruptedException {
		if (parent.equals(null))
			return;
		try {
			List<String> nodes = zk.getChildren(parent, false);
			for (String node : nodes) {
				System.out.println(node);
				listRecurNode("/" + node);
			}
		} catch (NoNodeException e) {
			return;
		}
	}
	
	void getCTime(String node) throws KeeperException, InterruptedException {
		Stat stat = new Stat();
		byte[] data = zk.getData(node, false, stat);
		System.out.println("Create time "+new Date(stat.getCtime()));
		System.out.println("ZNode zData "+new String(data));
	}
	
	public void stopZK() throws InterruptedException{
		zk.close();
	}
	
	public static void main(String args[]) throws KeeperException, InterruptedException, IOException{
		ZKAdmin admin = new ZKAdmin(args[0]);
		admin.startZK();
		admin.listNode("/");
		admin.getCTime("/hbase");
		admin.listRecurNode("/");
		admin.stopZK();
	}
}
