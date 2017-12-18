package com.iflytek.aiuiproduct.receiver;

import com.iflytek.aiui.utils.log.DebugLog;
import com.iflytek.aiuiproduct.constant.ProductConstant;
import com.iflytek.aiuiproduct.utils.DevBoardControlUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;

/**
 * 网络状态广播监听器。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月23日 上午10:33:03 
 *
 */
public class NetworkStateReceiver extends BroadcastReceiver {
	private final static String TAG = ProductConstant.TAG;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			NetworkInfo networkInfo  = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			DetailedState state = networkInfo.getDetailedState();
			
			if (DetailedState.CONNECTED == state) {
				DebugLog.LogD(TAG, "network connected.");
				
				DevBoardControlUtil.wifiStateLight(true);
			} else if (DetailedState.DISCONNECTED == state) {
				DebugLog.LogD(TAG, "network disconnected.");
				
				DevBoardControlUtil.wifiStateLight(false);
			} else if (DetailedState.CONNECTING == state) {
				DevBoardControlUtil.wifiStateLight(false);
			}
		}
	}
	
}
