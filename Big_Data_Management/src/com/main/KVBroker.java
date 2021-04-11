package com.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.data.SocketDetails;


public class KVBroker {
	
	private static List<SocketDetails> sockets = new ArrayList<SocketDetails>();
	private static List<String> data = new ArrayList<String>();
	private static String serverFilename;
	private static String dataFilename;
	private static Integer replicationFactor;
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		readArguments(args);
		establishConnections();
		readData();
		insertData();
		
		userCommands();
		
		closeConnections();
	}

	private static void readArguments(String[] args) {
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
	}
	
	private static void userCommands() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter a GET, QUERY or DELETE command:");
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.startsWith("GET")) {
				System.out.println(getOrQuery(line));
			} else if(line.startsWith("QUERY")) {
				System.out.println(getOrQuery(line));
			} else if(line.startsWith("DELETE")) {
				if(areAllServersUp()) {
					System.out.println(delete(line));
				} else {
					System.out.println("DELETE operation because not all servers are up.");
				}
			} else if(line.startsWith("EXIT")) {
				System.out.println("Received exit command, initiating shutdown.");
				break;
			}
			System.out.println("Enter a GET, QUERY or DELETE command:");
		}
		scanner.close();
	}
	
	private static void insertData() {
		Integer successful = 0;
		for(String dato : data) {
			List<SocketDetails> kSocks = getKSockets();
			Boolean success = false;
			for (SocketDetails sock : kSocks) {
				try {
					BufferedWriter writer = sock.getWriter();
					BufferedReader reader = sock.getReader();
					writer.append("PUT " + dato + System.lineSeparator()).flush();
					String response = reader.readLine();
					if(StringUtils.containsIgnoreCase(response, "ERROR")) {
						System.out.println("ERROR - Could not write data: [ " + dato + " ] to server running on port: " + sock.getPort());
						success = false;
						continue;
					}
					success = true;
				} catch (IOException e) {
					System.out.println("Could not write to socket: " + sock.getPort() + " IOException");
					success = false;
				}
			}
			if (success) {
				successful++;
			}
		}
		System.out.println("Inserted " + successful + " KV pairs successfully.");
	}
	
	private static String getOrQuery(String command) {
		List<SocketDetails> kSocks = getKSockets();
		for (SocketDetails sock : kSocks) {
			try {
				BufferedWriter writer = sock.getWriter();
				BufferedReader reader = sock.getReader();
				writer.append(command + System.lineSeparator()).flush();
				String response = reader.readLine();
				if (! StringUtils.containsIgnoreCase(response, "ERROR")) {
					return response;
				}
			} catch (IOException e) {
				System.out.println("Could not write to socket: " + sock.getPort() + " IOException");
			}
		}
		return "NOT FOUND";
	}
	
	private static String delete(String command) {
		for (SocketDetails sock : sockets) {
			try {
				BufferedWriter writer = sock.getWriter();
				BufferedReader reader = sock.getReader();
				writer.append(command + System.lineSeparator()).flush();
				String response = reader.readLine();
				if (! StringUtils.containsIgnoreCase(response, "ERROR")) {
					return response;
				}
			} catch (IOException e) {
				System.out.println("Could not write to socket: " + sock.getPort() + " IOException");
			}
		}
		return "NOT FOUND";
	}
	
	/**
	 * 
	 * @return k servers from the available server list. 
	 */
	private static List<SocketDetails> getKSockets() {
		Collections.shuffle(sockets);
		return sockets.subList(0, replicationFactor);
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
	
	private static boolean areAllServersUp() {
		for(SocketDetails socket : sockets) {
			if(!pingSocket(socket)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean pingSocket(SocketDetails socket) {
		try {
			socket.getWriter().append("PING" + System.lineSeparator()).flush();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
