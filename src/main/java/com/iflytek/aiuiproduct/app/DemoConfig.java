package com.iflytek.aiuiproduct.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import android.os.Environment;

public class DemoConfig {
	
	private static final String VOLUME_FOLLOW_VAD = "VOLUME_FOLLOW_VAD";
	private static final String EMOTION_TTS = "EMOTION_TTS";
	/**
	 * DemoConfig.properties 格式举例如下：
	 * 
	 * VOLUME_FOLLOW_VAD = true
	 * 
	 * EMOTION_TTS = false
	 */
	private static final String CONFIG_PROPERTIES_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/ProductDemo/DemoConfig.properties";
	
	private static final DemoConfig sInstance = new DemoConfig();
	
	public static DemoConfig getDemoConfig() {
		return sInstance;
	}
	
	
	
	private Properties mConfig;
	
	private DemoConfig() {
		mConfig = new Properties();
		File configFile = new File(CONFIG_PROPERTIES_PATH);
		if(configFile.exists()){
			try {
				InputStream input = new FileInputStream(configFile);
				mConfig.load(input);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isVolumeFollowVad(){
		return Boolean.valueOf(
				mConfig.getProperty(VOLUME_FOLLOW_VAD, "false"));
	}
	
	public boolean isEmotionTTS(){
		return Boolean.valueOf(
				mConfig.getProperty(EMOTION_TTS, "true"));
	}
	
}
