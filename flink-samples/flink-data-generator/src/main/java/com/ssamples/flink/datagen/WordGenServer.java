package com.ssamples.flink.datagen;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class WordGenServer extends Thread {

	static List<String> dictionary = new ArrayList<String>();

	Socket clientSocket;

	WordGenServer(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		try {
			int randomNum = ThreadLocalRandom.current().nextInt(0, dictionary.size());
			System.out.println("New client connection ");
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			while (true) {
				out.println(dictionary.get(randomNum));
				randomNum = ThreadLocalRandom.current().nextInt(0, dictionary.size());
				Thread.sleep(500);
			}
		} catch (IOException e) {
			System.out.println("Exception caught when trying to output data to client");
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			System.out.println("Sleep interrupted in data gen server");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		int portNumber = 9000;
		ServerSocket serverSocket = null;

		if (args.length == 0) {
			System.out.println("Need to pass in the location of dictionary");
			System.exit(1);
		}
		System.out.println("Using dictionary at " + args[0]);
		if (args.length > 1) {
			portNumber = Integer.parseInt(args[1]);
		}
		Scanner scanner = new Scanner(new File(args[0]));
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			dictionary.add(line);
		}
		System.out.println("Size of dictionary " + dictionary.size());

		try {
			serverSocket = new ServerSocket((portNumber));
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Got a new connection ");
				new WordGenServer(clientSocket).start();
			}
		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		} finally {
			scanner.close();
			serverSocket.close();
		}
	}

}
