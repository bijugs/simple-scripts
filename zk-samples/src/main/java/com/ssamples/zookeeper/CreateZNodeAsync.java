package com.ssamples.zookeeper;

import java.io.IOException;
import java.util.Random;

import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class CreateZNodeAsync implements Watcher {
	
	String hostPort = null;
	ZooKeeper zk = null;
	String server = null;
	boolean failed = false;
	
	public CreateZNodeAsync(String hP) {
		hostPort = hP;
		server = Integer.toString((new Random()).nextInt());
	}
	
	StringCallback createCallback = new StringCallback() {

		public void processResult(int rc, String path, Object ctx, String name) {
			System.out.println("Async call returned");
			switch(Code.get(rc)) {
			case CONNECTIONLOSS:
				break;
			case NODEEXISTS:
				break;
			case NONODE:
				createZNode();
				break;
			case OK:
				failed = true;
				break;
			default:
				break; 
			}			
		}	
	};
	
	DataCallback checkCallBack = new DataCallback(){

		public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
			switch (Code.get(rc)) {
			case CONNECTIONLOSS:
				checkZNode();
				return;
			case NONODE:
				createZNode();
				return;
			default:
				break;
			}
			
		}
		
	};
	
	public void startZK() throws IOException {
		zk = new ZooKeeper(hostPort, 15000, this);
	}

	public void stopZK() throws InterruptedException {
		zk.close();
	}
	
	public void process(WatchedEvent event) {
		System.out.println(event.toString());
	}
	
	public void createZNode() {
		zk.create("/master", server.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, createCallback, null);
	}

	public void checkZNode() {
		zk.getData("/master", false, checkCallBack, null);
	}
	
	public void closeZK() throws InterruptedException {
		zk.close();
	}
	
	public static void main(String args[]) throws InterruptedException {
		CreateZNodeAsync createZNode = new CreateZNodeAsync(args[0]);
		try {
			createZNode.startZK();
			createZNode.createZNode();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Thread.sleep(60000);
		createZNode.stopZK();
		createZNode.closeZK();
	}
}
