package com.ssamples.kafka;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

//import kafka.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.Node;

public class KafkaAdminClient {
	
	public static void main(String args[]) {
        Properties p = new Properties();
        p.put("bootstrap.servers", "localhost:9092");
        p.put("exclude.internal.topics",false);
		AdminClient aC = AdminClient.create(p);
		DescribeClusterResult dCR = aC.describeCluster();
		ListTopicsResult lTR = aC.listTopics();
		try {
			System.out.println("Cluster id "+dCR.clusterId().get());
			for(Node n : dCR.nodes().get())
				System.out.println("Node id "+ n.id()+ " Node port "+n.port() + " Node host "+n.host());
			System.out.println("Controller "+dCR.controller().get().id());
			for(String s : lTR.names().get())
				System.out.println("Topic name "+s);
			Collection<TopicListing> tL = lTR.listings().get();
			Iterator<TopicListing> it = tL.iterator();
			while(it.hasNext()) {
				System.out.println(it.next().name());
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		aC.close();
	}

}
