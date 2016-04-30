package org.xsnake.common;

import java.text.SimpleDateFormat;

import com.google.gson.Gson;

public class SingletonUtil {

	private static Gson gson = new Gson();
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	public static Gson getGson() {
		return gson;
	}

	public static SimpleDateFormat getSimpleDateFormat() {
		return sdf;
	}
	
}
