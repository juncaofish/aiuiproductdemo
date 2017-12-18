package com.iflytek.aiuiproduct.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Id工具类，用于生成Id标识。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年8月27日 下午4:02:21
 *
 */
public class IdUtil {
	private static Map<String, Integer> AlarmIds = new HashMap<String, Integer>();
	private static Object alarmSynObj = new Object();
	
	/**
	 * 清空某种数据的alarmIds。
	 * 
	 * @param dataType 数据类型，取值：audio，image，video
	 * @return
	 */
	public static boolean clearStreamId(String dataType) {
		synchronized (alarmSynObj) {
			return AlarmIds.remove(dataType) == null ? false : true;
		}
	}
	
	/**
	 * 获取StreamId。
	 * 
	 * @param dataType 数据类型，取值：audio，image，video，text，event
	 * @return streamId
	 */
	public static int getAlarmId(String dataType) {
		synchronized (alarmSynObj) {
			int id = 0;
			if (!AlarmIds.containsKey(dataType)) {
				id = 1;
			} else {
				id = AlarmIds.get(dataType);
				id = (id + 1) & 0xffff;
			}
			AlarmIds.put(dataType, id);
			
			return id;
		}
	}
}
