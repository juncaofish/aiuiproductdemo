package com.iflytek.aiuiproduct.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import com.iflytek.aiui.utils.log.DebugLog;
import com.iflytek.aiuiproduct.utils.FileUtil;
import com.iflytek.aiuiproduct.utils.FileUtil.DataFileHelper;

import android.content.Context;
import android.os.Environment;

/**
 * 崩溃日志收集器。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年6月27日 下午7:48:02 
 *
 */
public class CrashCollector implements UncaughtExceptionHandler {
	private static final String TAG = "CrashCollector";
	
	private static final String CRASH_LOG_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()
													+ "/ProductDemo/crash/";
	
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	
	private static CrashCollector INSTANCE = new CrashCollector();
	
	private DataFileHelper mCrashLogHelper;
	
	private CrashCollector() {
		
	}
	
	public static CrashCollector getInstance() {
		return INSTANCE;
	}
	
	public void init(Context context) {
		
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(CrashCollector.this);
		
		mCrashLogHelper = FileUtil.createFileHelper(CRASH_LOG_DIR);
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!saveCrashLog(ex) && null != mDefaultHandler) {
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {  
				Thread.sleep(3000);  
			} catch (InterruptedException e) {  
				DebugLog.LogE(TAG, "error:" + e.toString());  
			}  
			
			// 退出程序  
			android.os.Process.killProcess(android.os.Process.myPid());  
			System.exit(1);  
		}
	}
	
	private boolean saveCrashLog(Throwable ex) {
		if (null == ex) {
			return false;
		}
		
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		
		while (null != cause) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		
		String crashLog = writer.toString();
		printWriter.close();
		
		mCrashLogHelper.createFile("", FileUtil.SURFFIX_TXT, false);
		mCrashLogHelper.write(crashLog.getBytes(), true);
		mCrashLogHelper.closeWriteFile();
		
		return true;
	}

}
