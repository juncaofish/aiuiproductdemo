package com.iflytek.aiuiproduct.player;

import java.util.List;

import com.iflytek.aiui.assist.player.AIUIPlayer.AIUIPlayerListener;
import com.iflytek.aiui.assist.player.AIUIPlayer.ContentType;
import com.iflytek.aiui.assist.player.AIUIPlayer.PlayItem;
import com.iflytek.aiui.assist.player.AIUIPlayer.PlayState;
import com.iflytek.aiui.assist.player.AIUIPlayerKitVer;
import com.iflytek.aiui.servicekit.tts.SpeechSynthesizer;
import com.iflytek.aiui.utils.log.DebugLog;
import com.iflytek.aiuiproduct.app.DemoConfig;
import com.iflytek.aiuiproduct.constant.ProductConstant;
import com.iflytek.aiuiproduct.player.entity.SongPlayInfo;
import com.iflytek.aiuiproduct.player.service.MusicService;
import com.iflytek.aiuiproduct.utils.AppTimeLogger;
import com.iflytek.aiuiproduct.utils.TimeTrigger;
import com.iflytek.aiuiproduct.utils.TimeTrigger.TriggerListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

/**
 * 播放控制器，用来控制内容（文本、音乐链接、音频文件）的播放。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年4月7日 下午5:41:22
 *
 */
public class PlayController {
	private static final String TAG = ProductConstant.TAG;
	
	/** 提示音所在目录 **/
    private final static String WAV_PATH = "wav/";
	private final static String SEARCH_RESULT = WAV_PATH + "search_result.mp3";
	// 按值读数字
	public final static int RDN_VALUE = 1;
	// 按字符读数字
	public final static int RDN_STRING = 2;
	// 十秒的超时时间，用于其他服务之后恢复播放音乐
	public final static  int MUSIC_RECOVER_TIMEOUT = 10000;
	
	public static final String ACTION_MUSIC_PLAYSTATE = "com.iflytek.aiuiproduct.action.MusicPlayState";
	
	public static final String KEY_EMOTION = "emot";
	
	private static PlayController instance;

	private Context mContext;

	// 文本合成对象
	private SpeechSynthesizer mTTS;
	
	// 播放监听
	private PlayControllerListener mPlayControllerListener;

	// 音量控制对象
	private VolumeManager mVolumeManager;

	// AIUI播放器
	private AIUIPlayerKitVer mAIUIPlayer;
	
	private MusicPlayer mMusicPlayer;

	// 标志是否播放合成以及音乐
	private boolean isVoiceEnable = true;
	
	// 时间触发器，用于一段时间无交互恢复播放音乐
	private static TimeTrigger musicRecoverTrigger;
	
	private boolean isTTS = false;
	
	private VolControlThread mVolControlThread;
	private int mDesVolume;
	
	public interface PlayControllerListener {
		public void onCompleted(PalyControllerItem playItem, boolean hasNext);
		public void onError(PalyControllerItem playItem, int errorCode);
		public void onPause(PalyControllerItem playItem);
		public void onProgress(PalyControllerItem playItem, int progress);
		public void onResume(PalyControllerItem playItem);
		public void onStart(PalyControllerItem playItem);
		public void onStop(PalyControllerItem playItem);
	}
	
	private PlayController(Context context) {
		mContext = context;
		mTTS = SpeechSynthesizer.createSynthesizer(mContext, null);
		mAIUIPlayer = new AIUIPlayerKitVer(mContext, mTTS);
		mVolumeManager = new VolumeManager(mContext);

		mDesVolume = mVolumeManager.getCurrentVolume();
		mVolControlThread = new VolControlThread();
		mVolControlThread.start();
		
		mMusicPlayer = new MusicPlayer(context);
		
		DebugLog.LogD(TAG, "init PlayController Success");
		
		musicRecoverTrigger = new TimeTrigger(MUSIC_RECOVER_TIMEOUT);
		musicRecoverTrigger.setListener(new TriggerListener() {
			
			@Override
			public void onTrigger() {
				mMusicPlayer.resumeMusic();
			}
		});
	}

	public synchronized static PlayController getInstance(Context context) {
		if (null == instance) {
			instance = new PlayController(context);
		}
		
		return instance;
	}
	
	/**
	 * 是否播放合成和音乐
	 * @param isVoiceEnable true为播放 false为不播放
	 * @param arg1  扩展参数 比如音乐播放 合成不播放使用这两个参数的四种组合 
	 */
	public void setPlayVoiceEnable(boolean isVoiceEnable, boolean arg1) {
		this.isVoiceEnable = isVoiceEnable;
		
		if (!isVoiceEnable) {
			stopTTS();
			stopMusic();
		}
	}
	
	public void setPalyControllerListener(PlayControllerListener playControllerListener) {
		mPlayControllerListener = playControllerListener;
	}
	
	public void justTTS(String uuid, String text){
		justTTS(uuid, text, false, "", null);
	}

	public void justTTS(String uuid, String text, String emot){
		justTTS(uuid, text, false, emot, null);
	}
	
	public void justTTS(String uuid, String text, boolean readDigit, Runnable runnable) {
		justTTS(uuid, text, false, "", runnable);
	}
	
	public void justTTS(String uuid, String text, boolean readDigit, String emot, 
			Runnable runnable) {
		if(readDigit){
			playText(uuid, text, false, RDN_VALUE, emot, runnable);
		}else{
			playText(uuid, text, false, RDN_STRING, emot, runnable);
		}
	}
	
	public void playText(String uuid, String text) {
		playText(uuid, text, false, "", null);
	}

	public void playText(String uuid, String text, String emot){
		playText(uuid, text, false, emot, null);
	}
	
	public void playText(String uuid, String text, boolean readDigit, String emot, 
			Runnable runnable){
		if(readDigit){
			playText(uuid, text, true, RDN_VALUE, emot, runnable);
		}else{
			playText(uuid, text, true, RDN_STRING, emot, runnable);
		}
	}
	
	public void stopTTS(){
		DebugLog.LogD(TAG, "stop tts text");
		isTTS = false;
		mAIUIPlayer.stop(ContentType.TEXT);
	}

	public void playTone(String uuid, String assetFilePath){
		playTone(uuid, assetFilePath, null);
	}
	
	public void playTone(final String uuid, String assetFilePath, final Runnable runnable){
		if(!isVoiceEnable){
			if(runnable != null) {
				runnable.run();
			}
			return;
		}
		
		AppTimeLogger.onPlayToneStart(uuid);
		TonePlayer.playTone(mContext, assetFilePath, new Runnable() {
			
			@Override
			public void run() {
				AppTimeLogger.onPlayToneEnd(uuid);
				if(runnable != null) {
					runnable.run();
				}
			}
		});
	}
	
	
	public void playCurrentMusicInfo(String uuid){
		playText(uuid, mMusicPlayer.getCurrentMusicSinger() + "的" + mMusicPlayer.getCurrentMusicName(), 
				false, "", null);
	}
	
	public void playCurrentMusicSinger(String uuid){
		playText(uuid, mMusicPlayer.getCurrentMusicSinger(), false, "", null);
	}
	
	public void setMaxVolum(){
		setDesVol(VolumeManager.MAX_VOLUME_LEVEL);
        recoverVol();
	}
	
	public int getCurrentVolum(){
	    return mVolumeManager.getCurrentVolume();
	}
	
	public boolean isCurrentPlayMusic(){
		return mMusicPlayer.isCurrentPlaying();
	}

	public boolean isCurrentTTS() {
        return isTTS;
    }
	
	//渐渐恢复音量至原始状态
	public void recoverVol(){
	    mVolControlThread.start(mDesVolume);
	}
	
	//渐渐降低音量
	public void changeVolToLower(){
	    if(!mVolControlThread.isRunning()){
	        mDesVolume = mVolumeManager.getCurrentVolume();
	    }
	    mVolControlThread.start(mDesVolume  - 5);
	}
	
	//设置全局目标音量
	private void setDesVol(int vol){
	    mDesVolume = Math.max(vol,  VolumeManager.MIN_VOLUME_LEVEL);
	}
	
	//设置当前音量为全局目标音量
    public void setCurVolToDesVol(){
        mDesVolume = mVolumeManager.getCurrentVolume();
    }
    
	private class VolControlThread extends Thread{
	    private int desVol = mVolumeManager.getCurrentVolume();
	    private boolean isRunning = false;
	    
	    public boolean isRunning() {
	        return isRunning;
	    }
	    
	    public void start(int destVol){
	        desVol = Math.max(destVol,  VolumeManager.MIN_VOLUME_LEVEL);
	        isRunning = true;
	    }
	    
	    @Override
	    public void run() {
	        super.run();
	        while(true){
	            int curVol = mVolumeManager.getCurrentVolume();
	            if(curVol != desVol && isRunning){
                    isRunning = true;
                    mVolumeManager.plusVolume(curVol > desVol ? -1 : 1);
	            }else{
	                isRunning = false;
	            }
	            try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
	        }
	    }
	}
	
	/**
	 * 音乐播放函数
	 * 
	 * @param songInfoList 歌曲信息列表
	 */
	public void playSongList(String uuid, List<SongPlayInfo> songInfoList, boolean isMusic) {
		mMusicPlayer.playURLList(uuid, songInfoList, isMusic);
	}

	public void playURLList(String uuid, List<SongPlayInfo> newsList) {
		mMusicPlayer.playURLList(uuid, newsList, false);
	}

	/**
	 * 音乐控制命令回调接口
	 */
	public void onMusicCommand(String uuid, InsType insType) {
		mMusicPlayer.onMusicCommand(uuid, insType);
	}

	public void stopMusic(){
		cancelAutoResumeTrigger();
		mMusicPlayer.stopMusic();
	}

	private void cancelAutoResumeTrigger(){
		musicRecoverTrigger.cancel();
	}
	

	public void playText(String uuid, String text, boolean autoResume, int readDigit, String emotion, final Runnable runnable){
		// 取消恢复音乐的休眠定时
		musicRecoverTrigger.cancel();
		
		if (null != mTTS && DemoConfig.getDemoConfig().isEmotionTTS()) {
			mTTS.setParameter(KEY_EMOTION, emotion);
		}
		
		if(autoResume && mMusicPlayer.isCurrentPlaying()){
			Runnable temp = new Runnable() {
				public void run() {
					musicRecoverTrigger.start();
					
					if(runnable != null){
						runnable.run();
					}
				}
			};
			playText(uuid, text, readDigit, temp);
		}else{
			playText(uuid, text, readDigit, runnable);
		}
	}

	// 播放文本
	private void playText(String uuid, String text, int readDigit, final Runnable runnable) {
		mTTS.setParameter("rdn", readDigit + "");
		String speakText = TtsTextReplacer.replace(text);

		stopTTS();
		startTTS(uuid, speakText, runnable);
	}

	private void startTTS(final String uuid, String text, final Runnable runnable) {
		if (!isVoiceEnable || TextUtils.isEmpty(text)) {
			new Thread(runnable).run();
			return;
		}
		
		AppTimeLogger.onTTSStart(uuid);
		
		TonePlayer.stopPlay();
		
		if (null == mAIUIPlayer) {
			return;
		}
		mAIUIPlayer.playText(text, new AIUIPlayerListener() {
			
			@Override
			public void onStop(PlayItem arg0) {
			}
			
			@Override
			public void onStart(PlayItem arg0) {
				AppTimeLogger.onTTSPlayStart(uuid);
				isTTS = true;
			}
			
			@Override
			public void onResume(PlayItem arg0) {
				isTTS = true;
			}
			
			
			@Override
			public void onProgress(PlayItem arg0, int arg1) {
			}
			
			@Override
			public void onPause(PlayItem arg0) {
			    isTTS = false;
			}
			
			@Override
			public void onError(PlayItem playItem, int errorCode) {
				PalyControllerItem item = new PalyControllerItem();
				
				item.type = PlayContentType.TEXT;
				if (null != playItem) {
					item.content = playItem.content;
				}

				mPlayControllerListener.onError(item, errorCode);

			}
			
			@Override
			public void onCompleted(PlayItem arg0, boolean arg1) {
				AppTimeLogger.onTTSComplete(uuid);
				isTTS = false;
				if (null != runnable) {
					runnable.run();
				}
			}
		});
	}

	// 停止音乐播放控制，销毁所占资源
	public void stopPlayControl() {

	    stopTTS();
	    stopMusic();
	}
	
	public void destroy() {
		mMusicPlayer.destroy();
	}
	
	class MusicPlayer {
		private final static String VOLUME_PATH = WAV_PATH + "volume.wav";
		
		private Context mContext;
		// 要播放的歌曲信息
		private List<SongPlayInfo> mSongInfoList;

		// 当前播放歌曲信息
		private SongPlayInfo mSongInfo;
		
		// 播放的第几个歌曲
		private int playIndex = 0;

		// 歌单歌曲总数
		private int playCount = 0;
		
		private boolean isPlayMusic = true;

		// 标示音乐播放状态
		private PlayState mPlaySate;
		
		private HandlerThread mTonePlayThread;
		
		private Handler mTonePlayHandler;
		
		private BroadcastReceiver mPlayStateReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String playState = intent.getStringExtra(MusicService.KEY_PLAY_STATE);
				
				if ("onCompleted".equals(playState)) {
					DebugLog.LogD(TAG, "播放停止");
					// 当播放停止时自动播放下一首
					next();
				} else if ("onError".equals(playState)) {
					justTTS("", "内容开小差，换首别的吧", "");
				}
			}
			
		};
		
		public MusicPlayer(Context context){
			mContext = context;
			
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_MUSIC_PLAYSTATE);
			mContext.registerReceiver(mPlayStateReceiver, filter);
			
			mTonePlayThread = new HandlerThread("TonePlayThread");
			mTonePlayThread.start();
			mTonePlayHandler = new Handler(mTonePlayThread.getLooper());
			
		}
		
		public void onMusicCommand(String uuid, InsType insType){
			if(insType == null) return;
			
			switch (insType) {
				case PAST: {
					if (isPlayMusic) {
					    recoverVol();
						previous();
					}
				} break;
				
				case LIKE:
				{
					justTTS(uuid, "还是我最懂你   ", false, new Runnable() {
						
						@Override
						public void run() {
							resumeMusic();
						}
					});
				}break;
				
				case NEXT: {
					if (isPlayMusic) {
					    recoverVol();
						next();
					}
				} break;
				
				case DISLIKE:
				case PAUSE: {
				    recoverVol();
					pauseMusic();
				} break;
				
				case REPLAY: {
					resumeMusic();
				} break;
				
				case REPEAT: {
					replayLastResult();
				} break;
				
				case VOLMAX: {
					if (null != mVolumeManager) {
				        setDesVol(VolumeManager.MAX_VOLUME_LEVEL);
				        recoverVol();
					}
				} break;
				
				case VOLMIN: {
					if (null != mVolumeManager) {
					    setDesVol(VolumeManager.MIN_VOLUME_LEVEL);
					    recoverVol();
					}
				} break;
				case VOLMID: {
					if (null != mVolumeManager) {
					    setDesVol(VolumeManager.MID_VOLUME_LEVEL);
					    recoverVol();
					}
				} break;
				
				case VOLPLUS: {
				    playPromptTone(uuid, true);
				} break;
				
				case VOLMINUS: {
				    playPromptTone(uuid, false);
				} break;
				
				case REPLAYANSWER: {
					replayLastResult();
				} break;
				
				default:
					break;
			}
			
		}
		
		public boolean isCurrentPlaying(){
			return mPlaySate == PlayState.MUSIC_PLAYING;
		}

		// 上一首
		private void previous() {
			if (0 == playIndex) {
				startTTS(null, "当前是第一首", new Runnable() {
					
					@Override
					public void run() {
						resumeMusic();
					}
			   });
			} else {
				playIndex--;
				playItem("");
			}
		}

		// 暂停
		private void pauseMusic() {
			cancelAutoResumeTrigger();
			
			if (PlayState.MUSIC_PLAYING == mPlaySate) {
				DebugLog.LogD(TAG, "music player pause");
				sendMusicCmd(MusicService.CMD_PAUSE, "");
				mPlaySate = PlayState.MUSIC_PAUSE;
			}
		}

		// 恢复播放
		private void resumeMusic() {
			if (PlayState.STOP != mPlaySate) {
				DebugLog.LogD(TAG, "music player resume");
				stopTTS();
				sendMusicCmd(MusicService.CMD_RESUME, "");
				mPlaySate = PlayState.MUSIC_PLAYING;
			}
		}
		
		public void stopMusic(){
			DebugLog.LogD(TAG, "stop music player");
			
			mSongInfoList = null;
			mSongInfo = null;
			playIndex = 0;
			playCount = 0;
			
			sendMusicCmd(MusicService.CMD_STOP, "");
			mPlaySate = PlayState.STOP;
		}

		// 下一首
		private void next() {
			playIndex += 1;
			
			if(playIndex == playCount){
				if (isPlayMusic) {
					justTTS("", "当前歌单没有更多歌曲，播放停止", "");
				} else {
					justTTS("", "内容播报完毕", "");
				}
				return;
			}
			
			playItem("");
		}

		// 播放
		public void playURLList(final String uuid, List<SongPlayInfo> songList, boolean isMusic) {
			if (!isVoiceEnable || null == songList || songList.isEmpty()) {
				return;
			}
			DebugLog.LogD(TAG, "playItem");
			
			//停止当前所有播放
			TonePlayer.stopPlay();
			stopTTS();
			stopMusic();
			
			isPlayMusic = isMusic;
			mSongInfoList = songList;
			playCount = mSongInfoList.size();
			playIndex = 0;
		
			// 取消恢复音乐的休眠定时
			cancelAutoResumeTrigger();
		
			// 播放搜索到结果的提示音
			DebugLog.LogD(TAG, "SEARCH_RESULT");
		    playTone(uuid, SEARCH_RESULT, new Runnable() {
                public void run() {
                    playItem(uuid);
                }
            });
			
		}

		/**
		 * 播报歌手+歌曲名+播放音乐
		 */
		private void playItem(String uuid) {
			
			if (null != mSongInfoList && mSongInfoList.size() >= 1) {
				// 获得歌曲的信息
				mSongInfo = mSongInfoList.get(playIndex);
				String songName = mSongInfo.getSongName();
				String singerName = mSongInfo.getSingerName();
				final String playUrl = mSongInfo.getPlayUrl();
		        
				
				if(isPlayMusic && (TextUtils.isEmpty(songName) || TextUtils.isEmpty(playUrl))){
					startTTS(uuid, "歌曲信息有误，换一首吧", null);
					return;
				}
				
				if(TextUtils.isEmpty(playUrl)){
					return;
				}
				
				
				String answerText = mSongInfo.getAnswerText();
				String ttsText = "";
		        
				if (TextUtils.isEmpty(answerText)) {
		        	ttsText = singerName + "的" + songName;
				} else {
					ttsText = answerText;
				}
				justTTS(uuid, ttsText, true, new Runnable() {
					
					@Override
					public void run() {
						playMusicUrl(playUrl);
					}
				});
			}
		}

		private void replayLastResult(){
			mContext.sendBroadcast(new Intent(ProductConstant.ACTION_REPLAY));
		}

		/**
		 * 获得当前正在播放歌曲的名字
		 * 
		 * @return歌曲名字
		 */
		public String getCurrentMusicName() {
			if (null != mSongInfo) {
				String songName = mSongInfo.getSongName();
				return songName;
			}
			return "";
		}

		/**
		 * 获得当前正在播放歌曲的歌手名字
		 * 
		 * @return
		 */
		public String getCurrentMusicSinger() {
			if (null != mSongInfo) {
				String singerName = mSongInfo.getSingerName();
				return singerName;
			}
			return "";
		}
				
		/**
		 * 播放音量增减提示音
		 */
		private void playPromptTone(final String uuid, final boolean isIncrease) {
			// 连续播放三次提示音
			DebugLog.LogD(TAG, "play volum " + isIncrease + " prompt before");
			

			playSoundPool(uuid);
			
			mTonePlayHandler.postDelayed(new Runnable() {
		
					@Override
					public void run() {

						if (isIncrease) {
						    setDesVol(mDesVolume + 2);
						    recoverVol();
						} else {
						    setDesVol(mDesVolume - 2);
						    recoverVol();
						}
						
						playSoundPool(uuid);
						DebugLog.LogD(TAG, "play volum " + isIncrease + " prompt after");
					}
			}, 800);
			
		}

		private void playSoundPool(String uuid) {
			AppTimeLogger.onPlayToneStart(uuid);
			TonePlayer.playUseSoundPool(mContext, VOLUME_PATH, 3);
			AppTimeLogger.onPlayToneEnd(uuid);
		}

		/**
		 * 播放音乐
		 * @param playUrl 播放音乐的连接或者文件的路径
		 * @param playerListener 播放监听
		 */
		private void playMusicUrl(String playUrl) {
			DebugLog.LogD(TAG, "Start play music " + playUrl);
			
			sendMusicCmd(MusicService.CMD_PLAY, playUrl);
			mPlaySate = PlayState.MUSIC_PLAYING;
		}

		private void sendMusicCmd(String cmd, String url) {
			Intent intent = new Intent(MusicService.ACTION);
			intent.putExtra(MusicService.KEY_CMD, cmd);
			
			if (!TextUtils.isEmpty(url)) {
				intent.putExtra(MusicService.KEY_URL, url);
			}
			
			mContext.startService(intent);
		}
		
		public void destroy(){
			mContext.unregisterReceiver(mPlayStateReceiver);
		}
		
	}


	enum PlayContentType {
		MUSIC,
		TEXT
	}


	public class PalyControllerItem {
		// 内容类型
		public PlayContentType type;
				
		// 播放内容
	    public String content;
	}
}

