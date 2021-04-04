package com.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.data.SocketDetails;


public class KVBroker {
	
	private static List<SocketDetails> sockets = new ArrayList<SocketDetails>();
	private static List<String> data = new ArrayList<String>();
	private static Random rand = new Random(System.currentTimeMillis());
	private static String serverFilename;
	private static String dataFilename;
	private static Integer replicationFactor;
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		if(args == null || args.length == 0) {
			throw new RuntimeException("No arguments given.");
		}
		for (int index = 0; index < args.length; index++) {
			String string = args[index];
			System.out.println(string);
			if("-s".equals(string)) {
				serverFilename = args[index + 1];
			} else if("-i".equals(string)) {
				dataFilename = args[index + 1];
			} else if("-k".equals(string)) {
				replicationFactor = Integer.parseInt(args[index + 1]);
			}
		}
		if(areArgumentsInvalid()) {
			throw new RuntimeException("Invalid arguments given.");
		}
		establishConnections();
		readData();
		insertData();
		
		userCommands();
		
		closeConnections();
	}
	
	private static void userCommands() {
		Scanner scanner = new Scanner(System.in);
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.startsWith("GET")) {
				// traverse all servers and return the data of the first one that answers smthing other than ERROR
			} else if(line.startsWith("QUERY")) {
				// traverse all servers and return the data of the first one that answers smthing other than ERROR
			} else if(line.startsWith("DELETE")) {
				// check if all servers are UP, if not print a message that the DELETE operation cannot be executed.
			} else if(line.startsWith("PUT")) {
				// might not be needed 
			} else if(line.startsWith("EXIT")) {
				System.out.println("Received exit command, initiating shutdown.");
				break;
			}
			
		}
		scanner.close();
	}
	
	private static void insertData() {
		for(String dato : data) {
			List<SocketDetails> kSocks = getKSockets();
			for (SocketDetails sock : kSocks) {
				try {
					BufferedWriter writer = sock.getWriter();
					BufferedReader reader = sock.getReader();
					writer.append("PUT " + dato + System.lineSeparator());
					writer.flush();
					String response = reader.readLine();
					System.err.println("Response: " + response);
					if(StringUtils.containsIgnoreCase(response, "ERROR")) {
						System.out.println("ERROR - Could not write data: [ " + dato + " ] to server running on port: " + sock.getPort());
					}
				} catch (IOException e) {
					System.out.println("Could not write to socket: " + sock.getPort() + " IOException");
				}
			}
		}
	}
	
	private static List<SocketDetails> getKSockets() {
		if(replicationFactor == sockets.size()) {
			return sockets;
		} else if(replicationFactor == sockets.size() - 1) {
			return sockets.subList(1, sockets.size());
		} else if(replicationFactor > sockets.size()) {
			throw new RuntimeException("Replication factor is bigger than the available servers.");
		}
		// Else select randomly k servers from the socket list.
		List<SocketDetails> socks = new ArrayList<SocketDetails>();
		SocketDetails lastSocket = null;
		while(socks.size() <= replicationFactor) {
			SocketDetails cur = getRandomSocket();
			if(cur != lastSocket) {
				socks.add(cur);
				lastSocket = cur;
			}
		}
		return socks;
	}
	
	private static SocketDetails getRandomSocket() {
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
			throw new RuntimeException("File not found. File:" + file.getAbsolutePath());
		}
		Scanner scanner = new Scanner(file);
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String host = StringUtils.substringBefore(line, " ");
			Integer port = Integer.parseInt(StringUtils.substringAfter(line, " "));
			System.out.println("Socket: " + host + " " + port);
			sockets.add(new SocketDetails(host, port));
		}
		scanner.close();
	}
	
	private static void readData() throws FileNotFoundException {
		File file = new File(dataFilename);
		if(! file.exists() || !file.canRead()) {
			throw new RuntimeException("File not found. File: " + file.getAbsolutePath());
		}
		Scanner scanner = new Scanner(file);
		while(scanner.hasNextLine()) {
			data.add(scanner.nextLine());
			
		}
		scanner.close();
		System.out.println("Finished reading data from input file.");
	}
	
	private static void closeConnections() {
		for (SocketDetails socket : sockets) {
			if(socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Failed to close socket: " + socket.getHostname() + " " + socket.getPort());
				}
			}
		}
	}
	
	private static boolean areArgumentsInvalid() {
		if(serverFilename == null || dataFilename == null || replicationFactor == null ) {
			return true;
		}
		return false;
	}
	
	private static boolean pingSocket(Socket sock) {
		try {
			return sock.getInetAddress().isReachable(300);
		} catch (IOException e) {
			System.out.println("IOException when pinging socket: " + sock.getPort());
			return false;
		}
	}
}
