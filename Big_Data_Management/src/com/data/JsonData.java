package com.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;

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
	
	public static JsonData fromString(String jsonData) {
		String[] splitted = jsonData.split(":", 2);
		String key = StringUtils.substringBetween(splitted[0], "\"");
		String value = StringUtils.substringAfter(splitted[1], "{");
		value = StringUtils.substringBeforeLast(value, "}");
		JsonData parent = new JsonData(key);
		Stack<JsonData> active = new Stack<JsonData>();
		while(true) {
			String[] splittedVal = StringUtils.split(value, ";", 2);
			String possibleTuple = splittedVal[0];
			// Remove possibleTuple from value
			if(splittedVal.length > 1) {
				value = splittedVal[1];
			} else {
				value = "";
			}
			if(possibleTuple.contains("{") && possibleTuple.contains("}")) {
				// New JsonData with one tuple inside it
				JsonData json = new JsonData("");
				json.setKey(StringUtils.substringBetween(StringUtils.substringBefore(possibleTuple, ":"), "\""));
				String tuple = StringUtils.substringBetween(possibleTuple, "{", "}");
				json.getTuples().add(generateTupleFromString(tuple));
				if(!active.isEmpty()) {
					active.peek().getKeyValue().put(json.getKey(), json);
					popFromStackAccordingToTheNumberOfClosingTags(parent, active, StringUtils.substringBeforeLast(possibleTuple, "}"), json);
				} else {
					parent.getKeyValue().put(json.getKey(), json);
				}
			} else if(possibleTuple.contains("{")) {
				// Contains only an opening tag -> Create new JsonData
				active.add(new JsonData(""));
				active.peek().setKey(StringUtils.substringBetween(StringUtils.substringBefore(possibleTuple, ":"), "\""));
				String tuple = StringUtils.substringAfter(possibleTuple, "{");
				active.peek().getTuples().add(generateTupleFromString(tuple));
			} else if(possibleTuple.contains("}")) {
				// Contains one or more closing tags
				if(!active.isEmpty()) {
					active.peek().getTuples().add(generateTupleFromString(possibleTuple));
					popFromStackAccordingToTheNumberOfClosingTags(parent, active, possibleTuple, parent);
				} else {
					parent.getTuples().add(generateTupleFromString(possibleTuple));
				}
			}
			else {
				// Just a regular tuple
				if(!active.isEmpty() ) {
					active.peek().getTuples().add(generateTupleFromString(possibleTuple));
				} else {
					parent.getTuples().add(generateTupleFromString(possibleTuple));
				}
			}
			
			if(value.isEmpty()) {
				break;
			}
		}
		popEverythingFromStack(parent, active);
		
		
		return parent;
	}

	private static void popFromStackAccordingToTheNumberOfClosingTags(JsonData parent, Stack<JsonData> active,	String possibleTuple, JsonData json) {
		Integer matches = StringUtils.countMatches(possibleTuple, "}") ;
		JsonData dato = null;
		if(!active.isEmpty()) {
			for (int i = 0; i < matches; i++) {
				if(dato != null) {
					active.peek().getKeyValue().put(dato.getKey(), dato);
				}
				dato = active.pop();
			}
			if(dato != null && active.isEmpty()) {
				parent.getKeyValue().put(dato.getKey(), dato);
			} else if(dato != null && !active.isEmpty()) {
				active.peek().getKeyValue().put(dato.getKey(), dato);
			}
		} else if(matches <= 1) {
			parent.getKeyValue().put(json.getKey(), json);
		} else {
			throw new RuntimeException("Found " + matches + " matches but active is empty.");
		}
	}
	
	private static void popEverythingFromStack(JsonData parent, Stack<JsonData> active) {
		JsonData dato = null;
		while(!active.isEmpty()) {
			if(dato != null) {
				active.peek().getKeyValue().put(dato.getKey(), dato);
			}
			dato = active.pop();
		}

		if(dato != null) {
			parent.getKeyValue().put(dato.getKey(), dato);
		}
	}

	private static Tuple generateTupleFromString(String possibleTuple) {
		Tuple tuple = new Tuple();
		if(possibleTuple.contains("}")) {
			possibleTuple = StringUtils.substringBefore(possibleTuple, "}");
		}
		
		
		String tupKey = StringUtils.substringBefore(possibleTuple, ":").trim();
		tupKey = StringUtils.substringBetween(tupKey, "\"");
		tuple.setKey(tupKey);
		String tupValue = StringUtils.substringAfter(possibleTuple, ":").trim();
		if(tupValue.contains("\"")) {
			tuple.setValue(StringUtils.substringBetween(tupValue, "\""));
		} else if(tupValue.contains(".")){
			// Try to instantiate a Double
			tuple.setValue(Double.parseDouble(tupValue));
		} else {
			// Integer
			tuple.setValue(Integer.parseInt(tupValue));
		}
		return tuple;
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
