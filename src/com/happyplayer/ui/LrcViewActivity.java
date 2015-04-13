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
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.happyplayer.adapter.PopupLrcPlayListAdapter;
import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.common.Constants;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.KscLyricsLineInfo;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
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

	/**
	 * 颜色面板
	 */
	private ImageView imageviews[];

	/**
	 * 游标
	 */
	private ImageView flagimageviews[];

	private PopupWindow mPopupWindowDialog;

	/**
	 * 显示面板倒计时
	 */
	public int EndTime = -1;

	private PopupLrcPlayListAdapter adapter;

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
			case SongMessage.PLAY:
				if (pauseImageButton.getVisibility() != View.VISIBLE) {
					pauseImageButton.setVisibility(View.VISIBLE);
				}
				if (playImageButton.getVisibility() != View.INVISIBLE) {
					playImageButton.setVisibility(View.INVISIBLE);
				}
				break;
			case SongMessage.PLAYING:

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
			int status = MediaManage.getMediaManage(LrcViewActivity.this)
					.getPlayStatus();
			if (status == MediaManage.STOP) {
				pauseImageButton.setVisibility(View.INVISIBLE);
				playImageButton.setVisibility(View.VISIBLE);
			} else {
				pauseImageButton.setVisibility(View.VISIBLE);
				playImageButton.setVisibility(View.INVISIBLE);
			}

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

			adapter = new PopupLrcPlayListAdapter(LrcViewActivity.this,
					playlist, popPlayListView, mPopupWindow);
			popPlayListView.setAdapter(adapter);

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

		ImageView deleList = (ImageView) popupWindow
				.findViewById(R.id.dele_list);
		deleList.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.DELALLMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);
			}
		});

		popPlayListView = (ListView) popupWindow
				.findViewById(R.id.playlistView);

		popPlaysumTextTextView = (TextView) popupWindow
				.findViewById(R.id.playsumText);
	}

	/**
	 * 返回键退出程序
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			initPopupWindowInstance();
		}
		return super.onKeyDown(keyCode, event);
	}

	/*
	 * 获取PopupWindow实例
	 */
	private void initPopupWindowInstance() {
		if (mPopupWindowDialog != null && mPopupWindowDialog.isShowing()) {
			mPopupWindowDialog.dismiss();
		} else {
			initDialogPopuptWindow();

			if (EndTime < 0) {
				EndTime = 3000;
				mHandler.post(upDateVol);
			} else {
				EndTime = 3000;
			}
		}
	}

	Runnable upDateVol = new Runnable() {

		@Override
		public void run() {
			if (EndTime >= 0) {
				EndTime -= 200;
				mHandler.postDelayed(upDateVol, 200);
			} else {
				if (mPopupWindowDialog != null
						&& mPopupWindowDialog.isShowing()) {
					mPopupWindowDialog.dismiss();
				}
			}

		}
	};

	private void initDialogPopuptWindow() {

		int length = Constants.LRCCOLORS.length;
		imageviews = new ImageView[length];
		flagimageviews = new ImageView[length];
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View popupWindow = layoutInflater.inflate(R.layout.lrc_color, null);
		int i = 0;
		imageviews[i] = (ImageView) popupWindow.findViewById(R.id.colorpanel0);
		flagimageviews[i] = (ImageView) popupWindow
				.findViewById(R.id.select_flag0);
		flagimageviews[i].setVisibility(View.INVISIBLE);
		imageviews[i].setOnClickListener(new MyImageViewOnClickListener());
		imageviews[i].setBackgroundColor(Constants.LRCCOLORS[i++]);
		imageviews[i] = (ImageView) popupWindow.findViewById(R.id.colorpanel1);
		flagimageviews[i] = (ImageView) popupWindow
				.findViewById(R.id.select_flag1);
		flagimageviews[i].setVisibility(View.INVISIBLE);
		imageviews[i].setOnClickListener(new MyImageViewOnClickListener());
		imageviews[i].setBackgroundColor(Constants.LRCCOLORS[i++]);
		imageviews[i] = (ImageView) popupWindow.findViewById(R.id.colorpanel2);
		flagimageviews[i] = (ImageView) popupWindow
				.findViewById(R.id.select_flag2);
		flagimageviews[i].setVisibility(View.INVISIBLE);
		imageviews[i].setOnClickListener(new MyImageViewOnClickListener());
		imageviews[i].setBackgroundColor(Constants.LRCCOLORS[i++]);
		imageviews[i] = (ImageView) popupWindow.findViewById(R.id.colorpanel3);
		flagimageviews[i] = (ImageView) popupWindow
				.findViewById(R.id.select_flag3);
		flagimageviews[i].setVisibility(View.INVISIBLE);
		imageviews[i].setOnClickListener(new MyImageViewOnClickListener());
		imageviews[i].setBackgroundColor(Constants.LRCCOLORS[i++]);
		imageviews[i] = (ImageView) popupWindow.findViewById(R.id.colorpanel4);
		flagimageviews[i] = (ImageView) popupWindow
				.findViewById(R.id.select_flag4);
		flagimageviews[i].setVisibility(View.INVISIBLE);
		imageviews[i].setOnClickListener(new MyImageViewOnClickListener());
		imageviews[i].setBackgroundColor(Constants.LRCCOLORS[i++]);
		imageviews[i] = (ImageView) popupWindow.findViewById(R.id.colorpanel5);
		flagimageviews[i] = (ImageView) popupWindow
				.findViewById(R.id.select_flag5);
		flagimageviews[i].setVisibility(View.INVISIBLE);
		imageviews[i].setOnClickListener(new MyImageViewOnClickListener());
		imageviews[i].setBackgroundColor(Constants.LRCCOLORS[i++]);

		flagimageviews[Constants.LRC_COLOR_INDEX].setVisibility(View.VISIBLE);

		// 初始化弹出窗口
		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;

		mPopupWindowDialog = new PopupWindow(popupWindow,
				LayoutParams.FILL_PARENT, screenWidth / 6, true);
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		// 设置SelectPicPopupWindow弹出窗体的背景
		mPopupWindowDialog.setBackgroundDrawable(dw);
		// mPopupWindowDialog.setFocusable(true);
		mPopupWindowDialog.setOutsideTouchable(true);

		int[] location = new int[2];
		kscTwoLineLyricsView.getLocationOnScreen(location);

		mPopupWindowDialog.showAtLocation(kscTwoLineLyricsView,
				Gravity.NO_GRAVITY, location[0], location[1]
						- mPopupWindowDialog.getHeight());
	}

	private class MyImageViewOnClickListener implements OnClickListener {

		public void onClick(View arg0) {
			EndTime = 3000;
			int index = 0;
			int id = arg0.getId();
			switch (id) {
			case R.id.colorpanel0:
				index = 0;
				break;
			case R.id.colorpanel1:
				index = 1;
				break;
			case R.id.colorpanel2:
				index = 2;
				break;
			case R.id.colorpanel3:
				index = 3;
				break;
			case R.id.colorpanel4:
				index = 4;
				break;
			case R.id.colorpanel5:
				index = 5;
				break;
			default:
				break;
			}
			Constants.LRC_COLOR_INDEX = index;
			for (int i = 0; i < imageviews.length; i++) {
				if (i == index)
					flagimageviews[i].setVisibility(View.VISIBLE);
				else
					flagimageviews[i].setVisibility(View.INVISIBLE);
			}
			kscTwoLineLyricsView.invalidate();
			DataUtil.save(LrcViewActivity.this, Constants.LRC_COLOR_INDEX_KEY,
					Constants.LRC_COLOR_INDEX);
		}
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
					|| songMessage.getType() == SongMessage.LASTPLAYFINISH
					|| songMessage.getType() == SongMessage.PLAY) {
				Message msg = new Message();
				msg.obj = songMessage;
				songHandler.sendMessage(msg);
			} else if (songMessage.getType() == SongMessage.DEL_NUM) {
				popHandler.sendEmptyMessage(0);
			} else if (songMessage.getType() == SongMessage.DELALLMUSICED) {
				popHandler.sendEmptyMessage(1);
			}
		}
	}

	private Handler popHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (null != mPopupWindow) {
				List<SongInfo> playlist = MediaManage.getMediaManage(
						LrcViewActivity.this).getPlaylist();
				popPlaysumTextTextView.setText("播放列表(" + playlist.size() + ")");
				if (msg.what == 1) {
					if (adapter != null) {
						ObserverManage.getObserver().deleteObserver(adapter);
					}
					adapter = new PopupLrcPlayListAdapter(LrcViewActivity.this,
							playlist, popPlayListView, mPopupWindow);

					popPlayListView.setAdapter(adapter);

					int playIndex = MediaManage.getMediaManage(
							LrcViewActivity.this).getPlayIndex();
					if (playIndex != -1) {
						popPlayListView.setSelection(playIndex);
					}
				}
			}
		}

	};
}
