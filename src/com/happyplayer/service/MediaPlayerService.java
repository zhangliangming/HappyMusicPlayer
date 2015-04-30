package com.happyplayer.service;

import java.util.Observable;
import java.util.Observer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.IBinder;
import android.widget.Toast;

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

	/**
	 * 音频管理
	 */
	private AudioManager audioManager;

	// private boolean CAN_DUCK = false;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		context = MediaPlayerService.this.getBaseContext();
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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

			// int result = audioManager.requestAudioFocus(afChangeListener,
			// AudioManager.STREAM_MUSIC,
			// AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
			// 请求播放的音频焦点
			int result = audioManager.requestAudioFocus(afChangeListener,
			// 指定所使用的音频流
					AudioManager.STREAM_MUSIC,
					// 请求长时间的音频焦点
					AudioManager.AUDIOFOCUS_GAIN);
			if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				logger.i("获取音频焦点成功");
				// Toast.makeText(context, "获取音频焦点成功",
				// Toast.LENGTH_LONG).show();

				// if (CAN_DUCK) {
				// player.setVolume(0.5f, 0.5f);
				// } else {
				// player.setVolume(1.0f, 1.0f);
				// }
				player.start();
				isPlaying = true;
				if (playerThread == null) {
					playerThread = new Thread(new PlayerRunable());
					playerThread.start();
				}
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.PLAY);
				songMessage.setSongInfo(songInfo);
				ObserverManage.getObserver().setMessage(songMessage);

			} else {
				logger.i("获取音频焦点失败!!");
				Toast.makeText(context, "获取音频焦点失败!!", Toast.LENGTH_LONG).show();
			}

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

	OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			/**
			 * AUDIOFOCUS_GAIN：获得音频焦点。
			 * AUDIOFOCUS_LOSS：失去音频焦点，并且会持续很长时间。这是我们需要停止MediaPlayer的播放。
			 * AUDIOFOCUS_LOSS_TRANSIENT
			 * ：失去音频焦点，但并不会持续很长时间，需要暂停MediaPlayer的播放，等待重新获得音频焦点。
			 * AUDIOFOCUS_REQUEST_GRANTED 永久获取媒体焦点（播放音乐）
			 * AUDIOFOCUS_GAIN_TRANSIENT 暂时获取焦点 适用于短暂的音频
			 * AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK Duck我们应用跟其他应用共用焦点
			 * 我们播放的时候其他音频会降低音量
			 */
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				logger.i("AUDIOFOCUS_LOSS_TRANSIENT");
				// Toast.makeText(context, "AUDIOFOCUS_LOSS_TRANSIENT",
				// Toast.LENGTH_LONG).show();
				stop();

			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {

				// CAN_DUCK = true;
				if (player != null) {
					player.setVolume(0.5f, 0.5f);
				}

				// 降低音量
				// Toast.makeText(context, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK",
				// Toast.LENGTH_LONG).show();
				logger.i("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:" + focusChange);

			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

				if (player != null) {
					player.setVolume(1.0f, 1.0f);
				}
				// CAN_DUCK = false;

				// 恢复至正常音量
				logger.i("AUDIOFOCUS_GAIN");
				// Toast.makeText(context, "AUDIOFOCUS_GAIN", Toast.LENGTH_LONG)
				// .show();

				// if (player == null) {
				// play();
				// }

			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				logger.i("AUDIOFOCUS_LOSS");
				// Toast.makeText(context, "AUDIOFOCUS_LOSS", Toast.LENGTH_LONG)
				// .show();
				// audioManager.abandonAudioFocus(afChangeListener);
				stop();
			}
			// else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
			// {
			// logger.i("AUDIOFOCUS_REQUEST_GRANTED");
			// Toast.makeText(context, "AUDIOFOCUS_REQUEST_GRANTED",
			// Toast.LENGTH_LONG).show();
			// play();
			//
			// }
			else {
				// Toast.makeText(context, "focusChange:" + focusChange,
				// Toast.LENGTH_LONG).show();
				logger.i("focusChange:" + focusChange);
			}
		}
	};

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
		audioManager.abandonAudioFocus(afChangeListener);
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

	public void stop() {
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
	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SongMessage) {
			SongMessage songMessage = (SongMessage) data;
			if (songMessage.getType() == SongMessage.SEEKTO
					// || songMessage.getType() == SongMessage.PREVMUSIC
					// || songMessage.getType() == SongMessage.NEXTMUSIC
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
