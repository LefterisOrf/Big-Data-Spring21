package com.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

public class KVBroker {
	
	private static List<Socket> sockets = new ArrayList<Socket>();
	private static List<String> data = new ArrayList<String>();
	private static Random rand = new Random(System.currentTimeMillis());
	private static String serverFilename;
	private static String dataFilename;
	private static Integer replicationFactor;
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		for (int index = 0; index < args.length; index++) {
			String string = args[index];
			if("-s".equals(string)) {
				serverFilename = args[index + 1];
			} else if("-i".equals(string)) {
				dataFilename = args[index + 1];
			} else if("-k".equals(string)) {
				replicationFactor = Integer.parseInt(args[index + 1]);
			}
		}
		establishConnections();
		readData();
		insertData();
		
		
		closeConnections();
	}
	
	private static void insertData() {
		for(String dato : data) {
			List<Socket> kSocks = getKSockets();
			for (Socket sock : kSocks) {
				try {
					PrintWriter writer = new PrintWriter(sock.getOutputStream());
					writer.append("PUT " + dato);
					BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
					String response = reader.readLine();
					if("OK".equalsIgnoreCase(response)) {
						System.out.println("Successfully inserted data: [ " + dato + " ] to server running on port: " + sock.getPort());
						continue;
					} else {
						System.out.println("ERROR - Could not write data: [ " + dato + " ] to server running on port: " + sock.getPort());
					}
				} catch (IOException e) {
					System.out.println("Could not write to socket: " + sock.getPort() + " IOException");
				}
			}
		}
	}
	
	private static List<Socket> getKSockets() {
		if(replicationFactor == sockets.size()) {
			return sockets;
		} else if(replicationFactor == sockets.size() - 1) {
			return sockets.subList(1, sockets.size());
		} else if(replicationFactor > sockets.size()) {
			throw new RuntimeException("Replication factor is bigger than the available servers.");
		}
		// Else select randomly k servers from the socket list.
		List<Socket> socks = new ArrayList<Socket>();
		Socket lastSocket = null;
		while(socks.size() <= replicationFactor) {
			Socket cur = getRandomSocket();
			if(!pingSockets(cur)) { // If the selected socket is not reachable then skip it ( and remove it from the list (?) ) 
				System.out.println("Selected socket (" + cur.getPort() + ") is not reachable.");
				continue; 
			}
			if(cur != lastSocket) {
				socks.add(cur);
				lastSocket = cur;
			}
		}
		return socks;
	}
	
	private static Socket getRandomSocket() {
		if(sockets == null || sockets.isEmpty()) {
			throw new RuntimeException("No socket found");
		}
		
		if(sockets.size() == 1) {
			return sockets.get(0);
		}
		Integer index = rand.nextInt(sockets.size());
		return sockets.get(index);
	}
	
	private static void establishConnections() throws UnknownHostException, IOException {
		File file = new File(serverFilename);
		if(! file.exists() || !file.canRead()) {
			throw new RuntimeException("File not found.");
		}
		Scanner scanner = new Scanner(file);
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String host = StringUtils.substringBefore(line, " ");
			Integer port = Integer.parseInt(StringUtils.substringAfter(line, " "));
			
			Socket socket = new Socket(InetAddress.getByName(host), port);
			sockets.add(socket);
		}
		scanner.close();
	}
	
	private static void readData() throws FileNotFoundException {
		File file = new File(dataFilename);
		if(! file.exists() || !file.canRead()) {
			throw new RuntimeException("File not found.");
		}
		Scanner scanner = new Scanner(file);
		while(scanner.hasNextLine()) {
			data.add(scanner.nextLine());
			
		}
		scanner.close();
	}
	
	private static void closeConnections() {
		for (Socket socket : sockets) {
			if(socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Failed to close socket: " + socket.getInetAddress() + " " + socket.getPort());
				}
			}
		}
	}
	
	private static boolean pingSockets(Socket sock) {
		try {
			return sock.getInetAddress().isReachable(300);
		} catch (IOException e) {
			System.out.println("IOException when pinging socket: " + sock.getPort());
			return false;
		}
	}
}
