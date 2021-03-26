package com.main;

import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.data.JsonData;
import com.trie.Trie;

public class KVServer {
	
	private static Trie trie = new Trie();
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
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
		System.out.println("KV Server with id: " + Thread.currentThread().getId() + " will exit.");
	}
	
}
