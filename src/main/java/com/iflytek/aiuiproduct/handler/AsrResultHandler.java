package com.iflytek.aiuiproduct.handler;

import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.aiuiproduct.constant.ProductConstant;

import com.iflytek.aiuiproduct.player.InsType;
import com.iflytek.aiuiproduct.player.PlayController;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * 语法结果处理类。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年9月27日 下午7:05:36 
 *
 */
public class AsrResultHandler {
	private static final int MSG_ASR_RESULT = 1;
	
	/*离线音乐控制语义id*/
	public final static int MUSIC_LAST 		= 1001;
	public final static int MUSIC_NEXT 		= 1002;
	public final static int MUSIC_PAUSE		= 1003;
	public final static int MUSIC_PLAY 		= 1004;
	public final static int VOLUNE_UP 		= 1005;
	public final static int VOLUME_DOWN 	= 1006;
	public final static int MUSIC_REPLAY 	= 1007;
	
	/*离线休眠语义id*/
	public final static int ID_SLEEP 		= 2001; 
	
	/*智能家居控制语义id*/
	public final static int OPEN_LIGHT      = 3001;
	public final static int CLOSE_LIGHT     = 3002;
	public final static int OPEN_AIRCON     = 3003;
	public final static int CLOSE_AIRCON    = 3004;
	public final static int OPEN_HUMID     	= 3005;
	public final static int CLOSE_HUMID     = 3006;
	public final static int OPEN_FANNER     = 3007;
	public final static int CLOSE_FANNER    = 3008;
	
	private Context mContext;
	
	private HandlerThread mAsrHandlerThread;
	
	private ResultHandler mResultHandler;
	
	private PlayController mPlayController;
	
	public AsrResultHandler(Context context) {
		mContext = context;
		mAsrHandlerThread = new HandlerThread("AsrResultThread");
		mAsrHandlerThread.start();
		mResultHandler = new ResultHandler(mAsrHandlerThread.getLooper());
	
		mPlayController = PlayController.getInstance(context);
	}

	public void handleResult(JSONObject AsrResult) {
		if(AsrResult.length() == 0) return;
		
		int commandId = parseAsrResult(AsrResult);
		mResultHandler.removeMessages(MSG_ASR_RESULT);
		Message.obtain(mResultHandler, MSG_ASR_RESULT, commandId, 0).sendToTarget();
	
	}

	/**
	 * 解析离线语法识别结果并返回id。
	 * 
	 * @param resultJson
	 * @return id
	 */
	private int parseAsrResult(JSONObject resultJson) {
		int id = 0;
		
		if (null != resultJson) {
			try {
				if (resultJson.getInt("rc") == 4) {
					id = -1;
				} else {
					JSONObject ws = (JSONObject) resultJson.getJSONArray("ws").get(0);
					JSONObject cw = (JSONObject) ws.getJSONArray("cw").get(0);
					
					id = Integer.valueOf(cw.getString("id"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
				id = 0;
			}
		}
		
		return id;
	}
	
	public void destroy() {
		mAsrHandlerThread.quit();
		mResultHandler = null;
	}

	class ResultHandler extends Handler{
		
		public ResultHandler( Looper looper){
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == MSG_ASR_RESULT) {
				
				switch (msg.arg1) {
				case MUSIC_LAST:
					PlayController.getInstance(mContext).onMusicCommand("", InsType.PAST);;
					break;
					
				case MUSIC_NEXT:
					PlayController.getInstance(mContext).onMusicCommand("", InsType.NEXT);;
					break;
					
				case MUSIC_PAUSE:
					PlayController.getInstance(mContext).onMusicCommand("", InsType.PAUSE);
					break;
					
//				case AsrId.MUSIC_REPLAY:
//					PlayController.getInstance(mContext).replayAnswer();
//					PlayController.getInstance(mContext).;
//					break;
					
				case MUSIC_PLAY:
					PlayController.getInstance(mContext).onMusicCommand("", InsType.REPLAY);
					break;
					
				case VOLUNE_UP:
					PlayController.getInstance(mContext).onMusicCommand("", InsType.VOLPLUS);
					break;
					
				case VOLUME_DOWN:
					PlayController.getInstance(mContext).onMusicCommand("", InsType.VOLMINUS);;
					break;
				
				case ID_SLEEP:
					Intent mIntent = new Intent();
					mIntent.setAction(ProductConstant.ACTION_SLEEP);
					mContext.sendBroadcast(mIntent);
					break;
				
				case OPEN_LIGHT:
					mPlayController.playText("", "已为您把灯打开");
					break;
					
				case CLOSE_LIGHT:
					mPlayController.playText(null, "已为您把灯关闭");					
					break;
				case OPEN_AIRCON:
					mPlayController.playText(null, "已为您打开空调");
					break;
					
				case CLOSE_AIRCON:
					mPlayController.playText(null, "已为您关闭空调");
					break;
					
				case OPEN_HUMID:
					mPlayController.playText(null, "已为您打开加湿器");
					break;
					
				case CLOSE_HUMID:
					mPlayController.playText(null, "已为您关闭加湿器");
					break;
					
				case OPEN_FANNER:
					mPlayController.playText(null, "已为您打开电风扇");
					break;
					
				case CLOSE_FANNER:
					mPlayController.playText(null, "已为您关闭电风扇");
					break;
				default:
					break;
				}

			}
		}
	}
	
}
