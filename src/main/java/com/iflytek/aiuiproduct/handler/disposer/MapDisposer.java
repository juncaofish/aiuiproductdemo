package com.iflytek.aiuiproduct.handler.disposer;

import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;

import android.content.Context;

public class MapDisposer extends Disposer{

	public MapDisposer(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void disposeResult(SemanticResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canHandle(ServiceType type) {
		if(type == ServiceType.MAP){
			return true;
		}
		return false;
	}

}
