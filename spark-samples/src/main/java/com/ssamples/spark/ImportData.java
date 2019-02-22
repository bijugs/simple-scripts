package com.ssample.spark;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * This is a very simple main program showing the bare minimum to get data from a CSV file 
 * into a newly created HBase table. The CSV file itself can be found at the Github
 * GIST: https://gist.github.com/bolerio/f70c374c3e96495f5a69
 * 
 * @author https://github.com/bolerio 
 */
public class ImportData
{
	public static void main(String [] argv) throws Exception
	{
		// An HBase configuration object is necessary to create connection. By default it will connect
		// to localhost at the default port. 
		Configuration config = HBaseConfiguration.create();
                System.out.println("Connecting to ZK "+config.get("hbase.zookeeper.quorum"));
                UserGroupInformation.setConfiguration(config);
                UserGroupInformation.loginUserFromSubject(null);
		// We are creating an HBase connection and an Admin object that will allows use to create
		// a new HBase table
		try (Connection connection = ConnectionFactory.createConnection(config); Admin admin = connection.getAdmin())
		{
			// We assumed that the jaildata.csv file is in /tmp, adjust your location accordingly.
			CSVParser parser = CSVParser.parse(Paths.get("/tmp", "jaildata.csv").toFile(), Charset.defaultCharset(), CSVFormat.EXCEL);
			Iterator<CSVRecord> recordIterator = parser.iterator();
			recordIterator.next(); // skip titles
			
			// The table descriptor is essentially a schema definition object. The HBase column families
			// associated with a table need to be specified in advance. Particular column qualifiers are
			// open-ended and each column family can have any number of column qualifiers added ad hoc.
			//
			// We have to column families: arrest and charge. The idea is that the charge column family
			// will contain possible charges of which there are few varieties while the arrest column family
			// will contain values describing the individual and the occurrence of the arrest. 
			HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf("jaildata"));
			tableDescriptor.addFamily(new HColumnDescriptor("arrest"));
			tableDescriptor.addFamily(new HColumnDescriptor("charge"));
			admin.createTable(tableDescriptor);
			Table table = connection.getTable(TableName.valueOf("jaildata"));
			
			// Now we are ready to read the CSV and store in the HBase table
			while (recordIterator.hasNext())
			{
				CSVRecord csvRecord = recordIterator.next();
				ArrayList<Put> puts = new ArrayList<Put>();
				if (csvRecord.size() > 0 && csvRecord.get(0).trim().length() > 0)
				{
					// The CSV record's format is like this:
					// BookDate,Defendant,DOB,ChargeCode1,Charge1,ChargeCode2,Charge2,ChargeCode3,Charge3,Location
					String rowKey = csvRecord.get(1) + " -- " + csvRecord.get(0);
					Put put = new Put(Bytes.toBytes(rowKey));
					put.addColumn(Bytes.toBytes("arrest"), Bytes.toBytes("name"), Bytes.toBytes(csvRecord.get(1)));
					put.addColumn(Bytes.toBytes("arrest"), Bytes.toBytes("bookdate"), Bytes.toBytes(csvRecord.get(0)));
					put.addColumn(Bytes.toBytes("arrest"), Bytes.toBytes("dob"), Bytes.toBytes(csvRecord.get(2)));
					put.addColumn(Bytes.toBytes("arrest"), Bytes.toBytes("location"), Bytes.toBytes(csvRecord.get(9)));
					put.addColumn(Bytes.toBytes("charge"), Bytes.toBytes("charge1"), Bytes.toBytes(csvRecord.get(4)));
					put.addColumn(Bytes.toBytes("charge"), Bytes.toBytes("charge2"), Bytes.toBytes(csvRecord.get(6)));
					put.addColumn(Bytes.toBytes("charge"), Bytes.toBytes("charge3"), Bytes.toBytes(csvRecord.get(8)));	
					puts.add(put);
				}
				table.put(puts);
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
		}
	}
}
