package com.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.data.JsonData;
import com.trie.Trie;

/**
 * 
 * @author LefterisOrf
 * Compile with: 
 * Run with: java -cp "build;src\resources\commons-lang3-3.12.0.jar" com.main.KVServer -a localhost -p 9001
 *
 */
public class KVServer {
	private static String server;
	private static Integer port;
	private static Trie trie = new Trie();
	
	public static void main(String[] args) throws IOException {
		readArguments(args);
		ServerSocket sock = new ServerSocket(port, 50, InetAddress.getByName(server));
		System.out.println("Succesfully created a ServerSocket on port: " + port);
		Socket socket = sock.accept();
		System.out.println("Accepted a connection.");
		
		Scanner scanner = new Scanner(socket.getInputStream());
		PrintWriter writer = new PrintWriter(socket.getOutputStream());
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.equalsIgnoreCase("Exit")) {
				break;
			}
			
			if(StringUtils.contains(line, "GET")) {
				// get query
				String key = StringUtils.substringAfter(line, " ");
				JsonData data = trie.get(key);
				if(data == null) {
					writer.append("ERROR - Key was not found. \n").flush();
				} else {
					writer.append(data.toString() + " \n").flush();
				}
			} else if(StringUtils.contains(line, "QUERY")) {
				// query query
				String key = StringUtils.substringAfter(line, " ");
				String data = trie.query(key);
				if(data == null) {
					writer.append("ERROR - Key was not found. \n");
				} else {
					writer.append(data + "\n").flush();
				}
			} else if(StringUtils.contains(line, "PUT")) {
				String jsonData = StringUtils.substringAfter(line, " ");
				JsonData data = JsonData.fromString(jsonData);
				if(data == null) {
					writer.append("ERROR - Invalid put operation. \n").flush();
					continue;
				}
				trie.insertData(data);
				writer.append("OK \n").flush();
			} else if(StringUtils.contains(line, "PING")) {
				writer.append("OK \n").flush();
			} else {
				writer.append("Invalid query.\n").flush();
			}
		}
		scanner.close();
		socket.close();
		sock.close();
		
		System.out.println("KV Server with id: " + Thread.currentThread().getId() + " will exit.");
	}

	private static void readArguments(String[] args) {
		if(args == null || args.length == 0) {
			throw new RuntimeException("No arguments given.");
		}
		for (int index = 0; index < args.length; index++) {
			String string = args[index];
			System.out.println(string);
			if("-a".equals(string)) {
				server = args[index + 1];
			} else if("-p".equals(string)) {
				port = Integer.parseInt(args[index + 1]);
			}
		}
		if(areArgumentsInvalid()) {
			throw new RuntimeException("Invalid arguments given.");
		}
	}
	
	private static boolean areArgumentsInvalid() {
		if(server == null || server.isEmpty() || port == null ) {
			return true;
		}
		return false;
	}
}
