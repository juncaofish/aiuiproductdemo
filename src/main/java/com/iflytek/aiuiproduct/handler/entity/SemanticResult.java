package com.iflytek.aiuiproduct.handler.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 语义结果抽象类。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年6月29日 上午10:48:28
 *
 */
public class SemanticResult {
	public final static String KEY_SERVICE = "service";
	public final static String KEY_TEXT = "text";
	public final static String KEY_ANSWER = "answer";
	public final static String KEY_HISTORY = "history";
	public final static String KEY_PROMPT = "prompt";
	public final static String KEY_RESULT = "result";
	public final static String KEY_DIALOG_STAT = "dialog_stat";
	private final static String KEY_SEMANTIC = "semantic";
	public final static String KEY_SLOTS = "slots";
	public final static String KEY_CONTENT = "content";
	private final static String KEY_OPERATION = "operation";
	public final static String KEY_SID = "sid";
	public final static String KEY_UUID = "uuid";
	public final static String KEY_RC = "rc";
	public final static String KEY_EMOTION = "emotion";
	private final static String KEY_INTENT = "intent";
	private final static String KEY_DATA = "data";
	
	public final static String EMOTION_DEFAULT = "default";
	public final static String EMOTION_NEUTRAL = "neutral";
	public final static String EMOTION_ANGRY = "angry";


	protected String sid;
	protected String uuid;
	private ServiceType service;
	private String operation;
	private String answerText = "";
	private String emotion = "";
	private JSONObject semantic = null;
	private JSONObject data = null;
	private JSONObject json = null;
	
	public SemanticResult(String service, JSONObject json) {
		this.service = ServiceType.getServiceType(service);
		this.json = json;
		
		try {
			this.sid = json.getString(KEY_SID);
			this.uuid = json.getString(KEY_UUID);
			boolean isAIUI3_0 = json.has(KEY_OPERATION);
			if(isAIUI3_0){
				this.semantic = json.optJSONObject(KEY_SEMANTIC);
			}else{
				JSONObject fakeSemantic = json.getJSONArray(KEY_SEMANTIC).getJSONObject(0);
				JSONArray slots = fakeSemantic.optJSONArray(KEY_SLOTS);
				JSONObject fakeSlots = new JSONObject();
				for(int index=0;index<slots.length();index++){
					JSONObject item = slots.optJSONObject(index);
					fakeSlots.put(item.optString("name"), item.optString("value"));
				}
				fakeSemantic.put(KEY_SLOTS, fakeSlots);
				this.semantic = fakeSemantic;
			}
			this.operation = isAIUI3_0?
					json.getString(KEY_OPERATION):
					this.semantic.getString(KEY_INTENT);
			this.data = json.optJSONObject(KEY_DATA);
			
			if (json.has(KEY_ANSWER)) {
				JSONObject answerJson = json.getJSONObject(KEY_ANSWER);
				this.answerText = answerJson.optString(KEY_TEXT, "");
				this.emotion = answerJson.optString(KEY_EMOTION, "");
				
				if (EMOTION_DEFAULT.equals(emotion) 
						|| EMOTION_ANGRY.equals(emotion)) {
					this.emotion = "";
				}
			} else {
				this.answerText = "";
				this.emotion = "";
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public ServiceType getServiceType() {
		return service;
	}

	public String getAnswerText() {
		return answerText;
	}
	
	public String getEmotion() {
		return emotion;
	}

//	public JSONObject getJson() {
//		return json;
//	}
	
	public JSONObject getSemantic(){
		return semantic;
	}
	

	public JSONArray getResult() {
		if(data == null) {
			return null;
		}else {
			return data.optJSONArray(KEY_RESULT);
		}
	}
	
	public String getSid() {
		return sid;
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public String getOperation(){
		return operation;
	}
	
}
