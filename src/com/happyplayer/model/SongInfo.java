package com.happyplayer.model;

/**
 * 歌曲信息
 * 
 * @author Administrator
 * 
 */
public class SongInfo {
	/**
	 * 本地歌曲
	 */
	public static final int LOCAL = 0;
	/**
	 * 网上歌曲
	 */
	public static final int NET = 1;
	/**
	 * 无效
	 */
	public static final int INVALID = 0;
	/**
	 * 有效
	 */
	public static final int VALID = 1;

	private String sid; // id
	private long id; // 歌曲ID 3
	private String title; // 歌曲名称 0
	private String album; // 专辑 7
	private long albumId;// 专辑ID 6
	private String displayName; // 显示名称 4
	private String artist; // 歌手名称 2
	private long duration; // 歌曲时长 1
	private long size; // 歌曲大小 8
	private String sizeStr;// 歌曲大小：单位M / KB
	private String path; // 歌曲路径 5
	private String createTime;// 创建时间
	private int type; // 0是本地歌曲 1是网上下载歌曲
	/** -----------当是从网上下载的歌曲时-------------- */
	private String albumUrl; // 专辑图片下载路径
	private String downUrl;// 歌曲下载路径
	private long downSize;// 已经下载的进度
	private long playProgress;// 播放的进度
	private String category;// 分类
	private String childCategory;// 子分类
	private int valid;// 是否有效 0是无效 1是有效

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getSizeStr() {
		return sizeStr;
	}

	public void setSizeStr(String sizeStr) {
		this.sizeStr = sizeStr;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getAlbumUrl() {
		return albumUrl;
	}

	public void setAlbumUrl(String albumUrl) {
		this.albumUrl = albumUrl;
	}

	public String getDownUrl() {
		return downUrl;
	}

	public void setDownUrl(String downUrl) {
		this.downUrl = downUrl;
	}

	public long getDownSize() {
		return downSize;
	}

	public void setDownSize(long downSize) {
		this.downSize = downSize;
	}

	public long getPlayProgress() {
		return playProgress;
	}

	public void setPlayProgress(long playProgress) {
		this.playProgress = playProgress;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getChildCategory() {
		return childCategory;
	}

	public void setChildCategory(String childCategory) {
		this.childCategory = childCategory;
	}

	public int getValid() {
		return valid;
	}

	public void setValid(int valid) {
		this.valid = valid;
	}

	/**
	 * 获取当前歌曲剩余的长度
	 * 
	 * @return
	 */
	public int getSurplusProgress() {
		int surplusProgress = (int) (duration - playProgress);
		if (surplusProgress < 0) {
			surplusProgress = 0;
		}
		return surplusProgress;
	}

}
