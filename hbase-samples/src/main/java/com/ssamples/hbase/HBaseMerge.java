package com.ssamples.hbase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.exceptions.MergeRegionException;
import org.apache.hadoop.security.UserGroupInformation;

public class HBaseMerge {

    private String parent;
    private String keyTabPath;
    private String userName;
    private Table[] prevTables;
    
    public static void main(String[] args) {
        if(args.length != 4) {
            usage();
        }
        HBaseMerge merge = new HBaseMerge();
        merge.parent=args[0];
        merge.userName=args[1];
        merge.keyTabPath=args[2];
        String regex=args[3];
        
        for(int i=0; i < 500; i++) {
            System.out.printf("*******   Iteration %s  *************** %n", i);
            merge.merge(regex);
            
            Table[] list = merge.count(regex);
            
            if(!merge.isChanged(list)) {
                //if no changes, then stop merging
                return; 
            }
            
            try {
                Thread.sleep(20_000);
            } catch (InterruptedException e) {
            }
        }
    }

    private boolean isChanged(Table[] tables) {
        Arrays.sort(tables, new Comparator<Table>() {
            @Override
            public int compare(Table o1, Table o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        if(prevTables == null) {
            prevTables = tables;
            return true;
        }
        
        List<Table> currentList = Arrays.asList(tables);
        List<Table> prevList = Arrays.asList(prevTables);
        prevTables = tables;
        
        for(int i=0; i<prevList.size()&& i<currentList.size(); i++) {
            Table current = currentList.get(i);
            Table prev = prevList.get(i);
            
            if(current.name.equals(prev.name)) {
                if(current.regions < prev.regions) {
                    //there is a change, so scope for more changes
                    return true;
                }
            } else if(current.name.compareTo(prev.name) < 0){
                currentList.remove(i);
            } else {
                prevList.remove(i);
            }
        }
        
        //everything is same
        return false;
    }

    private static void usage() {
        System.out.println("Invalid parameters");
        System.out.println("Usage: java HBaseMerge <hbase-config-dir> <user> <keytab> <tables-regex>");
        System.exit(1);;
    }

    private void merge(String regex) {
        Configuration conf = getConfiguration();
        HBaseAdmin admin;
        try {
            admin = new HBaseAdmin(conf);
        } catch (IOException e) {
            throw new RuntimeException("Not able to get Admin", e);
        }
        try {
            HTableDescriptor[] tables = admin.listTables(regex);
            System.out.printf("Number of tables found for %s is %s%n", regex, tables.length);
            for (HTableDescriptor table : tables) {
                List<HRegionInfo> regions = admin.getTableRegions(table.getTableName());
                System.out.printf("Number of regions found for %s is %s%n", table.getTableName().getNameAsString(),
                        regions.size());
                if(regions.size() != 1) {
                    makeOnline(admin, regions);
                    mergeAdjacent(admin, regions);
                    regions = admin.getTableRegions(table.getTableName());
                    
                    System.out.printf("Number of regions found after merge for %s is %s%n",
                            table.getTableName().getNameAsString(), regions.size());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                admin.close();
            } catch (IOException e) {
            }
        }
    }
    
    private class Table implements Comparable<Table> {
        String name;
        int regions;
        
        @Override
        public int compareTo(Table o) {
            return o.regions - this.regions;
        }
    }
    private Table[] count(String regex) {
        Configuration conf = getConfiguration();
        HBaseAdmin admin;
        try {
            admin = new HBaseAdmin(conf);
        } catch (IOException e) {
            throw new RuntimeException("Not able to get Admin", e);
        }
        
        try {
            HTableDescriptor[] htables = admin.listTables(regex);
            Table[] tables = new Table[htables.length];
            int i=0;
            for (HTableDescriptor htable : htables) {
                List<HRegionInfo> regions = admin.getTableRegions(htable.getTableName());
                Table table = new Table();
                table.name = htable.getNameAsString();
                table.regions = regions.size();
                tables[i++] = table;
            }
            Arrays.sort(tables);
            
            System.out.println("Status as of "+new Date());
            for (Table table : tables) {
                System.out.printf("%s has regions %s%n", table.name, table.regions);
            }
            
            return tables;
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                admin.close();
            } catch (IOException e) {
            }
        }
    }

    private void mergeAdjacent(HBaseAdmin admin, List<HRegionInfo> regions) throws IOException {
        int count = regions.size();
        for (int j=0; j< count && regions.size() > 1; j++) {
            for (int i = j + 1; i < regions.size(); i++) {
                HRegionInfo first = regions.get(j);
                HRegionInfo region = regions.get(i);
                if(HRegionInfo.areAdjacent(first, region)) {
                    if(region.isMetaRegion() || region.isOffline() || region.isSplit()) {
                        //it is split or meta or offline
                        continue;
                    }
                    try {
                        admin.mergeRegions(first.getEncodedNameAsBytes(), region.getEncodedNameAsBytes(), false);
                    } catch(MergeRegionException e) {
                        e.printStackTrace();
                    }
                    //remove these adjacent regions and try with next adjacent
                    regions.remove(i);
                    regions.remove(j);
                    break;
                }
            }
        }
    }

    private void makeOnline(HBaseAdmin admin, List<HRegionInfo> regions)
            throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
        for (HRegionInfo region : regions) {
            if (region.isOffline()) {
                admin.assign(region.getRegionName());
            }
        }
    }

    private Configuration getConfiguration() {
        Configuration conf = new Configuration();
        conf.addResource(new Path(parent, "core-site.xml"));
        conf.addResource(new Path(parent, "hbase-site.xml"));
        UserGroupInformation.setConfiguration(conf);
        try {
            UserGroupInformation.loginUserFromKeytab(userName, keyTabPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return conf;
    }
}
