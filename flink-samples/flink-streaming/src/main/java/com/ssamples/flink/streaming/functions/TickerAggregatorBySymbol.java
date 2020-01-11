package com.ssamples.flink.streaming.functions;


import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import com.ssamples.flink.streaming.common.Ticker;

public class TickerAggregatorBySymbol
		implements AggregateFunction<Ticker, Tuple2<String, Long>, Tuple2<String, Long>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8528772774907786176L;
	
	@Override
	public Tuple2<String, Long> createAccumulator() {
		return new Tuple2<String, Long>("", 0L);
	}

	@Override
	public Tuple2<String, Long> add(Ticker value, Tuple2<String, Long> accumulator) {
		accumulator.f0 = value.getId();
		accumulator.f1 += 1;
		return accumulator;
	}

	@Override
	public Tuple2<String, Long> getResult(Tuple2<String, Long> accumulator) {
		return accumulator;
	}

	@Override
	public Tuple2<String, Long> merge(Tuple2<String, Long> a, Tuple2<String, Long> b) {
		return new Tuple2<String, Long>(a.f0, a.f1 + b.f1);
	}

}