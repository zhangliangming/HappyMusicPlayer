package com.happyplayer.model;

/**
 * 皮肤相关数据信息
 * 
 * @author Administrator
 * 
 */
public class SkinMessage {
	/**
	 * 颜色
	 */
	public final static int COLOR = 0;
	/**
	 * 皮肤图片
	 */
	public final static int PIC = 1;
	/**
	 * 歌手图片
	 */
	public final static int ART = 2;

	private String url;
	private String path;
	private String parentPath;
	public int type;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

}
