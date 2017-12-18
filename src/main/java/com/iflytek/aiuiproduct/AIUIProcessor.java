package com.iflytek.aiuiproduct;

import java.util.Iterator;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.aiui.AIUIErrorCode;
import com.iflytek.aiui.servicekit.AIUIAgent;
import com.iflytek.aiui.servicekit.AIUIConstant;
import com.iflytek.aiui.servicekit.AIUIEvent;
import com.iflytek.aiui.servicekit.AIUIListener;
import com.iflytek.aiui.servicekit.AIUIMessage;
import com.iflytek.aiui.utils.log.DebugLog;
import com.iflytek.aiuiproduct.app.DemoConfig;
import com.iflytek.aiuiproduct.constant.ProductConstant;
import com.iflytek.aiuiproduct.handler.AsrResultHandler;
import com.iflytek.aiuiproduct.handler.SemanticResultHandler;
import com.iflytek.aiuiproduct.player.InsType;
import com.iflytek.aiuiproduct.player.PlayController;
import com.iflytek.aiuiproduct.player.PlayControllerListenerAdapter;
import com.iflytek.aiuiproduct.player.PlayController.PalyControllerItem;
import com.iflytek.aiuiproduct.utils.AppTimeLogger;
import com.iflytek.aiuiproduct.utils.AppTimeLogger.TimeLog;
import com.iflytek.aiuiproduct.utils.AppTimeLogger.TimeLogSaveListener;
import com.iflytek.aiuiproduct.utils.ConfigUtil;
import com.iflytek.aiuiproduct.utils.DevBoardControlUtil;
import com.iflytek.aiuiproduct.utils.FileUtil;
import com.iflytek.aiuiproduct.utils.NetworkUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

/**
 * AIUI处理类
 * @author PR
 *
 */
public class AIUIProcessor extends PlayControllerListenerAdapter implements AIUIListener  {
		private static final String TAG = ProductConstant.TAG;
		
		private final static String WAV_PATH = "wav/";
		private final static String START_SUCCESS = WAV_PATH + "start_success.mp3";
		private final static String TONE_WRONG_APPID = WAV_PATH + "wrong_appid.mp3";
		
		private final static String XIAOAI_GREETING_EWZN = WAV_PATH + "xiaoai_greeting_ewzn.mp3";
		private final static String XIAOAI_GREETING_GS = WAV_PATH + "xiaoai_greeting_gs.mp3";
		private final static String XIAOAI_GREETING_SS = WAV_PATH + "xiaoai_greeting_ss.mp3";
		private final static String XIAOAI_GREETING_WTZN = WAV_PATH + "xiaoai_greeting_wtzn.mp3";
		private final static String XIAOAI_GOODBYE_NWZL = WAV_PATH + "xiaoai_goodbye_nwzl.mp3";
		
		// 唤醒后播放的欢迎音频
		private final static String[] WAKE_UP_TONES = {
			XIAOAI_GREETING_EWZN,		// 嗯，我在呢
			XIAOAI_GREETING_GS,			// 干啥
			XIAOAI_GREETING_SS,			// 啥事
			XIAOAI_GREETING_WTZN 		// 我听着呢
		};
		
//		private final static String[] WAKE_UP_TONES_TTS = {
//			"我在呢",
//			"干啥",
//			"啥事",
//			"我听着呢"
//		};
		
		private final static String GRAMMAR_FILE_PATH = "grammar/grammar.bnf";
	
		private AIUIAgent mAIUIAgent;
	
		private Context mContext;
		// 音乐文本播放控制对象
		private PlayController mPlayController;



		private int mCurrentState = AIUIConstant.STATE_IDLE;

		private boolean mIsOneshotMode = false;

		//导致休眠的错误码
		private int mSleepErrorCode = 0;
		//语义结果处理
		private SemanticResultHandler mSemanticHandler;
		//离线命令词处理
		private AsrResultHandler mAsrHandler;
		// 休眠的广播接收者
		private BroadcastReceiver mSleepReceiver;
		//音量变化广播接受者
		private BroadcastReceiver mVolChangedReceiver;
		//声音控制广播
		private BroadcastReceiver mVoiceCtrReceiver;
		
		public AIUIProcessor(Context context){
			mContext = context;

			// 关闭唤醒方向指示灯
			DevBoardControlUtil.sleepLight();

			// 设置WIFI指示灯
			DevBoardControlUtil.wifiStateLight(NetworkUtil.isNetworkAvailable(mContext));

			// 设置耗时日志监听器
			AppTimeLogger.setTimeLogSaveListener(mLogSaveListener);

			mPlayController = PlayController.getInstance(mContext);
			mPlayController.setPalyControllerListener(this);
			mPlayController.setMaxVolum();

			mSemanticHandler = new SemanticResultHandler(mContext);
			mAsrHandler = new AsrResultHandler(mContext);
						
			registerReceiver();
			
			String micType = ConfigUtil.getServiceInfo(context, "mic_type");
			if(!TextUtils.isEmpty(micType)){
				DevBoardControlUtil.setChannel(Integer.valueOf(
						micType.substring(0, 1)));
			}
		}
		
		
		
		public void setAgent(AIUIAgent agent){
			mAIUIAgent = agent;
		}
		
		private void registerReceiver(){
			mSleepReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
				    resetWakeup(false);
				}

			};
			mContext.registerReceiver(mSleepReceiver,  new IntentFilter(ProductConstant.ACTION_SLEEP));
		
			mVoiceCtrReceiver = new BroadcastReceiver(){
				@Override
				public void onReceive(Context context, Intent intent) {
					String playMode = intent.getStringExtra("play_mode");
					if(playMode.equals("enable")){
						mPlayController.setPlayVoiceEnable(true, true);
					}else if(playMode.equals("disable")){
						mPlayController.setPlayVoiceEnable(false, true);
					}
				}	

			};
			mContext.registerReceiver(mVoiceCtrReceiver, new IntentFilter(ProductConstant.ACTION_VOICE_CTRL));
			
			mVolChangedReceiver = new BroadcastReceiver() {
                
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(!ismIsWakeUp()){
                        mPlayController.setCurVolToDesVol();
                    }
                }
            };
            mContext.registerReceiver(mVolChangedReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
		}
		
		private TimeLogSaveListener mLogSaveListener = new TimeLogSaveListener() {
		
			@Override
			public void onSave(TimeLog log) {
				if (null != mAIUIAgent) {
					JSONObject jsonLog = log.toJson();
		
					AIUIMessage logMsg = new AIUIMessage(AIUIConstant.CMD_SEND_LOG, 0, 0, 
							jsonLog.toString(), null);
					mAIUIAgent.sendMessage(logMsg);
		
					DebugLog.LogD(TAG, "saveTimeLog");
				}
			}
		};
	
		//AIUI事件处理方法
		@Override
		public void onEvent(AIUIEvent event) {

			switch (event.eventType) {
			case AIUIConstant.EVENT_BIND_SUCCESS:
			{
				DebugLog.LogD(TAG, "EVENT_BIND_SUCESS");

				mPlayController.playTone("", START_SUCCESS);
					            
				// 关闭唤醒方向指示灯
	            DevBoardControlUtil.sleepLight();

	            // 设置WIFI指示灯
	            DevBoardControlUtil.wifiStateLight(NetworkUtil.isNetworkAvailable(mContext));
	            
			} break;

			case AIUIConstant.EVENT_WAKEUP: 
			{
				DebugLog.LogD(TAG, "EVENT_WAKEUP");
				
				processWakeup(event);
			} break;

			case AIUIConstant.EVENT_SLEEP: 
			{
				DebugLog.LogD(TAG, "EVENT_SLEEP");
				
				mCurrentState = AIUIConstant.STATE_READY;
				if(mSleepErrorCode != 0){
				    
					mPlayController.justTTS("", getErrorTip(mSleepErrorCode), false, new Runnable() {
						
						@Override
						public void run() {
							DevBoardControlUtil.sleepLight();
						}
					});
					mSleepErrorCode = 0;
				}else{
				    mPlayController.recoverVol();
					// 正在播放音乐且为自动休眠，直接熄灯
					if ((mPlayController.isCurrentPlayMusic()  || mPlayController.isCurrentTTS()) && event.arg1 == 0) {
						DevBoardControlUtil.sleepLight();
					} else {
					    if(event.arg1 == 0){
					        justLightOff("");
					    }else{
					        sayGoodbyeThenSleep("");
					    }
					}
				}
			} break;

			case AIUIConstant.EVENT_RESULT: 
			{
				DebugLog.LogD(TAG, "EVENT_RESULT");
				
				if (!mIsOneshotMode && !ismIsWakeUp()) {
					break;
				}

				processResult(event);
			} break;

			case AIUIConstant.EVENT_VAD:
			{
				if(DemoConfig.getDemoConfig().isVolumeFollowVad()) {
					processVolumeFollowVad(event);
				}
			} break;

			case AIUIConstant.EVENT_ERROR: 
			{
				int errorCode = event.arg1;
				processError(errorCode);
			} break;

			case AIUIConstant.EVENT_STATE: 
			{
				int serviceState = event.arg1;
				if (AIUIConstant.STATE_IDLE == serviceState) {
				    DebugLog.LogD(TAG, "STATE_IDLE");
                    DevBoardControlUtil.appidErrorLight(false);
                } else if (AIUIConstant.STATE_READY == serviceState) {
                    DebugLog.LogD(TAG, "STATE_READY");
                    DevBoardControlUtil.sleepLight();
					//第一次进入ready状态后构建离线语法，bind_success时AIUI可能并未处于就绪状态
                    if(mCurrentState == AIUIConstant.STATE_IDLE){
						buildGrammar();
					}
                } else if (AIUIConstant.STATE_WORKING == serviceState) {
                    DebugLog.LogD(TAG, "STATE_WORKING");
                }
                mCurrentState = serviceState;
			} break;
			
			case AIUIConstant.EVENT_CMD_RETURN: {
				processCmdReturnEvent(event);
			} break;
			
			case AIUIConstant.EVENT_PRE_SLEEP: {
			    if (!(mPlayController.isCurrentPlayMusic() || mPlayController.isCurrentTTS())){
			        justSayGoodbye("");
			    }
			}break;
			
			default:
				break;
			}
		}

		private void handleParamsReturn(AIUIEvent event){
			String params = event.info;
			Log.i(TAG, "params: " + params);
			try{
				JSONObject json = new JSONObject(params);
				Iterator<?> allParamKeys = json.keys();
				while (allParamKeys.hasNext()) {
					String paramsType = allParamKeys.next().toString();
					String paramsKey = json.opt(paramsType) + "";
					if(paramsType.equals(AIUIConstant.KEY_INTERACT_MODE)){
						handleInteractModeParams(paramsKey);
					}
				}
			}catch (JSONException e){
				e.printStackTrace();
			}
		}

		private void handleInteractModeParams(String value){
			if(value != null){
				if(value.equals(AIUIConstant.INTERACT_MODE_ONESHOT)){
					mIsOneshotMode = true;
				}else if(value.equals(AIUIConstant.INTERACT_MODE_CONTINUOUS)){
					mIsOneshotMode = false;
				}
			}
		}

		private void processVolumeFollowVad(AIUIEvent event){
		    switch (event.arg1) {
            case AIUIConstant.VAD_BOS:{
            	if(mPlayController.isCurrentPlayMusic() || mPlayController.isCurrentTTS() ){
            		mPlayController.changeVolToLower();
            	}
            }break;
            
            case AIUIConstant.VAD_EOS:{
                mPlayController.recoverVol();
            }break;
            
            default:
                break;
            }
		}
		
		private void processWakeup(AIUIEvent event) {

			queryInteractMode();

		    if(ismIsWakeUp() && (mPlayController.isCurrentPlayMusic() || mPlayController.isCurrentTTS()) ){
                mPlayController.recoverVol();
            }
		    
			mPlayController.onMusicCommand("", InsType.PAUSE);
			mPlayController.stopTTS();
		
			if (!ismIsWakeUp() && ConfigUtil.isSaveAppTimeLog()) {
				AppTimeLogger.onRealWakeup();
				DebugLog.LogD(TAG, "makeWakeDir");
			}

			mCurrentState = AIUIConstant.STATE_WORKING;
			try {
				JSONObject wakeInfo = new JSONObject(event.info);
				
				int wakeAngle = wakeInfo.getInt("angle");
				DebugLog.LogD(TAG, "wakeAngle=" + wakeAngle);
				DevBoardControlUtil.wakeUpLight(wakeAngle);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mPlayController.playTone("", WAKE_UP_TONES[new Random().nextInt(WAKE_UP_TONES.length)]);
//	        mPlayController.justTTS("", WAKE_UP_TONES_TTS[new Random().nextInt(WAKE_UP_TONES.length)]);	
		}

		private void queryInteractMode() {
			if (null != mAIUIAgent) {
				try{
					JSONObject queryJson = new JSONObject();
					queryJson.put(AIUIConstant.PARAMS_TYPE, AIUIConstant.PARAM_SPEECH);
					AIUIMessage queryModeMsg = new AIUIMessage(
							AIUIConstant.CMD_QUERY_PARAMS, 0, 0, queryJson.toString(), null);
					mAIUIAgent.sendMessage(queryModeMsg);
				}catch (JSONException e){
					e.printStackTrace();
				}
			}
		}
		
		private void resetWakeup(boolean resetAIUI) {
			if (null != mAIUIAgent) {
				if (resetAIUI) {
					AIUIMessage resetMsg = new AIUIMessage(
							AIUIConstant.CMD_RESET, 0, 0, "", null);
					mAIUIAgent.sendMessage(resetMsg);
		
					DebugLog.LogD(TAG, "reset AIUI");
				} else {
					// 重置唤醒状态
					AIUIMessage resetWakeupMsg = new AIUIMessage(
							AIUIConstant.CMD_RESET_WAKEUP, 0, 0, "", null);
					mAIUIAgent.sendMessage(resetWakeupMsg);
		
					DebugLog.LogD(TAG, "reset Wakeup");
				}
			}
		}
		
		private void processCmdReturnEvent(AIUIEvent event) {
			switch (event.arg1) {
		
			case AIUIConstant.CMD_BUILD_GRAMMAR: {
				Log.d(TAG, "构建语法成功");
			} break;

			case AIUIConstant.CMD_QUERY_PARAMS: {
				handleParamsReturn(event);
			}break;
		
			default:
				break;
			}
		}
		
		private void buildGrammar() {
			String grammar = FileUtil.readAssetsFile(mContext, GRAMMAR_FILE_PATH);
			AIUIMessage buildGrammar = new AIUIMessage(AIUIConstant.CMD_BUILD_GRAMMAR, 
					0, 0, grammar, null);
		
			mAIUIAgent.sendMessage(buildGrammar);
			Log.d(TAG,"sendMessage start");
		}


		/**
		 * 出错处理函数。
		 * 
		 * @param errorCode 错误码
		 */
		private void processError(final int errorCode) {
			DebugLog.LogD(TAG, "AIUI error=" + errorCode);

			// 错误提示
			switch (errorCode) {
				case AIUIErrorCode.MSP_ERROR_TIME_OUT:
				case AIUIErrorCode.MSP_ERROR_NO_RESPONSE_DATA:  // 结果超时

				case AIUIErrorCode.MSP_ERROR_LMOD_RUNTIME_EXCEPTION:		// 16005，需要重启AIUI会话									
				case AIUIErrorCode.MSP_ERROR_NOT_FOUND:
				{
					//避免休眠时设置mSleepErrorCode，无法清空
					if(ismIsWakeUp()){
						 mSleepErrorCode = errorCode;
		                 resetWakeup(false);
					}
				}
				break;
				// appid校验不通过
				case AIUIErrorCode.MSP_ERROR_DB_INVALID_APPID:
				{
					DevBoardControlUtil.appidErrorLight(true);
					mPlayController.playTone("", TONE_WRONG_APPID);
				} break;
				
				case AIUIErrorCode.ERROR_SERVICE_BINDER_DIED:
				case AIUIErrorCode.ERROR_NO_NETWORK:
				{
					mPlayController.justTTS("",getErrorTip(errorCode) , false, new Runnable() {
						
						@Override
						public void run() {
							DevBoardControlUtil.sleepLight();
						}
					});
				}break;
				case AIUIErrorCode.MSP_ERROR_NLP_TIMEOUT:
				{
					mPlayController.playText("", "语义结果超时，请稍等一下！");
				}break;
				
				case AIUIErrorCode.MSP_ERROR_AIUI_NO_ENOUGH_LICENSE:{
					//已在ControlService中处理授权播报，此处不处理
				}break;
				
				case -1:
				{
				    if(ismIsWakeUp()){
				        mPlayController.justTTS("", "内容开小差，换点别的吧！", "");
				    }
				} break;
				
				default:{
					mPlayController.playText("", "AIUI " + errorCode + " 错误，请稍等一下");
					break;
				}
			}
		}
		
		private String getErrorTip(int errorCode){
		    mPlayController.recoverVol();
			switch (errorCode) {
			case AIUIErrorCode.MSP_ERROR_NOT_FOUND:
				return "场景参数设置出错！";
			case AIUIErrorCode.ERROR_SERVICE_BINDER_DIED:
				return "AIUI服务已断开！";
			case  AIUIErrorCode.ERROR_NO_NETWORK:
			    return "网络未连接，请连接网络！";
			case AIUIErrorCode.MSP_ERROR_TIME_OUT:
			case AIUIErrorCode.MSP_ERROR_NO_RESPONSE_DATA:
				return "网络有点问题我去休息了，请稍后再试！";
			default:
				return  "AIUI "+ errorCode + "错误，我去休息了";
			}
		}

		private void processResult(AIUIEvent event) {
				long posRsltOnArrival = System.currentTimeMillis();
		
				try {
					JSONObject bizParamJson = new JSONObject(event.info);
					JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
					JSONObject params = data.getJSONObject("params");
					JSONObject content = data.getJSONArray("content").getJSONObject(0);
		
					if (content.has("cnt_id")) {
						String cnt_id = content.getString("cnt_id");
						JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));
						String sub = params.optString("sub");
						
						//后处理结果
						if("tpp".equals(sub)) {

						}else {
							JSONObject result = cntJson.getJSONObject("intent");
							long posRsltParseFinish = System.currentTimeMillis();
							
							if ("nlp".equals(sub)) {
								//在线语义结果
								mSemanticHandler.handleResult(result, event.data, params.toString(), posRsltOnArrival, posRsltParseFinish);	
							} else if ("asr".equals(sub)) {
								// 处理离线语法结果
								mAsrHandler.handleResult(result);
							}
						}

					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		}
				
		private void justSayGoodbye(String uuid){
		    mPlayController.playTone(uuid, XIAOAI_GOODBYE_NWZL, null);
		}

		private void justLightOff(final String uuid) {
            DebugLog.LogD(TAG, "gotoSleep");
            
            mPlayController.stopPlayControl();
        
            AppTimeLogger.onSleep(uuid);
            
            DevBoardControlUtil.sleepLight();
            
        }
		
		/**
         * 休眠操作。
         * 
         * @param uuid
         */
        private void sayGoodbyeThenSleep(final String uuid) {
            DebugLog.LogD(TAG, "gotoSleep");
            
            mPlayController.stopPlayControl();
        
            // 播放休眠提示音
            mPlayController.playTone(uuid, XIAOAI_GOODBYE_NWZL, new Runnable() {
        
                @Override
                public void run() {
                    AppTimeLogger.onSleep(uuid);
        
                    DevBoardControlUtil.sleepLight();
                }
            });
  
//            mPlayController.justTTS("", "那我走了", false, new Runnable() {
//				
//				@Override
//				public void run() {
//					DevBoardControlUtil.sleepLight();
//				}
//			});
        }
        
		private void unregisterReceiver(){
			mContext.unregisterReceiver(mSleepReceiver);
			mContext.unregisterReceiver(mVoiceCtrReceiver);
			mContext.unregisterReceiver(mVolChangedReceiver);
		}

		public void destroy(){
			unregisterReceiver();
			mSemanticHandler.destroy();
			mAsrHandler.destroy();
			mAIUIAgent = null;
		}
		
		//PlayListenner OnError
		@Override
		public void onError(PalyControllerItem playItem, final int errorCode) {
			DebugLog.LogD(TAG, "TTS Error. ErrorCode=" + errorCode);
			
			processError(errorCode);
		}


        // 判断是不是处于唤醒状态
        public boolean ismIsWakeUp() {
            return mCurrentState == AIUIConstant.STATE_WORKING;
        }
}
