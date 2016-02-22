package com.ssamples.hbase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MetaTableAccessor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.hadoop.hbase.util.Bytes;

public class HBaseAdminTool {
	private static Connection conn;
	private static Admin adm;
	private static Configuration conf;

	public static void main(String args[]) throws Exception {
		conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "localhost");
		conf.set("hbase.rootdir", "hdfs://localhost:9000/hbase");
		conf.set("fs.defaultFS", "hdfs://localhost:9000/");
		//
		// to resolve java.io.IOException: No FileSystem for scheme: hdfs
		//
		conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
		conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
		conn = ConnectionFactory.createConnection(conf);
		adm = conn.getAdmin();

		while (true) {
			System.out.println("What admin action to take?");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String in = br.readLine();
			switch (in) {
			case "creatabl":
				System.out.println("Create table");
				createTable();
				break;
			case "deletabl":
				System.out.println("Delete table");
				deleteTable();
				break;
			case "comptabl":
				System.out.println("Compact table");
				compactTable();
				break;
			case "creaname":
				System.out.println("Create name space");
				createNameSpace();
				break;
			case "delename":
				System.out.println("Delete name space");
				deleteNameSpace();
				break;
			case "locatrow":
				System.out.println("Find region for row");
				rowLocation();
				break;
			case "getregin":
				System.out.println("Get regions of a table");
				getRegions();
				break;
			case "getdistr":
				System.out.println("Get table region distribution");
				getDistribution();
				break;
			case "disttabl":
				System.out.println("Distribute table regions");
				distributeRegion();
				break;
			case "calclocl":
				System.out.println("Calculate data locality of table");
				calculateLocality();
				break;
			case "quit":
			default:
				System.out.println("Shutting down");
				adm.close();
				System.exit(0);
			}
		}
	}

	private static void calculateLocality() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter the table name :");
			String tbl = br.readLine();
			String fileRoot = conn.getConfiguration().get("hbase.rootdir");
			List<HRegionInfo> regions = adm.getTableRegions(TableName.valueOf(tbl));
			String nameSpace = TableName.valueOf(tbl).getNamespaceAsString();
			Set<byte[]> families = conn.getTable(TableName.valueOf(tbl)).getTableDescriptor().getFamiliesKeys();
			int noRegions = regions.size();
			FileSystem fs = FileSystem.get(conf);
			int matchCount = 0;
			for (int i = 0; i < noRegions; i++) {
				String regionName = regions.get(i).getEncodedName();
				String host = MetaTableAccessor.getRegionLocation(conn, regions.get(i)).getHostname();
				for (byte[] family : families) {
					RemoteIterator<LocatedFileStatus> ri = fs.listFiles(new Path(fileRoot+"/data/" + nameSpace + "/"
							+ tbl + "/" + regionName + "/" + Bytes.toString(family)), true);
					while (ri.hasNext()) {
						BlockLocation[] bl = ri.next().getBlockLocations();	
						for (int j = 0; j < bl.length; j++) {
							String[] hosts = bl[j].getHosts();
							for (int k = 0; k < hosts.length; k++) {
								System.out.println("Hosts "+hosts[k]);
								if (host.equalsIgnoreCase(hosts[k])) {
									matchCount++;
									break;
								}
							}
						}
					}
				}
			}
			System.out.println(matchCount/noRegions * 100 + " Percentage of data is local");
		} catch (IOException e) {
			System.out.println("Error calculating the locality of table data");
			e.printStackTrace();
		}
	}

	private static void getDistribution() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter the table name :");
			String tbl = br.readLine();
			Collection<ServerName> servers = adm.getClusterStatus().getServers();
			Hashtable<String, Integer> distribution = new Hashtable<String, Integer>(servers.size());
			for (ServerName s : servers) {
				distribution.put(s.getHostname(), 0);
			}
			List<HRegionInfo> regions = adm.getTableRegions(TableName.valueOf(tbl));
			int noRegions = regions.size();
			for (int i = 0; i < noRegions; i++) {
				String host = MetaTableAccessor.getRegionLocation(conn, regions.get(i)).getHostname();
				int val = distribution.get(host);
				distribution.put(host, val + 1);
			}
			Enumeration<String> keys = distribution.keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				System.out.println("Host " + key + " value " + distribution.get(key));
			}
		} catch (IOException e) {
			System.out.println("Error in getting region distribution information for the table");
		}
	}

	private static void distributeRegion() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter the table name ");
			String tbl = br.readLine();
			Collection<ServerName> servers = adm.getClusterStatus().getServers();
			List<String> rsList = new ArrayList<String>(servers.size());
			for (ServerName s : servers) {
				rsList.add(s.getServerName());
			}
			List<HRegionInfo> regions = adm.getTableRegions(TableName.valueOf(tbl));
			int noRegions = regions.size();
			int rsIdx = 0;
			for (int i = 0; i < noRegions; i++) {
				adm.move(regions.get(i).getEncodedNameAsBytes(), Bytes.toBytes(rsList.get(rsIdx)));
				if (rsIdx == servers.size())
					rsIdx = 0;
				else
					rsIdx++;
			}
		} catch (IOException e) {
			System.out.println("Error during distribution of table regions");
		}
	}

	private static void getRegions() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter the table name :");
			String tbl = br.readLine();
			List<HRegionInfo> regions = adm.getTableRegions(TableName.valueOf(tbl));
			int noRegions = regions.size();
			for (int i = 0; i < noRegions; i++) {
				HRegionInfo region = regions.get(i);
				System.out.println(region.getEncodedName() + " " + region.getRegionNameAsString() + " "
						+ MetaTableAccessor.getRegionLocation(conn, region).getHostname());
			}
		} catch (IOException e) {
			System.out.println("Error in getting region information for the table");
		}
	}

	private static void rowLocation() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter the table name storing the row :");
			String table = br.readLine();
			System.out.print("Enter the row string :");
			String row = br.readLine();
			RegionLocator rLocator = conn.getRegionLocator(TableName.valueOf(table));
			HRegionLocation rLocation = rLocator.getRegionLocation(Bytes.toBytes(row), true);
			if (rLocation.equals(null))
				System.out.println("Region not found for the key provided");
			else
				System.out.println("Row is served from region " + rLocation.getRegionInfo().getRegionNameAsString()
						+ " at RS " + rLocation.getHostname());
		} catch (IOException e) {
			System.out.println("Error in locating region for the row");
		}
	}

	private static void createNameSpace() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String name = br.readLine();
			if (name.equals(""))
				System.out.println("Name should not be empty");
			else {
				NamespaceDescriptor nameSpace = NamespaceDescriptor.create(name).build();
				adm.createNamespace(nameSpace);
				System.out.println("Namespace creation successful");
			}
		} catch (IOException e) {
			System.out.println("Error in creating namespace");
		}
	}

	private static void deleteNameSpace() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String name = br.readLine();
			if (name.equals(""))
				System.out.println("Name can't be empty");
			else {
				adm.deleteNamespace(name);
				System.out.println("Deletion of namespace successful");
			}
		} catch (IOException e) {
			System.out.println("Error deleting namespace");
		}
	}

	private static void compactTable() {
		System.out.print("Enter table name to compact:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			TableName tblName = TableName.valueOf(br.readLine());
			if (adm.isTableAvailable(tblName))
				adm.compact(tblName);
			else
				System.out.println("The table doesn't exist");
			return;
		} catch (Exception IOException) {
			System.out.println("Issue with intiating compation on table");
		}
	}

	private static void createTable() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.print("Enter table name:");
			String tblName = br.readLine();
			if (adm.isTableAvailable(TableName.valueOf(tblName)))
				System.out.println("Table already exists");
			else {
				HTableDescriptor tblDesc = new HTableDescriptor(TableName.valueOf("test"));
				System.out.println("Enter column family name, null when done:");
				String cfName;
				while (!(cfName = br.readLine()).equals("")) {
					System.out.println("CF Name " + cfName);
					HColumnDescriptor cfDesc = new HColumnDescriptor(cfName);
					tblDesc.addFamily(cfDesc);
					System.out.println("Enter column family name, null when done:");
				}
				adm.createTable(tblDesc);
				if (adm.isTableAvailable(TableName.valueOf("test")))
					System.out.println("Table created successfully");
				else
					System.out.println("Table creation failed");
			}
		} catch (IOException e) {
			System.out.println("Table creation failed");
		}
	}

	private static void deleteTable() {
		System.out.print("Enter table name:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			TableName tblName = TableName.valueOf(br.readLine());
			if (adm.isTableAvailable(tblName)) {
				adm.disableTable(tblName);
				adm.deleteTable(tblName);
			} else
				System.out.println("Table doesn't exist; no need to delete");
		} catch (IOException e) {
			System.out.println("Table deletion failed");
		}
	}
}
