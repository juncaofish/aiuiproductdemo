package com.iflytek.aiuiproduct.utils;

import java.util.Timer;
import java.util.TimerTask;

import com.iflytek.aiui.utils.log.DebugLog;

/**
 * 时间触发器，可在一定延时之后触发
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月23日 上午10:30:09
 *
 */
public class TimeTrigger {
	private static final String TAG = "TimeTrigger";
	public interface TriggerListener {
		public void onTrigger();
	}

	class TriggerTask extends TimerTask {

		@Override
		public void run() {
			mTimer.cancel();
			mTimer = null;
			mTimerTask = null;

			if (null != mListener) {
				mListener.onTrigger();
			}
		}
	}

	private long mDelay;

	private Timer mTimer;

	private TimerTask mTimerTask;

	private TriggerListener mListener;

	/**
	 * 构建函数
	 * 
	 * @param delay 延时，单位：ms。将在timeLen之后触发
	 */
	public TimeTrigger(long delay) {
		mDelay = delay;
	}

	public void setListener(TriggerListener listener) {
		mListener = listener;
	}

	/**
	 * 开启触发器
	 */
	public void start() {
		if (null == mTimer) {
			mTimer = new Timer();
			mTimerTask = new TriggerTask();
			mTimer.schedule(mTimerTask, mDelay);
			
			DebugLog.LogD(TAG, "TimeTrigger start");
		}
	}

	/**
	 * 重置触发器到初始状态，重新计时
	 * 
	 * @return 触发器开启时返回true，关闭状态下返回false
	 */
	public boolean reset() {
		if (null != mTimer) {
			if (null != mTimerTask) {
				mTimerTask.cancel();
				mTimerTask = new TriggerTask();
				mTimer.schedule(mTimerTask, mDelay);
				
				DebugLog.LogD(TAG, "TimeTrigger reset");
				return true;
			}
		}
		return false;
	}

	/**
	 * 取消触发器
	 */
	public void cancel() {
		if (null != mTimer) {
			mTimer.cancel();
			mTimerTask.cancel();

			mTimer = null;
			mTimerTask = null;
			
			DebugLog.LogD(TAG, "TimeTrigger cancel");
		}
	}
}
