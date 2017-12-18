package com.iflytek.aiuiproduct.handler.disposer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;
import com.iflytek.aiuiproduct.utils.AlarmManagerUtil;
import com.iflytek.aiuiproduct.utils.DateTimeUtil;
import com.iflytek.aiuiproduct.utils.IdUtil;


public class SnoozeDisposer extends Disposer {

	private static final String TAG = "SnoozeDisposer";
	
	private static String KEY_ANSWER = "answer";
	private static String KEY_TEXT = "text";
	private static String KEY_CONTENT = "content";
	private static String KEY_DATETIME = "datetime";
	private static String KEY_DATE = "date";
	private static String KEY_TIME = "time";
	
	public SnoozeDisposer(Context context) {
		super(context);
	}
	
	@Override
	public void disposeResult(SemanticResult result) {
		try {
			String answer = result.getAnswerText();
			String content = result.getSemantic().getJSONObject(KEY_SLOTS).optString(KEY_CONTENT, "");
			getPlayController().playText(result.getUUID(), answer + content, true, null , null);
			
			JSONObject dateTime = result.getSemantic().getJSONObject(KEY_SLOTS).getJSONObject(KEY_DATETIME);
			String date = dateTime.getString(KEY_DATE);
			String time = dateTime.getString(KEY_TIME);
			
			int[] ymd = DateTimeUtil.getClockTime(date, "-");
			int[] hsm = DateTimeUtil.getClockTime(time, ":");
			
			AlarmManagerUtil.setAlarm(mContext, IdUtil.getAlarmId("alarm"), ymd[0], ymd[1], ymd[2], hsm[0], hsm[1], hsm[2], content);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean canHandle(ServiceType type) {
		if (ServiceType.SCHEDULEX.equals(type)) {
			return true;
		}
		return false;
	}
}
