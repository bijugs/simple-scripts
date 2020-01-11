package com.ssamples.flink.streaming.common;

public class Ticker {
	private String id;
	private String action;
	private int tickerCount;
	
	public int getTickerCount() {
		return tickerCount;
	}
	public void setTickerCount(int tickerCount) {
		this.tickerCount = tickerCount;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}	
}
