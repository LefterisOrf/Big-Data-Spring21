package com.data;

public class Tuple {
	private String key;
	private Object value;
	
	public Tuple() {
	}
	
	public Tuple(String key, Object value) {
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		String str = "\"" + key + "\" : ";
		if(value instanceof String) {
			str += "\"" + value + "\" ";
		} else {
			str += value + " ";
		}
		return str;
	}
	
}
