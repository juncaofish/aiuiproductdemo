package com.iflytek.aiuiproduct.handler.disposer;

import android.content.Context;
import com.iflytek.aiuiproduct.constant.ProductConstant;
import com.iflytek.aiuiproduct.handler.entity.SemanticResult;
import com.iflytek.aiuiproduct.handler.entity.ServiceType;
import com.iflytek.aiuiproduct.player.PlayController;

/**
 * 结果处理抽象类。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年6月21日 下午4:31:44
 *
 */
public abstract class Disposer {
	
	
	protected final static String KEY_SEMANTIC = "semantic";
	protected final static String KEY_SLOTS = "slots";
//	protected final static String KEY_OPERATION = "operation";
	
	protected static final String KEY_DATA = "data";
	protected static final String KEY_RESULT = "result";
	
	protected final static String KEY_INSTYPE = "insType";
	public final static String OPERATION_INS = "INSTRUCTION";
	
	public final static String KEY_DIALOG_STAT = "dialog_stat";
	public final static String DIALOG_STAT_INVALID = "DataValid";
	public final static String KEY_STATE = "used_state";
	
	protected final static String TAG = ProductConstant.TAG;

	private PlayController playController;
	
	protected Context mContext;
	
	public Disposer(Context context) {
		mContext = context;
		playController = PlayController.getInstance(context);
	}
	
	protected PlayController getPlayController() {
		return playController;
	}
	
	public abstract void disposeResult(SemanticResult result);
	
	public abstract boolean canHandle(ServiceType type);

}
