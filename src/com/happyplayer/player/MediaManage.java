package com.happyplayer.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;

import com.happyplayer.common.Constants;
import com.happyplayer.db.SongDB;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;

public class MediaManage implements Observer {
	private static MediaManage _mediaManage;
	private static List<SongInfo> playlist;

	private int playIndex = -1;
	private String playSID = "";

	public MediaManage(Context context) {
		init(context);
	}

	public static MediaManage getMediaManage(Context context) {
		if (_mediaManage == null) {
			_mediaManage = new MediaManage(context);
		}
		return _mediaManage;
	}

	private void init(Context context) {
		playlist = SongDB.getSongInfoDB(context).getAllSong();
		playSID = Constants.PLAY_SID;
		for (int i = 0; i < playlist.size(); i++) {
			SongInfo tempSongInfo = playlist.get(i);
			if (tempSongInfo.getSid().equals(playSID)) {
				playIndex = i;

				// 发送历史歌曲数据给其它页面
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.INIT);
				songMessage.setSongInfo(tempSongInfo);

				ObserverManage.getObserver().setMessage(songMessage);
				break;
			}
		}
		ObserverManage.getObserver().addObserver(MediaManage.this);
	}

	/**
	 * 获取歌曲列表的大小
	 * 
	 * @return
	 */
	public int getCount() {
		return playlist.size();
	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SongMessage) {
			SongMessage songMessage = (SongMessage) data;
			if (songMessage.getType() == SongMessage.DELMUSIC) {
				SongInfo songInfo = songMessage.getSongInfo();
				refresh(songInfo.getSid());
			} else if (songMessage.getType() == SongMessage.ADDMUSIC) {
				SongInfo songInfo = songMessage.getSongInfo();
				add(songInfo);
			} else if (songMessage.getType() == SongMessage.SCAN_NUM) {
				playIndex = getPlayIndex();
			} else if (songMessage.getType() == SongMessage.SELECTPLAY) {
				playIndex = getPlayIndex();
				// System.out.println("SELECTPLAY:playIndex--->" + playIndex);
				play(playlist.get(playIndex));
			}
		}
	}

	/**
	 * 播放歌曲
	 * 
	 * @param songInfo
	 */
	private void play(SongInfo songInfo) {
		// 发送历史歌曲数据给其它页面
		SongMessage songMessage = new SongMessage();
		songMessage.setType(SongMessage.INIT);
		songMessage.setSongInfo(songInfo);
		ObserverManage.getObserver().setMessage(songMessage);
	}

	/**
	 * 获取当前播放歌曲的索引
	 * 
	 * @return
	 */
	private int getPlayIndex() {
		int index = 0;
		playSID = Constants.PLAY_SID;
		for (int i = 0; i < playlist.size(); i++) {
			SongInfo tempSongInfo = playlist.get(i);
			if (tempSongInfo.getSid().equals(playSID)) {
				return i;
			}
		}
		return index;
	}

	private void add(SongInfo songInfo) {
		if (playlist == null || playlist.size() == 0) {
			playlist = new ArrayList<SongInfo>();
			playlist.add(songInfo);
			return;
		}
		char category = songInfo.getCategory().charAt(0);
		String childCategory = songInfo.getChildCategory();
		for (int i = 0; i < playlist.size(); i++) {
			SongInfo tempSongInfo = playlist.get(i);
			char tempCategory = tempSongInfo.getCategory().charAt(0);
			if (category == tempCategory) {
				String tempChildCategory = tempSongInfo.getChildCategory();
				if (childCategory.compareTo(tempChildCategory) < 0) {
					playlist.add(i, songInfo);
					return;
				}
			} else if (category < tempCategory) {
				playlist.add(i, songInfo);
				return;
			} else if (i == playlist.size() - 1) {
				playlist.add(songInfo);
				return;
			}
		}
	}

	/**
	 * 通过sid来删除 playlist 中的数据
	 * 
	 * @param sid
	 */
	private void refresh(String sid) {
		if (playlist == null || playlist.size() == 0)
			return;

		for (int i = 0; i < playlist.size(); i++) {
			if (playlist.get(i).getSid().equals(sid)) {
				SongMessage songMessage = new SongMessage();
				songMessage.setNum(-1);
				songMessage.setType(SongMessage.DEL_NUM);
				ObserverManage.getObserver().setMessage(songMessage);
				playlist.remove(i);
				break;
			}
		}
	}
}
