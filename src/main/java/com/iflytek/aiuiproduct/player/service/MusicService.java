package com.iflytek.aiuiproduct.player.service;

import com.iflytek.aiui.assist.player.AIUIPlayerKitVer;
import com.iflytek.aiui.assist.player.AIUIPlayer.AIUIPlayerListener;
import com.iflytek.aiui.assist.player.AIUIPlayer.ContentType;
import com.iflytek.aiui.assist.player.AIUIPlayer.PlayItem;
import com.iflytek.aiui.servicekit.tts.SpeechSynthesizer;
import com.iflytek.aiuiproduct.player.PlayController;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 音乐播放服务。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月19日 下午3:56:46 
 *
 */
public class MusicService extends Service {
	public static final String ACTION = "com.iflytek.aiuiproduct.action.MusicService";
	
	public static final String KEY_CMD = "cmd";
	
	public static final String KEY_URL = "url";
	
	public static final String KEY_PLAY_STATE = "play_state";
	
	public static final String CMD_PLAY = "play";
	
	public static final String CMD_STOP = "stop";
	
	public static final String CMD_PAUSE = "pause";
	
	public static final String CMD_RESUME = "resume";

	private AIUIPlayerKitVer mAIUIPlayer;
	
	private AIUIPlayerListener mPlayerListener = new AIUIPlayerListener() {
		
		@Override
		public void onStop(PlayItem item) {
		}
		
		@Override
		public void onStart(PlayItem item) {
		}
		
		@Override
		public void onResume(PlayItem item) {
		}
		
		@Override
		public void onProgress(PlayItem item, int progress) {
		}
		
		@Override
		public void onPause(PlayItem item) {
		}
		
		@Override
		public void onError(PlayItem item, int error) {
			Intent intent = new Intent(PlayController.ACTION_MUSIC_PLAYSTATE);
			intent.putExtra(KEY_PLAY_STATE, "onError");
			
			sendBroadcast(intent);
		}
		
		@Override
		public void onCompleted(PlayItem item, boolean hasNext) {
			Intent intent = new Intent(PlayController.ACTION_MUSIC_PLAYSTATE);
			intent.putExtra(KEY_PLAY_STATE, "onCompleted");
			
			sendBroadcast(intent);
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mAIUIPlayer = new AIUIPlayerKitVer(MusicService.this, (SpeechSynthesizer) null);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String cmd = intent.getStringExtra(KEY_CMD);
		
		if (CMD_PLAY.equals(cmd)) {
			String playUrl = intent.getStringExtra(KEY_URL);
			mAIUIPlayer.playMusic(playUrl, mPlayerListener);
			sendMusicState(CMD_PLAY);
		} else if (CMD_STOP.equals(cmd)) {
			mAIUIPlayer.stop(ContentType.MUSIC);
			sendMusicState(CMD_STOP);
		} else if (CMD_PAUSE.equals(cmd)) {
			mAIUIPlayer.pause(ContentType.MUSIC);
			sendMusicState(CMD_PAUSE);
		} else if (CMD_RESUME.equals(cmd)) {
			mAIUIPlayer.resume(ContentType.MUSIC);
			sendMusicState(CMD_RESUME);
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	private void sendMusicState(String state) {
		Intent intent = new Intent(PlayController.ACTION_MUSIC_PLAYSTATE);
		intent.putExtra(KEY_PLAY_STATE, state);
		
		sendBroadcast(intent);
	}
}
