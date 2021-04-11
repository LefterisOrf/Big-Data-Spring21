package com.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import com.data.JsonData;
import com.data.JsonData.Tuple;

public class CreateData {
	private static String filename;
	private static Integer maxStringLength;
	private static Integer lines;
	private static Integer maxNesting;
	private static Integer maxKeysInLine;
	private static char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	private static Map<String, Class<?>> datatypes= new HashMap<String, Class<?>>();
	private static Random random = new Random(System.currentTimeMillis());
	
	public static void main(String args[]) throws FileNotFoundException {
		readArguments(args);
		readFile();
		generateData();
	}

	private static void readArguments(String[] args) {
		for (int index = 0; index < args.length; index++) {
			String string = args[index];
			if("-k".equals(string)) {
				filename = args[index + 1];
			} else if("-n".equals(string)) {
				lines = Integer.parseInt(args[index + 1]);
			} else if("-d".equals(string)) {
				maxNesting = Integer.parseInt(args[index + 1]);
			} else if("-l".equals(string)) {
				maxStringLength = Integer.parseInt(args[index + 1]);
			} else if("-m".equals(string)) {
				maxKeysInLine = Integer.parseInt(args[index + 1]);
			}
		}
		if(areArgumentsInvalid()) {
			throw new RuntimeException("Invalid arguments. Program will exit.");
		}
	}
	
	private static void generateData() {
		for (int i = 0; i < lines; i++) {
			JsonData father = generateRandomSimpleData("key" + i);
			insertNestedTuple(father);
			JsonData temp = JsonData.fromString(father.toString());
			if(! temp.toString().equals(father.toString())) {
				System.out.println("Mismatch between 2 generated JsonData.");
				System.out.println("Original data: " + father);
				System.out.println("Generated data: " + temp);
				System.out.println("----------------------------------------");
			}
//			System.out.print(father.toString() + System.lineSeparator());
		}
	}
	
	private static void insertNestedTuple(JsonData head) {
		JsonData previousData = null, currentData = null;
		for(int i = 0; i < maxNesting; i++) {
			String key = generateString();
			currentData = generateRandomSimpleData(key);
			if(previousData != null) {
				currentData.getKeyValue().insert(previousData);
			}
			previousData = currentData;
			if(random.nextInt() % 2 == 0) {
				break;
			}
		}
		
		if(currentData != null) {
			head.getKeyValue().insert(currentData);
		}
	}
	
	/*
	 * Returns a Json Data with no values in its map. (Contains only simple key value pairs )
	 */
	private static JsonData generateRandomSimpleData(String key) {
		JsonData data = new JsonData(key);
		for(int i = 0; i < maxKeysInLine; i ++) {
			data.getTuples().add(getRandomTuple());
			if(random.nextInt() % 3 == 0) {
				break;
			}
		}
		return data;
	}
	
	private static Tuple getRandomTuple() {
		Tuple tuple = new Tuple();
		Class<?> klazz = null;
		for(String key : datatypes.keySet()) {
			klazz = datatypes.get(key);
			tuple.setKey(key);
			if(random.nextBoolean()) {
				break;
			}
		}
		
		if(klazz == Integer.class) {
			tuple.setValue(new Integer(random.nextInt()) );
		} else if(klazz == String.class) {
			tuple.setValue(generateString());
		} else if(klazz == Float.class) {
			tuple.setValue(new Float(random.nextFloat()));
		} else if(klazz == Double.class) {
			tuple.setValue(new Double(random.nextDouble()));
		} else {
			throw new RuntimeException("Invalid klazz value: " + klazz.getSimpleName());
		}
		return tuple;
	}
	
	private static String generateString() {
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < maxStringLength; i++) {
			str.append(alphabet[random.nextInt(alphabet.length) ]);
			if(random.nextInt() % 11 == 0) {
				break;
			}
		}
		return str.toString();
	}
	
	private static void readFile() throws FileNotFoundException {
		File file = new File(filename);
		if(! file.exists() || !file.canRead()) {
			throw new RuntimeException("File cannot be opened. Filename: " + filename + " path : " + file.getAbsolutePath());
		}
		Scanner scanner = new Scanner(file);
		while(scanner.hasNextLine()) {
			String data = scanner.nextLine();
			String[] splitted = data.split(" ");
			if(splitted.length < 2) {
				scanner.close();
				throw new RuntimeException("Invalid data format in file.");
			}
			String key = splitted[0];
			String classString = splitted[1];
			Class<?> klass = null;
			switch (classString) {
			case "string":
				klass = String.class;
				break;
			case "int":
				klass = Integer.class;
				break;
			case "float":
				klass = Float.class;
				break;
			case "double":
				klass = Double.class;
				break;
			}
			if(klass != null) {
				datatypes.put(key, klass);
			}
		}
		scanner.close();
	}
	
	private static boolean areArgumentsInvalid() {
		if(filename == null || maxStringLength == null || lines == null || maxNesting == null || maxKeysInLine == null) {
			return true;
		}
		return false;
	}
	
}
