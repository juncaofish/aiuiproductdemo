package com.iflytek.aiuiproduct.player;

import android.content.Context;
import android.media.AudioManager;

/**
 * 音量设置管理
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月23日 上午10:37:28
 *
 */
public class VolumeManager {
	private AudioManager mAudioManager;

	/** 音量大小数组 **/
	public static final int[] mLevelArray = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };

	public static final int MAX_VOLUME_LEVEL = mLevelArray.length - 1;
	public static final int MIN_VOLUME_LEVEL = 7;
	public static final int MID_VOLUME_LEVEL = 10;
	// 每次增加或减少的音量
	public static final int VOLUME_LEVEL = 2;

	public VolumeManager(Context context) {
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * 把音量设置到最大
	 */
	public void setMaxVolume() {
		setVolume(MAX_VOLUME_LEVEL);
	}

	/**
	 * 把音量设置为最小
	 */
	public void setMinVolume() {
		setVolume(MIN_VOLUME_LEVEL);
	}

	/**
	 * 音量增加
	 * 
	 * @param volume 要增加的大小
	 */
	public void plusVolume() {
		int volume = VOLUME_LEVEL;
		int currentVolume = getCurrentVolume();
		if (-1 != currentVolume) {
			volume += currentVolume;
			if (volume >= MAX_VOLUME_LEVEL) {
				volume = MAX_VOLUME_LEVEL;
			}
			setVolume(volume);
		}
	}

	/**
	 * 音量增加
	 * 
	 * @param volume 要增加的大小
	 */
	public void plusVolume(int volume) {
		int currentVolume = getCurrentVolume();
		if (-1 != currentVolume) {
			volume += currentVolume;
			if (volume >= MAX_VOLUME_LEVEL) {
				volume = MAX_VOLUME_LEVEL;
			}
			setVolume(volume);
		}
	}

	/**
	 * 音量减小
	 * 
	 * @param volume 要减小的音量
	 */
	public void minusVolume() {
		int volume = VOLUME_LEVEL;
		int currentVolume = getCurrentVolume();
		if (-1 !=currentVolume) {
			volume = currentVolume - volume;
			if (volume <= MIN_VOLUME_LEVEL) {
				volume = MIN_VOLUME_LEVEL;
			}
			setVolume(volume);
		}
	}

	/**
	 * 音量减小
	 * 
	 * @param volume 要减小的音量
	 */
	public void minusVolume(int volume) {
		int currentVolume = getCurrentVolume();
		if (-1 != currentVolume) {
			volume = currentVolume - volume;
			if (volume <= MIN_VOLUME_LEVEL) {
				volume = MIN_VOLUME_LEVEL;
			}
			setVolume(volume);
		}
	}

	/**
	 * 音量设置为中间大小
	 */
	public void setMidVolume() {
		setVolume(MID_VOLUME_LEVEL);
	}

	/**
	 * 设置音量
	 * 
	 * @param volume 音量大小
	 */
	public void setVolume(int volume) {
		if (null != mAudioManager) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
		}
	}

	/**
	 * 获得当前媒体播放音量
	 * 
	 * @return 当前音量
	 */
	public int getCurrentVolume() {
		if (null != mAudioManager) {
			int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			return currentVolume;
		}
		return -1;
	}
}
