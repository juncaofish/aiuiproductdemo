package com.iflytek.aiuiproduct.handler.disposer;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.iflytek.aiui.utils.log.DebugLog;
import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;

/**
 * 家具控制语义结果处理器。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年6月21日 下午4:30:45
 * 
 */
public class SmartHomeDisposer extends Disposer {
	private final static  String TAG = "SmartHomeDisposer";	
	
	
	private final static String KEY_DIRECT = "direct";
	private final static  String DIRECT_PLUS = "+";
	private final static  String DIRECT_MINUS = "-";
	
	ServiceType serviceType;

	public static enum OperationType {
		OPEN, CLOSE, SET, OTHER
	}

	static HashMap<String, OperationType> operationMap = new HashMap<String, OperationType>();

	static {
		operationMap.put("OPEN", OperationType.OPEN);
		operationMap.put("CLOSE", OperationType.CLOSE);
		operationMap.put("SET", OperationType.SET);
	}

	private String operation;

	public SmartHomeDisposer(Context context) {
		super(context);
	}
	
	public OperationType getOperationType() {
		if (operation == null) {
			return OperationType.OTHER;
		}
		OperationType type = operationMap.get(operation);
		if (null == type) {
			type = OperationType.OTHER;
		}
		return type;
	}
	
	@Override
	public void  disposeResult(SemanticResult result) {
		if (null != result) {
			ServiceType serviceType = result.getServiceType();
			String text = "";
			DebugLog.LogD(TAG, serviceType.name());
			
		    operation = result.getOperation();

			OperationType operationType = getOperationType();

			DebugLog.LogD(TAG, operationType.name());
			
			switch (serviceType) {
			case FREEZER_SMARTH:
				processFreezerSmartHome(operationType, result.getUUID(), result.getSemantic());
				break;
//				case CURTAIN_SMARTHOME:
//				case HUMIDIFIER_SMARTHOME:
//				case LIGHT_SMARTHOME:
//				case AIRCONTROL_SMARTHOME:					
			default:
				text = result.getAnswerText(); 
				getPlayController().playText(result.getUUID(), text, true, "", null);
				break;
			}
		}
	}
	
	@Override
	public boolean canHandle(ServiceType type){
		if(type == ServiceType.HUMIDIFIER_SMARTH
				|| type == ServiceType.LIGHT_SMARTH
				|| type == ServiceType.AIRCONTROL_SMARTH
				|| type == ServiceType.CURTAIN_SMARTH
				|| type == ServiceType.FREEZER_SMARTH
				|| type == ServiceType.DISHORDER ){
			return true;
		}else{
			return false;
		}
	}
	
	private void processFreezerSmartHome(OperationType operation, String uuid, JSONObject semantic){
		switch (operation) {
		case CLOSE:
			getPlayController().playText(uuid, "已为您关闭冰箱");
			break;
		case OPEN:
			getPlayController().playText(uuid, "已为您打开冰箱");
			break;
		case SET:
			try {
				JSONObject slots = semantic.getJSONObject(KEY_SLOTS);
				String temperatureZone = slots.optString("temperatureZone","");
				String attr = slots.getString("attr");
				String attrType = slots.getString("attrType");
				String text = null;
				String ATTR_VALUE_KEY = "attrValue";
				if(attr.equals("温度")){
					if(attrType.equals("Integer")){
						int temperature = slots.getInt(ATTR_VALUE_KEY);
						text = "准备把您冰箱" + temperatureZone +"的温度调到" + temperature + "度";
					}else if(attrType.equals("String")){
						String val = slots.getString(ATTR_VALUE_KEY);
						if(val.equals("MAX")){
							text = "准备把您冰箱" +temperatureZone + "的温度调节到最高温度";
						}else if(val.equals("MIN")){
							text = "准备把您冰箱" +temperatureZone + "的温度调节到最低温度";
						}
					}else {
						JSONObject temp = slots.getJSONObject(ATTR_VALUE_KEY);
						int offset = temp.getInt("offset");
						String direct = temp.getString(KEY_DIRECT);
						
						if (DIRECT_MINUS.equals(direct)) {
							text = "准备把您冰箱" + temperatureZone +"的温度调低" + offset + "度";
						}else if (DIRECT_PLUS.equals(direct)) {
							text = "准备把您冰箱" + temperatureZone +"的温度调高" + offset + "度";
						}
					}
				}else if (attr.equals("开关")) {
					String val = slots.getString(ATTR_VALUE_KEY);
					if(val.equals("开")){
						text = "准备开启您冰箱" + temperatureZone;
					}else if(val.equals("关")){
						text = "准备关闭您冰箱" + temperatureZone;
					}
				} else {
					String val = slots.getString(ATTR_VALUE_KEY);
					if(val.equals("开")) {
						text = "准备将您冰箱" + temperatureZone + "开启" + attr;
					}else{
						text = "准备关闭您冰箱" + temperatureZone + "的" + attr;
					}
					
					if(!attr.equals("解冻")){
						text += "模式";
					}
				}
				
				int duration = 0;
				if((duration = slots.optInt("duration", 0)) != 0) {
					text = duration + "分钟后" + text;
				}
				
				if(slots.has("datetime")){
					String timeOrig = slots.getJSONObject("datetime").getString("timeOrig");
					text = timeOrig + text;
				}
				
				
				if(text != null){
					getPlayController().playText(uuid, text, true, "", null);
				}
				
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		default:
			break;
		}
	}
}
