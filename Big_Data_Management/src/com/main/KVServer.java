package com.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;

import com.data.JsonData;
import com.trie.Trie;

/**
 * 
 * @author LefterisOrf
 * Compile with: javac -sourcepath src -d build src/com/**\/*.java -cp "src/resources/commons-lang3-3.12.0.jar"
 * Run with: java -cp "build;src\resources\commons-lang3-3.12.0.jar" com.main.KVServer -a localhost -p 9001
 *
 */
public class KVServer {
	private static String server;
	private static Integer port;
	private static Trie<JsonData> trie = new Trie<>();
	
	public static void main(String[] args) throws IOException {
		readArguments(args);
		ServerSocket sock = new ServerSocket(port, 50, InetAddress.getByName(server));
		System.out.println("Succesfully created a ServerSocket on port: " + port);
		Socket socket = sock.accept();
		System.out.println("Accepted a connection.");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		BufferedWriter writer = new BufferedWriter(new PrintWriter(socket.getOutputStream()));
		String line;
		while((line = reader.readLine()) != null) {
			if(line.equalsIgnoreCase("Exit")) {
				break;
			}
			
			if(StringUtils.contains(line, "GET")) {
				get(writer, line);
			} else if(StringUtils.contains(line, "QUERY")) {
				query(writer, line);
			} else if(StringUtils.contains(line, "PUT")) {
				put(writer, line);
			} else if(StringUtils.contains(line, "DELETE")) {
				delete(writer, line);
			} else if(StringUtils.contains(line, "PING")) {
				writer.append("OK" + System.lineSeparator()).flush();
			} else {
				writer.append("Invalid query." + System.lineSeparator()).flush();
			}
		}
		reader.close();
		socket.close();
		sock.close();
		
		System.out.println("KV Server with id: " + Thread.currentThread().getId() + " will exit.");
	}

	private static void delete(BufferedWriter writer, String line) throws IOException {
		if(trie.delete(StringUtils.substringAfter(line, " "))) {
			writer.append("OK" + System.lineSeparator()).flush();
		} else {
			writer.append("ERROR - KEY NOT FOUND" + System.lineSeparator()).flush();
		}
	}

	private static void put(BufferedWriter writer, String line) throws IOException {
		String jsonData = StringUtils.substringAfter(line, " ");
		JsonData data = JsonData.fromString(jsonData);
		if(data == null) {
			writer.append("ERROR - Invalid put operation." + System.lineSeparator()).flush();
			return;
		}
		trie.insert(data);
		writer.append("OK" + System.lineSeparator()).flush();
	}

	private static void query(BufferedWriter writer, String line) throws IOException {
		String fullKey = StringUtils.substringAfter(line, " ");
		String highLevelKey = StringUtils.substringBefore(fullKey, ".");
		JsonData data = trie.get(highLevelKey);
		String childKeys = StringUtils.substringAfter(fullKey, ".");
		if(childKeys.isEmpty() && data != null) {
			writer.append(data.toString()  + System.lineSeparator()).flush();
		} else if(childKeys.isEmpty() && data == null) {
			writer.append("ERROR - Key was not found." + System.lineSeparator()).flush();
		}
		
		
		String[] keyArr = StringUtils.split(childKeys, ".");
		String result;
		if(data != null  && ( result = data.getChildKey(keyArr)) != null) {
			writer.append(result  + System.lineSeparator()).flush();
		} else {
			writer.append("ERROR - Key was not found." + System.lineSeparator()).flush();
		}
	}

	private static void get(BufferedWriter writer, String line) throws IOException {
		String key = StringUtils.substringAfter(line, " ");
		JsonData data = trie.get(key);
		if(data == null) {
			writer.append("ERROR - Key was not found." + System.lineSeparator()).flush();
		} else {
			writer.append(data.toString()  + System.lineSeparator()).flush();
		}
	}

	private static void readArguments(String[] args) {
		if(args == null || args.length == 0) {
			throw new RuntimeException("No arguments given.");
		}
		for (int index = 0; index < args.length; index++) {
			String string = args[index];
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
