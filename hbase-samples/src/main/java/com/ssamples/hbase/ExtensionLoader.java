package com.ssamples.hbase;

import java.io.File;  
import java.lang.reflect.Constructor;  
import java.lang.reflect.InvocationTargetException;  
import java.net.MalformedURLException;  
import java.net.URL;  
import java.net.URLClassLoader;

public class ExtensionLoader {

  public void LoadClass(String classpath) throws ClassNotFoundException {
    System.out.println("Class path "+System.getProperty("user.dir"));
    File pluginsDir = new File(System.getProperty("user.dir")+"/target");
    System.out.println("Directory "+pluginsDir);
    for (File jar : pluginsDir.listFiles()) {
      System.out.println("Jar "+jar);
      try {
        ClassLoader loader = URLClassLoader.newInstance(
            new URL[] { jar.toURL() },
            getClass().getClassLoader()
        );
        Class clazz = Class.forName(classpath, true, loader);
        String m = clazz.getDeclaredMethod("loginUserFromKeytab",
                                     String.class, String.class).getName();
        System.out.println("clazz "+clazz+" "+m);
        clazz.getDeclaredMethod("loginUserFromKeytab",
                               String.class, String.class).invoke(null,"bach_tester@ADDEV.BLOOMBERG.COM","/home/bnair10/bach_tester.keytab");
        return;
      } catch (ClassNotFoundException e) {
        // There might be multiple JARs in the directory,
        // so keep looking
        continue;
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      /*} catch (InstantiationException e) {
        e.printStackTrace();*/
      }
    }
    throw new ClassNotFoundException("Class " + classpath
        + " wasn't found in directory " + System.getProperty("java.class.path") );
  }

  public static void main(String args[]) throws ClassNotFoundException {
    ExtensionLoader ext = new ExtensionLoader();
    ext.LoadClass("org.apache.hadoop.security.UserGroupInformation");
  }
}
