package com.happyplayer.manage;
//package com.happyplayer.player;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Observable;
//import java.util.Observer;
//import java.util.Random;
//
//import android.app.ActivityManager;
//import android.content.Context;
//import android.content.Intent;
//
//import com.happyplayer.common.Constants;
//import com.happyplayer.db.SongDB;
//import com.happyplayer.model.SongInfo;
//import com.happyplayer.model.SongMessage;
//import com.happyplayer.observable.ObserverManage;
//import com.happyplayer.service.MediaPlayerService;
//import com.happyplayer.util.DataUtil;
//
//public class MediaPlayerManage implements Observer {
//	private static MediaPlayerManage _mediaManage;
//	private List<SongInfo> playlist;
//	private SongInfo playSongInfo;
//	private int playIndex = -1;
//	private String playSID = "";
//	private SongMessage songMessage;
//
//	public static final int PLAYING = 1;
//
//	public static final int STOP = 0;
//
//	private int status = 0;
//
//	private Context context;
//
//	private Intent playerService;
//
//	public MediaPlayerManage(Context context) {
//		this.context = context;
//		init(context);
//	}
//
//	public static MediaPlayerManage getMediaManage(Context context) {
//		if (_mediaManage == null) {
//			_mediaManage = new MediaPlayerManage(context);
//		}
//		return _mediaManage;
//	}
//
//	private void init(Context context) {
//
//		playerService = new Intent(context, MediaPlayerService.class);
//
//		playlist = SongDB.getSongInfoDB(context).getAllSong();
//		playSID = Constants.PLAY_SID;
//		for (int i = 0; i < playlist.size(); i++) {
//			SongInfo tempSongInfo = playlist.get(i);
//			if (tempSongInfo.getSid().equals(playSID)) {
//
//				playIndex = i;
//
//				playSongInfo = tempSongInfo;
//
//				// 发送历史歌曲数据给其它页面
//				SongMessage songMessage = new SongMessage();
//				songMessage.setType(SongMessage.INIT);
//				songMessage.setSongInfo(tempSongInfo);
//
//				ObserverManage.getObserver().setMessage(songMessage);
//				break;
//			}
//		}
//		ObserverManage.getObserver().addObserver(MediaPlayerManage.this);
//	}
//
//	/**
//	 * 获取歌曲列表的大小
//	 * 
//	 * @return
//	 */
//	public int getCount() {
//		return playlist.size();
//	}
//
//	@Override
//	public void update(Observable arg0, Object data) {
//		if (data instanceof SongMessage) {
//			SongMessage songMessage = (SongMessage) data;
//			if (songMessage.getType() == SongMessage.DELMUSIC) {
//				SongInfo songInfo = songMessage.getSongInfo();
//				refresh(songInfo.getSid());
//			} else if (songMessage.getType() == SongMessage.ADDMUSIC) {
//				SongInfo songInfo = songMessage.getSongInfo();
//				add(songInfo);
//			} else if (songMessage.getType() == SongMessage.SCAN_NUM) {
//				if (playIndex != -1) {
//					playIndex = getPlayIndex();
//				}
//			} else if (songMessage.getType() == SongMessage.SELECTPLAY) {
//				playIndex = getPlayIndex();
//				selectPlay(playlist.get(playIndex));
//			} else if (songMessage.getType() == SongMessage.SELECTPLAYED) {
//				playIndex = getPlayIndex();
//				selectPlay(playlist.get(playIndex));
//			} else if (songMessage.getType() == SongMessage.PLAYORSTOPMUSIC) {
//				playOrStop();
//			} else if (songMessage.getType() == SongMessage.NEXTMUSIC) {
//				nextPlay(false);
//			} else if (songMessage.getType() == SongMessage.FINISHNEXTMUSICED) {
//				nextPlay(true);
//			} else if (songMessage.getType() == SongMessage.PREVMUSIC) {
//				prevSong();
//			} else if (songMessage.getType() == SongMessage.SEEKTO) {
//				int progress = songMessage.getProgress();
//				seekTo(progress);
//			} else if (songMessage.getType() == SongMessage.PLAY) {
//				status = PLAYING;
//			} else if (songMessage.getType() == SongMessage.PLAYING) {
//				if (playSongInfo != null) {
//					SongInfo songInfo = songMessage.getSongInfo();
//					playSongInfo.setPlayProgress(songInfo.getPlayProgress());
//				}
//			}
//		}
//	}
//
//	/**
//	 * 快进
//	 * 
//	 * @param progress
//	 */
//	private void seekTo(int progress) {
//		// 如果服务正在运行，则是正在播放
//		if (isServiceRunning(context, MediaPlayerService.class.getName())) {
//			status = STOP;
//			context.stopService(playerService);
//		}
//		if (playSongInfo == null) {
//			return;
//		}
//		playSongInfo.setPlayProgress(progress);
//		play(playSongInfo);
//	}
//
//	/**
//	 * 播放或者暂停
//	 * 
//	 */
//	private void playOrStop() {
//		if (playIndex == -1) {
//			songMessage = new SongMessage();
//			songMessage.setType(SongMessage.ERROR);
//			String errorMessage = "没的选中相关的歌曲!!";
//			songMessage.setErrorMessage(errorMessage);
//			ObserverManage.getObserver().setMessage(songMessage);
//		} else {
//			play(playlist.get(playIndex));
//		}
//	}
//
//	/**
//	 * 上一首
//	 */
//	private void prevSong() {
//		if (playlist.size() == 0) {
//			songMessage = new SongMessage();
//			songMessage.setType(SongMessage.ERROR);
//			String errorMessage = "没有歌曲列表!!";
//			songMessage.setErrorMessage(errorMessage);
//			ObserverManage.getObserver().setMessage(songMessage);
//			return;
//		}
//		playIndex = getPlayIndex();
//
//		int playMode = Constants.PLAY_MODE;
//		// 默认是0单曲循环，1顺序播放，2随机播放
//		switch (playMode) {
//		case 0:
//			break;
//		case 1:
//			playIndex--;
//			if (playIndex < 0) {
//				playIndex = 0;
//
//				songMessage = new SongMessage();
//				songMessage.setType(SongMessage.ERROR);
//				String errorMessage = "已经是第一首了!!";
//				songMessage.setErrorMessage(errorMessage);
//				ObserverManage.getObserver().setMessage(songMessage);
//
//				return;
//			}
//			break;
//		case 2:
//			playIndex = new Random().nextInt(playlist.size());
//			break;
//		}
//
//		SongInfo tempSongInfo = playlist.get(playIndex);
//
//		Constants.PLAY_SID = tempSongInfo.getSid();
//		songMessage = new SongMessage();
//		songMessage.setType(SongMessage.PREVMUSICED);
//		songMessage.setSongInfo(tempSongInfo);
//
//		DataUtil.save(context, Constants.PLAY_SID_KEY, Constants.PLAY_SID);
//
//		ObserverManage.getObserver().setMessage(songMessage);
//
//		// 如果服务正在运行，则是正在播放
//		if (isServiceRunning(context, MediaPlayerService.class.getName())) {
//			status = STOP;
//			context.stopService(playerService);
//		}
//		if (playSongInfo != null) {
//			playSongInfo = null;
//		}
//		play(tempSongInfo);
//	}
//
//	/**
//	 * 下一首
//	 * 
//	 * @param isFinsh
//	 *            是否是播放完成后调用
//	 * 
//	 */
//	private void nextPlay(boolean isFinsh) {
//		if (playlist.size() == 0) {
//			songMessage = new SongMessage();
//			songMessage.setType(SongMessage.ERROR);
//			String errorMessage = "没有歌曲列表!!";
//			songMessage.setErrorMessage(errorMessage);
//			ObserverManage.getObserver().setMessage(songMessage);
//			return;
//		}
//		playIndex = getPlayIndex();
//
//		int playMode = Constants.PLAY_MODE;
//		// 默认是0单曲循环，1顺序播放，2随机播放
//		switch (playMode) {
//		case 0:
//			break;
//		case 1:
//			playIndex++;
//			if (playIndex >= playlist.size()) {
//				playIndex = playlist.size() - 1;
//
//				if (isFinsh) {
//					playSongInfo = null;
//					playIndex = -1;
//					Constants.PLAY_SID = "";
//					songMessage = new SongMessage();
//					songMessage.setType(SongMessage.LASTPLAYFINISH);
//
//					SongInfo tempSongInfo = new SongInfo();
//					tempSongInfo.setSid("");
//					tempSongInfo.setPlayProgress(0);
//					tempSongInfo.setDuration(100);
//					tempSongInfo.setArtist("歌手");
//					tempSongInfo.setDisplayName("歌名");
//					songMessage.setSongInfo(tempSongInfo);
//
//					ObserverManage.getObserver().setMessage(songMessage);
//
//					// 如果服务正在运行，则是正在播放
//					if (isServiceRunning(context,
//							MediaPlayerService.class.getName())) {
//						status = STOP;
//						context.stopService(playerService);
//					}
//
//					DataUtil.save(context, Constants.PLAY_SID_KEY,
//							Constants.PLAY_SID);
//
//				} else {
//					songMessage = new SongMessage();
//					songMessage.setType(SongMessage.ERROR);
//					String errorMessage = "已经是最后一首了!!";
//					songMessage.setErrorMessage(errorMessage);
//					ObserverManage.getObserver().setMessage(songMessage);
//				}
//				return;
//			}
//			break;
//		case 2:
//			playIndex = new Random().nextInt(playlist.size());
//			break;
//		}
//
//		SongInfo tempSongInfo = playlist.get(playIndex);
//
//		Constants.PLAY_SID = tempSongInfo.getSid();
//		songMessage = new SongMessage();
//		songMessage.setType(SongMessage.NEXTMUSICED);
//		songMessage.setSongInfo(tempSongInfo);
//		DataUtil.save(context, Constants.PLAY_SID_KEY, Constants.PLAY_SID);
//		ObserverManage.getObserver().setMessage(songMessage);
//
//		// 如果服务正在运行，则是正在播放
//		if (isServiceRunning(context, MediaPlayerService.class.getName())) {
//			status = STOP;
//			context.stopService(playerService);
//		}
//
//		if (playSongInfo != null) {
//			playSongInfo = null;
//		}
//		play(tempSongInfo);
//	}
//
//	/**
//	 * 选择播放歌曲
//	 * 
//	 * @param songInfo
//	 */
//	private void selectPlay(SongInfo songInfo) {
//		// 如果服务正在运行，则是正在播放
//		if (isServiceRunning(context, MediaPlayerService.class.getName())) {
//			status = STOP;
//			context.stopService(playerService);
//		}
//		if (playSongInfo != null) {
//			playSongInfo = null;
//		}
//		play(songInfo);
//	}
//
//	/**
//	 * 播放歌曲
//	 * 
//	 * @param songInfo
//	 */
//	private void play(SongInfo songInfo) {
//		status = STOP;
//		// 如果服务正在运行，则是正在播放
//		if (isServiceRunning(context, MediaPlayerService.class.getName())) {
//			context.stopService(playerService);
//			// 发送暂停信息
//			songMessage = new SongMessage();
//			songMessage.setSongInfo(playSongInfo);
//			songMessage.setType(SongMessage.STOPING);
//			ObserverManage.getObserver().setMessage(songMessage);
//
//			return;
//
//		}
//		if (playSongInfo == null) {
//			playSongInfo = songInfo;
//			playSongInfo.setPlayProgress(0);
//
//			// 发送历史歌曲数据给其它页面
//			songMessage = new SongMessage();
//			songMessage.setType(SongMessage.INIT);
//			songMessage.setSongInfo(songInfo);
//			ObserverManage.getObserver().setMessage(songMessage);
//		}
//		File file = new File(playSongInfo.getPath());
//		if (!file.exists()) {
//
//			songMessage = new SongMessage();
//			songMessage.setType(SongMessage.ERROR);
//			String errorMessage = "歌曲文件不存在，1秒后跳转下一首!!";
//			songMessage.setErrorMessage(errorMessage);
//			ObserverManage.getObserver().setMessage(songMessage);
//
//			if (isServiceRunning(context, MediaPlayerService.class.getName())) {
//				context.stopService(playerService);
//			}
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			// 跳转下一首
//			nextPlay(false);
//			return;
//		}
//
//		// 启动播放服务
//		context.startService(playerService);
//
//	}
//
//	/**
//	 * 判断某服务是否已经开启
//	 * 
//	 * @param mContext
//	 * @param className
//	 *            服务的类名
//	 * @return
//	 */
//	private boolean isServiceRunning(Context mContext, String className) {
//
//		boolean isRunning = false;
//		ActivityManager activityManager = (ActivityManager) mContext
//				.getSystemService(Context.ACTIVITY_SERVICE);
//		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
//				.getRunningServices(30);
//
//		if (!(serviceList.size() > 0)) {
//			return false;
//		}
//
//		for (int i = 0; i < serviceList.size(); i++) {
//			if (serviceList.get(i).service.getClassName().equals(className) == true) {
//				isRunning = true;
//				break;
//			}
//		}
//		return isRunning;
//	}
//
//	/*
//	 * 获取播放状态
//	 */
//	public int getPlayStatus() {
//		return status;
//	}
//
//	/**
//	 * 获取当前播放歌曲的索引
//	 * 
//	 * @return
//	 */
//	public int getPlayIndex() {
//		int index = -1;
//		playSID = Constants.PLAY_SID;
//		for (int i = 0; i < playlist.size(); i++) {
//			SongInfo tempSongInfo = playlist.get(i);
//			if (tempSongInfo.getSid().equals(playSID)) {
//				return i;
//			}
//		}
//		return index;
//	}
//
//	private void add(SongInfo songInfo) {
//		if (playlist == null || playlist.size() == 0) {
//			playlist = new ArrayList<SongInfo>();
//			playlist.add(songInfo);
//			return;
//		}
//		char category = songInfo.getCategory().charAt(0);
//		String childCategory = songInfo.getChildCategory();
//		for (int i = 0; i < playlist.size(); i++) {
//			SongInfo tempSongInfo = playlist.get(i);
//			char tempCategory = tempSongInfo.getCategory().charAt(0);
//			if (category == tempCategory) {
//				String tempChildCategory = tempSongInfo.getChildCategory();
//				if (childCategory.compareTo(tempChildCategory) < 0) {
//					playlist.add(i, songInfo);
//					return;
//				}
//			} else if (category < tempCategory) {
//				playlist.add(i, songInfo);
//				return;
//			} else if (i == playlist.size() - 1) {
//				playlist.add(songInfo);
//				return;
//			}
//		}
//	}
//
//	/**
//	 * 通过sid来删除 playlist 中的数据
//	 * 
//	 * @param sid
//	 */
//	private void refresh(String sid) {
//		if (playlist == null || playlist.size() == 0)
//			return;
//
//		for (int i = 0; i < playlist.size(); i++) {
//			if (playlist.get(i).getSid().equals(sid)) {
//				SongMessage songMessage = new SongMessage();
//				songMessage.setNum(-1);
//				songMessage.setType(SongMessage.DEL_NUM);
//				ObserverManage.getObserver().setMessage(songMessage);
//				playlist.remove(i);
//				break;
//			}
//		}
//	}
//
//	public List<SongInfo> getPlaylist() {
//		return playlist;
//	}
//
//	public SongInfo getPlaySongInfo() {
//		return playSongInfo;
//	}
//}
