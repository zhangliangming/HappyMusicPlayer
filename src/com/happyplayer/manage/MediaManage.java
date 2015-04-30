package com.happyplayer.manage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import android.content.Context;

import com.happyplayer.common.Constants;
import com.happyplayer.db.SongDB;
import com.happyplayer.logger.MyLogger;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.service.MediaPlayerService;
import com.happyplayer.util.DataUtil;

public class MediaManage implements Observer {
	private static MediaManage _mediaManage;
	private List<SongInfo> playlist;
	private SongInfo playSongInfo;
	private int playIndex = -1;
	private String playSID = "";
	private SongMessage songMessage;

	public static final int PLAYING = 1;

	public static final int STOP = 0;

	private int status = 0;

	private Context context;

	private MyLogger logger = MyLogger.getLogger(Constants.USERNAME);

	public MediaManage(Context context) {
		this.context = context;
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

				playSongInfo = tempSongInfo;

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
			} else if (songMessage.getType() == SongMessage.DELALLMUSIC) {
				delAllMusic();
			} else if (songMessage.getType() == SongMessage.ADDMUSIC) {
				SongInfo songInfo = songMessage.getSongInfo();
				add(songInfo);
			} else if (songMessage.getType() == SongMessage.SCAN_NUM) {
				if (playIndex != -1) {
					playIndex = getPlayIndex();
				}
			} else if (songMessage.getType() == SongMessage.SELECTPLAY) {
				playIndex = getPlayIndex();
				selectPlay(playlist.get(playIndex));
			} else if (songMessage.getType() == SongMessage.SELECTPLAYED) {
				playIndex = getPlayIndex();
				selectPlay(playlist.get(playIndex));
			} else if (songMessage.getType() == SongMessage.PLAYORSTOPMUSIC) {
				playOrStop();
			} else if (songMessage.getType() == SongMessage.NEXTMUSIC) {
				nextPlay(false);
			} else if (songMessage.getType() == SongMessage.FINISHNEXTMUSICED) {
				nextPlay(true);
			} else if (songMessage.getType() == SongMessage.PREVMUSIC) {
				prevSong();
			} else if (songMessage.getType() == SongMessage.SEEKTO) {
				int progress = songMessage.getProgress();
				seekTo(progress);
			} else if (songMessage.getType() == SongMessage.PLAY) {
				status = PLAYING;
			} else if (songMessage.getType() == SongMessage.PLAYING) {
				if (playSongInfo != null) {
					SongInfo songInfo = songMessage.getSongInfo();
					if (playSongInfo.getSid().equals(songInfo.getSid())) {
						playSongInfo
								.setPlayProgress(songInfo.getPlayProgress());
					}
				}
			} else if (songMessage.getType() == SongMessage.STOPPLAY) {
				stop();
			} else if (songMessage.getType() == SongMessage.STOPING) {
				if (status != STOP)
					status = STOP;
			}
		}
	}

	/**
	 * 快进
	 * 
	 * @param progress
	 */
	private void seekTo(int progress) {
		// // 如果服务正在运行，则是正在播放
		// if (MediaPlayerService.isPlaying) {
		// status = STOP;
		// context.stopService(playerService);
		// }
		if (playSongInfo == null) {
			return;
		}
		playSongInfo.setPlayProgress(progress);
		play(playSongInfo);
	}

	/**
	 * 暂停播放
	 */
	private void stop() {
		if (playIndex == -1) {
			return;
		}
		// 如果服务正在运行，则是正在播放
		if (MediaPlayerService.isPlaying) {
			status = STOP;
			songMessage = new SongMessage();
			songMessage.setType(SongMessage.STOP);
			ObserverManage.getObserver().setMessage(songMessage);
			return;
		}
	}

	/**
	 * 播放或者暂停
	 * 
	 */
	private void playOrStop() {
		if (playIndex == -1) {
			songMessage = new SongMessage();
			songMessage.setType(SongMessage.ERROR);
			String errorMessage = "没的选中相关的歌曲!!";
			songMessage.setErrorMessage(errorMessage);
			ObserverManage.getObserver().setMessage(songMessage);
		} else {
			// 如果服务正在运行，则是正在播放
			if (MediaPlayerService.isPlaying) {
				status = STOP;
				// context.stopService(playerService);

				songMessage = new SongMessage();
				songMessage.setType(SongMessage.STOP);
				ObserverManage.getObserver().setMessage(songMessage);

				return;
			}
			play(playlist.get(playIndex));
		}
	}

	/**
	 * 上一首
	 */
	private void prevSong() {
		if (playlist.size() == 0) {
			songMessage = new SongMessage();
			songMessage.setType(SongMessage.ERROR);
			String errorMessage = "没有歌曲列表!!";
			songMessage.setErrorMessage(errorMessage);
			ObserverManage.getObserver().setMessage(songMessage);
			return;
		}
		playIndex = getPlayIndex();

		int playMode = Constants.PLAY_MODE;
		// 默认是0单曲循环，1顺序播放，2随机播放
		switch (playMode) {
		case 0:
			break;
		case 1:
			playIndex--;
			if (playIndex < 0) {
				playIndex = 0;

				songMessage = new SongMessage();
				songMessage.setType(SongMessage.ERROR);
				String errorMessage = "已经是第一首了!!";
				songMessage.setErrorMessage(errorMessage);
				ObserverManage.getObserver().setMessage(songMessage);

				return;
			}
			break;
		case 2:
			playIndex = new Random().nextInt(playlist.size());
			break;
		}

		SongInfo tempSongInfo = playlist.get(playIndex);

		Constants.PLAY_SID = tempSongInfo.getSid();
		songMessage = new SongMessage();
		songMessage.setType(SongMessage.PREVMUSICED);
		songMessage.setSongInfo(tempSongInfo);

		DataUtil.save(context, Constants.PLAY_SID_KEY, Constants.PLAY_SID);

		ObserverManage.getObserver().setMessage(songMessage);

		// // 如果服务正在运行，则是正在播放
		// if (MediaPlayerService.isPlaying) {
		// status = STOP;
		// context.stopService(playerService);
		// }
		if (playSongInfo != null) {
			playSongInfo = null;
		}
		play(tempSongInfo);
	}

	/**
	 * 下一首
	 * 
	 * @param isFinsh
	 *            是否是播放完成后调用
	 * 
	 */
	private void nextPlay(boolean isFinsh) {
		if (playlist.size() == 0) {
			songMessage = new SongMessage();
			songMessage.setType(SongMessage.ERROR);
			String errorMessage = "没有歌曲列表!!";
			songMessage.setErrorMessage(errorMessage);
			ObserverManage.getObserver().setMessage(songMessage);
			return;
		}
		playIndex = getPlayIndex();

		int playMode = Constants.PLAY_MODE;
		// 默认是0单曲循环，1顺序播放，2随机播放
		switch (playMode) {
		case 0:
			break;
		case 1:
			playIndex++;
			if (playIndex >= playlist.size()) {
				playIndex = playlist.size() - 1;

				if (isFinsh) {
					playSongInfo = null;
					playIndex = -1;
					Constants.PLAY_SID = "";
					songMessage = new SongMessage();
					songMessage.setType(SongMessage.LASTPLAYFINISH);

					SongInfo tempSongInfo = new SongInfo();
					tempSongInfo.setSid("");
					tempSongInfo.setPlayProgress(0);
					tempSongInfo.setDuration(100);
					tempSongInfo.setArtist("歌手");
					tempSongInfo.setDisplayName("歌名");
					songMessage.setSongInfo(tempSongInfo);

					ObserverManage.getObserver().setMessage(songMessage);

					// 如果服务正在运行，则是正在播放
					if (MediaPlayerService.isPlaying) {
						status = STOP;
						// context.stopService(playerService);
						songMessage = new SongMessage();
						songMessage.setType(SongMessage.STOP);
						ObserverManage.getObserver().setMessage(songMessage);

					}

					DataUtil.save(context, Constants.PLAY_SID_KEY,
							Constants.PLAY_SID);

				} else {
					songMessage = new SongMessage();
					songMessage.setType(SongMessage.ERROR);
					String errorMessage = "已经是最后一首了!!";
					songMessage.setErrorMessage(errorMessage);
					ObserverManage.getObserver().setMessage(songMessage);
				}
				return;
			}
			break;
		case 2:
			playIndex = new Random().nextInt(playlist.size());
			break;
		}

		SongInfo tempSongInfo = playlist.get(playIndex);

		Constants.PLAY_SID = tempSongInfo.getSid();
		songMessage = new SongMessage();
		songMessage.setType(SongMessage.NEXTMUSICED);
		songMessage.setSongInfo(tempSongInfo);
		DataUtil.save(context, Constants.PLAY_SID_KEY, Constants.PLAY_SID);
		ObserverManage.getObserver().setMessage(songMessage);

		// // 如果服务正在运行，则是正在播放
		// if (MediaPlayerService.isPlaying) {
		// status = STOP;
		// context.stopService(playerService);
		// }

		if (playSongInfo != null) {
			playSongInfo = null;
		}
		play(tempSongInfo);
	}

	/**
	 * 选择播放歌曲
	 * 
	 * @param songInfo
	 */
	private void selectPlay(SongInfo songInfo) {
		// // 如果服务正在运行，则是正在播放
		// if (MediaPlayerService.isPlaying) {
		// status = STOP;
		// context.stopService(playerService);
		// }
		if (playSongInfo != null) {
			playSongInfo = null;
		}
		play(songInfo);
	}

	/**
	 * 播放歌曲
	 * 
	 * @param songInfo
	 */
	private void play(SongInfo songInfo) {
		status = STOP;
		if (playSongInfo == null) {
			playSongInfo = songInfo;
			playSongInfo.setPlayProgress(0);

			// 发送历史歌曲数据给其它页面
			songMessage = new SongMessage();
			songMessage.setType(SongMessage.INIT);
			songMessage.setSongInfo(songInfo);
			ObserverManage.getObserver().setMessage(songMessage);

		}
		File file = new File(playSongInfo.getPath());
		if (!file.exists()) {

			songMessage = new SongMessage();
			songMessage.setType(SongMessage.ERROR);
			String errorMessage = "歌曲文件不存在，1秒后跳转下一首!!";
			songMessage.setErrorMessage(errorMessage);
			ObserverManage.getObserver().setMessage(songMessage);

			if (MediaPlayerService.isPlaying) {
				// context.stopService(playerService);
				songMessage = new SongMessage();
				songMessage.setType(SongMessage.STOP);
				ObserverManage.getObserver().setMessage(songMessage);

			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 跳转下一首
			nextPlay(false);
			return;
		}
		// 启动播放服务
		// context.startService(playerService);
		songMessage = new SongMessage();
		songMessage.setType(SongMessage.TOPLAY);
		ObserverManage.getObserver().setMessage(songMessage);

	}

	/*
	 * 获取播放状态
	 */
	public int getPlayStatus() {
		return status;
	}

	/**
	 * 获取当前播放歌曲的索引
	 * 
	 * @return
	 */
	public int getPlayIndex() {
		int index = -1;
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
	 * 删除所有的歌曲列表
	 */
	private void delAllMusic() {
		if (playlist == null || playlist.size() == 0)
			return;
		int size = 0 - playlist.size();
		for (int i = 0; i < playlist.size(); i++) {
			if (playSongInfo != null) {
				if (playlist.get(i).getSid().equals(playSongInfo.getSid())) {
					stopMusic();
				}
			}
		}
		playlist = new ArrayList<SongInfo>();
		SongDB.getSongInfoDB(context).delete();
		SongMessage songMessage = new SongMessage();
		songMessage.setNum(size);
		songMessage.setType(SongMessage.DELALLMUSICED);
		ObserverManage.getObserver().setMessage(songMessage);

	}

	/**
	 * stop停止正在播放的歌曲
	 */
	private void stopMusic() {

		// 如果正在播放
		if (MediaPlayerService.isPlaying) {
			status = STOP;
			songMessage = new SongMessage();
			songMessage.setType(SongMessage.STOP);
			ObserverManage.getObserver().setMessage(songMessage);

		}

		playSongInfo = null;
		playIndex = -1;
		Constants.PLAY_SID = "";
		songMessage = new SongMessage();
		songMessage.setType(SongMessage.LASTPLAYFINISH);

		SongInfo tempSongInfo = new SongInfo();
		tempSongInfo.setSid("");
		tempSongInfo.setPlayProgress(0);
		tempSongInfo.setDuration(100);
		tempSongInfo.setArtist("歌手");
		tempSongInfo.setDisplayName("歌名");
		songMessage.setSongInfo(tempSongInfo);

		ObserverManage.getObserver().setMessage(songMessage);

		new Thread() {

			@Override
			public void run() {
				DataUtil.save(context, Constants.PLAY_SID_KEY,
						Constants.PLAY_SID);
			}

		}.start();

	}

	/**
	 * 通过sid来删除 playlist 中的数据
	 * 
	 * @param sid
	 */
	private void refresh(final String sid) {
		if (playlist == null || playlist.size() == 0)
			return;
		new Thread() {

			@Override
			public void run() {
				SongDB.getSongInfoDB(context).delete(sid);
			}

		}.start();
		for (int i = 0; i < playlist.size(); i++) {
			if (playlist.get(i).getSid().equals(sid)) {
				if (playSongInfo != null) {
					if (playlist.get(i).getSid().equals(playSongInfo.getSid())) {
						stopMusic();
					}
				}
				SongMessage songMessage = new SongMessage();
				songMessage.setNum(-1);
				songMessage.setSongInfo(playlist.get(i));
				songMessage.setType(SongMessage.DEL_NUM);
				ObserverManage.getObserver().setMessage(songMessage);
				playlist.remove(i);
				break;
			}
		}
	}

	public List<SongInfo> getPlaylist() {
		return playlist;
	}

	public SongInfo getPlaySongInfo() {
		return playSongInfo;
	}
}
