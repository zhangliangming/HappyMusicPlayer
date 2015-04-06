package com.happyplayer.ui;

import java.util.ArrayList;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.common.Constants;
import com.happyplayer.iface.PageAction;
import com.happyplayer.model.KscLyricsLineInfo;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
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

public class Copy_3_of_MainActivity extends FragmentActivity implements
		Observer {
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
				unregisterReceiver(onClickReceiver);
				notificationManager.cancel(0);
				ActivityManager.getInstance().exit();
			}
		}

	};

	private Handler notifyHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// 自定义界面
			final RemoteViews mRemoteViews = new RemoteViews(getPackageName(),
					R.layout.notify_view);

			Intent buttoncloseIntent = new Intent("close");
			PendingIntent pendcloseButtonIntent = PendingIntent.getBroadcast(
					Copy_3_of_MainActivity.this, 0, buttoncloseIntent, 0);

			mRemoteViews.setOnClickPendingIntent(R.id.close,
					pendcloseButtonIntent);

			Intent buttonplayIntent = new Intent("play");
			PendingIntent pendplayButtonIntent = PendingIntent.getBroadcast(
					Copy_3_of_MainActivity.this, 0, buttonplayIntent, 0);

			mRemoteViews.setOnClickPendingIntent(R.id.play,
					pendplayButtonIntent);

			Intent buttonpauseIntent = new Intent("pause");
			PendingIntent pendpauseButtonIntent = PendingIntent.getBroadcast(
					Copy_3_of_MainActivity.this, 0, buttonpauseIntent, 0);

			Intent buttonnextIntent = new Intent("next");
			PendingIntent pendnextButtonIntent = PendingIntent.getBroadcast(
					Copy_3_of_MainActivity.this, 0, buttonnextIntent, 0);

			mRemoteViews.setOnClickPendingIntent(R.id.next,
					pendnextButtonIntent);

			Intent buttonprewtIntent = new Intent("prew");
			PendingIntent pendprewButtonIntent = PendingIntent.getBroadcast(
					Copy_3_of_MainActivity.this, 0, buttonprewtIntent, 0);

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
					// mRemoteViews.setImageViewBitmap(R.id.icon_pic, bm);//
					// 显示专辑封面图片
					// } else {
					// mRemoteViews.setImageViewResource(R.id.icon_pic,
					// R.drawable.ic_launcher);// 显示专辑封面图片
					// }
					// } else {
					// // 网上下载歌曲
					// }
					ImageUtil.loadAlbum(Copy_3_of_MainActivity.this,
							singerPicImageView,
							R.drawable.playing_bar_default_avatar,
							songInfo.getPath(), songInfo.getSid(),
							songInfo.getDownUrl());

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
				// Copy_3_of_MainActivity.this, false);
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
				ImageUtil.loadAlbum(Copy_3_of_MainActivity.this,
						singerPicImageView,
						R.drawable.playing_bar_default_avatar,
						songInfo.getPath(), songInfo.getSid(),
						songInfo.getDownUrl());

				// pauseImageButton.setVisibility(View.INVISIBLE);
				// playImageButton.setVisibility(View.VISIBLE);

				songNameTextView.setText(songInfo.getDisplayName());
				singerNameTextView.setText(songInfo.getArtist());
				seekBar.setEnabled(true);
				seekBar.setProgress((int) songInfo.getPlayProgress());
				seekBar.setMax((int) songInfo.getDuration());
				timeTextView.setText("-"
						+ MediaUtils.formatTime(songInfo.getSurplusProgress()));

				initKscLyrics(songInfo);

				break;
			case SongMessage.LASTPLAYFINISH:

				pauseImageButton.setVisibility(View.INVISIBLE);
				playImageButton.setVisibility(View.VISIBLE);
				songNameTextView.setText(songInfo.getDisplayName());
				singerNameTextView.setText(songInfo.getArtist());
				seekBar.setEnabled(false);
				seekBar.setProgress((int) songInfo.getPlayProgress());
				seekBar.setMax((int) songInfo.getDuration());
				timeTextView.setText("-00:00");

				Bitmap bm = MediaUtils.getDefaultArtwork(
						Copy_3_of_MainActivity.this, false);
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

					timeTextView.setText("-"
							+ MediaUtils.formatTime(songInfo
									.getSurplusProgress()));
				}

				if (mMenu.isMenuShowing()) {
					reshLrcView((int) songInfo.getPlayProgress());
				}

				break;
			case SongMessage.STOPING:
				pauseImageButton.setVisibility(View.INVISIBLE);
				playImageButton.setVisibility(View.VISIBLE);

				seekBar.setProgress((int) songInfo.getPlayProgress());
				timeTextView.setText("-"
						+ MediaUtils.formatTime(songInfo.getSurplusProgress()));

				break;
			case SongMessage.ERROR:
				// pauseImageButton.setVisibility(View.INVISIBLE);
				// playImageButton.setVisibility(View.VISIBLE);

				String errorMessage = songMessage.getErrorMessage();
				Toast.makeText(Copy_3_of_MainActivity.this, errorMessage,
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
				DataUtil.save(Copy_3_of_MainActivity.this,
						Constants.BAR_LRC_IS_OPEN_KEY,
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
				DataUtil.save(Copy_3_of_MainActivity.this,
						Constants.BAR_LRC_IS_OPEN_KEY,
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
					// 往弹出窗口传输相关的进度
					seekBar.popupWindowShow(seekBar.getProgress(), mMenu,
							kscTwoLineLyricsView.getTimeLrc(seekBar
									.getProgress()));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// 拖动条开始拖动的时候调用
				seekBar.popupWindowShow(seekBar.getProgress(), mMenu,
						kscTwoLineLyricsView.getTimeLrc(seekBar.getProgress()));
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

		if (Constants.BAR_LRC_IS_OPEN) {
			mMenu.toggle();
		}

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
		int icon = R.drawable.ic_launcher;
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
		intent.setClass(Copy_3_of_MainActivity.this,
				Copy_3_of_MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		PendingIntent pendingIntent = PendingIntent.getActivity(
				Copy_3_of_MainActivity.this, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		mNotification.contentIntent = pendingIntent;

		SongMessage songMessage = new SongMessage();
		songMessage.setSongInfo(null);
		Message msg = new Message();
		msg.obj = songMessage;
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
					unregisterReceiver(onClickReceiver);
					notificationManager.cancel(0);
					ActivityManager.getInstance().exit();
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
			}
		}
	}
}
