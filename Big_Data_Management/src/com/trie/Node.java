package com.trie;

import java.util.HashMap;
import java.util.Map;

import com.data.JsonData;

/*
 * Holds the key of the node, a Map to the children nodes 
 * along with a JsonData reference in case we are at a complete node. 
 */
public class Node {
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

	public void setCurrentKey(Character currentKey) {
		this.currentKey = currentKey;
	}

	public Map<Character, Node> getChildren() {
		return children;
	}

	public void setChildren(Map<Character, Node> children) {
		this.children = children;
	}
	
}
