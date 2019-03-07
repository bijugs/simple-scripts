package com.ssamples.avro;

public class ClassLoad {

    public static void main(String[] args) {
	try {          
            // Printing the classloader of this class.
            System.out.println("ClassLoad.getClass().getClassLoader()?= " + ClassLoad.class.getClassLoader());
            System.out.println("ClassLoad.getClass().getClassLoader().getParent()?= " + ClassLoad.class.getClassLoader().getParent());
            System.out.println("ClassLoad.getClass().getClassLoader().getParent().getParent()?= " + ClassLoad.class.getClassLoader().getParent().getParent());
            // Trying to explicitly load the class again using the extension classloader.
            Class.forName("com.ssamples.avro.ClassLoad", true,  ClassLoad.class.getClassLoader());
            Class.forName("com.ssamples.avro.ClassLoad", true,  ClassLoad.class.getClassLoader().getParent());
	} catch (ClassNotFoundException e) {
            e.printStackTrace();
	}
    }
}
