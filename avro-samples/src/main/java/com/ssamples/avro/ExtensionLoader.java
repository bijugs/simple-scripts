package com.ssamples.avro;

import java.io.File;  
import java.lang.reflect.Constructor;  
import java.lang.reflect.InvocationTargetException;  
import java.net.MalformedURLException;  
import java.net.URL;  
import java.net.URLClassLoader;

public class ExtensionLoader {

  public void LoadClass(String classname) throws ClassNotFoundException, Exception {
    System.out.println("Class path "+System.getProperty("user.dir"));
    File pluginsDir = new File(System.getProperty("user.dir"));
    System.out.println("Directory "+pluginsDir);
    for (File jar : pluginsDir.listFiles()) {
      System.out.println("Jar "+jar);
      try {
        //ClassLoader loader = URLClassLoader.newInstance(
        //    new URL[] { jar.toURL() },
        //    getClass().getClassLoader()
        //);
        ClassLoader loader = new URLClassLoader(new URL[] { new URL("file:///tmp/hbase-test.jar") });
        //Class clazz = Class.forName(classpath, true, loader);
        Class clazz = loader.loadClass(classname);
        String m = clazz.getDeclaredMethod("loginUserFromKeytab",
                                     String.class, String.class).getName();
        System.out.println("clazz "+clazz+" "+m+" "+clazz.hashCode());
        clazz.getDeclaredMethod("loginUserFromKeytab",
                               String.class, String.class).invoke(null,"bach_tester@ADDEV.BLOOMBERG.COM","/home/bnair10/bach_tester.keytab");
        ClassLoader loader1 = new URLClassLoader(new URL[] { new URL("file:///tmp/hbase-test.jar") });
        //Class clazz = Class.forName(classpath, true, loader);
        Class clazz1 = loader1.loadClass(classname);
        String m1 = clazz1.getDeclaredMethod("loginUserFromKeytab",
                                     String.class, String.class).getName();
        System.out.println("clazz1 "+clazz1+" "+m1+" "+clazz1.hashCode());
        clazz1.getDeclaredMethod("loginUserFromKeytab",
                               String.class, String.class).invoke(null,"bach_tester@ADDEV.BLOOMBERG.COM","/home/bnair10/bach_tester.keytab");
        Thread.sleep(600000);
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
    throw new ClassNotFoundException("Class " + classname
        + " wasn't found in directory " + System.getProperty("java.class.path") );
  }

  public static void main(String args[]) throws Exception {
    ExtensionLoader ext = new ExtensionLoader();
    ext.LoadClass("org.apache.hadoop.security.UserGroupInformation");
    ext = new ExtensionLoader();
    ext.LoadClass("org.apache.hadoop.security.UserGroupInformation");
  }
}
