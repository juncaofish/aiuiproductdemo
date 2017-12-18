package com.iflytek.aiuiproduct.handler.disposer;

import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;
import com.iflytek.aiuiproduct.player.InsType;

import android.content.Context;
import android.text.TextUtils;

public class PhoneNumberDisposer extends Disposer {

	public PhoneNumberDisposer(Context context) {
		super(context);
	}

	@Override
	public void disposeResult(SemanticResult result) {
		String uuid = result.getUUID();
		String operation = result.getOperation();
		
		if(OPERATION_INS.equals(operation)){
			try {
				JSONObject semantic = result.getSemantic();
				String insType = semantic.getJSONObject(KEY_SLOTS).getString(KEY_INSTYPE);
				getPlayController().onMusicCommand(uuid, InsType.parseInsType(insType));

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else{
			String text = result.getAnswerText();
			if(!TextUtils.isEmpty(text)){
				getPlayController().playText(result.getUUID(), text);
			}
		}
	}

	
	public boolean canHandle(ServiceType type){
		if(type == ServiceType.NUMBER_MASTER 
				|| type == ServiceType.TELEPHONE){
			return true;
		}else{
			return false;
		}
	}
	
	
	
}
