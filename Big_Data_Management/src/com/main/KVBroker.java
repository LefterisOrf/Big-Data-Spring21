package com.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class KVBroker {

	
	public static void main(String[] args) {
		try {
			Socket socket = new Socket("127.0.0.1", 9001);
			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			
			writer.append("PUT \"key12\" : { \"name\":\"Giorgos\" ; \"address\": { \"name\": \"Panepisthmiou\" } } ");
			
			
			writer.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
