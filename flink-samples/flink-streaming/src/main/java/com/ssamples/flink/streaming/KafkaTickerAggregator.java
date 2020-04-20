package com.ssamples.flink.streaming;

import java.util.Objects;
import java.util.Properties;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ssamples.flink.streaming.common.Ticker;
import com.ssamples.flink.streaming.functions.TickerAggregatorBySymbol;

public class KafkaTickerAggregator {
	private static final Logger LOG = LoggerFactory.getLogger(KafkaTickerAggregator.class);
	private static final ObjectMapper OM = new ObjectMapper();

	public static void main(String[] args) throws Exception {

		final String brokerList;
		final String kafkaTopic;
		try {
			final ParameterTool params = ParameterTool.fromArgs(args);
			brokerList = params.get("brokers");
			kafkaTopic = params.get("topic");
		} catch (Exception e) {
			System.err.println("No broker/topic specified. Please run 'KafkaTickerAggregator "
					+ "--brokers <broker:port> --topic <kafka-topic> ");
			return;
		}

		final StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();

		Properties properties = new Properties();
		properties.setProperty("bootstrap.servers", brokerList);
		properties.setProperty("group.id", "KafkaTickerAggregator");
		LOG.info("Properties set {}", properties);
		System.out.println("Hey Hey.. you got some eyes");

		FlinkKafkaConsumer<String> kafkaSource = new FlinkKafkaConsumer<>(kafkaTopic, new SimpleStringSchema(), properties);
		DataStream<String> stream = see.addSource(kafkaSource);

		LOG.info("stream created, {}", stream);
		System.out.println("Hey Hey.. stream created");

		KeyedStream<Ticker, String> tickerBuySellStream = stream.map(data -> {
			try {
				return OM.readValue(data, Ticker.class);
			} catch (Exception e) {
				LOG.info("exception reading data: " + data);
				return null;
			}
		}).filter(Objects::nonNull).keyBy(Ticker::getId);

		DataStream<Tuple2<String, Long>> result = tickerBuySellStream.timeWindow(Time.seconds(5))
				.aggregate(new TickerAggregatorBySymbol());

		result.print();

		see.execute("TickerAggregationApp");

	}

}
