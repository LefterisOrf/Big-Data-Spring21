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

public class KVServer {
	private static Trie trie = new Trie();
	
	public static void main(String[] args) throws IOException {
		ServerSocket sock = new ServerSocket(9001, 50, InetAddress.getByName("127.0.0.1"));
		System.out.println("Succesfully created a ServerSocket on port: 9001");
		Socket socket = sock.accept();
		System.out.println("Accepted a connection.");
		
		Scanner scanner = new Scanner(socket.getInputStream());
		PrintWriter writer = new PrintWriter(socket.getOutputStream());
		System.out.println("Type your query: \n");
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			System.out.println("Received the following command: " + line);
			if(line.equalsIgnoreCase("Exit")) {
				break;
			}
			
			if(StringUtils.contains(line, "GET")) {
				// get query
				String key = StringUtils.substringAfter(line, " ");
				JsonData data = trie.get(key);
				if(data == null) {
					System.out.println("Key was not found.");
				} else {
					System.out.println(data.toString());
				}
			} else if(StringUtils.contains(line, "QUERY")) {
				// query query
				String key = StringUtils.substringAfter(line, " ");
				String data = trie.query(key);
				if(data == null) {
					System.out.println("Key was not found.");
				} else {
					System.out.println(data);
				}
			} else if(StringUtils.contains(line, "PUT")) {
				String jsonData = StringUtils.substringAfter(line, " ");
				JsonData data = JsonData.fromString(jsonData);
				if(data == null) {
					writer.append("Invalid put operation. \n").flush();
					continue;
				}
				trie.insertData(data);
				writer.append("OK \n").flush();
			} else {
				writer.append("Invalid query, please check your spelling. \n");
			}
		}
		scanner.close();
		socket.close();
		sock.close();
		
		System.out.println("KV Server with id: " + Thread.currentThread().getId() + " will exit.");
	}
	
}
