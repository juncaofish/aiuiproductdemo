package com.iflytek.aiuiproduct.handler.disposer;

import org.json.JSONObject;

import com.iflytek.aiuiproduct.constant.ProductConstant;
import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;
import com.iflytek.aiuiproduct.player.InsType;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class CMDDisposer extends Disposer {
	private String INSTYPE_SLEEP = "sleep";
	private String INSTYPE_IPCONFIG= "ipconfig";
	private String INSTYPE_MUTE = "mute";
	private String INSTYPE_UNMUTE = "unmute";

	
	
	public CMDDisposer(Context context) {
		super(context);
	}

	@Override
	public void disposeResult(SemanticResult result) {
		String operation = result.getOperation();
		
		if(operation.equals(OPERATION_INS)){
			parseCmd(result.getUUID(), result.getSemantic());
		}else{
			getPlayController().playText(result.getUUID(), result.getAnswerText());
		}
	}
	
	public boolean canHandle(ServiceType type){
		if(type == ServiceType.CMD){
			return true;
		}else{
			return false;
		}
	}
	
	private void parseCmd(String uuid, JSONObject cmdSemantic){
		String insTypeStr = cmdSemantic.optJSONObject(KEY_SLOTS).optString(KEY_INSTYPE);
	
		if(INSTYPE_SLEEP.equals(insTypeStr)){
			sleep(uuid);
		}else if(INSTYPE_IPCONFIG.equals(insTypeStr)){
			broadcastIP(uuid);
		//体验效果原因，暂不处理
		}else if(INSTYPE_MUTE.equals(insTypeStr)){
			
		}else if(INSTYPE_UNMUTE.equals(insTypeStr)){
			
		}else{
			getPlayController().onMusicCommand(uuid, InsType.parseInsType(insTypeStr));
		}
	}
	
	private void sleep(final String uuid){
	    
	    Intent intent = new Intent();
        intent.setAction(ProductConstant.ACTION_SLEEP);
        intent.putExtra(SemanticResult.KEY_UUID, uuid);
        
        mContext.sendBroadcast(intent);
        	    
	}
	
	
	private void broadcastIP(String uuid) {
		WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		
		if (wifiManager.isWifiEnabled()) {
			ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiNet = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wifiNet.isConnected()) {
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				int intIP = wifiInfo.getIpAddress();
				
				getPlayController().playText(uuid, "我的局域网地址是" + intToIP(intIP).replaceAll("\\.", "点"));
			} else {
				getPlayController().playText(uuid, "WIFI未连接");
			}
		} else {
			getPlayController().playText(uuid, "WIFI未打开");
		}
	}
	
	private String intToIP(int i) {       
		return (i & 0xFF ) + "." +  ((i >> 8 ) & 0xFF) + "." + 
				((i >> 16 ) & 0xFF) + "." + ( i >> 24 & 0xFF) ;  
	}

	
	
}
