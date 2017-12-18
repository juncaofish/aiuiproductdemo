package com.iflytek.aiuiproduct.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.aiui.utils.log.DebugLog;
import com.iflytek.aiuiproduct.utils.FileUtil.DataFileHelper;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * App耗时记录类。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年4月21日 下午3:55:30 
 *
 */
public class AppTimeLogger {
	public final static String TAG = "AppTimeLogger";
	
	private final static String TIME_LOG_DIR = Environment.getExternalStorageDirectory()
													.getAbsolutePath() + "/AIUI/time/";
	
	private final static String DATA_LOG_DIR = Environment.getExternalStorageDirectory()
													.getAbsolutePath() + "/AIUI/data/";
	
	private final static String FILE_INDEX = "index.txt";
	
	private static String CUR_WAKE_DIR = "";
	
	private static DataFileHelper timeLogHelper;
	
	private static DataFileHelper realEosFileHelper;
	
	private static Map<String, TimeLog> timeLogs = new HashMap<String, AppTimeLogger.TimeLog>();
	
	public final static String TIME_POS_REAL_EOS = "p_real_eos";
	
	public final static String TIME_POS_BOS = "p_bos";
	
	public final static String TIME_POS_EOS = "p_eos";
	
	public final static String TIME_POS_NLP_SDK = "p_nlp_sdk";
	
	public final static String TIME_LEN_NLP_FROM_BOS = "bos_nlp";
	
	public final static String TIME_LEN_NLP_FROM_EOS = "eos_nlp";
	
	public final static String TIME_LEN_IAT_FROM_BOS = "bos_iat";
	
	public final static String TIME_LEN_IAT_FROM_EOS = "eos_iat";
	
	public final static String TIME_POS_NLP_ARRIVE_APP = "p_nlp_app";
	
	public final static String TIME_POS_NLP_PARSE_FINISH = "p_prs_fin";
	
	public final static String TIME_LEN_NLP_PARSE = "nlp_prs";
	
	public final static String TIME_POS_MUSIC_ON_CONVERT = "p_mcvt";
	
	public final static String TIME_LEN_ARRIVAL_CONVERT = "arv_cvt";
	
	public final static String TIME_POS_MUSIC_CONVERT_FINISH = "p_mcvt_fin";
	
	public final static String TIME_LEN_MUSIC_CONVERT = "nlp_mcvt";
	
	public final static String TIME_POS_TONE_PLAY_START = "p_tone";
	
	public final static String TIME_POS_TONE_PLAY_FINISH = "p_tone_fin";
	
	public final static String TIME_LEN_ARRIVAL_TONE_PLAY_START = "arv_tone";
	
	public final static String TIME_LEN_TONE_PLAY = "tone_play";
	
	public final static String TIME_POS_TTS_START = "p_tts";
	
	public final static String TIME_POS_TTS_FINISH = "p_tts_fin";
	
	public final static String TIME_LEN_ARRIVAL_TTS_START = "arv_tts";
	
	public final static String TIME_LEN_TTS_TOTAL = "tts_ttl";
	
	public final static String TIME_POS_TTS_PLAY_START = "p_ttsps";
	
	public final static String TIME_POS_TTS_PLAY_FINISH = "p_ttsp_fin";
	
	public final static String TIME_LEN_ARRIVAL_TTS_PLAY_START = "arv_ttsps";
	
	public final static String TIME_LEN_TTS_PLAY = "tts_play";
	
	public static String curUUID = "";
	
	private static TimeLogSaveListener mLogSaveListener;
	
	public interface TimeLogSaveListener {
		public void onSave(TimeLog log);
	}
	
	public static class TimeLog {
		public String sid = "";
		
		public String uuid = "";
		
		public String rc = "";
		
		public String tag = "";
		
		public String service = "";
		
		public String params = "";
		
		public String stream_id = "";
		
		public String insType = "";
		
		public Map<String, Long> times = new HashMap<String, Long>();
		
		public List<String> keyList = new ArrayList<String>();
		
		public TimeLog(String sid) {
			this.sid = sid;
		}
		
		public void putTime(String name, long time) {
			putTime(name, time, false);
		}
		
		public void putTime(String name, long time, boolean isLen) {
			times.put(name, time);
			keyList.add(name);
			
			if (isLen) {
				Log.d(TAG, service + "," + name + "=" + time);
			}
		}
		
		public Long getTime(String name) {
			return times.get(name);
		}

		public String getSid() {
			return sid;
		}

		public void setSid(String sid) {
			this.sid = sid;
		}
		
		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		
		public String getRC() {
			return this.rc;
		}
		
		public void setRC(String rc) {
			this.rc = rc;
		}

		public String getTag() {
			return tag;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}

		public String getService() {
			return service;
		}

		public void setService(String service) {
			this.service = service;
		}
		
		public void setParams(String params) {
			this.params = params;
		}
		
		public String getParams() {
			return params;
		}
		
		public String getStreamId() {
			return stream_id;
		}

		public void setStreamId(String streamId) {
			this.stream_id = streamId;
		}
		
		public void setInsType(String insType) {
			Log.d(TAG, service + ",insType=" + insType);
			
			this.insType = insType;
		}
		
		public String getInsType() {
			return insType;
		}

		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			
			buffer.append("sid=").append(sid).append("\n")
				.append("stream_id=").append(stream_id).append("\n")
				.append("uuid=").append(uuid).append("\n")
				.append("rc=").append(rc).append("\n")
				.append("tag=").append(tag).append("\n")
				.append("service=").append(service).append("\n")
				.append("params=").append(params).append("\n");
			
			if (!TextUtils.isEmpty(insType)) {
				buffer.append("insType=").append(insType).append("\n");
			}
			
			String key = "";
			for (int i = 0; i < keyList.size(); i++) {
				key = keyList.get(i);
				
				buffer.append(key).append("=").append(times.get(key)).append("\n");
			}
			
			buffer.append("\n\n");
			
			return buffer.toString();
		}
		
		public JSONObject toJson() {
			JSONObject timeLogJson = new JSONObject();
			
			try {
				timeLogJson.put("sid", sid);
				timeLogJson.put("service", service);
				timeLogJson.put("params", new JSONObject(params));
				timeLogJson.put("stream_id", stream_id);
				
				if (!TextUtils.isEmpty(insType)) {
					timeLogJson.put("insType", insType);
				}
				
				for (String key: times.keySet()) {
					timeLogJson.put(key, times.get(key));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return timeLogJson;
		}
		
	}
	
	private static TimeLog getTimeLog(String uuid) {
		return timeLogs.get(uuid);
	}
	
	public static void setTimeLogSaveListener(TimeLogSaveListener listener) {
		mLogSaveListener = listener;
	}
	
	private static int makeWakeDir() {
		int wakeIndex = getWakeIndex();
		CUR_WAKE_DIR = TIME_LOG_DIR + "wake" + wakeIndex + "/";
		
		File wakeDir = new File(CUR_WAKE_DIR);
		if (!wakeDir.exists()) {
			wakeDir.mkdirs();
		}
		
		timeLogs.clear();
		curUUID = null;
		
		closeLogFile();
		timeLogHelper = FileUtil.createFileHelper(CUR_WAKE_DIR);
		realEosFileHelper = FileUtil.createFileHelper(CUR_WAKE_DIR);
		
		return wakeIndex;
	}
	
	private static void createLogFile(String filename) {
		if (null != timeLogHelper) {
			timeLogHelper.createAppendableFile(filename, FileUtil.SURFFIX_TXT);
		}
	}
	
	private static void createRealEosFile(String filename) {
		if (null != realEosFileHelper) {
			realEosFileHelper.createAppendableFile(filename, FileUtil.SURFFIX_TXT);
		}
	}
	
	private static void closeLogFile() {
		if (null != timeLogHelper) {
			timeLogHelper.closeAppendableFile();
		}
		
		if (null != realEosFileHelper) {
			realEosFileHelper.closeAppendableFile();
		}
	}
	
	private static void writeLog(String log) {
		if (null != timeLogHelper) {
			timeLogHelper.append(log);
		}
	}
	
	private static void writeRealEos(String realEosInfo) {
		if (null != realEosFileHelper) {
			realEosFileHelper.append(realEosInfo);
		}
	}
	
	private static int getWakeIndex() {
		int index = 1;
		
		File logDir = new File(TIME_LOG_DIR);
		if (!logDir.exists()) {
			logDir.mkdirs();
		}
		
		File indexFile = new File(TIME_LOG_DIR + FILE_INDEX);
		BufferedReader br = null;
		if (indexFile.exists()) {
			try {
				br = new BufferedReader(new FileReader(indexFile));
				String line = br.readLine();
				
				if (!TextUtils.isEmpty(line)) {
					index = Integer.parseInt(line);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				
			} finally {
				if (null != br) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		try {
			FileWriter fw = new FileWriter(indexFile);
			fw.write(String.valueOf(index + 1));
			
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return index;
	}
	
	private static void addTimeLog(String uuid, TimeLog log) {
		if (!TextUtils.isEmpty(uuid)) {
			timeLogs.put(uuid, log);
		}
	}
	
	private static void putTime(String uuid, String name) {
		if (!TextUtils.isEmpty(uuid)) {
			putTime(uuid, name, System.currentTimeMillis(), false);
		}
	}
	
	private static void putTime(String uuid, String name, long time) {
		if (!TextUtils.isEmpty(uuid)) {
			putTime(uuid, name, time, false);
		}
	}
	
	private static void computeTimeLen(String uuid, String from, String to, String lenName) {
		if (TextUtils.isEmpty(uuid)) {
			return;
		}
		
		Long fromTime = getTime(uuid, from);
		Long toTime = getTime(uuid, to);
		long timeLen = -1;
		
		if (null != fromTime && toTime != null) {
			timeLen = toTime - fromTime;
		}
		
		putTime(uuid, lenName, timeLen, true);
	}
	
	private static void putTime(String uuid, String name, long time, boolean isLen) {
		if (TextUtils.isEmpty(uuid)) {
			return;
		}
		
		if (!TextUtils.isEmpty(curUUID)) {
			if (!curUUID.equals(uuid)) {
				saveTimeLog(curUUID);
			}
		}
		
		curUUID = uuid;
		
		TimeLog log = timeLogs.get(uuid);
		
		if (null != log) {
			log.putTime(name, time, isLen);
		}
	}
	
	private static Long getTime(String uuid, String name) {
		TimeLog log = timeLogs.get(uuid);
		
		if (null != log) {
			return log.getTime(name);
		}
		
		return null;
	}
	
	private static void saveTimeLog(String uuid) {
		TimeLog log = timeLogs.remove(uuid);
		
		if (null != log) {
			if (null != mLogSaveListener) {
				mLogSaveListener.onSave(log);
			}
			
			writeLog(log.toString());
		}
	}
	
	private static void saveCurTimeLog() {
		saveTimeLog(curUUID);
	}
	
	private static void setService(String uuid, String service) {
		TimeLog log = timeLogs.get(uuid);
		
		if (null != log) {
			log.setService(service);
		}
	}
	
	private static long getRealEos(String tag) {
		if (TextUtils.isEmpty(tag)) {
			return -1;
		}
		String[] parts = tag.split(":|;");
		DebugLog.LogD(TAG, parts.toString());
		return Long.parseLong(parts[1]);
	}
	
	public static void onRealWakeup(){
		// 当该次唤醒创建文件夹
		AppTimeLogger.makeWakeDir();
		AppTimeLogger.createLogFile("time");
	}
	
	public static void onSemanticResult(JSONObject resultJson,Bundle data, 
			String params, long posRsltArrival, long posRsltParseFinish){
		String sid = data.getString("sid");
		String serviceName = resultJson.optString("service");
		String uuid = resultJson.optString("uuid");
		String rc = resultJson.optString("rc");
		
		long lenRsltParse = posRsltParseFinish - posRsltArrival;
		
		Long posBos = data.getLong("p_bos");
		Long posEos = data.getLong("p_eos");
		Long resultTimePos = data.getLong("p_rslt");
		Long timeSpentFromBos = data.getLong("bos_rslt");
		Long timeSpentFromEos = data.getLong("eos_rslt");
		Long iatTimeFromBos = data.getLong("bos_iat");
		Long iatTimeFromEos = data.getLong("eos_iat");

		String tag = data.getString("tag");
		String streamId = data.getString("stream_id");

		DebugLog.LogD(AppTimeLogger.TAG, "\nsid=" + sid);
		AppTimeLogger.saveCurTimeLog();

		TimeLog timeLog = new TimeLog(sid);
		timeLog.setUuid(uuid);
		timeLog.setRC(rc);
		timeLog.setService(serviceName);
		timeLog.setParams(params);
		timeLog.setStreamId(streamId);

		AppTimeLogger.addTimeLog(uuid, timeLog);
		AppTimeLogger.putTime(uuid, AppTimeLogger.TIME_POS_NLP_SDK, resultTimePos, false);
		AppTimeLogger.putTime(uuid, "p_bos_orgin", posBos, true);
		AppTimeLogger.putTime(uuid, AppTimeLogger.TIME_POS_EOS, posEos, true);

		// 加入真实语音后端点的时间点
		if (!TextUtils.isEmpty(tag)) {
			timeLog.putTime(AppTimeLogger.TIME_POS_REAL_EOS, getRealEos(tag));
			timeLog.setTag(tag);
		}

		timeLog.putTime(AppTimeLogger.TIME_LEN_NLP_FROM_BOS, timeSpentFromBos, true);
		timeLog.putTime(AppTimeLogger.TIME_POS_BOS, resultTimePos - timeSpentFromBos);
		timeLog.putTime(AppTimeLogger.TIME_LEN_NLP_FROM_EOS, timeSpentFromEos, true);
		timeLog.putTime(AppTimeLogger.TIME_LEN_IAT_FROM_BOS, iatTimeFromBos, true);
		timeLog.putTime(AppTimeLogger.TIME_LEN_IAT_FROM_EOS, iatTimeFromEos, true);
		timeLog.putTime(AppTimeLogger.TIME_POS_NLP_ARRIVE_APP, posRsltArrival);
		timeLog.putTime(AppTimeLogger.TIME_POS_NLP_PARSE_FINISH, posRsltParseFinish);
		timeLog.putTime(AppTimeLogger.TIME_LEN_NLP_PARSE, lenRsltParse, true);
	}
	
	public static void onSleep(String uuid){
		AppTimeLogger.saveTimeLog(uuid);
	}
	
	public static void onMusicParseStart(String uuid){
		AppTimeLogger.putTime(uuid, AppTimeLogger.TIME_POS_MUSIC_ON_CONVERT);
		AppTimeLogger.computeTimeLen(uuid,
				AppTimeLogger.TIME_POS_NLP_ARRIVE_APP,
				AppTimeLogger.TIME_POS_MUSIC_ON_CONVERT,
				AppTimeLogger.TIME_LEN_ARRIVAL_CONVERT);
	}
	
	public static void onMusicParseEnd(String uuid){
		AppTimeLogger.putTime(uuid, AppTimeLogger.TIME_POS_MUSIC_CONVERT_FINISH);
		AppTimeLogger.computeTimeLen(uuid,
				AppTimeLogger.TIME_POS_MUSIC_ON_CONVERT,
				AppTimeLogger.TIME_POS_MUSIC_CONVERT_FINISH,
				AppTimeLogger.TIME_LEN_MUSIC_CONVERT);

	}
	
	public static void setInsType(String uuid, String insType){
		TimeLog timeLog = AppTimeLogger.getTimeLog(uuid);
		
		if (null != timeLog) {
			timeLog.setInsType(insType);
		}
	}
	
	public static void onTTSStart(String uuid){
		AppTimeLogger.putTime(uuid, AppTimeLogger.TIME_POS_TTS_START);
		AppTimeLogger.computeTimeLen(uuid,
				AppTimeLogger.TIME_POS_NLP_ARRIVE_APP,
				AppTimeLogger.TIME_POS_TTS_START,
				AppTimeLogger.TIME_LEN_ARRIVAL_TTS_START);
	}
	
	public static void onTTSPlayStart(String uuid){
		AppTimeLogger.putTime(uuid,
				AppTimeLogger.TIME_POS_TTS_PLAY_START);
		AppTimeLogger.computeTimeLen(uuid,
				AppTimeLogger.TIME_POS_NLP_ARRIVE_APP,
				AppTimeLogger.TIME_POS_TTS_PLAY_START,
				AppTimeLogger.TIME_LEN_ARRIVAL_TTS_PLAY_START);
	}
	
	public static void onTTSComplete(final String uuid) {
		AppTimeLogger.putTime(uuid,
				AppTimeLogger.TIME_POS_TTS_PLAY_FINISH);
		AppTimeLogger.computeTimeLen(uuid,
				AppTimeLogger.TIME_POS_TTS_PLAY_START,
				AppTimeLogger.TIME_POS_TTS_PLAY_FINISH,
				AppTimeLogger.TIME_LEN_TTS_PLAY);

		AppTimeLogger.putTime(uuid,
				AppTimeLogger.TIME_POS_TTS_FINISH);
		AppTimeLogger.computeTimeLen(uuid,
				AppTimeLogger.TIME_POS_TTS_START,
				AppTimeLogger.TIME_POS_TTS_FINISH,
				AppTimeLogger.TIME_LEN_TTS_TOTAL);

		AppTimeLogger.saveTimeLog(uuid);
	}
	
	public static void onPlayToneStart(String uuid){
		AppTimeLogger.putTime(uuid,
				AppTimeLogger.TIME_POS_TONE_PLAY_START);
		AppTimeLogger.computeTimeLen(uuid,
				AppTimeLogger.TIME_POS_NLP_ARRIVE_APP,
				AppTimeLogger.TIME_POS_TONE_PLAY_START,
				AppTimeLogger.TIME_LEN_ARRIVAL_TONE_PLAY_START);
	}
	
	public static void onPlayToneEnd(String uuid){
		AppTimeLogger.putTime(uuid,
				AppTimeLogger.TIME_POS_TONE_PLAY_FINISH);
		AppTimeLogger.computeTimeLen(uuid,
				AppTimeLogger.TIME_POS_TONE_PLAY_START,
				AppTimeLogger.TIME_POS_TONE_PLAY_FINISH,
				AppTimeLogger.TIME_LEN_TONE_PLAY);
	}
	
}
