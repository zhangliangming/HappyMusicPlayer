package com.happyplayer.ui;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.happyplayer.adapter.PopupPlayListAdapter;
import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.common.Constants;
import com.happyplayer.model.KscLyricsLineInfo;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.player.MediaManage;
import com.happyplayer.util.ActivityManager;
import com.happyplayer.util.DataUtil;
import com.happyplayer.util.KscLyricsManamge;
import com.happyplayer.util.KscLyricsParser;
import com.happyplayer.util.MediaUtils;
import com.happyplayer.widget.HBaseSeekBar;
import com.happyplayer.widget.KscTwoLineMLyricsView;

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

	private RelativeLayout seekbarRelativeLayout;

	/**
	 * 判断其是否是正在拖动
	 */
	private boolean isStartTrackingTouch = false;
	private HBaseSeekBar seekBar;

	private TextView songProgressTextView;

	private TextView songSizeTextView;

	/**
	 * 歌词解析
	 */
	private KscLyricsParser kscLyricsParser;
	/**
	 * 歌词
	 */
	private TreeMap<Integer, KscLyricsLineInfo> lyricsLineTreeMap;
	/**
	 * 歌词视图
	 */
	private KscTwoLineMLyricsView kscTwoLineLyricsView;

	private ImageView listImageButton;
	/**
	 * 播放列表弹出窗口
	 */
	private PopupWindow mPopupWindow;
	/**
	 * 弹出窗口播放列表
	 */
	private ListView popPlayListView;

	private TextView popPlaysumTextTextView;

	private Handler playmodeHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
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
		}
	};

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

				initKscLyrics(songInfo);

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

				initKscLyrics(songInfo);
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

				reshLrcView((int) songInfo.getPlayProgress());
				break;
			case SongMessage.STOPING:
				seekBar.setProgress((int) songInfo.getPlayProgress());
				songProgressTextView.setText(MediaUtils
						.formatTime((int) songInfo.getPlayProgress()));

				pauseImageButton.setVisibility(View.INVISIBLE);
				playImageButton.setVisibility(View.VISIBLE);

				reshLrcView((int) songInfo.getPlayProgress());
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

			initKscLyrics(songInfo);
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

	/**
	 * 初始化歌词
	 * 
	 * @param songInfo
	 *            当前歌曲的信息
	 */
	private void initKscLyrics(final SongInfo songInfo) {
		new AsyncTaskHandler() {
			@Override
			protected void onPostExecute(Object result) {
				kscLyricsParser = (KscLyricsParser) result;
				lyricsLineTreeMap = kscLyricsParser.getLyricsLineTreeMap();
				kscTwoLineLyricsView.init();
				if (lyricsLineTreeMap.size() != 0) {
					kscTwoLineLyricsView.setKscLyricsParser(kscLyricsParser);
					kscTwoLineLyricsView
							.setLyricsLineTreeMap(lyricsLineTreeMap);
					kscTwoLineLyricsView.setBlLrc(true);
					kscTwoLineLyricsView.invalidate();
				} else {
					kscTwoLineLyricsView.setBlLrc(false);
					kscTwoLineLyricsView.invalidate();
				}
			}

			@Override
			protected Object doInBackground() throws Exception {

				return KscLyricsManamge.getKscLyricsParser(songInfo
						.getDisplayName());
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

		seekbarRelativeLayout = (RelativeLayout) findViewById(R.id.seekbar);

		seekBar = (HBaseSeekBar) findViewById(R.id.playerSeekBar);
		seekBar.setEnabled(false);
		seekBar.setProgress(0);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// // 拖动条进度改变的时候调用
				if (isStartTrackingTouch) {
					int progress = seekBar.getProgress();
					// 往弹出窗口传输相关的进度
					seekBar.popupWindowShow(progress, seekbarRelativeLayout,
							kscTwoLineLyricsView.getTimeLrc(progress));

				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				int progress = seekBar.getProgress();
				// 往弹出窗口传输相关的进度
				seekBar.popupWindowShow(progress, seekbarRelativeLayout,
						kscTwoLineLyricsView.getTimeLrc(progress));
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

		kscTwoLineLyricsView = (KscTwoLineMLyricsView) findViewById(R.id.kscTwoLineLyricsView);

		listImageButton = (ImageView) findViewById(R.id.playlist_buttom);
		listImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				getPopupWindowInstance();

				int[] location = new int[2];
				listImageButton.getLocationOnScreen(location);

				mPopupWindow.showAtLocation(listImageButton,
						Gravity.NO_GRAVITY, location[0], location[1]
								- mPopupWindow.getHeight());
			}
		});
	}

	/**
	 * 获取PopupWindow实例
	 */
	private void getPopupWindowInstance() {
		if (null != mPopupWindow) {
			mPopupWindow.dismiss();
			return;
		} else {
			initPopuptWindow();

			List<SongInfo> playlist = MediaManage.getMediaManage(
					LrcViewActivity.this).getPlaylist();

			popPlaysumTextTextView.setText("播放列表(" + playlist.size() + ")");

			popPlayListView.setAdapter(new PopupPlayListAdapter(
					LrcViewActivity.this, playlist, popPlayListView,
					mPopupWindow));

			int playIndex = MediaManage.getMediaManage(LrcViewActivity.this)
					.getPlayIndex();
			if (playIndex != -1) {
				popPlayListView.setSelection(playIndex);
			}
		}
	}

	/**
	 * 创建PopupWindow
	 */
	private void initPopuptWindow() {
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		final View popupWindow = layoutInflater.inflate(
				R.layout.popup_lrc_playlist, null);

		mPopupWindow = new PopupWindow(popupWindow, getWindowManager()
				.getDefaultDisplay().getWidth() / 4 * 3, getWindowManager()
				.getDefaultDisplay().getHeight() / 3 * 2 - 80, true);

		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		mPopupWindow.setBackgroundDrawable(dw);

		// 设置popWindow的显示和消失动画
		// mPopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
		// 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
		mPopupWindow.setFocusable(true);
		// mPopupWindow.setOutsideTouchable(true);
		popupWindow.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// int bottomHeight = mMenu.getTop();
				int topHeight = popupWindow.findViewById(R.id.pop_layout)
						.getTop();
				int y = (int) event.getY();
				if (event.getAction() == MotionEvent.ACTION_UP) {
					// y > bottomHeight ||
					if (topHeight > y) {
						mPopupWindow.dismiss();
					}
				}
				return true;
			}
		});

		// popWindow消失监听方法
		mPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				mPopupWindow = null;
			}
		});

		/**
		 * 顺序播放
		 */
		final ImageView modeALLImageButton = (ImageView) popupWindow
				.findViewById(R.id.mode_all_buttom);
		/**
		 * 随机播放
		 */
		final ImageView modeRandomImageButton = (ImageView) popupWindow
				.findViewById(R.id.mode_random_buttom);
		/**
		 * 单曲循环
		 */
		final ImageView modeSingleImageButton = (ImageView) popupWindow
				.findViewById(R.id.mode_single_buttom);

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
				
				playmodeHandler.sendEmptyMessage(0);
			}
		});

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
				playmodeHandler.sendEmptyMessage(0);
			}
		});

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
				playmodeHandler.sendEmptyMessage(0);
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

		popPlayListView = (ListView) popupWindow
				.findViewById(R.id.playlistView);

		popPlaysumTextTextView = (TextView) popupWindow
				.findViewById(R.id.playsumText);
	}

	/**
	 * 
	 * @param playProgress
	 *            根据当前歌曲播放进度，刷新歌词
	 */
	private void reshLrcView(int playProgress) {
		// 判断当前的歌曲是否有歌词
		boolean blLrc = kscTwoLineLyricsView.getBlLrc();
		if (blLrc) {
			kscTwoLineLyricsView.showLrc(playProgress);
		}
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
