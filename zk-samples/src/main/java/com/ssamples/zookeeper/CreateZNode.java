package com.ssamples.zookeeper;

import java.io.IOException;
import java.util.Random;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class CreateZNode implements Watcher {

	ZooKeeper zk;
	String hostPort;
	Random random = new Random();
	String server = Integer.toHexString(random.nextInt());
	
	public void process(WatchedEvent event) {
		System.out.println(event);
	}

	void startZK() throws IOException {
		zk = new ZooKeeper(hostPort, 15000, this);
	}
	
	void stopZK() throws IOException, InterruptedException {
		zk.close();
	}
	
	void tryBeingMaster() throws KeeperException, InterruptedException {
		zk.create("/master", server.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}
	
	public CreateZNode(String hostPort) {
		this.hostPort = hostPort;
	}
	
	public boolean checkZNode() throws KeeperException, InterruptedException {
		Stat stat = new Stat();
		try {
			byte[] data = zk.getData("/master", true, stat);
			System.out.println("Data = "+new String(data));
		} catch (NoNodeException e) {
			return false;
		}
		return true;
	}
	
	public static void main(String args[]) throws IOException, InterruptedException, KeeperException {
		CreateZNode m = new CreateZNode(args[0]);
		m.startZK();
		while (true) {
			try {
				m.tryBeingMaster();
				break;
			} catch (NodeExistsException e) {
				System.out.println("Node already exists");
			} catch (ConnectionLossException e) {
				System.out.println("Connection loss exception; its life; do nothing");
			}
			if (m.checkZNode()) break;
		}
		m.checkZNode();
		Thread.sleep(60000);
		m.stopZK();
	}
	
}
