package com.ssamples.hbase;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

class MultiUserGroupInformation {
    // Workaround for the fact that UserGroupInformation is a static singleton.
    // This allows multiple connections.

    private final Class clsUGI;
    private final Configuration configuration;
    private final String user;
    private final String keytabPath;
    static Logger LOG = Logger.getLogger(MultiUserGroupInformation.class);

    private String quorum()  {
        return configuration.get("hbase.zookeeper.quorum");
    }

    // This create a version of 'UserGroupInformation.class' from a distinct
    // class loader.
    private static class UGIClassLoader extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            LOG.info("Load class "+ name);
            if (!name.equals("UserGroupInformation")) {
                return super.loadClass(name);
            }

            try {
                InputStream istream = ClassLoader.getSystemResourceAsStream(
                                                 name);
                if (null == istream) {
                    throw new ClassNotFoundException("Class not found: " +
                                                     name);
                }
                byte[] classBytes = IOUtils.toByteArray(istream);
                istream.close();
                return defineClass(name, classBytes, 0, classBytes.length);
            } catch (IOException e) {
                throw new ClassNotFoundException(
                "UserGroupInformation missing", e);
            }
        }
    }

    MultiUserGroupInformation(Configuration cfg,
                              String        user,
                              String        keytabPath) throws IOException {
        this.configuration = cfg;
        this.user          = user;
        this.keytabPath    = keytabPath;
        try {
            LOG.info("Login to Kerberos: quorum "+ quorum());

            clsUGI = new UGIClassLoader().loadClass(
                "org.apache.hadoop.security.UserGroupInformation");

            clsUGI.getDeclaredMethod("setConfiguration",
                                     Configuration.class)
                .invoke(null, cfg);

            clsUGI.getDeclaredMethod("loginUserFromKeytab",
                                     String.class, String.class)
                .invoke(null, user, keytabPath);
        } catch (ClassNotFoundException |
                 NoSuchMethodException  |
                 IllegalAccessException |
                 InvocationTargetException e) {
            throw new IOException("Failed to set UserGroupInformation", e);
        }
    }

    public void refresh() throws IOException {
        try {
            LOG.info("Refresh Kerberos Login: quorum= "+ quorum());

            Object ugi = clsUGI.getDeclaredMethod("getLoginUser").invoke(null);
            if (ugi == null) {
                clsUGI.getDeclaredMethod("loginUserFromKeytab",
                                      String.class, String.class)
                    .invoke(null, user, keytabPath);
            }
            else {
                LOG.info("Relogin from Keytab");
                ugi.getClass()
                   .getDeclaredMethod("checkTGTAndReloginFromKeytab")
                   .invoke(ugi);
            }
            LOG.info("Refresh Completed: quorum "+ quorum());
        } catch(NoSuchMethodException  |
                IllegalAccessException |
                InvocationTargetException e) {
            throw new IOException("Failed to relogin UserGroupInformation", e);
        }
    }
}
