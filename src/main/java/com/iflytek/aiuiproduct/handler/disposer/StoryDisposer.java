package com.iflytek.aiuiproduct.handler.disposer;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;
import com.iflytek.aiuiproduct.player.entity.SongPlayInfo;

import android.content.Context;

public class StoryDisposer extends Disposer{
	private static final String KEY_URL = "playUrl";
	
	public StoryDisposer(Context context) {
		super(context);
	}
	
	@Override
	public void disposeResult(final SemanticResult semanticResult) {
		final List<SongPlayInfo> playList = new ArrayList<SongPlayInfo>();
		try {
			JSONArray result = semanticResult.getResult();
			if(result == null || result.length() ==0) {
				getPlayController().playText(semanticResult.getUUID(), semanticResult.getAnswerText());
			} else {
				for (int i = 0; i < result.length(); i++) {
					JSONObject newsJson = result.optJSONObject(i);

					String url = newsJson.getString(KEY_URL);

					SongPlayInfo playInfo = new SongPlayInfo();

					if(i == 0){
						playInfo.setAnswerText(semanticResult.getAnswerText());
					}else{
						playInfo.setAnswerText("***");
					}

					playInfo.setPlayUrl(url);

					playList.add(playInfo);
				}
				getPlayController().playSongList(semanticResult.getUUID(), playList,false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean canHandle(ServiceType type){
		if(type == ServiceType.STORY){
			return true;
		}else{
			return false;
		}
	}

}
