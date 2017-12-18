package com.iflytek.aiuiproduct.player;

import java.io.IOException;

import com.iflytek.aiuiproduct.constant.ProductConstant;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.text.TextUtils;
import android.util.Log;

/**
 * 提示音播放器。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月23日 上午9:53:10 
 *
 */
public class TonePlayer {
	private static final String TAG = ProductConstant.TAG;
	private static boolean sEnabled = true;
	private static MediaPlayer player;

	public static void setEnabled(boolean enabled){
		sEnabled = enabled;
	}
	
	/**
	 * 如需要取消播放，直接调用返回的MediaPlayer.release()
	 */
	public static MediaPlayer play(Context ctx, int resId, Runnable completionRun) {
		MediaPlayer player = MediaPlayer.create(ctx, resId);
		playInternal(player, completionRun);
		return player;
	}

	/**
	 * 如需要取消播放，直接调用返回的MediaPlayer.release()
	 */
	public static MediaPlayer play(Context ctx, String assetsFileName, Runnable completionRun) {
		MediaPlayer player = createMediaPlayer(ctx, assetsFileName);
		playInternal(player, completionRun);
		return player;
	}

	public static MediaPlayer play(Context ctx, int resId) {
		MediaPlayer player = MediaPlayer.create(ctx, resId);
		playInternal(player, null);
		return player;
	}

	public static MediaPlayer play(Context ctx, String assetsFileName) {
		MediaPlayer player = createMediaPlayer(ctx, assetsFileName);
		playInternal(player, null);
		return player;
	}
	
	private static SoundPool pool;

	public static synchronized void playUseSoundPool(Context context, 
			final String path, final int looptimes) {
		try {
			if (null == context) {
				return;
			}
			
			if (null != pool) {
				pool.release();
			} 
			
			pool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
			
			final AssetFileDescriptor afd = context.getAssets().openFd(path);
			
			pool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
					soundPool.play(sampleId, 1, 1, 0, looptimes, 1);
					
					if (null != afd) {
						try {
							afd.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			
			pool.load(afd, 1);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void playInternal(final MediaPlayer player, final Runnable completionRun) {
		if (player == null) { // 播放错误或失败时继续执行下面的操作，避免卡住
			if (completionRun != null) {
				completionRun.run();
			}
			return;
		}
		
		player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (completionRun != null) {
					completionRun.run();
				}
				mp.release();
			}
		});
		
		player.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.d(TAG, "Error=" + what + ",extra=" + extra);
				player.start();
				return true;
			}
		});
		player.start();
	}

	private static MediaPlayer createMediaPlayer(Context context, String assetFileName) {
		MediaPlayer mp = new MediaPlayer();
		try {
			if (assetFileName.contains("/AIUIProductDemo/")) {
				mp.setDataSource(assetFileName);
				mp.prepare();
				return mp;
			} else if (assetFileName.contains("http")) {
				mp.setDataSource(assetFileName);
				mp.prepare();
				return mp;
			} else {
				AssetFileDescriptor afd = context.getAssets().openFd(assetFileName);
				if (afd == null) {
					return null;
				}
				mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				afd.close();
				mp.prepare();
				return mp;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			mp.release();
		}
		return null;
	}
	
	public static void playTone(Context context, String assetFileName) {
		playTone(context, assetFileName, null);
	}

	public static synchronized void playTone(Context context, String assetFileName, final Runnable finishCb) {
		if(!sEnabled) return;
		
		Log.d(TAG, "Tone Player play asset  " + assetFileName);
		
		stopPlay();
		
		player = new MediaPlayer();
		try {
			if (!TextUtils.isEmpty(assetFileName)) {
				AssetFileDescriptor afd = context.getAssets().openFd(assetFileName);
				player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			
				if (null != afd) {
					afd.close();
				}
				
				player.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						if (null != finishCb) {
							finishCb.run();
						}
						
						stopPlay();
						Log.d(TAG, "release");
					}
				});
				
				player.prepare();
				player.start();
			} else {
				stopPlay();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IllegalStateException e2) {
			e2.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
	}
	
	/**
	 * 根据
	 *  http://stackoverflow.com/questions/9609479/android-mediaplayer-went-away-with-unhandled-events
	 * release 之前加 reset
	 */

	public static void stopPlay() {
		synchronized (TonePlayer.class) {
			if (null == player) return;
			try {
				if(player.isPlaying()){
					player.stop();
				}
				player.reset();
			} catch (IllegalStateException e) {
				Log.d(TAG, "Tone Player stop Play IllegalStateException");
			} finally {
				player.release();
				player = null;
			}
		}
	}
	
}
