package com.ssamples.flink.datagen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This class implements java socket client
 */
public class WordGenClient {

	public static void main(String[] args)
			throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {

		int portNumber = 9000;
		String hostName = null;
		if (args.length > 0) {
			hostName = args[0];
		} else {
			InetAddress host = InetAddress.getLocalHost();
			hostName = host.getHostName();
		}
		
		if (args.length > 1) {
			portNumber = Integer.parseInt(args[1]);
		}
		Socket socket = null;

		// establish socket connection to server
		socket = new Socket(hostName, 9000);
		// read the server response message
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String serverInput;
		System.out.println("echo: " + in.readLine());
		while ((serverInput = in.readLine()) != null) {
			System.out.println("echo: " + serverInput);
			Thread.sleep(1000);
		}
		// close resources
		in.close();
		socket.close();
	}
}
