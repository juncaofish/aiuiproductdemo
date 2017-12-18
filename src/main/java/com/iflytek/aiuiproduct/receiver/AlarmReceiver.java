package com.iflytek.aiuiproduct.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.iflytek.aiui.utils.log.DebugLog;
import com.iflytek.aiuiproduct.VoiceBroadcastService;
import com.iflytek.aiuiproduct.player.PlayController;

import java.util.Calendar;
import java.util.Random;

import static com.iflytek.aiuiproduct.utils.AppTimeLogger.TAG;

/**
 * Created by caoja on 2017/12/17.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private final static String WAV_PATH = "wav/";
    private final static String START_SUCCESS = WAV_PATH + "start_success.mp3";
    private static String time = "";
    private final static String[] RECOMMEND_TRIPS = {
        "上海天气好冷啊，我们去香港过冬吧。十二月二十三日开始维多利亚港口就有比基尼圣诞节活动哟，我们要去看看吗？",
        "本周末上海气温两度，三亚二十度，带本宝宝去三亚过周末吧？",
        "主人主人，我想吃火锅，宝宝刚刚欢快的看了下，主人我们要去重庆的火锅里浪一浪吗？"
    };

    @Override
    public void onReceive(final Context context, Intent intent) {
        PlayController.getInstance(context).playTone("", START_SUCCESS, new Runnable() {
            @Override
            public void run() {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                DebugLog.LogD(TAG, "Alarm triggered at " + String.valueOf(hour));
                if(hour > 6 && hour < 23) {
                    if (hour < 10) {
                        time = "早间播报";
                    } else if (hour < 14) {
                        time = "午间播报";
                    } else if (hour < 18) {
                        time = "下午播报";
                    } else {
                        time = "晚间播报";
                    }
                    PlayController.getInstance(context).playText("", RECOMMEND_TRIPS[new Random().nextInt(RECOMMEND_TRIPS.length)]);
                }
            }
        });
        Intent serviceIntent = new Intent(context, VoiceBroadcastService.class);
        context.startService(serviceIntent);
    }
}
