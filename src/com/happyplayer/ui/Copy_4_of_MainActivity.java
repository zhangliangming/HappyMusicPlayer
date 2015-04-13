package com.happyplayer.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.happyplayer.adapter.PopupPlayListAdapter;
import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.common.Constants;
import com.happyplayer.common.CopyOfConstants;
import com.happyplayer.iface.PageAction;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.KscLyricsLineInfo;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.service.EasytouchService;
import com.happyplayer.service.FloatLrcService;
import com.happyplayer.service.LockService;
import com.happyplayer.slidingmenu.SlidingMenu;
import com.happyplayer.slidingmenu.SlidingMenu.OnClosedListener;
import com.happyplayer.slidingmenu.SlidingMenu.OnOpenedListener;
import com.happyplayer.util.ActivityManager;
import com.happyplayer.util.DataUtil;
import com.happyplayer.util.ImageUtil;
import com.happyplayer.util.KscLyricsManamge;
import com.happyplayer.util.KscLyricsParser;
import com.happyplayer.util.MediaUtils;
import com.happyplayer.widget.BaseSeekBar;
import com.happyplayer.widget.KscTwoLineLyricsView;

public class Copy_4_of_MainActivity extends FragmentActivity implements Observer {
	private ViewPager viewPager;
	/**
	 * 页面列表
	 */
	private ArrayList<Fragment> fragmentList;

	private View mainView;

	private long mExitTime;

	private TabFragmentPagerAdapter tabFragmentPagerAdapter;

	private ImageView flagImageView;
	private TextView timeTextView;

	private SlidingMenu mMenu;

	private MainPageAction action;

	private int TAB_INDEX = 0;
	/**
	 * 歌手图片和专辑图片
	 */
	private ImageView singerPicImageView;
	/**
	 * 歌名
	 */
	private TextView songNameTextView;
	/**
	 * 歌手
	 */
	private TextView singerNameTextView;
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
	 * 播放按钮
	 */
	private ImageButton playImageButton;
	/**
	 * 暂停按钮
	 */
	private ImageButton pauseImageButton;
	/**
	 * 下一首按钮
	 */
	private ImageButton nextImageButton;
	/**
	 * 播放列表按钮
	 */
	private ImageButton listImageButton;

	private BaseSeekBar seekBar;
	/**
	 * 判断其是否是正在拖动
	 */
	private boolean isStartTrackingTouch = false;
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
	private KscTwoLineLyricsView kscTwoLineLyricsView;

	private NotificationManager notificationManager;
	private Notification mNotification;
	private Notification mLrcNotification;
	BroadcastReceiver onClickReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("play")) {
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.PLAYORSTOPMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);
			} else if (intent.getAction().equals("pause")) {
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.PLAYORSTOPMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);
			} else if (intent.getAction().equals("next")) {
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.NEXTMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);
			} else if (intent.getAction().equals("prew")) {
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.PREVMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);
			} else if (intent.getAction().equals("close")) {
				close();
			} else if (intent.getAction().equals("lrcMove")) {
				if (Constants.DESLRCMOVE) {
					Constants.DESLRCMOVE = false;
				} else {
					Constants.DESLRCMOVE = true;
				}

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.DESLRCMOVE);
				ObserverManage.getObserver().setMessage(songMessage);

				notificationManager.cancel(1);
				createNotifiLrcView();

				new AsyncTaskHandler() {

					@Override
					protected void onPostExecute(Object result) {

					}

					protected Object doInBackground() throws Exception {

						DataUtil.save(Copy_4_of_MainActivity.this,
								Constants.DESLRCMOVE_KEY, Constants.DESLRCMOVE);
						return null;
					}
				}.execute();
			}
		}

	};

	private Handler notifyHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				// 自定义界面
				final RemoteViews mRemoteViews = new RemoteViews(
						getPackageName(), R.layout.notify_view);

				Intent buttoncloseIntent = new Intent("close");
				PendingIntent pendcloseButtonIntent = PendingIntent
						.getBroadcast(Copy_4_of_MainActivity.this, 0, buttoncloseIntent,
								0);

				mRemoteViews.setOnClickPendingIntent(R.id.close,
						pendcloseButtonIntent);

				Intent buttonplayIntent = new Intent("play");
				PendingIntent pendplayButtonIntent = PendingIntent
						.getBroadcast(Copy_4_of_MainActivity.this, 0, buttonplayIntent, 0);

				mRemoteViews.setOnClickPendingIntent(R.id.play,
						pendplayButtonIntent);

				Intent buttonpauseIntent = new Intent("pause");
				PendingIntent pendpauseButtonIntent = PendingIntent
						.getBroadcast(Copy_4_of_MainActivity.this, 0, buttonpauseIntent,
								0);

				Intent buttonnextIntent = new Intent("next");
				PendingIntent pendnextButtonIntent = PendingIntent
						.getBroadcast(Copy_4_of_MainActivity.this, 0, buttonnextIntent, 0);

				mRemoteViews.setOnClickPendingIntent(R.id.next,
						pendnextButtonIntent);

				Intent buttonprewtIntent = new Intent("prew");
				PendingIntent pendprewButtonIntent = PendingIntent
						.getBroadcast(Copy_4_of_MainActivity.this, 0, buttonprewtIntent,
								0);

				mRemoteViews.setOnClickPendingIntent(R.id.prew,
						pendprewButtonIntent);

				SongMessage songMessage = (SongMessage) msg.obj;
				final SongInfo songInfo = songMessage.getSongInfo();
				if (songInfo != null) {

					switch (songMessage.getType()) {
					case SongMessage.INIT:

						mRemoteViews.setTextViewText(R.id.songName,
								songInfo.getDisplayName());

						mRemoteViews.setImageViewResource(R.id.play,
								R.drawable.statusbar_btn_play);
						mRemoteViews.setOnClickPendingIntent(R.id.play,
								pendplayButtonIntent);

						// // 本地歌曲
						// if (songInfo.getType() == SongInfo.LOCAL) {
						// Bitmap bm = ImageUtil.getFirstArtwork(
						// songInfo.getPath(), songInfo.getSid());
						// if (bm != null) {
						// mRemoteViews.setImageViewBitmap(R.id.icon_pic,
						// bm);// 显示专辑封面图片
						// } else {
						// mRemoteViews.setImageViewResource(
						// R.id.icon_pic, R.drawable.ic_launcher);// 显示专辑封面图片
						// }
						// } else {
						// // 网上下载歌曲
						// }
						Bitmap bm = ImageUtil.getAlbum(Copy_4_of_MainActivity.this,
								songInfo.getPath(), songInfo.getSid(),
								songInfo.getDownUrl(), "");
						if (bm != null) {
							mRemoteViews.setImageViewBitmap(R.id.icon_pic, bm);// 显示专辑封面图片
						} else {
							mRemoteViews.setImageViewResource(R.id.icon_pic,
									R.drawable.ic_launcher);// 显示专辑封面图片
						}
						break;
					case SongMessage.LASTPLAYFINISH:

						mRemoteViews.setTextViewText(R.id.songName, "歌名");
						mRemoteViews.setImageViewResource(R.id.play,
								R.drawable.statusbar_btn_play);
						mRemoteViews.setOnClickPendingIntent(R.id.play,
								pendplayButtonIntent);

						break;
					case SongMessage.PLAYING:

						mRemoteViews.setImageViewResource(R.id.play,
								R.drawable.statusbar_btn_pause);
						mRemoteViews.setOnClickPendingIntent(R.id.play,
								pendpauseButtonIntent);

						break;
					case SongMessage.STOPING:

						mRemoteViews.setImageViewResource(R.id.play,
								R.drawable.statusbar_btn_play);
						mRemoteViews.setOnClickPendingIntent(R.id.play,
								pendplayButtonIntent);

						break;

					case SongMessage.ERROR:
						mRemoteViews.setTextViewText(R.id.songName, "歌名");
						mRemoteViews.setImageViewResource(R.id.play,
								R.drawable.statusbar_btn_play);
						mRemoteViews.setOnClickPendingIntent(R.id.play,
								pendplayButtonIntent);
						break;
					}
				} else {
					mRemoteViews.setImageViewResource(R.id.icon_pic,
							R.drawable.ic_launcher);// 显示专辑封面图片
				}

				mNotification.contentView = mRemoteViews;

				// mRemoteViews.setOnClickPendingIntent(R.id.play,
				// playPendingIntent());

				notificationManager.notify(0, mNotification);
				break;
			case 1:

				// 自定义界面
				final RemoteViews notifyLrcView = new RemoteViews(
						getPackageName(), R.layout.notify_lrc_view);

				Intent lrcMoveIntent = new Intent("lrcMove");
				PendingIntent pendlrcMoveIntent = PendingIntent.getBroadcast(
						Copy_4_of_MainActivity.this, 0, lrcMoveIntent, 0);

				notifyLrcView.setOnClickPendingIntent(R.id.bg,
						pendlrcMoveIntent);
				if (Constants.DESLRCMOVE) {
					notifyLrcView.setImageViewResource(R.id.lrc_pic,
							R.drawable.minilyric_desktop_unlocked);
					notifyLrcView.setTextViewText(R.id.title1, "点击解锁桌面歌词");
					notifyLrcView.setTextViewText(R.id.title2, "桌面歌词已解锁");
				} else {
					notifyLrcView.setImageViewResource(R.id.lrc_pic,
							R.drawable.minilyric_desktop_lock);
					notifyLrcView.setTextViewText(R.id.title1, "点击解锁桌面歌词");
					notifyLrcView.setTextViewText(R.id.title2, "桌面歌词已锁定");
				}

				mLrcNotification.contentView = notifyLrcView;

				notificationManager.notify(1, mLrcNotification);
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
				// // 本地歌曲
				// if (songInfo.getType() == SongInfo.LOCAL) {
				// new AsyncTaskHandler() {
				//
				// @Override
				// protected void onPostExecute(Object result) {
				// Bitmap bm = (Bitmap) result;
				// if (bm != null) {
				// singerPicImageView
				// .setBackgroundDrawable(new BitmapDrawable(
				// bm));
				// // 显示专辑封面图片
				// } else {
				// bm = MediaUtils.getDefaultArtwork(
				// MainActivity.this, false);
				// singerPicImageView
				// .setBackgroundDrawable(new BitmapDrawable(
				// bm));// 显示专辑封面图片
				// }
				// }
				//
				// @Override
				// protected Object doInBackground() throws Exception {
				// return ImageUtil.getFirstArtwork(
				// songInfo.getPath(), songInfo.getSid());
				// }
				// }.execute();
				//
				// } else {
				// // 网上下载歌曲
				// }

				ImageUtil.loadAlbum(Copy_4_of_MainActivity.this, singerPicImageView,
						R.drawable.playing_bar_default_avatar,
						songInfo.getPath(), songInfo.getSid(),
						songInfo.getDownUrl());

				// pauseImageButton.setVisibility(View.INVISIBLE);
				// playImageButton.setVisibility(View.VISIBLE);

				songNameTextView.setText(songInfo.getDisplayName());
				singerNameTextView.setText(songInfo.getArtist());
				seekBar.setEnabled(true);
				seekBar.setMax((int) songInfo.getDuration());
				seekBar.setProgress((int) songInfo.getPlayProgress());
				timeTextView
						.setText("-"
								+ MediaUtils.formatTime(songInfo.getSurplusProgress()));

				initKscLyrics(songInfo);

				break;
			case SongMessage.LASTPLAYFINISH:

				pauseImageButton.setVisibility(View.INVISIBLE);
				playImageButton.setVisibility(View.VISIBLE);
				songNameTextView.setText(songInfo.getDisplayName());
				singerNameTextView.setText(songInfo.getArtist());
				seekBar.setEnabled(false);
				seekBar.setMax((int) songInfo.getDuration());
				seekBar.setProgress((int) songInfo.getPlayProgress());
				timeTextView.setText("-00:00");

				Bitmap bm = MediaUtils.getDefaultArtwork(Copy_4_of_MainActivity.this,
						false);
				singerPicImageView
						.setBackgroundDrawable(new BitmapDrawable(bm));// 显示专辑封面图片

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

					timeTextView
							.setText("-"
									+ MediaUtils.formatTime(songInfo.getSurplusProgress()));
				}

				if (mMenu.isMenuShowing()) {
					reshLrcView((int) songInfo.getPlayProgress());
				}

				break;
			case SongMessage.STOPING:
				pauseImageButton.setVisibility(View.INVISIBLE);
				playImageButton.setVisibility(View.VISIBLE);

				seekBar.setProgress((int) songInfo.getPlayProgress());
				timeTextView
						.setText("-"
								+ MediaUtils.formatTime(songInfo.getSurplusProgress()));

				reshLrcView((int) songInfo.getPlayProgress());
				break;
			case SongMessage.ERROR:
				// pauseImageButton.setVisibility(View.INVISIBLE);
				// playImageButton.setVisibility(View.VISIBLE);

				String errorMessage = songMessage.getErrorMessage();
				Toast.makeText(Copy_4_of_MainActivity.this, errorMessage,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

	};

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

	/**
	 * 退出程序
	 */
	private void close() {

		Constants.APPCLOSE = true;

		if (Constants.SHOWEASYTOUCH) {
			Intent easytouchServiceIntent = new Intent(Copy_4_of_MainActivity.this,
					EasytouchService.class);
			stopService(easytouchServiceIntent);
		}
		if (Constants.SHOWDESLRC) {
			Intent floatLrcServiceIntent = new Intent(Copy_4_of_MainActivity.this,
					FloatLrcService.class);
			stopService(floatLrcServiceIntent);
		}

		if (CopyOfConstants.SHOWLOCK) {
			Intent lockServiceIntent = new Intent(this, LockService.class);
			stopService(lockServiceIntent);
		}

		unregisterReceiver(onClickReceiver);
		notificationManager.cancel(0);
		notificationManager.cancel(1);
		ActivityManager.getInstance().exit();
	}

	@Override
	protected void onDestroy() {
		close();
		super.onDestroy();
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainView = LayoutInflater.from(this).inflate(R.layout.activity_main,
				null);
		setContentView(mainView);
		init();
		createNotifiView();
		setBackground();

		if (Constants.SHOWDESLRC)
			createNotifiLrcView();

		if (Constants.SHOWEASYTOUCH) {
			Intent easytouchServiceIntent = new Intent(this,
					EasytouchService.class);
			startService(easytouchServiceIntent);
		}

		if (Constants.SHOWDESLRC) {
			Intent floatLrcServiceIntent = new Intent(this,
					FloatLrcService.class);
			startService(floatLrcServiceIntent);
		}

		if (CopyOfConstants.SHOWLOCK) {
			Intent lockServiceIntent = new Intent(this, LockService.class);
			startService(lockServiceIntent);
		}

		ObserverManage.getObserver().addObserver(this);
		ActivityManager.getInstance().addActivity(this);
	}

	private void init() {
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		fragmentList = new ArrayList<Fragment>();

		action = new MainPageAction();
		fragmentList.add(new MyFragment(action));

		tabFragmentPagerAdapter = new TabFragmentPagerAdapter(
				getSupportFragmentManager());
		viewPager.setAdapter(tabFragmentPagerAdapter);

		viewPager.setCurrentItem(0);

		// 设置viewpager的缓存页面
		// viewPager.setOffscreenPageLimit(fragmentList.size());
		viewPager.setOnPageChangeListener(new TabOnPageChangeListener());

		mMenu = (SlidingMenu) findViewById(R.id.player_bar_bg);
		mMenu.setMode(SlidingMenu.LEFT);
		mMenu.setFadeEnabled(false);
		mMenu.setBehindScrollScale(1f);
		mMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mMenu.setTouchModeAbove(SlidingMenu.SLIDING_CONTENT);

		View centMenu = LayoutInflater.from(this).inflate(R.layout.cent_menu,
				null, false);
		flagImageView = (ImageView) centMenu.findViewById(R.id.flag);
		timeTextView = (TextView) centMenu.findViewById(R.id.time);
		timeTextView.setVisibility(View.INVISIBLE);

		singerPicImageView = (ImageView) centMenu.findViewById(R.id.singer_pic);
		singerNameTextView = (TextView) centMenu.findViewById(R.id.singer_name);
		songNameTextView = (TextView) centMenu.findViewById(R.id.song_name);
		playImageButton = (ImageButton) centMenu.findViewById(R.id.play_buttom);
		playImageButton.setVisibility(View.VISIBLE);

		playImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.PLAYORSTOPMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);

			}
		});

		pauseImageButton = (ImageButton) centMenu
				.findViewById(R.id.pause_buttom);
		pauseImageButton.setVisibility(View.INVISIBLE);

		pauseImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.PLAYORSTOPMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);

			}
		});

		nextImageButton = (ImageButton) centMenu.findViewById(R.id.next_buttom);

		nextImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.NEXTMUSIC);
				ObserverManage.getObserver().setMessage(songMessage);

			}
		});

		listImageButton = (ImageButton) centMenu.findViewById(R.id.list_buttom);
		listImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				getPopupWindowInstance();

				int[] location = new int[2];
				mMenu.getLocationOnScreen(location);

				mPopupWindow.showAtLocation(mMenu, Gravity.NO_GRAVITY,
						location[0], location[1] - mPopupWindow.getHeight());
			}
		});

		mMenu.setContent(centMenu);

		View left_Menu = LayoutInflater.from(this).inflate(R.layout.left_menu,
				null, false);

		kscTwoLineLyricsView = (KscTwoLineLyricsView) left_Menu
				.findViewById(R.id.kscTwoLineLyricsView);

		mMenu.setMenu(left_Menu);
		mMenu.setOnOpenedListener(new OnOpenedListener() {

			@Override
			public void onOpened() {
				flagImageView
						.setBackgroundResource(R.drawable.kg_ic_playing_bar_drag_opened);
				timeTextView.setVisibility(View.VISIBLE);
				Constants.BAR_LRC_IS_OPEN = true;
				DataUtil.save(Copy_4_of_MainActivity.this, Constants.BAR_LRC_IS_OPEN_KEY,
						Constants.BAR_LRC_IS_OPEN);
			}
		});

		mMenu.setOnClosedListener(new OnClosedListener() {

			@Override
			public void onClosed() {
				flagImageView
						.setBackgroundResource(R.drawable.kg_ic_playing_bar_drag_closed);
				timeTextView.setVisibility(View.INVISIBLE);

				Constants.BAR_LRC_IS_OPEN = false;
				DataUtil.save(Copy_4_of_MainActivity.this, Constants.BAR_LRC_IS_OPEN_KEY,
						Constants.BAR_LRC_IS_OPEN);
			}
		});

		seekBar = (BaseSeekBar) centMenu.findViewById(R.id.seekBar);
		seekBar.setEnabled(false);
		seekBar.setProgress(0);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// // 拖动条进度改变的时候调用
				if (isStartTrackingTouch) {
					int progress = seekBar.getProgress();
					// System.out.println("onProgressChanged :--->" + progress);
					// 往弹出窗口传输相关的进度
					seekBar.popupWindowShow(progress, mMenu,
							kscTwoLineLyricsView.getTimeLrc(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				int progress = seekBar.getProgress();
				// System.out.println("onStartTrackingTouch :--->" + progress);
				// 拖动条开始拖动的时候调用
				seekBar.popupWindowShow(progress, mMenu,
						kscTwoLineLyricsView.getTimeLrc(progress));
				isStartTrackingTouch = true;
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				isStartTrackingTouch = false;
				// 拖动条停止拖动的时候调用
				seekBar.popupWindowDismiss();

				int progress = seekBar.getProgress();
				// System.out.println("onStopTrackingTouch :--->" + progress);

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.SEEKTO);
				songMessage.setProgress(progress);
				ObserverManage.getObserver().setMessage(songMessage);
			}
		});

		if (Constants.BAR_LRC_IS_OPEN) {
			mMenu.toggle();
		}

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
					Copy_4_of_MainActivity.this).getPlaylist();

			popPlaysumTextTextView.setText("播放列表(" + playlist.size() + ")");

			popPlayListView
					.setAdapter(new PopupPlayListAdapter(Copy_4_of_MainActivity.this,
							playlist, popPlayListView, mPopupWindow));

			int playIndex = MediaManage.getMediaManage(Copy_4_of_MainActivity.this)
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
				R.layout.popup_main_playlist, null);

		mPopupWindow = new PopupWindow(popupWindow, LayoutParams.FILL_PARENT,
				getWindowManager().getDefaultDisplay().getHeight()
						- mMenu.getHeight() - 50, true);

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
				Toast.makeText(Copy_4_of_MainActivity.this, "随机播放", Toast.LENGTH_SHORT)
						.show();

				Constants.PLAY_MODE = 2;
				DataUtil.save(Copy_4_of_MainActivity.this, Constants.PLAY_MODE_KEY,
						Constants.PLAY_MODE);
			}
		});

		modeRandomImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				modeALLImageButton.setVisibility(View.INVISIBLE);
				modeRandomImageButton.setVisibility(View.INVISIBLE);
				modeSingleImageButton.setVisibility(View.VISIBLE);
				Toast.makeText(Copy_4_of_MainActivity.this, "单曲循环", Toast.LENGTH_SHORT)
						.show();

				Constants.PLAY_MODE = 0;
				DataUtil.save(Copy_4_of_MainActivity.this, Constants.PLAY_MODE_KEY,
						Constants.PLAY_MODE);
			}
		});

		modeSingleImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				modeALLImageButton.setVisibility(View.VISIBLE);
				modeRandomImageButton.setVisibility(View.INVISIBLE);
				modeSingleImageButton.setVisibility(View.INVISIBLE);

				Toast.makeText(Copy_4_of_MainActivity.this, "顺序播放", Toast.LENGTH_SHORT)
						.show();

				Constants.PLAY_MODE = 1;
				DataUtil.save(Copy_4_of_MainActivity.this, Constants.PLAY_MODE_KEY,
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

		popPlayListView = (ListView) popupWindow
				.findViewById(R.id.playlistView);

		popPlaysumTextTextView = (TextView) popupWindow
				.findViewById(R.id.playsumText);
	}

	private void createNotifiView() {

		// 获取到系统的notificationManager
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		IntentFilter filter = new IntentFilter();
		filter.addAction("play");
		filter.addAction("pause");
		filter.addAction("next");
		filter.addAction("prew");
		filter.addAction("close");
		registerReceiver(onClickReceiver, filter);
		// 更新通知栏
		int icon = R.drawable.icon;
		CharSequence tickerText = "乐乐音乐，传播好的音乐";
		long when = System.currentTimeMillis();
		mNotification = new Notification(icon, tickerText, when);
		// FLAG_AUTO_CANCEL 该通知能被状态栏的清除按钮给清除掉
		// FLAG_NO_CLEAR 该通知不能被状态栏的清除按钮给清除掉
		// FLAG_ONGOING_EVENT 通知放置在正在运行
		// FLAG_INSISTENT 是否一直进行，比如音乐一直播放，知道用户响应
		mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		// mNotification.flags |= Notification.FLAG_NO_CLEAR;

		// DEFAULT_ALL 使用所有默认值，比如声音，震动，闪屏等等
		// DEFAULT_LIGHTS 使用默认闪光提示
		// DEFAULT_SOUND 使用默认提示声音
		// DEFAULT_VIBRATE 使用默认手机震动，需加上<uses-permission
		// android:name="android.permission.VIBRATE" />权限
		// mNotification.defaults = Notification.DEFAULT_SOUND;

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClass(Copy_4_of_MainActivity.this, Copy_4_of_MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		PendingIntent pendingIntent = PendingIntent
				.getActivity(Copy_4_of_MainActivity.this, 0, intent,
						PendingIntent.FLAG_CANCEL_CURRENT);
		mNotification.contentIntent = pendingIntent;

		SongMessage songMessage = new SongMessage();
		songMessage.setSongInfo(null);
		Message msg = new Message();
		msg.what = 0;
		msg.obj = songMessage;
		notifyHandler.sendMessage(msg);
	}

	private void createNotifiLrcView() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("lrcMove");
		registerReceiver(onClickReceiver, filter);
		// 更新通知栏
		int icon;
		if (Constants.DESLRCMOVE) {
			icon = R.drawable.minilyric_desktop_unlocked;
		} else {
			icon = R.drawable.minilyric_desktop_lock;
		}

		CharSequence tickerText = "";
		long when = System.currentTimeMillis();
		mLrcNotification = new Notification(icon, tickerText, when);
		// FLAG_AUTO_CANCEL 该通知能被状态栏的清除按钮给清除掉
		// FLAG_NO_CLEAR 该通知不能被状态栏的清除按钮给清除掉
		// FLAG_ONGOING_EVENT 通知放置在正在运行
		// FLAG_INSISTENT 是否一直进行，比如音乐一直播放，知道用户响应
		mLrcNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		// mNotification.flags |= Notification.FLAG_NO_CLEAR;

		// DEFAULT_ALL 使用所有默认值，比如声音，震动，闪屏等等
		// DEFAULT_LIGHTS 使用默认闪光提示
		// DEFAULT_SOUND 使用默认提示声音
		// DEFAULT_VIBRATE 使用默认手机震动，需加上<uses-permission
		// android:name="android.permission.VIBRATE" />权限
		// mNotification.defaults = Notification.DEFAULT_SOUND;

		SongMessage songMessage = new SongMessage();
		songMessage.setSongInfo(null);
		Message msg = new Message();
		msg.what = 1;
		notifyHandler.sendMessage(msg);
	}

	private void setBackground() {
		viewPager
				.setBackgroundResource(Constants.PICIDS[Constants.DEF_PIC_INDEX]);
	}

	/**
	 * 
	 * viewpager的监听事件
	 * 
	 */
	private class TabOnPageChangeListener implements OnPageChangeListener {

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		public void onPageSelected(int arg0) {
			TAB_INDEX = arg0;
			if (TAB_INDEX == 0 && fragmentList.size() == 2) {
				fragmentList.remove(1);
				tabFragmentPagerAdapter = new TabFragmentPagerAdapter(
						getSupportFragmentManager());
				viewPager.setAdapter(tabFragmentPagerAdapter);
			}
		}
	}

	/**
	 * 
	 * @author Administrator Fragment滑动事件
	 */
	public class TabFragmentPagerAdapter extends FragmentPagerAdapter {

		public TabFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragmentList.get(arg0);
		}

		@Override
		public int getCount() {
			return fragmentList.size();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (TAB_INDEX == 0) {
				if ((System.currentTimeMillis() - mExitTime) > 2000) {
					Toast.makeText(this, R.string.exit_tip, Toast.LENGTH_SHORT)
							.show();
					mExitTime = System.currentTimeMillis();
				} else {
					close();
				}
			} else {
				viewPager.setCurrentItem(0);
			}
		}
		return false;
	}

	/**
	 * 打开歌曲窗口
	 * 
	 * @param v
	 */
	public void openLrcDialog(View v) {
		Intent intent = new Intent(this, LrcViewActivity.class);
		startActivity(intent);
	}

	private class MainPageAction implements PageAction {

		@Override
		public void addPage(Fragment fragment) {
			if (!fragmentList.contains(fragment)) {
				if (fragmentList.size() == 2) {
					fragmentList.remove(1);
				}
				fragmentList.add(fragment);
				tabFragmentPagerAdapter.notifyDataSetChanged();
			}
			viewPager.setCurrentItem(fragmentList.size());
		}

		@Override
		public void finish() {
			viewPager.setCurrentItem(0);
		}
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
					|| songMessage.getType() == SongMessage.ERROR
					|| songMessage.getType() == SongMessage.LASTPLAYFINISH) {
				Message msg = new Message();
				msg.obj = songMessage;
				songHandler.sendMessage(msg);

				Message msg2 = new Message();
				msg2.obj = songMessage;
				notifyHandler.sendMessage(msg2);
			} else if (songMessage.getType() == SongMessage.EXIT) {
				close();
			} else if (songMessage.getType() == SongMessage.DES_LRC) {
				notificationManager.cancel(1);
				if (Constants.SHOWDESLRC) {
					createNotifiLrcView();
				}
			}
		}
	}
}
