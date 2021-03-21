package com.trie;

import com.data.JsonData;

public class Trie {

	private Node root;
	
	public Trie() {
		this.root = new Node();
	}
	
	public String get(String key) {
		char[] keyAr = key.toCharArray();
		Node current = root;
		for (int i = 0; i < keyAr.length; i++) {
			char c = keyAr[i];
			current = current.getChild(c);
			if(current == null) {
				return null;
			}
		}
		return current.getData() != null ?  current.getData().toString() : null;
	}
	
	public String query(String key) {
		// First of all split the key on '.' 
		
		// The high level key is in splitted[0] so we need to get(splitted[0])
		
		// Then iterate through the KeyValueMap of the node to find the subsequent keys.
		return "";
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
	
	
}
