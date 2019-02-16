package com.ssamples.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {

	public static void main(String args[]) throws Exception {
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		DatagramSocket serverSocket = new DatagramSocket(9876);
		while(true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			String sentence = new String(receivePacket.getData());
			sendData = sentence.toUpperCase().getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
		}
	}
}
