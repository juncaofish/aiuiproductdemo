package com.iflytek.aiuiproduct.handler.disposer;

import android.content.Context;

import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;

public class IdiomDisposer extends Disposer {
	public IdiomDisposer(Context context) {
		super(context);
	}

	@Override
	public void disposeResult(SemanticResult result) {
		getPlayController().justTTS("", result.getAnswerText());
	}

	@Override
	public boolean canHandle(ServiceType type) {
		if (type.equals(ServiceType.IDIOM)) {
			return true;
		}
		return false;
	}

}
