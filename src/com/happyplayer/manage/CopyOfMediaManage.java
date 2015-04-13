package com.happyplayer.manage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.PowerManager;

import com.happyplayer.common.Constants;
import com.happyplayer.db.SongDB;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.util.DataUtil;

public class CopyOfMediaManage implements Observer {
	private static CopyOfMediaManage _mediaManage;
	private List<SongInfo> playlist;
	private SongInfo playSongInfo;
	private int playIndex = -1;
	private String playSID = "";

	private static MediaPlayer player = null;
	private Thread playerThread = null;
	private SongMessage songMessage;

	public static final int PLAYING = 1;

	public static final int STOP = 0;

	private int status = 0;

	private Context context;

	public CopyOfMediaManage(Context context) {
		this.context = context;
		init(context);
	}

	public static CopyOfMediaManage getMediaManage(Context context) {
		if (_mediaManage == null) {
			_mediaManage = new CopyOfMediaManage(context);
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
		ObserverManage.getObserver().addObserver(CopyOfMediaManage.this);
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
			} else if (songMessage.getType() == SongMessage.PREVMUSIC) {
				prevSong();
			} else if (songMessage.getType() == SongMessage.SEEKTO) {
				int progress = songMessage.getProgress();
				seekTo(progress);
			}
		}
	}

	/**
	 * 快进
	 * 
	 * @param progress
	 */
	private void seekTo(int progress) {
		if (player != null) {
			if (player.isPlaying())
				player.stop();
			player.reset();
			player.release();
			player = null;
		}
		if (playSongInfo == null) {
			return;
		}
		playSongInfo.setPlayProgress(progress);
		if (playerThread == null) {
			playerThread = new Thread(new PlayerRunable());
			playerThread.start();
		}
		if (player == null) {
			player = new MediaPlayer();
			// 设定CUP锁定
			player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
			player.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					nextPlay(true);
				}
			});

			// 播放音乐时发生错误的事件处理
			player.setOnErrorListener(new MediaPlayer.OnErrorListener() {

				public boolean onError(MediaPlayer mp, int what, int extra) {
					// 释放资源
					try {
						mp.release();
					} catch (Exception e) {
						e.printStackTrace();
					}

					return false;
				}
			});

			try {
				File file = new File(playSongInfo.getPath());
				if (!file.exists()) {

					songMessage = new SongMessage();
					songMessage.setType(SongMessage.ERROR);
					String errorMessage = "歌曲文件不存在，跳转下一首!!";
					songMessage.setErrorMessage(errorMessage);
					ObserverManage.getObserver().setMessage(songMessage);

					Thread.sleep(500);
					// 跳转下一首
					nextPlay(false);
					return;
				}

				player.setDataSource(file.getAbsolutePath());
				player.prepare();
				if (playSongInfo.getPlayProgress() != 0) {
					player.seekTo((int) playSongInfo.getPlayProgress());
				}
				player.start();

			} catch (Exception e) {
				e.printStackTrace();

				songMessage = new SongMessage();
				songMessage.setType(SongMessage.ERROR);
				String errorMessage = "歌曲文件格式不支持，跳转下一首!!";
				songMessage.setErrorMessage(errorMessage);
				ObserverManage.getObserver().setMessage(songMessage);

				// 跳转下一首
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				nextPlay(false);
				return;
			}
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

		if (player != null) {
			if (player.isPlaying())
				player.stop();
			player.reset();
			player.release();
			player = null;
		}
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

					if (player != null) {
						if (player.isPlaying())
							player.stop();
						player.reset();
						player.release();
						player = null;
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

		if (player != null) {
			if (player.isPlaying())
				player.stop();
			player.reset();
			player.release();
			player = null;
		}
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
		if (player != null) {
			if (player.isPlaying())
				player.stop();
			player.reset();
			player.release();
			player = null;
		}
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
		if (playerThread == null) {
			playerThread = new Thread(new PlayerRunable());
			playerThread.start();
		}

		status = STOP;

		if (player == null) {
			player = new MediaPlayer();
			player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
			player.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					nextPlay(true);
				}
			});

			// 播放音乐时发生错误的事件处理
			player.setOnErrorListener(new MediaPlayer.OnErrorListener() {

				public boolean onError(MediaPlayer mp, int what, int extra) {
					// 释放资源
					try {
						mp.release();
					} catch (Exception e) {
						e.printStackTrace();
					}

					return false;
				}
			});

			if (playSongInfo == null) {
				playSongInfo = songInfo;
				playSongInfo.setPlayProgress(0);

				// 发送历史歌曲数据给其它页面
				songMessage = new SongMessage();
				songMessage.setType(SongMessage.INIT);
				songMessage.setSongInfo(songInfo);
				ObserverManage.getObserver().setMessage(songMessage);
			}
			try {
				File file = new File(playSongInfo.getPath());
				if (!file.exists()) {

					songMessage = new SongMessage();
					songMessage.setType(SongMessage.ERROR);
					String errorMessage = "歌曲文件不存在，跳转下一首!!";
					songMessage.setErrorMessage(errorMessage);
					ObserverManage.getObserver().setMessage(songMessage);

					Thread.sleep(500);
					// 跳转下一首
					nextPlay(false);
					return;
				}

				player.setDataSource(file.getAbsolutePath());
				player.prepare();
				if (playSongInfo.getPlayProgress() != 0) {
					player.seekTo((int) playSongInfo.getPlayProgress());
				}
				player.start();
				status = PLAYING;

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.PLAY);
				songMessage.setSongInfo(playSongInfo);
				ObserverManage.getObserver().setMessage(songMessage);

			} catch (Exception e) {
				e.printStackTrace();

				songMessage = new SongMessage();
				songMessage.setType(SongMessage.ERROR);
				String errorMessage = "歌曲文件格式不支持，跳转下一首!!";
				songMessage.setErrorMessage(errorMessage);
				ObserverManage.getObserver().setMessage(songMessage);

				// 跳转下一首
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				nextPlay(false);

				return;
			}
		} else {
			if (player != null) {
				if (player.isPlaying())
					player.stop();
				player.reset();
				player.release();
				player = null;
				status = STOP;
				songMessage = new SongMessage();
				songMessage.setSongInfo(playSongInfo);
				songMessage.setType(SongMessage.STOPING);
				ObserverManage.getObserver().setMessage(songMessage);
			}
		}
	}

	private class PlayerRunable implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(100);
					if (player != null && player.isPlaying()) {
						if (playSongInfo != null) {
							playSongInfo.setPlayProgress(player
									.getCurrentPosition());

							SongMessage songMessage = new SongMessage();
							songMessage.setType(SongMessage.PLAYING);
							songMessage.setSongInfo(playSongInfo);
							ObserverManage.getObserver()
									.setMessage(songMessage);
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
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

	public List<SongInfo> getPlaylist() {
		return playlist;
	}

	public SongInfo getPlaySongInfo() {
		return playSongInfo;
	}
}
