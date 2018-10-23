package com.ssamples.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Collections;
import java.util.Properties;

public class SimpleConsumer {

    public static void main(String[] args) {
        // The topic we are going to read records from
        String KAFKA_TOPIC_NAME = args[0];
        // Set consumer configuration properties
        final Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "simple-sample");
        //consumerProps.put("group.id", "simple-sample");

        // Create a new consumer
        try (final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            // Subscribe to the topic
            consumer.subscribe(Collections.singleton(KAFKA_TOPIC_NAME));

            // Continuously read records from the topic
            while (true) {
                final ConsumerRecords<String, String> records = consumer.poll(5);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Received: " + record);
                }
            }
        }
    }

}
