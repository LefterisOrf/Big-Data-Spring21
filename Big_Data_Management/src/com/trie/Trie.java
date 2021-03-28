package com.trie;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.data.JsonData;

public class Trie {

	private Node root;
	
	public Trie() {
		this.root = new Node();
	}
	
	public JsonData get(String key) {
		char[] keyAr = key.toCharArray();
		Node current = root;
		for (int i = 0; i < keyAr.length; i++) {
			char c = keyAr[i];
			current = current.getChild(c);
			if(current == null) {
				return null;
			}
		}
		return current.getData() != null ?  current.getData() : null;
	}
	
	public String query(String key) {
		// First of all split the key on '.' 
		String[] keys = StringUtils.split(key, ".");
		if(keys == null || keys.length == 0) {
			//Key does not contain a '.' ( equals to a GET request )
			JsonData data = get(key);
			if(data != null)
				return data.toString();
			else 
				return null;
		}
		// The high level key is in splitted[0] so we need to get(splitted[0])
		String primaryKey = keys[0];
		JsonData data = get(primaryKey);
		if(data == null) {
			return null;
		}
		// Then iterate through the KeyValueMap of the node to find the subsequent keys.
		String[] childKeys = Arrays.copyOfRange(keys, 1, keys.length);
		return data.getChildKey(childKeys);
	}
	
	public void insertData(JsonData data) {
		char[] keys = data.getKey().toCharArray();
		Node current = root;
		for(int i = 0; i < keys.length; i++) {
			Node node = current.getChild(keys[i]);
			if(node == null) {
				// create a Node for each of the remaining keys and set the JsonData to the last one.
				Node nodeChain = createNodeForEachRemainigKey(keys, i, data);
				current.putChild(nodeChain.getCurrentKey(), nodeChain);
				break;
			} else if( i == keys.length - 1){
				// if the last key set it in value
				node.setData(data);
				break;
			} else {
				current = node;
				continue;
			}
		}
	}

	private Node createNodeForEachRemainigKey(char[] keys, int i, JsonData data) {
		Node previous = null;
		for(int j = keys.length - 1; j >= i; j--) {
			Node node = new Node(keys[j]);
			if(previous != null) {
				node.putChild(previous.getCurrentKey(), previous);
			} else {
				//First iteration means we are at the last char of the key
				node.setData(data);
			}
			previous = node;
		}
		return previous;
	}
	
	
	
	/*
	 * Holds the key of the node, a Map to the children nodes 
	 * along with a JsonData reference in case we are at a complete node. 
	 */
	private static class Node {
		private Character currentKey;
		private Map<Character, Node> children;
		private JsonData data;
		
		public Node(Character key) {
			this();
			this.currentKey = key;
		}
		
		public Node() {
			this.data = null;
			this.children = new HashMap<Character, Node>();
			this.currentKey = null;
		}
		
		public Node getChild(Character c) {
			return children.get(c);
		}
		
		public void putChild(Character c, Node node) {
			this.children.put(c, node);
		}

		public JsonData getData() {
			return data;
		}

		public void setData(JsonData data) {
			this.data = data;
		}

		public Character getCurrentKey() {
			return currentKey;
		}

		@SuppressWarnings("unused")
		public Map<Character, Node> getChildren() {
			return children;
		}
	}
	
}
