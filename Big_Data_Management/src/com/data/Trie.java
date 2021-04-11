package com.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.data.interfaces.KVClass;

public class Trie<E extends KVClass> {

	private Node<E> root;
	private List<String> keys;
	
	public Trie() {
		this.root = new Node<E>();
		this.keys = new ArrayList<String>();
	}
	
	public E get(String key) {
		Node<E> current = find(key);
		return current != null ?  current.getPayload() : null;
	}
	
	public boolean delete(String key) {
		Node<E> temp;
		if((temp = find(key)) == null) {
			return false;
		}
		
		if(temp.getPayload() == null) {
			return false;
		}
		
		temp.setPayload(null);
		Node<E> parent;
		while((parent = temp.getParent()) != null && temp.getChildren().isEmpty() && temp.getPayload() != null) {
			parent.getChildren().remove(temp.getKey());
			temp = parent;
		}
		keys.remove(key);
		return true;
	}
	
	public boolean insert(E payload) {
		if(payload == null) {
			return false;
		}
		char[] keys = payload.getKey().toCharArray();
		Node<E> current = root;
		for(int i = 0; i < keys.length; i++) {
			Node<E> node = current.get(keys[i]);
			if(node == null) {
				// create a Node for each of the remaining keys and set the JsonData to the last one.
				Node<E> nodeChain = createNodeForEachRemainigKey(keys, i, payload);
				current.insert(nodeChain.getKey(), nodeChain);
				nodeChain.setParent(current);
				break;
			} else if( i == keys.length - 1){
				// if the last key set it in value. Overwrites any possible previous data.
				node.setPayload(payload);
				break;
			} else {
				current = node;
				continue;
			}
		}
		this.keys.add(payload.getKey());
		return true;
	}
	
	public boolean isEmpty() {
		if(root == null || (root.getPayload() == null && root.getChildren().isEmpty())) {
			return true;
		}
		return false;
	}
	
	private Node<E> createNodeForEachRemainigKey(char[] keys, int i, E data) {
		Node<E> previous = null;
		for(int j = keys.length - 1; j >= i; j--) {
			Node<E> node = new Node<>(keys[j]);
			if(previous != null) {
				node.insert(previous.getKey(), previous);
				previous.setParent(node);
			} else {
				//First iteration means we are at the last char of the key
				node.setPayload(data);
			}
			previous = node;
		}
		return previous;
	}
	
	private Node<E> find(String key) {
		char[] keyAr = key.toCharArray();
		Node<E> current = root;
		for (int i = 0; i < keyAr.length; i++) {
			char c = keyAr[i];
			current = current.get(c);
			if(current == null) {
				return null;
			}
		}
		return current.getPayload() != null ?  current : null;
	}
	
	public List<String> getKeys() {
		return keys;
	}

	public static class Node<E> {
		private Node<E> parent;
		private Character key;
		private HashMap<Character, Node<E>> children;
		private E payload;
		
		public Node() {
			this.parent = null;
			this.children = new HashMap<Character, Trie.Node<E>>();
		}
		
		public Node(Character key) {
			this();
			this.key = key;
			this.payload = null;
		}
		
		public Node(Character key, E payload) {
			this(key);
			this.payload = payload;
		}
		
		public Node<E> get(Character key) {
			return children.get(key);
		}
		
		public void insert(Character key, Node<E> node) {
			this.children.put(key, node);
		}
		
		public Character getKey() {
			return key;
		}
		public void setKey(Character key) {
			this.key = key;
		}
		public HashMap<Character, Node<E>> getChildren() {
			return children;
		}
		public void setChildren(HashMap<Character, Node<E>> children) {
			this.children = children;
		}
		public E getPayload() {
			return payload;
		}
		public void setPayload(E payload) {
			this.payload = payload;
		}

		public Node<E> getParent() {
			return parent;
		}

		public void setParent(Node<E> parent) {
			this.parent = parent;
		}
		
	}
}
