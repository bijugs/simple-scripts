package com.ssamples.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

	public static void main(String args[]) throws Exception {
		String clientSentence;
		String capitalizedSentence;
		ServerSocket welcomeSocket = new ServerSocket(6789);
		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(
					connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			clientSentence = inFromClient.readLine();
			capitalizedSentence = clientSentence.toUpperCase() + '\n';
			outToClient.writeBytes(capitalizedSentence);
		}
	}

}
