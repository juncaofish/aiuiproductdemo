package com.iflytek.aiuiproduct.player.entity;

/**
 * 歌曲播放必需的信息。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月11日 下午4:19:30 
 *
 */
public class SongPlayInfo {
	private String songName = "";
	
	private String singerName = "";
	
	private String playUrl = "";
	
	private String answerText;

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public String getSingerName() {
		return singerName;
	}

	public void setSingerName(String singerName) {
		this.singerName = singerName;
	}

	public String getPlayUrl() {
		return playUrl;
	}

	public void setPlayUrl(String playUrl) {
		this.playUrl = playUrl;
	}

	public String getAnswerText() {
		return answerText;
	}

	public void setAnswerText(String answerText) {
		this.answerText = answerText;
	}

}
