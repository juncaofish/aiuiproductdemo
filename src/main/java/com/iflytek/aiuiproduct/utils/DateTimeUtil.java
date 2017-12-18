package com.iflytek.aiuiproduct.utils;

public class DateTimeUtil {

	public static int[] getClockTime(String data, String regex) {
		String[] times = data.split(regex);
		int[] intTime = new int[times.length];
		for(int i = 0; i < times.length; i++) {
			intTime[i] = Integer.valueOf(times[i]);
		}
		return intTime;
	}
}
