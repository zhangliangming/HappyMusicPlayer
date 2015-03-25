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
	// 0是扫描完成后提醒 1是添加歌曲 2是歌曲插队 3是删除歌曲 4是删除歌曲和本地文件
	// 5 是上一首 6是下一首 7是播放或者暂停 8是快进 9是正在播放 10是播放暂停 11初始化 12播放选中的歌曲
	private int type;

	private int num;// 歌曲数目

	private int progress; // 快进进度

	private SongInfo songInfo;// 歌曲数据

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

}
