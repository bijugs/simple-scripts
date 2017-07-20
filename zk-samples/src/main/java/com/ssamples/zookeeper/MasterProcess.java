package com.ssamples.zookeeper;

import java.io.IOException;

import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class MasterProcess implements Watcher{

	static ZooKeeper zk;
	static String hostPort;
	boolean connected = false;
	boolean expired = false;
	
	public MasterProcess(String hp) {
		hostPort = hp;
	}
	
	public void process(WatchedEvent event) {
		// TODO Auto-generated method stub
		if (event.getType() == Event.EventType.None) {
			switch (event.getState()) {
			case AuthFailed:
				break;
			case ConnectedReadOnly:
				break;
			case Disconnected:
				System.out.println("State changed to disconnected");
				connected = false;
				break;
			case Expired:
				System.out.println("State changed to Expired");
				connected = false;
				expired = true;
				break;
			case SaslAuthenticated:
				break;
			case SyncConnected:
				System.out.println("State changed to SyncConnected");
				connected = true;
				break;
			default:
				break;
			}
		} else  if (event.getType() == Event.EventType.NodeDeleted) {
			createZNode();
		}
	}

	void startZK() {
		try {
			zk = new ZooKeeper(hostPort, 15000, this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void stopZK() {
		try {
			zk.close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	StringCallback createCallBack = new StringCallback() {

		public void processResult(int rc, String path, Object ctx, String name) {
			switch (Code.get(rc)) {
			case CONNECTIONLOSS:
				break;
			case NODEEXISTS:
				monitorZNode();
				break;
			case NONODE:
				createZNode();
				break;
			default:
				break;
			}		
		}		
	};
	
	void monitorZNode() {
			//zk.exists("/master", existsWatcher, existsCallBack, null);
			try {
				zk.exists("/master", true);
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Monitoring the master znode");
	}
	
	StatCallback existsCallBack = new StatCallback() {

		public void processResult(int rc, String path, Object ctx, Stat stat) {
			switch (Code.get(rc)){
			case CONNECTIONLOSS:
				monitorZNode();
				break;
			case NODEEXISTS:
				monitorZNode();
				break;
			case NONODE:
				createZNode();
				break;
			case OK:
				break;
			default:
				break;
			}		
		}	
	};
	
	Watcher existsWatcher = new Watcher(){

		public void process(WatchedEvent event) {
			if (event.getType() == Event.EventType.NodeDeleted) {
				createZNode();
			}
		}
		
	};
	
	void createZNode() {
		zk.create("/master", "master1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, createCallBack, null);
		System.out.println("Ha ha.. I got to create the ZNode");
	}
	
	public static void main(String args[]) throws InterruptedException{
		MasterProcess mp = new MasterProcess(args[0]);
		mp.startZK();
		while (!mp.connected)
			Thread.sleep(1000);
		mp.createZNode();
		while (!mp.expired)
			Thread.sleep(10000);
		mp.stopZK();
	}
	
}
