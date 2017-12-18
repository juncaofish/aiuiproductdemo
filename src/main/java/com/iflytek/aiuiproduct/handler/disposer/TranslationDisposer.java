package com.iflytek.aiuiproduct.handler.disposer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;

public class TranslationDisposer extends Disposer {

	private  final String KEY_TRANSLATED = "translated";
	
	public TranslationDisposer(Context context) {
		super(context);
	}

	@Override
	public void disposeResult(SemanticResult semanticResult) {
		
		try {
			JSONArray result = semanticResult.getResult();
			if(result == null | result.length() == 0) return;
			String translated = result.getJSONObject(0).getString(KEY_TRANSLATED);
			getPlayController().justTTS("", translated);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean canHandle(ServiceType type) {
		if (type.equals(ServiceType.TRANSLATION)) {
			return true;
		}
		return false;
	}

}
