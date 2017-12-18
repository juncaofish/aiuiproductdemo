package com.iflytek.aiuiproduct.utils;


import com.iflytek.aiui.utils.log.DebugLog;
import com.iflytek.aiuiproduct.utils.TimeTrigger.TriggerListener;
import com.iflytek.devboard.control.BoardController;


/**
 * 开发板控制工具类。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月23日 上午10:31:43 
 *
 */
public class DevBoardControlUtil {
	private final static String TAG = "lightcontrol";
	
	/** led灯的三种颜色 **/
	public final static  int COLOR_RED = BoardController.COLOR_RED;
	public final static  int COLOR_BLUE = BoardController.COLOR_BLUE;
	public final static  int COLOR_GREEN = BoardController.COLOR_GREEN;
	
	private static int sChannel = 5;
	// 设置定时时间
	private static TimeTrigger lightTimeTrigger;
	// 设置是否灯闪烁
	private static boolean isFlashing = false;
	// 设置是否红灯常亮
	private static boolean isRedNormallyOn = false;
	
	private static int wakeAngle = 0;

	private static boolean isWakeUp = false;
	
	public static void setChannel(int channel) {
		sChannel = channel;
	}
	
	/**
	 * 自动休眠灯光控制
	 */
	public static void sleepLight() {
		isWakeUp = false;
		// 休眠关闭灯
		BoardController.closeRGBLight();
		
		if (null != lightTimeTrigger) {
			lightTimeTrigger.cancel();
		}
		BoardController.closeDirectionLight();
	}
	
	
	private static void setRGB(int color) {
		BoardController.closeRGBLight();
		BoardController.setRGBLight(color);
	}
	
	/**
	 * 唤醒灯光控制
	 * @param angle 唤醒角度
	 */
	public static void wakeUpLight(int angle) {		
		isWakeUp = true;
		wakeAngle = angle;
		setRGB(COLOR_GREEN);
		//唤醒角度亮灯
		BoardController.setDirectionLight(wakeAngle, sChannel);
	}

	/**
	 * 拒识结果操作灯光
	 */
	public static void rejectionLight() {
    	setRGB(COLOR_BLUE);
    	setTimeTrigger(3000);
		
		//方向灯闪一圈
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				for (int i = 1; i <= 12; i++) {
					BoardController.setDirectionLight(wakeAngle + i*30, sChannel);
					
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				DebugLog.LogD(TAG, "set wakeAngle");
				
				BoardController.setDirectionLight(wakeAngle, sChannel);
			}
		}).start();
	
	}
	
	/**
	 * wifi连接灯光控制
	 * @param isConnect 判断是否连接
	 */
	public static void wifiStateLight(boolean isConnect) {
		DebugLog.LogD(TAG, "isConnect :  " + isConnect);
		
		isFlashing = !isConnect;
		if(isFlashing){
			setLightFlashing(1000, COLOR_RED);
		}
	}
	
	/**
	 * appid检验不通过灯光控制
	 * @param appidState 是否亮appid校验结果标示灯
	 * @param isWakeUp 是否唤醒
	 */
	public static void appidErrorLight(boolean appidError) {
		DebugLog.LogD(TAG, "appidState = " + appidError);
		
		if (appidError) {
			isRedNormallyOn = true;
			setLightNormalON(COLOR_RED);
		} else {
			isRedNormallyOn = false;
		}
	}

	/**
	 * 颜色灯闪烁
	 * @param time 闪烁相隔时间
	 * @param col 灯的颜色
	 */
	private static void setLightFlashing(final int time, final int col) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (isFlashing) {
					DebugLog.LogD(TAG, "isFlashing = " + isFlashing);
					
					setRGB(col);
					
					try {
						Thread.sleep(time);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					BoardController.closeRGBLight();
					
					try {
						Thread.sleep(time);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				if (isWakeUp) {
					setRGB(COLOR_GREEN);
				}
			}
		}).start();
	}
	
	/**
	 * 颜色灯闪烁
	 * @param time 闪烁相隔时间
	 * @param col 灯的颜色
	 */
	private static void setLightNormalON(final int col) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (isRedNormallyOn) {
					DebugLog.LogD(TAG, "isRedNormallyOn = " + isRedNormallyOn);
					
					setRGB(col);
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				if (isWakeUp) {
					wakeUpLight(wakeAngle);
				} else {
					sleepLight();
				}
			}
		}).start();
	}
	
	private static void setTimeTrigger(int time) {
		lightTimeTrigger = new TimeTrigger(time);
    	lightTimeTrigger.setListener(new TriggerListener() {
			
			@Override
			public void onTrigger() {
				setRGB(COLOR_GREEN);
			}
		});
    	lightTimeTrigger.start();
	}
}
