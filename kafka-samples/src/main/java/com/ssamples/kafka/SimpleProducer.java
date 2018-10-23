package com.ssamples.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;

public class SimpleProducer {
    private static Producer<Integer, String> producer;
    private final Properties properties = new Properties();

    public SimpleProducer() {
        properties.put("metadata.broker.list", "localhost:9092"+
                                               ",localhost:9093");
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        properties.put("request.required.acks", "1");
        properties.put("ProducerConfig.CLIENT_ID_CONFIG", "simple-sample");
        //properties.put("client.id", "simple-sample");
        producer = new Producer<>(new ProducerConfig(properties));
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
          new SimpleProducer();
          System.out.println("Time taken new producer "+(System.currentTimeMillis() - startTime));
          String topic = args[0];
          String msg = args[1];
          KeyedMessage<Integer, String> data = null;
          startTime = System.currentTimeMillis();
          for (int i = 0; i < 100; i++) {
            data = new KeyedMessage<>(topic, msg);
            producer.send(data);
          }
          System.out.println("Total time taken by producer "+(System.currentTimeMillis() - startTime));
          producer.close();
       } catch (Exception e) {
          System.out.println("Total time taken for exception "+(System.currentTimeMillis() - startTime));
          e.printStackTrace();
       }
    }
}
