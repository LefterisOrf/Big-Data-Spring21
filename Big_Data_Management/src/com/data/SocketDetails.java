package com.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketDetails {
	private String hostname;
	private Integer port;
	private Socket socket;
	private BufferedWriter writer;
	private BufferedReader reader;
	
	public SocketDetails(String hostname, Integer port) throws UnknownHostException, IOException {
		this.hostname = hostname;
		this.port = port;
		this.socket = new Socket(InetAddress.getByName(hostname), port);
		this.socket.setSoTimeout(800);
		this.writer = new BufferedWriter(new PrintWriter(socket.getOutputStream()));
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public boolean isClosed() {
		if(socket == null) {
			return true;
		}
		return socket.isClosed();
	}
	
	public void close() throws IOException {
		if(socket == null) {
			return;
		}
		socket.close();
	}
	
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public BufferedWriter getWriter() {
		return writer;
	}
	public void setWriter(BufferedWriter writer) {
		this.writer = writer;
	}
	public BufferedReader getReader() {
		return reader;
	}
	public void setReader(BufferedReader reader) {
		this.reader = reader;
	}
	
}
