package com.happyplayer.service;

import java.util.Observable;
import java.util.Observer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.IBinder;

import com.happyplayer.common.Constants;
import com.happyplayer.logger.MyLogger;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;

/**
 * 
 * 播放器服务，主要负责播放
 * 
 */
public class MediaPlayerService extends Service implements Observer {
	private MyLogger logger = MyLogger.getLogger(Constants.USERNAME);
	public static Boolean isServiceRunning = false;
	public static Boolean isPlaying = false;
	private Thread playerThread = null;
	private SongMessage songMessage;
	private MediaPlayer player;
	private Context context;

	private SongInfo songInfo;

	private Boolean isFirstStart = true;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		context = MediaPlayerService.this.getBaseContext();
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		ObserverManage.getObserver().addObserver(this);
		isServiceRunning = true;
		if (!isFirstStart) {
			isFirstStart = false;
			play();
		}
		logger.i("------MediaPlayerService被创建了------");
	}

	/**
	 * 播放
	 */
	private void play() {
		if (player == null) {
			player = new MediaPlayer();
		}
		songInfo = MediaManage.getMediaManage(context).getPlaySongInfo();
		if (songInfo == null) {
			return;
		}
		try {
			player.reset();
			player.setDataSource(songInfo.getPath());
			player.prepare();
			if (songInfo.getPlayProgress() != 0) {
				player.seekTo((int) songInfo.getPlayProgress());
			}
			player.start();
			isPlaying = true;
			if (playerThread == null) {
				playerThread = new Thread(new PlayerRunable());
				playerThread.start();
			}

			isServiceRunning = true;

			SongMessage songMessage = new SongMessage();
			songMessage.setType(SongMessage.PLAY);
			songMessage.setSongInfo(songInfo);
			ObserverManage.getObserver().setMessage(songMessage);

		} catch (Exception e) {
			e.printStackTrace();
			songMessage = new SongMessage();
			songMessage.setType(SongMessage.ERROR);
			String errorMessage = "歌曲文件格式不支持，1秒后跳转下一首!!";
			songMessage.setErrorMessage(errorMessage);
			ObserverManage.getObserver().setMessage(songMessage);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			if (songMessage != null) {
				// 跳转下一首
				songMessage = new SongMessage();
				songMessage.setType(SongMessage.NEXTMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);
			}
		}
		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				// 下一首
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.FINISHNEXTMUSICED);
				ObserverManage.getObserver().setMessage(songMessage);
			}
		});

		player.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int arg1, int arg2) {
				songMessage = new SongMessage();
				songMessage.setType(SongMessage.ERROR);
				String errorMessage = "播放歌曲出错，1秒后跳转下一首!!";
				songMessage.setErrorMessage(errorMessage);
				ObserverManage.getObserver().setMessage(songMessage);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				if (songMessage != null) {
					// 跳转下一首
					songMessage = new SongMessage();
					songMessage.setType(SongMessage.NEXTMUSIC);
					ObserverManage.getObserver().setMessage(songMessage);
				}
				return false;
			}
		});
	}

	private class PlayerRunable implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(100);
					if (player != null && player.isPlaying()) {
						if (songInfo != null) {
							songInfo.setPlayProgress(player
									.getCurrentPosition());

							songMessage = new SongMessage();
							songMessage.setType(SongMessage.PLAYING);
							songMessage.setSongInfo(songInfo);
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

	@Override
	public void onDestroy() {
		logger.i("------MediaPlayerService被回收了------");
		isServiceRunning = false;
		if (player != null) {
			if (player.isPlaying()) {
				player.stop();
				isPlaying = false;
				if (songInfo != null) {
					songMessage = new SongMessage();
					songMessage.setSongInfo(songInfo);
					songMessage.setType(SongMessage.STOPING);
					ObserverManage.getObserver().setMessage(songMessage);
				}
			}
			player.reset();
			player.release();
			player = null;
		}
		ObserverManage.getObserver().deleteObserver(this);
		super.onDestroy();
		int status = MediaManage.getMediaManage(context).getPlayStatus();
		logger.i("status:-->" + status);
		logger.i("Constants.APPCLOSE:-->" + Constants.APPCLOSE);
		// 如果当前的状态不是暂停，如果播放服务被回收了，要重新启动服务
		if (!Constants.APPCLOSE && status != MediaManage.STOP) {
			// 在此重新启动,使服务常驻内存
			startService(new Intent(this, MediaPlayerService.class));
		}
	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SongMessage) {
			SongMessage songMessage = (SongMessage) data;
			if (songMessage.getType() == SongMessage.SEEKTO
					|| songMessage.getType() == SongMessage.PREVMUSIC
					|| songMessage.getType() == SongMessage.NEXTMUSIC
					|| songMessage.getType() == SongMessage.FINISHNEXTMUSICED
					|| songMessage.getType() == SongMessage.SELECTPLAYED
					|| songMessage.getType() == SongMessage.SELECTPLAY) {
				if (player != null) {
					if (player.isPlaying())
						player.stop();
					isPlaying = false;
					player.reset();
					player.release();
					player = null;
				}
			} else if (songMessage.getType() == SongMessage.STOP) {
				if (player != null) {
					if (player.isPlaying()) {
						player.stop();
						isPlaying = false;
						if (songInfo != null) {
							songMessage = new SongMessage();
							songMessage.setSongInfo(songInfo);
							songMessage.setType(SongMessage.STOPING);
							ObserverManage.getObserver()
									.setMessage(songMessage);
						}
					}
					player.reset();
					player.release();
					player = null;
				}
			} else if (songMessage.getType() == SongMessage.TOPLAY) {
				play();
			}
		}
	}
}
