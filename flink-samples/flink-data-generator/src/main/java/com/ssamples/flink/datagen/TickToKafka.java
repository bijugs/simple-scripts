package com.ssamples.flink.datagen;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class TickToKafka {

    private static Producer<Integer, String> producer;
    private final Properties properties = new Properties();
    static List<String> tickSymbols = new ArrayList<String>();
    static String[] tickActions = {"BUY","SELL"};

    public TickToKafka(String kafkaBrokers) {
    	String serializer = StringSerializer.class.getName();
        properties.put("bootstrap.servers", kafkaBrokers);
        properties.put("acks", "1");
        properties.put("client.id", "tick-to-kafka");
        properties.put("key.serializer", serializer);
        properties.put("value.serializer", serializer);
        producer = new KafkaProducer<>(properties);
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Scanner scanner = null;
		if (args.length < 3) {
			System.out.println("Need to pass in tick symbol file, kafka brokers, topic name");
			System.exit(1);
		}
		System.out.println("Using ticks from " + args[0]);

        try {
          new TickToKafka(args[1]);
          scanner = new Scanner(new File(args[0]));
  		  while (scanner.hasNextLine()) {
  			String line = scanner.nextLine();
  			tickSymbols.add(line.trim());
  		  }
  		  System.out.println("Size of dictionary " + tickSymbols.size());
          System.out.println("Time taken new producer "+(System.currentTimeMillis() - startTime));
          String topic = args[2];
          ProducerRecord<Integer, String> data = null;
          startTime = System.currentTimeMillis();
		  while (true) {
			Thread.sleep(200);
			int symbolNo = ThreadLocalRandom.current().nextInt(0, tickSymbols.size());
			int actionNo = ThreadLocalRandom.current().nextInt(0, tickActions.length);
			int count = ThreadLocalRandom.current().nextInt(1, 1000);
			data = new ProducerRecord<>(topic,
						("{\"id\":\"" + tickSymbols.get(symbolNo) + "\"," + 
						 "\"action\":\"" + tickActions[actionNo] + "\"," + 
						 "\"tickerCount\":" + count +"}"));
			producer.send(data);
		  }
       } catch (Exception e) {
          System.out.println("Total time taken for exception "+(System.currentTimeMillis() - startTime));
          e.printStackTrace();
       } finally {
    	   scanner.close();
    	   producer.close();
       }
    }
    
}
