package com.main;

import java.io.IOException;
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
		System.out.println("Type your query: \n");
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.equalsIgnoreCase("Exit")) {
				break;
			}
			
			if(StringUtils.containsIgnoreCase(line, "GET")) {
				// get query
				String key = StringUtils.substringAfter(line, " ");
				JsonData data = trie.get(key);
				if(data == null) {
					System.out.println("Key was not found.");
				} else {
					System.out.println(data.toString());
				}
			} else if(StringUtils.containsIgnoreCase(line, "QUERY")) {
				// query query
				String key = StringUtils.substringAfter(line, " ");
				String data = trie.query(key);
				if(data == null) {
					System.out.println("Key was not found.");
				} else {
					System.out.println(data);
				}
			} else if(StringUtils.containsIgnoreCase(line, "PUT")) {
				String jsonData = StringUtils.substringAfter(line, " ");
				JsonData data = JsonData.fromString(jsonData);
				if(data == null) {
					System.out.println("Invalid put operation.");
					continue;
				}
				trie.insertData(data);
			} else {
				System.out.println("Invalid query, please check your spelling.");
			}
			System.out.println("Type your query: \n");
		}
		scanner.close();
		socket.close();
		sock.close();
		
		System.out.println("KV Server with id: " + Thread.currentThread().getId() + " will exit.");
	}
	
}
