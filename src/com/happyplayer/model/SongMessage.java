package com.happyplayer.model;

/**
 * 保存歌曲操作的相关信息
 * 
 * @author Administrator
 * 
 */
public class SongMessage {
	/**
	 * 删除后提醒
	 */
	public static final int DEL_NUM = -1;
	/**
	 * 扫描完成后提醒
	 */
	public static final int SCAN_NUM = 0;
	/**
	 * 添加歌曲
	 */
	public static final int ADDMUSIC = 1;
	/**
	 * 歌曲插队
	 */
	public static final int MUSICINSERT = 2;
	/**
	 * 删除歌曲
	 */
	public static final int DELMUSIC = 3;
	/**
	 * 删除歌曲和本地文件
	 */
	public static final int DELMUSICANDFILE = 4;
	/**
	 * 上一首
	 */
	public static final int PREVMUSIC = 5;

	/**
	 * 下一首
	 */
	public static final int NEXTMUSIC = 6;

	/**
	 * 播放或者暂停
	 */
	public static final int PLAYORSTOPMUSIC = 7;

	/**
	 * 播放或者暂停
	 */
	public static final int SEEKTO = 8;

	/**
	 * 正在播放
	 */
	public static final int PLAYING = 9;

	/**
	 * 暂停播放
	 */
	public static final int STOPING = 10;

	/**
	 * 初始化
	 */
	public static final int INIT = 11;
	/**
	 * 播放选中的歌曲
	 */
	public static final int SELECTPLAY = 12;

	public static final int ERROR = 13;
	/**
	 * 下一首
	 */
	public static final int NEXTMUSICED = 14;
	/**
	 * 上一首完成
	 */
	public static final int PREVMUSICED = 15;
	/**
	 * 最后一首播放完成
	 */
	public static final int LASTPLAYFINISH = 16;
	/**
	 * 退出
	 */
	public static final int EXIT = 17;
	/***
	 * 桌面歌词
	 */
	public static final int DES_LRC = 18;
	/**
	 * 桌面歌词是否可移动
	 */
	public static final int DESLRCMOVE = 19;

	public static final int SELECTPLAYED = 20;
	/**
	 * 播放完成跳转下一首
	 */
	public static final int FINISHNEXTMUSICED = 21;
	/**
	 * 播放
	 */
	public static final int PLAY = 22;

	/**
	 * 暂停
	 */
	public static final int STOP = 23;
	/**
	 * 去播放
	 */
	public static final int TOPLAY = 24;
	/**
	 * 删除所有歌曲
	 */
	public static final int DELALLMUSIC = 25;

	/**
	 * 删除所有歌曲和本地文件
	 */
	public static final int DELALLMUSICANDFILE = 26;
	/**
	 * 删除所有歌曲完成
	 */
	public static final int DELALLMUSICED = 27;

	/**
	 * 桌面歌词移动完成
	 */
	public static final int DESLRCMOVEED = 28;
	/**
	 * 停止播放
	 */
	public static final int STOPPLAY = 29;
	// 0是扫描完成后提醒 1是添加歌曲 2是歌曲插队 3是删除歌曲 4是删除歌曲和本地文件
	// 5 是上一首 6是下一首 7是播放或者暂停 8是快进 9是正在播放 10是播放暂停 11初始化 12播放选中的歌曲
	private int type;

	private int num;// 歌曲数目

	private int progress; // 快进进度

	private SongInfo songInfo;// 歌曲数据

	private String errorMessage;// 错误信息

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public SongInfo getSongInfo() {
		return songInfo;
	}

	public void setSongInfo(SongInfo songInfo) {
		this.songInfo = songInfo;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
