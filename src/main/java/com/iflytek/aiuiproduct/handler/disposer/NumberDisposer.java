package com.iflytek.aiuiproduct.handler.disposer;

import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;

import android.content.Context;

public class NumberDisposer extends Disposer {

	public NumberDisposer(Context context) {
		super(context);
	}

	@Override
	public void disposeResult(SemanticResult result) {
		getPlayController().playText(result.getUUID(), result.getAnswerText(), true, 0, "", null);
	}

	@Override
	public boolean canHandle(ServiceType type) {
		if(type == ServiceType.DATETIME || type == ServiceType.CALC){
			return true;
		}else{
			return false;
		}
	}

}
