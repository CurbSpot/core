package com.foodtruckopt.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.log4j.Logger;

public class Util {
	private static Logger logger = Logger.getLogger(Util.class);

	public static boolean isEmptyOrNull(String string) {
		// TODO Auto-generated method stub
		return string == null || string.isEmpty();
	}

	public static String stringValue(Object string) {
		return string == null ? "" : string.toString();
	}

	public static boolean isSame(Map<String, Object> map, String key, String value) {
		logger.debug("value of=" + key + "=" + stringValue(map.get(key)) + " compareValue=" + stringValue(value));
		return stringValue(map.get(key)).equals(stringValue(value));
	}

	public static String toString(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String read;

			while ((read = br.readLine()) != null) {
				sb.append(read + System.lineSeparator());
			}
		}
		return sb.toString();
	}
}
