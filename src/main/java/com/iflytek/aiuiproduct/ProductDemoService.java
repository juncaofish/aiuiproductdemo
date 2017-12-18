package com.iflytek.aiuiproduct;

import com.iflytek.aiui.servicekit.AIUIAgent;
import com.iflytek.aiui.utils.log.DebugLog;
import com.iflytek.aiuiproduct.constant.ProductConstant;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 产品演示demo的主Service。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月21日 下午1:56:56 
 *
 */
public class ProductDemoService extends Service{
	private static final String TAG = ProductConstant.TAG;
	
	private static final int NOTIFICATION_ID = 1;

	public static final String ACTION = "com.iflytek.aiuiproduct.action.DemoService";

	// AIUI服务控制对象
	private AIUIAgent mAIUIAgent;

	private AIUIProcessor mProcessor;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mProcessor = new AIUIProcessor(this);
		mAIUIAgent = AIUIAgent.createAgent(this, mProcessor);
        mProcessor.setAgent(mAIUIAgent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		DebugLog.LogD(TAG, "onStartCommand");

		startForeground();
		return super.onStartCommand(intent, Service.START_STICKY, startId);
	}


	private void startForeground() {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction(ACTION);
		serviceIntent.setPackage(getPackageName());

		PendingIntent pendingIntent = PendingIntent.getService(ProductDemoService.this, 0, serviceIntent, 0);
		Notification notification = new Notification.Builder(ProductDemoService.this)
											.setTicker("AIUIProductDemo is running.")
											.setContentTitle("AIUIProductDemo")
											.setContentText("Hello, AIUIProductDemo!")
											.setContentIntent(pendingIntent)
											.build();

		startForeground(NOTIFICATION_ID, notification);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		DebugLog.LogD(TAG, "destroy AIUIAgent");
		
		mProcessor.destroy();
		
		if(mAIUIAgent != null){
			mAIUIAgent.destroy();
			mAIUIAgent = null;
		}

		stopForeground(true);
	}
}
