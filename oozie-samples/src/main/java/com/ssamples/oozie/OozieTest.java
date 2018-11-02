package com.ssamples.oozie;

import org.apache.hadoop.conf.*;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.AuthOozieClient;
import org.apache.hadoop.security.UserGroupInformation;

public class OozieTest {

    public static void main(String args[]) throws Exception {
        Configuration conf = new Configuration();
        conf.set("hadoop.security.authentication", "Kerberos");
        UserGroupInformation.setConfiguration(conf);
        OozieClient wc = new AuthOozieClient("http://oozie-node:11000/oozie","KERBEROS");       
        String id = wc.getJobId("Test");
    }
}
