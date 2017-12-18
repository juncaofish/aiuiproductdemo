package com.iflytek.aiuiproduct.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

/**
 * 配置读取类。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月22日 下午2:13:00 
 *
 */
public class ConfigUtil {
	private static final String CFG_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() 
													+ "/AIUIProductDemo.properties";
	
	private static final String KEY_SAVE_APP_TIME_LOG = "save_app_time_log";
	
	private static boolean saveAppTimeLog = false;
	
	static {
		readCfg();
	}
	
	private static void readCfg() {
		File cfgFile = new File(CFG_FILE);
		if (!cfgFile.exists()) {
			return;
		}
		
		try {
			FileInputStream ins = new FileInputStream(cfgFile);
			Properties p = new Properties();
			p.load(ins);
			
			String saveAppTimeLogStr = p.getProperty(KEY_SAVE_APP_TIME_LOG);
			if ("1".equals(saveAppTimeLogStr)) {
				saveAppTimeLog = true;
			}
			
			ins.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isSaveAppTimeLog() {
		return saveAppTimeLog;
	}
	

	public static String getServiceInfo (Context context, String key) {

		Uri uri = Uri.parse("content://com.iflytek.aiui.serviceinfo.provider");
		Bundle bundle = null;

		try{
			bundle = context.getContentResolver().call(uri, "getServiceInfo", "", null);
		}catch(Exception e) {
			return "";
		}

		String serviceInfo = bundle.getString("service_info");
		try {
			JSONObject json = new JSONObject(serviceInfo);
			if (json.has(key)) {
				return json.getString(key);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	
	
}
