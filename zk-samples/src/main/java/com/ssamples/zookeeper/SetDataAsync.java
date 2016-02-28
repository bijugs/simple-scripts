package com.ssamples.zookeeper;

import java.io.IOException;
import java.util.Random;

import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class SetDataAsync implements Watcher {

	String hostPort = null;
	String server = null;
	ZooKeeper zk = null;
	
	public void process(WatchedEvent event) {
		System.out.println(event);
	}
	
	public SetDataAsync(String hp){
		hostPort = hp;
		server = Integer.toString((new Random()).nextInt());
	}

	public void startZK() throws IOException {
		zk = new ZooKeeper(hostPort,15000,this);
	}
	
	StringCallback createCallBack = new StringCallback(){

		public void processResult(int rc, String path, Object ctx, String name) {
			switch (Code.get(rc)) {
			case CONNECTIONLOSS:
				break;
			case OK:
				break;
			default:
				break;
			}	
		}
	};
	
	public void createZNode() {
		zk.create("/master", server.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, createCallBack, null);
	}
	
	DataCallback checkCallBack = new DataCallback(){

		public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
			switch (Code.get(rc)) {
			case CONNECTIONLOSS:
				break;
			case NONODE:
				createZNode();
				break;
			default:
				break;
			}	
		}
	};
	
	public void checkZNode() {
		zk.getData("/master", false, checkCallBack, null);
	}
	
	StatCallback statCallBack = new StatCallback(){

		public void processResult(int rc, String path, Object ctx, Stat stat) {
			switch(Code.get(rc)) {
			case CONNECTIONLOSS:
				setData((String)ctx);
				break;
			default:
				break;
			}
		}
	};
	
	public void setData(String data) {
		zk.setData("/master", data.getBytes(), -1, statCallBack, data);
	}
	
	public void stopZK() throws InterruptedException {
		zk.close();
	}
	
	public static void main(String args[]) throws IOException, InterruptedException {
		SetDataAsync setDataZK = new SetDataAsync(args[0]);
		setDataZK.startZK();
		setDataZK.createZNode();
		setDataZK.setData("Hello");
		setDataZK.stopZK();
	}
}
