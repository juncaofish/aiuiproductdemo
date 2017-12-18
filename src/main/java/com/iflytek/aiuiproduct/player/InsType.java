package com.iflytek.aiuiproduct.player;

import java.util.HashMap;
import java.util.Map;

public enum InsType {
	NEXT,
	PAST,
	REPLAY,
	REPEAT,
	PAUSE,
	VOLMAX,
	VOLMID,
	VOLMIN,
	VOLMINUS,
	VOLPLUS,
	REPLAYANSWER,
	DISLIKE,
	LIKE;
	
	/** 音乐播放控制命令 **/
	private final static String INSTYPE_NEXT = "next";
	private final static String INSTYPE_PAST = "past";
	private final static String INSTYPE_REPLAY = "replay";
	private final static String INSTYPE_REPEAT = "repeat";
	private final static String INSTYPE_PAUSE = "pause";
	private final static String INSTYPE_REPLAYANSWER = "replayAnswer";
	

	/** 音量控制命令 **/
	private final static String INSTYPE_VOLMAX = "volume_max";
	private final static String INSTYPE_VOLMID = "volume_mid";
	private final static String INSTYPE_VOLMIN = "volume_min";
	private final static String INSTYPE_VOLMINUS = "volume_minus";
	private final static String INSTYPE_VOLPLUS = "volume_plus";
	
	private final static String INSTYPE_DISLIKE = "dislike";
	private final static String INSTYPE_LIKE = "like";
	
	
	
	private static Map<String, InsType> insTypeMap;
	
	static {
		insTypeMap = new HashMap<String, InsType>();
		insTypeMap.put(INSTYPE_NEXT, InsType.NEXT);
		insTypeMap.put(INSTYPE_PAST, InsType.PAST);
		insTypeMap.put(INSTYPE_REPLAY, InsType.REPLAY);
		insTypeMap.put(INSTYPE_REPEAT, InsType.REPEAT);
		insTypeMap.put(INSTYPE_PAUSE, InsType.PAUSE);
		insTypeMap.put(INSTYPE_VOLMAX, InsType.VOLMAX);
		insTypeMap.put(INSTYPE_VOLMID, InsType.VOLMID);
		insTypeMap.put(INSTYPE_VOLMIN, InsType.VOLMIN);
		insTypeMap.put(INSTYPE_VOLMINUS, InsType.VOLMINUS);
		insTypeMap.put(INSTYPE_VOLPLUS, InsType.VOLPLUS);
		insTypeMap.put(INSTYPE_REPLAYANSWER, InsType.REPLAYANSWER);
		insTypeMap.put(INSTYPE_DISLIKE, InsType.DISLIKE);
		insTypeMap.put(INSTYPE_LIKE, InsType.LIKE);
	}
	
	public static InsType parseInsType(String instype){
		return insTypeMap.get(instype);
	}
}