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

/**
 * 新闻结果处理器。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年9月5日 下午4:58:10 
 *
 */
public class NewsDisposer extends Disposer {
	private static final String KEY_URL = "url";

	public NewsDisposer(Context context) {
		super(context);
	}
	
	@Override
	public void disposeResult(final SemanticResult semanticResult) {
		final List<SongPlayInfo> playList = new ArrayList<SongPlayInfo>();
		try {
			JSONArray result = semanticResult.getResult();
			if(result != null && result.length() != 0){
			    int size = result.length();
	            for (int i = 0; i < size; i++) {
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
			
			}else{
			    getPlayController().playText(semanticResult.getUUID(), semanticResult.getAnswerText());
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		/*getPlayController().playText(result.getUUID(), result.getAnswerText(), 
				false, new Runnable() {
			
			@Override
			public void run() {
				getPlayController().playURLList(result.getUUID(), playList);
			}
		});*/
	}
	
	public boolean canHandle(ServiceType type){
		if(type == ServiceType.NEWS){
			return true;
		}else{
			return false;
		}
	}
}
