package com.iflytek.aiuiproduct.receiver;

import com.iflytek.aiuiproduct.player.PlayController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmManagerReceiver extends BroadcastReceiver {

	private static final String ALARM_TIPS_PATH = "wav/alarm.mp3";
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String msg = intent.getStringExtra("msg");
        
        PlayController.getInstance(context).playTone("", ALARM_TIPS_PATH, new Runnable() {
			
			@Override
			public void run() {
				PlayController.getInstance(context).playText("", "您有一个事件提醒，时间到了，请记得" + msg);
			}
		});
    }
}
