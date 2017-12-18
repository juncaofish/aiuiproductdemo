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
import android.text.TextUtils;

public class JokeDisposer extends Disposer{

	public JokeDisposer(Context context) {
		super(context);
	}

	@Override
	public void disposeResult(SemanticResult semanticResult) {
		
		
		try {
			JSONArray result = semanticResult.getResult();
			if(result == null || result.length() == 0) return;
			JSONObject entry = result.getJSONObject(0);
			if(!TextUtils.isEmpty(entry.optString("content"))){
				String conent = entry.getString("content");
				String title = entry.optString("title");
				
				getPlayController().playText(semanticResult.getUUID(), 
						"请听笑话，" + title + "。" + conent, true, "", null);
			}else{
				List<SongPlayInfo> playList = new ArrayList<SongPlayInfo>();
				
				for(int index = 0; index < result.length(); index++){
					entry = result.getJSONObject(index);
					
					SongPlayInfo songPlayInfo = new SongPlayInfo();
					songPlayInfo.setPlayUrl(entry.optString("mp3Url"));
					songPlayInfo.setAnswerText("***");
					playList.add(songPlayInfo);
				}
				
				getPlayController().playURLList(semanticResult.getUUID(), playList);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public boolean canHandle(ServiceType type){
		if(type == ServiceType.JOKE){
			return true;
		}else{
			return false;
		}
	}

}
