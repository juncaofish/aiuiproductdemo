package com.iflytek.aiuiproduct.handler.disposer;

import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;

import android.content.Context;
import android.text.TextUtils;

/**
 * 信息类的结果处理器，包括天气、火车和航班。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年6月21日 下午4:32:03
 *
 */
public class InfoDisposer extends Disposer {
	
	
	public InfoDisposer(Context context) {
		super(context);
	}

	@Override
	public void disposeResult(SemanticResult result) {
		String text = result.getAnswerText();
		ServiceType serviceType = result.getServiceType();
		
		
		if (!TextUtils.isEmpty(text)) {
			if (serviceType == ServiceType.TRAIN || serviceType == ServiceType.FLIGHT) {
				// 数字纠错、火车、航班业务中的数字分开读
				getPlayController().playText(result.getUUID(), text);
			}else if(ServiceType.WEATHER == serviceType  || ServiceType.PM25 == serviceType) {
				getPlayController().playText(result.getUUID(), text, true, "", null);
			}
			
		}
	}
	
	public boolean canHandle(ServiceType type){
		if(type == ServiceType.TRAIN || type == ServiceType.FLIGHT 
				|| type == ServiceType.WEATHER  || type == ServiceType.PM25){
			return true;
		}else{
			return false;
		}
	}
}
