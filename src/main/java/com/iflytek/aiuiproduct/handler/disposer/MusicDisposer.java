package com.iflytek.aiuiproduct.handler.disposer;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.aiui.utils.log.DebugLog;
import com.iflytek.aiuiproduct.constant.ProductConstant;
import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;
import com.iflytek.aiuiproduct.player.InsType;
import com.iflytek.aiuiproduct.player.entity.SongPlayInfo;
import com.iflytek.aiuiproduct.utils.AppTimeLogger;
import android.content.Context;
import android.content.Intent;

/**
 * 音乐结果处理器。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年6月21日 下午4:29:54 
 *
 */
public class MusicDisposer extends Disposer {
	private final static String INSTYPE_SLEEP = "sleep";
	private final static String INSTYPE_BROADCAST = "broadcast";
	private final static String INSTYPE_BROADCASTSINGER = "broadcastsinger";
	
	public MusicDisposer(Context context) {
		super(context);
	}

	@Override
	public void disposeResult(final SemanticResult result) {
		String operation = result.getOperation();
		
		if (OPERATION_INS.equals(operation)) {
			parseMusicCommand(result);
		} else {
			parseMusicResult(result);
		}
	}
	
	public boolean canHandle(ServiceType type){
		if(type == ServiceType.MUSICX){
			return true;
		}else{
			return false;
		}
	}
	


	public void parseMusicResult(SemanticResult result) {
		String uuid = result.getUUID();
		
		AppTimeLogger.onMusicParseStart(uuid);
		
		List<SongPlayInfo> songInfos = parseMusicJson(result);

		AppTimeLogger.onMusicParseEnd(uuid);

		if (null == songInfos || songInfos.size() <= 0) {
			getPlayController().playText(result.getUUID(), result.getAnswerText());
		} else {
			getPlayController().playSongList(result.getUUID(), songInfos, true);
		}
	}

	public void parseMusicCommand(SemanticResult result) {
		
		try {
			// 控制语义
			JSONObject semantic = result.getSemantic();
			String insType = semantic.getJSONObject(KEY_SLOTS).getString(KEY_INSTYPE);
			
			String uuid = result.getUUID();
			AppTimeLogger.setInsType(uuid, insType);
			
			DebugLog.LogD(TAG, "insType" + insType);
			
			if (INSTYPE_SLEEP.equals(insType)) {
				Intent intent = new Intent(ProductConstant.ACTION_SLEEP);
				intent.putExtra(SemanticResult.KEY_UUID, uuid);
				mContext.sendBroadcast(intent);
				return;
			} else if (INSTYPE_BROADCAST.equals(insType)) {
				getPlayController().playCurrentMusicInfo(result.getUUID());
				return;
			} else if (INSTYPE_BROADCASTSINGER.equals(insType)) {
				getPlayController().playCurrentMusicSinger(result.getUUID());
				return;
			} else {
				getPlayController().onMusicCommand(uuid, InsType.parseInsType(insType));
				return;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private List<SongPlayInfo> parseMusicJson(SemanticResult semanticResult) {

		try {
			JSONArray result = semanticResult.getResult();
			
			if (null != result && result.length() > 0) {
				List<SongPlayInfo> songPlayInfos = new ArrayList<SongPlayInfo>();

				for (int i = 0; i < result.length(); i++) {

					JSONObject songInfoJson = result.getJSONObject(i);
					
					
					SongPlayInfo playInfo = new SongPlayInfo();
					
					playInfo.setSongName(songInfoJson.optString("songname"));
					playInfo.setPlayUrl(songInfoJson.optString("audiopath"));
					
					JSONArray singernames = songInfoJson.optJSONArray("singernames");
					if (singernames != null && singernames.length() > 0) {
						StringBuffer singerName = new StringBuffer();
						for (int j = 0; j < singernames.length(); j++) {
							singerName.append(singernames.optString(j) + ",");
						}
						
						playInfo.setSingerName(singerName.toString());
					}

					if(i == 0){
						playInfo.setAnswerText(semanticResult.getAnswerText());
					}
					
					songPlayInfos.add(playInfo);
				}
				return songPlayInfos;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
