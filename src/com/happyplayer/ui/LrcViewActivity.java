package com.happyplayer.ui;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.common.Constants;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.player.MediaManage;
import com.happyplayer.util.ActivityManager;
import com.happyplayer.util.DataUtil;
import com.happyplayer.util.MediaUtils;
import com.happyplayer.widget.HBaseSeekBar;

public class LrcViewActivity extends Activity implements Observer {

	private RelativeLayout parent;

	private TextView songNameTextView;

	private TextView songerTextView;

	/**
	 * 播放按钮
	 */
	private ImageView playImageButton;
	/**
	 * 暂停按钮
	 */
	private ImageView pauseImageButton;
	/**
	 * 下一首按钮
	 */
	private ImageView nextImageButton;
	/**
	 * 上一首按钮
	 */
	private ImageView prevImageButton;

	/**
	 * 顺序播放
	 */
	private ImageView modeALLImageButton;
	/**
	 * 随机播放
	 */
	private ImageView modeRandomImageButton;
	/**
	 * 单曲循环
	 */
	private ImageView modeSingleImageButton;

	/**
	 * 判断其是否是正在拖动
	 */
	private boolean isStartTrackingTouch = false;
	private HBaseSeekBar seekBar;

	private TextView songProgressTextView;

	private TextView songSizeTextView;

	private Handler songHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			SongMessage songMessage = (SongMessage) msg.obj;
			final SongInfo songInfo = songMessage.getSongInfo();
			switch (songMessage.getType()) {
			case SongMessage.INIT:

				songNameTextView.setText(songInfo.getDisplayName());
				songerTextView.setText(songInfo.getArtist());

				seekBar.setEnabled(true);
				seekBar.setMax((int) songInfo.getDuration());
				seekBar.setProgress((int) songInfo.getPlayProgress());

				songProgressTextView.setText(MediaUtils
						.formatTime((int) songInfo.getPlayProgress()));
				songSizeTextView.setText(MediaUtils.formatTime((int) songInfo
						.getDuration()));

				break;
			case SongMessage.LASTPLAYFINISH:

				pauseImageButton.setVisibility(View.INVISIBLE);
				playImageButton.setVisibility(View.VISIBLE);
				songNameTextView.setText(songInfo.getDisplayName());
				songerTextView.setText(songInfo.getArtist());
				seekBar.setEnabled(false);
				seekBar.setMax((int) songInfo.getDuration());
				seekBar.setProgress((int) songInfo.getPlayProgress());
				songProgressTextView.setText("00:00");
				songSizeTextView.setText("00:00");

				break;

			case SongMessage.PLAYING:

				if (pauseImageButton.getVisibility() != View.VISIBLE) {
					pauseImageButton.setVisibility(View.VISIBLE);
				}
				if (playImageButton.getVisibility() != View.INVISIBLE) {
					playImageButton.setVisibility(View.INVISIBLE);
				}
				if (!isStartTrackingTouch) {
					seekBar.setProgress((int) songInfo.getPlayProgress());
					songProgressTextView.setText(MediaUtils
							.formatTime((int) songInfo.getPlayProgress()));
				}
				break;
			case SongMessage.STOPING:
				seekBar.setProgress((int) songInfo.getPlayProgress());
				songProgressTextView.setText(MediaUtils
						.formatTime((int) songInfo.getPlayProgress()));

				pauseImageButton.setVisibility(View.INVISIBLE);
				playImageButton.setVisibility(View.VISIBLE);
				break;

			}
		}

	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			SongInfo songInfo = (SongInfo) msg.obj;

			songNameTextView.setText(songInfo.getDisplayName());
			songerTextView.setText(songInfo.getArtist());

			pauseImageButton.setVisibility(View.INVISIBLE);
			playImageButton.setVisibility(View.VISIBLE);

			seekBar.setEnabled(true);
			seekBar.setMax((int) songInfo.getDuration());
			seekBar.setProgress((int) songInfo.getPlayProgress());

			songProgressTextView.setText(MediaUtils.formatTime((int) songInfo
					.getPlayProgress()));
			songSizeTextView.setText(MediaUtils.formatTime((int) songInfo
					.getDuration()));
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lrc_view);
		init();
		setBackground();
		loadData();
		ObserverManage.getObserver().addObserver(this);
		ActivityManager.getInstance().addActivity(this);
	}

	private void loadData() {
		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {

			}

			@Override
			protected Object doInBackground() throws Exception {
				SongInfo songInfo = MediaManage.getMediaManage(
						LrcViewActivity.this).getPlaySongInfo();
				if (songInfo != null) {
					Message msg = new Message();
					msg.obj = songInfo;
					mHandler.sendMessage(msg);
				}
				return null;
			}
		}.execute();

	}

	private void init() {
		songProgressTextView = (TextView) findViewById(R.id.songProgress);
		songSizeTextView = (TextView) findViewById(R.id.songSize);

		parent = (RelativeLayout) findViewById(R.id.parent);

		songNameTextView = (TextView) findViewById(R.id.songName);
		songerTextView = (TextView) findViewById(R.id.songer);

		playImageButton = (ImageView) findViewById(R.id.playing_buttom);
		playImageButton.setVisibility(View.VISIBLE);

		playImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.PLAYORSTOPMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);

			}
		});

		pauseImageButton = (ImageView) findViewById(R.id.pause_buttom);
		pauseImageButton.setVisibility(View.INVISIBLE);

		pauseImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.PLAYORSTOPMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);

			}
		});

		nextImageButton = (ImageView) findViewById(R.id.next_buttom);

		nextImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.NEXTMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);

			}
		});

		prevImageButton = (ImageView) findViewById(R.id.prev_buttom);

		prevImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.PREVMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);

			}
		});

		modeALLImageButton = (ImageView) findViewById(R.id.mode_all_buttom);

		modeALLImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				modeALLImageButton.setVisibility(View.INVISIBLE);
				modeRandomImageButton.setVisibility(View.VISIBLE);
				modeSingleImageButton.setVisibility(View.INVISIBLE);
				Toast.makeText(LrcViewActivity.this, "随机播放", Toast.LENGTH_SHORT)
						.show();

				Constants.PLAY_MODE = 2;
				DataUtil.save(LrcViewActivity.this, Constants.PLAY_MODE_KEY,
						Constants.PLAY_MODE);
			}
		});

		modeRandomImageButton = (ImageView) findViewById(R.id.mode_random_buttom);

		modeRandomImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				modeALLImageButton.setVisibility(View.INVISIBLE);
				modeRandomImageButton.setVisibility(View.INVISIBLE);
				modeSingleImageButton.setVisibility(View.VISIBLE);
				Toast.makeText(LrcViewActivity.this, "单曲循环", Toast.LENGTH_SHORT)
						.show();

				Constants.PLAY_MODE = 0;
				DataUtil.save(LrcViewActivity.this, Constants.PLAY_MODE_KEY,
						Constants.PLAY_MODE);
			}
		});

		modeSingleImageButton = (ImageView) findViewById(R.id.mode_single_buttom);

		modeSingleImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				modeALLImageButton.setVisibility(View.VISIBLE);
				modeRandomImageButton.setVisibility(View.INVISIBLE);
				modeSingleImageButton.setVisibility(View.INVISIBLE);

				Toast.makeText(LrcViewActivity.this, "顺序播放", Toast.LENGTH_SHORT)
						.show();

				Constants.PLAY_MODE = 1;
				DataUtil.save(LrcViewActivity.this, Constants.PLAY_MODE_KEY,
						Constants.PLAY_MODE);
			}
		});

		// 默认是0单曲循环，1顺序播放，2随机播放
		switch (Constants.PLAY_MODE) {
		case 0:
			modeALLImageButton.setVisibility(View.INVISIBLE);
			modeRandomImageButton.setVisibility(View.INVISIBLE);
			modeSingleImageButton.setVisibility(View.VISIBLE);
			break;
		case 1:
			modeALLImageButton.setVisibility(View.VISIBLE);
			modeRandomImageButton.setVisibility(View.INVISIBLE);
			modeSingleImageButton.setVisibility(View.INVISIBLE);
			break;
		case 2:
			modeALLImageButton.setVisibility(View.INVISIBLE);
			modeRandomImageButton.setVisibility(View.VISIBLE);
			modeSingleImageButton.setVisibility(View.INVISIBLE);
			break;
		}

		seekBar = (HBaseSeekBar) findViewById(R.id.playerSeekBar);
		seekBar.setEnabled(false);
		seekBar.setProgress(0);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// // 拖动条进度改变的时候调用
				if (isStartTrackingTouch) {
					// 往弹出窗口传输相关的进度
					seekBar.popupWindowShow(seekBar.getProgress());
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// 拖动条开始拖动的时候调用
				seekBar.popupWindowShow(seekBar.getProgress());
				isStartTrackingTouch = true;
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				isStartTrackingTouch = false;
				// 拖动条停止拖动的时候调用
				seekBar.popupWindowDismiss();

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.SEEKTO);
				songMessage.setProgress(seekBar.getProgress());
				ObserverManage.getObserver().setMessage(songMessage);
			}
		});
	}

	public void back(View v) {
		finish();
	}

	private void setBackground() {
		parent.setBackgroundResource(Constants.PICIDS[Constants.DEF_PIC_INDEX]);
	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SkinMessage) {
			SkinMessage msg = (SkinMessage) data;
			if (msg.type == SkinMessage.PIC) {
				setBackground();
			}
		} else if (data instanceof SongMessage) {
			SongMessage songMessage = (SongMessage) data;
			if (songMessage.getType() == SongMessage.INIT
					|| songMessage.getType() == SongMessage.PLAYING
					|| songMessage.getType() == SongMessage.STOPING
					|| songMessage.getType() == SongMessage.LASTPLAYFINISH) {
				Message msg = new Message();
				msg.obj = songMessage;
				songHandler.sendMessage(msg);
			}
		}
	}
}
