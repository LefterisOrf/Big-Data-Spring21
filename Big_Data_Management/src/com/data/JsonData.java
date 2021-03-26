package com.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;

/*
 * Class that holds a key and the tuples that this key holds.
 * In case some of the key's children are complex types, we add them on the KeyValue map as separate JsonData attributes
 */
public class JsonData {
	private String key;
	private List<Tuple> tuples;
	private Map<String, JsonData> keyValue;

	public JsonData(String key) {
		this.key = key;
		tuples = new ArrayList<Tuple>();
		keyValue = new HashMap<String, JsonData>();
	}
	
	public String getChildKey(String... keys) {
		if(keys == null || keys.length == 0 || keyValue.isEmpty()) {
			return null;
		}
		JsonData child = null;
		JsonData father = this;
		for (int i = 0; i < keys.length; i++) {
			child = father.getChild(keys[i]);
			if(child == null) {
				// search on tuples list.
				return father.getFromTupleList(keys[i]);
			}
			father = child;
		}
		return child.toString();
	}
	
	private JsonData getChild(String key) {
		return keyValue.get(key);
	}
	
	private String getFromTupleList(String key) {
		for (Tuple tuple : tuples) {
			if(tuple.getKey().equals(key)) {
				return tuple.toString();
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		String string = null;
		string = "\"" + key + "\" " + ": { " ;
		for(int i = 0; i < tuples.size(); i++) {
			string += tuples.get(i) == null ? "" : tuples.get(i);
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
		String key = StringUtils.substringBefore(jsonData, ":");
		key = StringUtils.substringBetween(key, "\"");
		String value = StringUtils.substringAfter(jsonData, ":");
		value = StringUtils.substringAfter(value, "{");
		value = StringUtils.substringBeforeLast(value, "}");
		if(key == null || key.isEmpty()) {
			return null;
		}
		JsonData parent = new JsonData(key);
		Stack<JsonData> active = new Stack<JsonData>();
		while(true) {
			if(value.trim().isEmpty()) {
				break;
			}
			
			String[] splittedVal = StringUtils.split(value, ";", 2);
			if(splittedVal.length == 0) {
				return null;
			}
			
			String possibleTuple = splittedVal[0];
			// Remove possibleTuple from value
			if(splittedVal.length > 1) {
				value = splittedVal[1];
			} else {
				value = "";
			}
			if(possibleTuple.contains("{") && possibleTuple.contains("}")) {
				// New JsonData with one tuple inside it and add it to the father ( either the head of the stack or the parent )
				JsonData json = createSimpleJsonData(possibleTuple);
				if(!active.isEmpty()) {
					active.peek().getKeyValue().put(json.getKey(), json);
					popFromStackAccordingToTheNumberOfClosingTags(parent, active, StringUtils.substringBeforeLast(possibleTuple, "}"), json);
				} else {
					parent.getKeyValue().put(json.getKey(), json);
				}
			} else if(possibleTuple.contains("{")) {
				// Contains only an opening tag -> Create new JsonData and add it to the stack
				active.add(new JsonData(""));
				active.peek().setKey(StringUtils.substringBetween(StringUtils.substringBefore(possibleTuple, ":"), "\""));
				String tuple = StringUtils.substringAfter(possibleTuple, "{");
				active.peek().getTuples().add(generateTupleFromString(tuple));
			} else if(possibleTuple.contains("}")) {
				/* Contains one or more closing tags. Create a Tuple, add it to the head of the stack ( or the parent ) 
				 * and then add the jsonData to their respective fathers */
				if(!active.isEmpty()) {
					active.peek().getTuples().add(generateTupleFromString(possibleTuple));
					popFromStackAccordingToTheNumberOfClosingTags(parent, active, possibleTuple, parent);
				} else {
					parent.getTuples().add(generateTupleFromString(possibleTuple));
				}
			}
			else {
				// Create a simple tuple and add it either to the head of the stack or to the parent.
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

	/*
	 * Creates a JsonData which contains only 1 tuple and nothing more.
	 */
	private static JsonData createSimpleJsonData(String possibleTuple) {
		JsonData json = new JsonData("");
		json.setKey(StringUtils.substringBetween(StringUtils.substringBefore(possibleTuple, ":"), "\""));
		String tuple = StringUtils.substringBetween(possibleTuple, "{", "}");
		json.getTuples().add(generateTupleFromString(tuple));
		return json;
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
		if(possibleTuple == null || possibleTuple.trim().isEmpty()) {
			return null;
		}
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
