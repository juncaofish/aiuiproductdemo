package com.iflytek.aiuiproduct.handler.disposer;

import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;

import android.content.Context;

/**
 * 简单业务处理器，只播报answer字段。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年4月1日 上午11:02:17
 *
 */
public class SimpleDisposer extends Disposer {

	public SimpleDisposer(Context context) {
		super(context);
	}

	@Override
	public void disposeResult(SemanticResult result) {
	    String answer = result.getAnswerText();
		getPlayController().playText(result.getUUID(), answer, 
				result.getEmotion());
	}
	
	@Override
	public boolean canHandle(ServiceType type){
		return false;
	}

}
