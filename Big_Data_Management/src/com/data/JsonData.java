package com.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JsonData {
	private String key;
	private List<Tuple> tuples;
	private Map<String, JsonData> keyValue;

	public JsonData(String key) {
		this.key = key;
		tuples = new ArrayList<Tuple>();
		keyValue = new HashMap<String, JsonData>();
	}
	
	public void insertKeyValue(Tuple tuple, String... parentKeys) {
		if(parentKeys == null || parentKeys.length == 0  ||
			tuple.getKey() == null || tuple.getValue() == null) {
			throw new RuntimeException("Cannot insert a tuple without a key.");
		}
		
		if(parentKeys.length == 1 && (parentKeys[0]).equals(this.key)) {
			tuples.add(tuple);
			return;
		} else if(parentKeys.length == 1 && keyValue.get(parentKeys[0]) != null) {
			keyValue.get(parentKeys[0]).insertKeyValue(tuple, parentKeys[0]);
		} else {
			List<String> q = new LinkedList<String>(Arrays.asList(parentKeys));	
			String key = q.remove(0);
			if(key.equals(this.key)) {
				this.insertKeyValue(tuple, q.toArray(new String[q.size()]));
			} else {
				JsonData json = new JsonData(key);
				keyValue.put(key, json);
				if(! q.isEmpty()) {
					json.insertKeyValue(tuple, q.toArray(new String[q.size()]));
				}
			}
		}
	}
	
	
	@Override
	public String toString() {
		String string = null;
		string = "\"" + key + "\" " + ": { " ;
		for(int i = 0; i < tuples.size(); i++) {
			string += tuples.get(i);
			if(i < (tuples.size() - 1) || !keyValue.isEmpty()) {
				string += " ; ";
			}
		}
		for (String childKey : keyValue.keySet()) {
			JsonData childData =  keyValue.get(childKey);
			string += childData.toString() + " ; ";
			string = string.substring(0, string.length() - 2) + " "; 
		}
		string += " } ";
		return string;
	}
	
	public List<Tuple> getTuples() {
		return tuples;
	}

	public void setTuples(List<Tuple> tuples) {
		this.tuples = tuples;
	}

	public Map<String, JsonData> getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(Map<String, JsonData> keyValue) {
		this.keyValue = keyValue;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
}