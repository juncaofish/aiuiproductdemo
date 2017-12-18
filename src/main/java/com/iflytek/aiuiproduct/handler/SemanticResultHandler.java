package com.iflytek.aiuiproduct.handler;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiuiproduct.constant.ProductConstant;
import com.iflytek.aiuiproduct.handler.disposer.CMDDisposer;
import com.iflytek.aiuiproduct.handler.disposer.Disposer;
import com.iflytek.aiuiproduct.handler.disposer.InfoDisposer;
import com.iflytek.aiuiproduct.handler.disposer.JokeDisposer;
import com.iflytek.aiuiproduct.handler.disposer.MapDisposer;
import com.iflytek.aiuiproduct.handler.disposer.MusicDisposer;
import com.iflytek.aiuiproduct.handler.disposer.NewsDisposer;
import com.iflytek.aiuiproduct.handler.disposer.NumberDisposer;
import com.iflytek.aiuiproduct.handler.disposer.PhoneNumberDisposer;
import com.iflytek.aiuiproduct.handler.disposer.RadioDisposer;
import com.iflytek.aiuiproduct.handler.disposer.SimpleDisposer;
import com.iflytek.aiuiproduct.handler.disposer.SmartHomeDisposer;
import com.iflytek.aiuiproduct.handler.disposer.SnoozeDisposer;
import com.iflytek.aiuiproduct.handler.disposer.StoryDisposer;
import com.iflytek.aiuiproduct.handler.disposer.TranslationDisposer;
import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.utils.AppTimeLogger;
import com.iflytek.aiuiproduct.utils.DevBoardControlUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

/**
 * 语义结果处理类。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月23日 上午10:32:47
 *
 */
public class SemanticResultHandler {
	private static final String KEY_SERVICE = "service";
	private final static String KEY_RC = "rc";
	private final static String KEY_TEXT = "text";
	
	private final static String TAG = "SemanticResultHandler";
	
	private ResultHandler mResultHandler;
	
	private HandlerThread mHandlerThread;
	
	private List<Disposer> mSubDisposers = new ArrayList<Disposer>();
	
	private Disposer mDefaultDisposer;
	
	//上一次非INSTRUCTTION的结果
	private SemanticResult mLastActiveResult = null;
	
	private BroadcastReceiver mReplayReceiver = null;
	
	private Context mContext;
	
	public SemanticResultHandler(Context context) {
		
		mSubDisposers.add(new InfoDisposer(context));
		mSubDisposers.add(new MusicDisposer(context));
		mSubDisposers.add(new NewsDisposer(context));
		mSubDisposers.add(new PhoneNumberDisposer(context));
		mSubDisposers.add(new RadioDisposer(context));
		mSubDisposers.add(new SmartHomeDisposer(context));
		mSubDisposers.add(new StoryDisposer(context));
		mSubDisposers.add(new JokeDisposer(context));
		mSubDisposers.add(new CMDDisposer(context));
		mSubDisposers.add(new NumberDisposer(context));
		mSubDisposers.add(new TranslationDisposer(context));
		mSubDisposers.add(new SnoozeDisposer(context));
		mSubDisposers.add(new MapDisposer(context));
		mDefaultDisposer = new SimpleDisposer(context);
		
		mContext = context;
		mHandlerThread = new HandlerThread("ResultHandleThread");
		mHandlerThread.start();
		mResultHandler = new ResultHandler(mHandlerThread.getLooper());
		
		mReplayReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				disposeResult(mLastActiveResult);
			}
		};
		mContext.registerReceiver(mReplayReceiver, new IntentFilter(ProductConstant.ACTION_REPLAY));
	}
	
	
	public void destroy() {
		if (null != mHandlerThread) {
			mHandlerThread.quit();
			mResultHandler = null;
		}
		
		mContext.unregisterReceiver(mReplayReceiver);
	}
	
	//语义结果解析
	public static SemanticResult parseSemanticResult(JSONObject json) {
		int rc = json.optInt(KEY_RC);
		String text = json.optString(KEY_TEXT);
		//拒识效果展示
		if(rc == AIUIConstant.NLP_UNKNOWN && !TextUtils.isEmpty(text) && !text.matches("^[。？，！]")){
			DevBoardControlUtil.rejectionLight();
		}
		
		if (!json.has(KEY_SERVICE)) {
			return null;
		}
		
		String service = json.optString(KEY_SERVICE);
		
		return new SemanticResult(service, json);
	}
	
	private static final int MSG_SEMANTIC_RESULT = 1;
	
	class ResultHandler extends Handler {
		
		public ResultHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch (msg.what) {
				case MSG_SEMANTIC_RESULT:
				{
					boolean isHandled = false;
					SemanticResult result = (SemanticResult) msg.obj;
					for(Disposer disposer : mSubDisposers){
						if(disposer.canHandle(result.getServiceType())){
							disposer.disposeResult(result);
							isHandled = true;
							break;
						}
					}
					
					if(!isHandled){
						mDefaultDisposer.disposeResult(result);
					}
							            
					Log.d(TAG, String.format("handleMessage | handle %s result.", result.getServiceType().name()));
				} break;
	
				default:
					break;
			}
		}
	}

	private void disposeResult(SemanticResult result) {
		if (null != mResultHandler && null != result) {
			
			if(!Disposer.OPERATION_INS.equals(result.getOperation())){
				mLastActiveResult = result;
			}
			
			// 清空以前的消息，避免积压
			mResultHandler.removeMessages(MSG_SEMANTIC_RESULT);
			mResultHandler.obtainMessage(MSG_SEMANTIC_RESULT, result).sendToTarget();
		}
	}
	
	public void handleResult(JSONObject resultJson, Bundle data, String params, 
				long posRsltArrival, long posRsltParseFinish){
		try {
			resultJson.put("sid", data.getString("sid"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	
		// 获得语义结果业务类型
		SemanticResult semanticResult = SemanticResultHandler.parseSemanticResult(resultJson);
	
		if (null != semanticResult) {
			AppTimeLogger.onSemanticResult(resultJson, data, params, 
					posRsltArrival, posRsltParseFinish);
	
			disposeResult(semanticResult);
		}
	}
}
