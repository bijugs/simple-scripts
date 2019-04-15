package com.ssamples.hbase.stochasticbalancer;

import java.util.Map;

import org.apache.hadoop.hbase.RegionMetrics;
import org.apache.hadoop.hbase.Size;

public class BalancerTestMetrics implements RegionMetrics {
	
	private long readRequestCount = 0;
	private long writeRequestCount = 0;
	private long cpRequestCount = 0;
	private Size memStoreSizeMB;
	private Size storefileSizeMB;
	
	BalancerTestMetrics(long readRequestCount, 
			long writeRequestCount, 
			long cpRequestCount,
			int memStoreSize,
			int storefileSize) {
		this.readRequestCount = readRequestCount;
		this.writeRequestCount = writeRequestCount;
		this.cpRequestCount = cpRequestCount;
		this.memStoreSizeMB = new Size(memStoreSize, Size.Unit.MEGABYTE);
		this.storefileSizeMB = new Size(storefileSize, Size.Unit.MEGABYTE);
	}

	@Override
	public byte[] getRegionName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStoreCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getStoreFileCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Size getStoreFileSize() {
		// TODO Auto-generated method stub
		return this.storefileSizeMB;
	}

	@Override
	public Size getMemStoreSize() {
		// TODO Auto-generated method stub
		return this.memStoreSizeMB;
	}

	@Override
	public long getReadRequestCount() {
		// TODO Auto-generated method stub
		return readRequestCount;
	}

	@Override
	public long getWriteRequestCount() {
		// TODO Auto-generated method stub
		return writeRequestCount;
	}

	@Override
	public long getCpRequestCount() {
		// TODO Auto-generated method stub
		return cpRequestCount;
	}

	@Override
	public long getFilteredReadRequestCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Size getStoreFileIndexSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Size getStoreFileRootLevelIndexSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Size getStoreFileUncompressedDataIndexSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Size getBloomFilterSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getCompactingCellCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCompactedCellCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCompletedSequenceId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<byte[], Long> getStoreSequenceId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Size getUncompressedStoreFileSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getDataLocality() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLastMajorCompactionTimestamp() {
		// TODO Auto-generated method stub
		return 0;
	}

}
