package com.happyplayer.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
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
import com.happyplayer.iface.PageAction;
import com.happyplayer.json.ArtistAlbumJson;
import com.happyplayer.logger.MyLogger;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.KscLyricsLineInfo;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.receiver.PhoneReceiver;
import com.happyplayer.service.EasytouchService;
import com.happyplayer.service.FloatLrcService;
import com.happyplayer.service.LockService;
import com.happyplayer.service.MediaPlayerService;
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
import com.happyplayer.widget.HBaseSeekBar;
import com.happyplayer.widget.KscTwoLineLyricsView;

public class MainActivity extends FragmentActivity implements Observer {
	public static boolean SCREEN_OFF = false;
	private MyLogger logger = MyLogger.getLogger(Constants.USERNAME);
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

	private PopupPlayListAdapter adapter;

	private NotificationManager notificationManager;
	private Notification mNotification;
	private RemoteViews mRemoteViews;
	private Notification mLrcNotification;
	private RemoteViews notifyLrcView;
	/**
	 * 来电监听
	 */
	private MobliePhoneStateListener mPhoneStateListener = null;

	private PopupWindow volumePopupWindow = null;

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

						DataUtil.save(MainActivity.this,
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
				if (mRemoteViews == null) {
					mRemoteViews = new RemoteViews(getPackageName(),
							R.layout.notify_view);
				}

				Intent buttoncloseIntent = new Intent("close");
				PendingIntent pendcloseButtonIntent = PendingIntent
						.getBroadcast(MainActivity.this, 0, buttoncloseIntent,
								0);

				mRemoteViews.setOnClickPendingIntent(R.id.close,
						pendcloseButtonIntent);

				Intent buttonplayIntent = new Intent("play");
				PendingIntent pendplayButtonIntent = PendingIntent
						.getBroadcast(MainActivity.this, 0, buttonplayIntent, 0);

				mRemoteViews.setOnClickPendingIntent(R.id.play,
						pendplayButtonIntent);

				Intent buttonpauseIntent = new Intent("pause");
				PendingIntent pendpauseButtonIntent = PendingIntent
						.getBroadcast(MainActivity.this, 0, buttonpauseIntent,
								0);

				Intent buttonnextIntent = new Intent("next");
				PendingIntent pendnextButtonIntent = PendingIntent
						.getBroadcast(MainActivity.this, 0, buttonnextIntent, 0);

				mRemoteViews.setOnClickPendingIntent(R.id.next,
						pendnextButtonIntent);

				Intent buttonprewtIntent = new Intent("prew");
				PendingIntent pendprewButtonIntent = PendingIntent
						.getBroadcast(MainActivity.this, 0, buttonprewtIntent,
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
						Bitmap bm = ImageUtil.getAlbum(MainActivity.this,
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
					case SongMessage.PLAY:

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
				if (notifyLrcView == null) {
					// 自定义界面
					notifyLrcView = new RemoteViews(getPackageName(),
							R.layout.notify_lrc_view);
				}
				Intent lrcMoveIntent = new Intent("lrcMove");
				PendingIntent pendlrcMoveIntent = PendingIntent.getBroadcast(
						MainActivity.this, 0, lrcMoveIntent, 0);

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

		@SuppressLint("ShowToast")
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

				ImageUtil.loadAlbum(MainActivity.this, singerPicImageView,
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
				timeTextView.setText("-"
						+ MediaUtils.formatTime(songInfo.getSurplusProgress()));

				initKscLyrics(songInfo);
				initArtistAlbum(songInfo, true);

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

				Bitmap bm = MediaUtils.getDefaultArtwork(MainActivity.this,
						false);
				singerPicImageView
						.setBackgroundDrawable(new BitmapDrawable(bm));// 显示专辑封面图片

				initKscLyrics(songInfo);

				initArtistAlbum(songInfo, false);

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

					timeTextView
							.setText("-"
									+ MediaUtils.formatTime((int) (songInfo
											.getDuration() - songInfo
											.getPlayProgress())));
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

				reshLrcView((int) songInfo.getPlayProgress());
				break;
			case SongMessage.ERROR:
				// pauseImageButton.setVisibility(View.INVISIBLE);
				// playImageButton.setVisibility(View.VISIBLE);

				String errorMessage = songMessage.getErrorMessage();
				Toast.makeText(MainActivity.this, errorMessage, 100).show();
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

	private List<SkinMessage> artistList = null;

	/**
	 * 初始化歌手图片
	 * 
	 * @param songInfo
	 */
	private void initArtistAlbum(SongInfo songInfo, boolean isLoadImage) {
		imageHandler.removeCallbacks(myRunnable);
		
		SkinMessage msg = new SkinMessage();
		msg.type = SkinMessage.PIC;
		ObserverManage.getObserver().setMessage(msg);
		
		if (isLoadImage) {
			artistList = new ArrayList<SkinMessage>();
			String artist = songInfo.getArtist();
			// 从本地文件夹里查找图片
			File artFile = new File(Constants.PATH_ARTIST + File.separator
					+ artist);
			if (!artFile.exists()) {
				artFile.mkdirs();
				loadNetPic(artist);
			} else {
				loadLocalPic(artist, artFile);
			}
		}
	}

	private Handler imageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
		}
	};

	private Runnable myRunnable = new Runnable() {
		public void run() {
			int length = artistList.size();
			if (length == 0) {
				return;
			}
			int index = new Random().nextInt(length);

			ObserverManage.getObserver().setMessage(artistList.get(index));
			
			imageHandler.postDelayed(this, 1000* 30);
		}
	};

	/**
	 * 加载本地图片
	 * 
	 * @param artist
	 */
	private void loadLocalPic(final String artist, final File artFile) {

		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {
				artistList = (List<SkinMessage>) result;
				if (artistList.size() != 0) {
					imageHandler.post(myRunnable);
				} else {
					loadNetPic(artist);
				}
			}

			@Override
			protected Object doInBackground() throws Exception {
				List<SkinMessage> artistListTemp = new ArrayList<SkinMessage>();
				File[] files = artFile.listFiles();
				if (files == null || files.length == 0) {
				} else {
					for (int i = 0; i < files.length; i++) {
						if (files[i].getName().endsWith(".jpg")) {
							SkinMessage skinMessage = new SkinMessage();
							skinMessage.setPath(files[i].getPath());
							skinMessage.type = SkinMessage.ART;
							artistListTemp.add(skinMessage);
						}
					}
				}
				return artistListTemp;
			}
		}.execute();
	}

	/**
	 * 加载网络图片
	 * 
	 * @param artist
	 */
	private void loadNetPic(final String artist) {
		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {
				artistList = (List<SkinMessage>) result;
				if (artistList.size() != 0) {
					imageHandler.post(myRunnable);
				}
			}

			@Override
			protected Object doInBackground() throws Exception {
				return ArtistAlbumJson.getArtistAlbum(artist,
						MainActivity.this, 4);
			}
		}.execute();
	}

	/**
	 * 退出程序
	 */
	private void close() {

		Constants.APPCLOSE = true;

		if (EasytouchService.isServiceRunning) {
			Intent easytouchServiceIntent = new Intent(MainActivity.this,
					EasytouchService.class);
			stopService(easytouchServiceIntent);
		}
		if (FloatLrcService.isServiceRunning) {
			Intent floatLrcServiceIntent = new Intent(MainActivity.this,
					FloatLrcService.class);
			stopService(floatLrcServiceIntent);
		}

		if (LockService.isServiceRunning) {
			Intent lockServiceIntent = new Intent(MainActivity.this,
					LockService.class);
			stopService(lockServiceIntent);
		}

		// 如果服务正在运行，则是正在播放
		if (MediaPlayerService.isServiceRunning) {
			stopService(new Intent(MainActivity.this, MediaPlayerService.class));
		}

		this.unregisterReceiver(onClickReceiver);
		this.unregisterReceiver(mSystemReceiver);

		// this.unregisterReceiver(phoneReceiver);
		ComponentName name = new ComponentName(this.getPackageName(),
				PhoneReceiver.class.getName());
		mAudioManager.unregisterMediaButtonEventReceiver(name);

		// TelephonyManager tmgr = (TelephonyManager)
		// getSystemService(Context.TELEPHONY_SERVICE);
		// tmgr.listen(mPhoneStateListener, 0);

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

	private AudioManager mAudioManager;

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

		// if (Constants.SHOWLOCK) {
		// Intent lockServiceIntent = new Intent(MainActivity.this,
		// LockService.class);
		// startService(lockServiceIntent);
		// }

		startService(new Intent(MainActivity.this, MediaPlayerService.class));

		ObserverManage.getObserver().addObserver(this);
		ActivityManager.getInstance().addActivity(this);

		/* 注册广播 */
		IntentFilter mSystemFilter = new IntentFilter();
		// 屏幕
		mSystemFilter.addAction("android.intent.action.SCREEN_ON");
		mSystemFilter.addAction("android.intent.action.SCREEN_OFF");

		// 耳机
		// mSystemFilter.addAction("android.intent.action.HEADSET_PLUG");
		mSystemFilter.addAction("android.media.AUDIO_BECOMING_NOISY");
		// 短信
		mSystemFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		this.registerReceiver(mSystemReceiver, mSystemFilter);

		// // 耳机事件
		// IntentFilter phoneFilter = new
		// IntentFilter("android.intent.action.MEDIA_BUTTON");
		// phoneFilter.setPriority(2147483647);
		// this.registerReceiver(phoneReceiver, phoneFilter);

		ComponentName name = new ComponentName(this.getPackageName(),
				PhoneReceiver.class.getName());
		mAudioManager.registerMediaButtonEventReceiver(name);

		// 添加来电监听事件
		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); // 获取系统服务
		telManager.listen(new MobliePhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void init() {
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
				DataUtil.save(MainActivity.this, Constants.BAR_LRC_IS_OPEN_KEY,
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
				DataUtil.save(MainActivity.this, Constants.BAR_LRC_IS_OPEN_KEY,
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
					MainActivity.this).getPlaylist();

			popPlaysumTextTextView.setText("播放列表(" + playlist.size() + ")");

			adapter = new PopupPlayListAdapter(MainActivity.this, playlist,
					popPlayListView, mPopupWindow);

			popPlayListView.setAdapter(adapter);

			int playIndex = MediaManage.getMediaManage(MainActivity.this)
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
				Toast.makeText(MainActivity.this, "随机播放", Toast.LENGTH_SHORT)
						.show();

				Constants.PLAY_MODE = 2;
				DataUtil.save(MainActivity.this, Constants.PLAY_MODE_KEY,
						Constants.PLAY_MODE);
			}
		});

		modeRandomImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				modeALLImageButton.setVisibility(View.INVISIBLE);
				modeRandomImageButton.setVisibility(View.INVISIBLE);
				modeSingleImageButton.setVisibility(View.VISIBLE);
				Toast.makeText(MainActivity.this, "单曲循环", Toast.LENGTH_SHORT)
						.show();

				Constants.PLAY_MODE = 0;
				DataUtil.save(MainActivity.this, Constants.PLAY_MODE_KEY,
						Constants.PLAY_MODE);
			}
		});

		modeSingleImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				modeALLImageButton.setVisibility(View.VISIBLE);
				modeRandomImageButton.setVisibility(View.INVISIBLE);
				modeSingleImageButton.setVisibility(View.INVISIBLE);

				Toast.makeText(MainActivity.this, "顺序播放", Toast.LENGTH_SHORT)
						.show();

				Constants.PLAY_MODE = 1;
				DataUtil.save(MainActivity.this, Constants.PLAY_MODE_KEY,
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
		intent.setClass(MainActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		PendingIntent pendingIntent = PendingIntent
				.getActivity(MainActivity.this, 0, intent,
						PendingIntent.FLAG_CANCEL_CURRENT);
		mNotification.contentIntent = pendingIntent;

		SongMessage songMessage = new SongMessage();
		songMessage.setSongInfo(null);
		Message msg = new Message();
		msg.what = 0;
		msg.obj = songMessage;
		notifyHandler.sendMessage(msg);
	}

	// 屏幕变暗/变亮的广播 ， 我们要调用KeyguardManager类相应方法去解除屏幕锁定
	private BroadcastReceiver mSystemReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			/**
			 * ACTION_SCREEN_OFF表示按下电源键，屏幕黑屏 ACTION_SCREEN_ON 屏幕黑屏情况下，按下电源键
			 */

			if (action.equals("android.intent.action.SCREEN_OFF")) {
				logger.i("SCREEN_OFF");
				SCREEN_OFF = true;
				if (FloatLrcService.isServiceRunning) {
					stopService(new Intent(MainActivity.this,
							FloatLrcService.class));
				}

				if (EasytouchService.isServiceRunning) {
					stopService(new Intent(MainActivity.this,
							EasytouchService.class));
				}

				// if (LockService.isServiceRunning) {
				// Intent lockServiceIntent = new Intent(MainActivity.this,
				// LockService.class);
				// stopService(lockServiceIntent);
				// }

				//
				// if (!LockService.isServiceRunning) {
				// Intent lockServiceIntent = new Intent(MainActivity.this,
				// LockService.class);
				// startService(lockServiceIntent);
				// }

				// KeyguardManager km = (KeyguardManager) context
				// .getSystemService(Context.KEYGUARD_SERVICE);
				// if (km.inKeyguardRestrictedInputMode()) {

				int status = MediaManage.getMediaManage(context)
						.getPlayStatus();

				if (!ShowLockActivity.active && status == MediaManage.PLAYING
						&& Constants.SHOWLOCK) {
					Intent lockIntent = new Intent(MainActivity.this,
							ShowLockActivity.class);
					lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					startActivity(lockIntent);
				}
				// }
			} else if (action.equals("android.intent.action.SCREEN_ON")) {
				logger.i("SCREEN_ON");
				SCREEN_OFF = false;
				if (!FloatLrcService.isServiceRunning) {
					startService(new Intent(MainActivity.this,
							FloatLrcService.class));
				}
				if (!EasytouchService.isServiceRunning) {
					startService(new Intent(MainActivity.this,
							EasytouchService.class));
				}

				// KeyguardManager km = (KeyguardManager) context
				// .getSystemService(Context.KEYGUARD_SERVICE);
				// if (km.inKeyguardRestrictedInputMode()) {
				int status = MediaManage.getMediaManage(context)
						.getPlayStatus();

				if (!ShowLockActivity.active && status == MediaManage.PLAYING
						&& Constants.SHOWLOCK) {
					Intent lockIntent = new Intent(MainActivity.this,
							ShowLockActivity.class);
					lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					startActivity(lockIntent);
				}
				// }
			}
			// else if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
			// /**
			// * 因为当拔出有线耳机时，播放器不会马上暂停，要等上一秒钟，才会收到Android的系统广播，
			// */
			// int state = intent.getIntExtra("state", -1);
			// // state --- 0代表拔出，1代表插入
			// switch (state) {
			// case 0:
			// logger.i("耳机拔出-->");
			//
			// SongMessage songMessage = new SongMessage();
			// songMessage.setType(SongMessage.STOPPLAY);
			// ObserverManage.getObserver().setMessage(songMessage);
			//
			// break;
			// case 1:
			// logger.i("耳机插入-->");
			// break;
			// default:
			// break;
			// }
			//
			// }
			else if (action.equals("android.media.AUDIO_BECOMING_NOISY")) {
				logger.i("耳机拔出-->");
				/**
				 * 从硬件层面来看，直接监听耳机拔出事件不难，耳机的拔出和插入，会引起手机电平的变化，然后触发什么什么中断，
				 * 
				 * 最终在stack overflow找到答案，监听Android的系统广播AudioManager.
				 * ACTION_AUDIO_BECOMING_NOISY，
				 * 但是这个广播只是针对有线耳机，或者无线耳机的手机断开连接的事件，监听不到有线耳机和蓝牙耳机的接入
				 * ，但对于我的需求来说足够了，监听这个广播就没有延迟了，UI可以立即响应
				 */
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.STOPPLAY);
				ObserverManage.getObserver().setMessage(songMessage);
			} else if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
				logger.i("接收到短信-->");
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.STOPPLAY);
				ObserverManage.getObserver().setMessage(songMessage);
			}
		}
	};

	// private BroadcastReceiver phoneReceiver = new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// if (action.equals("android.intent.action.MEDIA_BUTTON")) {
	// // 耳机事件 Intent 附加值为(Extra)点击MEDIA_BUTTON的按键码
	//
	// KeyEvent event = (KeyEvent) intent
	// .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
	// if (event == null)
	// return;
	//
	// boolean isActionUp = (event.getAction() == KeyEvent.ACTION_UP);
	// if (!isActionUp)
	// return;
	//
	// int keyCode = event.getKeyCode();
	// long eventTime = event.getEventTime() - event.getDownTime();// 按键按下到松开的时长
	// Message msg = Message.obtain();
	// msg.what = 100;
	// Bundle data = new Bundle();
	// data.putInt("key_code", keyCode);
	// data.putLong("event_time", eventTime);
	// msg.setData(data);
	// phoneHandler.sendMessage(msg);
	//
	// // 终止广播(不让别的程序收到此广播，免受干扰)
	// // abortBroadcast();
	// // 改发有序广播
	// // sendOrderedBroadcast(intent, null);
	// }
	// }
	//
	// };

	/**
	 * 耳机处理
	 */
	private Handler phoneHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case 100:// 单击按键广播
				Bundle data = msg.getData();
				// 按键值
				int keyCode = data.getInt("key_code");
				// 按键时长
				long eventTime = data.getLong("event_time");
				// 设置超过10毫秒，就触发长按事件
				boolean isLongPress = (eventTime > 10);

				switch (keyCode) {
				case KeyEvent.KEYCODE_HEADSETHOOK:// 播放或暂停
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:// 播放或暂停
					playOrPause();
					break;

				// 短按=播放下一首音乐，长按=当前音乐快进
				case KeyEvent.KEYCODE_MEDIA_NEXT:
					if (isLongPress) {
						fastNext(50000);// 自定义
					} else {
						playNext();// 自定义
					}
					break;

				// 短按=播放上一首音乐，长按=当前音乐快退
				case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
					if (isLongPress) {
						fastPrevious(50000);// 自定义
					} else {
						playPrevious();// 自定义
					}
					break;
				}

				break;
			// 快进
			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
				fastNext(10000);// 自定义
				break;
			// 快退
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				fastPrevious(10000);// 自定义
				break;
			case KeyEvent.KEYCODE_MEDIA_STOP:

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.STOPPLAY);
				ObserverManage.getObserver().setMessage(songMessage);

				break;
			default:// 其他消息-则扔回上层处理
				super.handleMessage(msg);
			}
		}

		private void fastPrevious(int dProgress) {
			SongInfo tempSongInfo = MediaManage.getMediaManage(
					MainActivity.this).getPlaySongInfo();
			if (tempSongInfo != null) {
				long progress = tempSongInfo.getPlayProgress();
				long minProgress = 0;
				progress = progress - dProgress;
				if (progress <= minProgress) {
					progress = minProgress;
				}
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.SEEKTO);
				songMessage.setProgress((int) progress);
				ObserverManage.getObserver().setMessage(songMessage);
			}

		}

		private void fastNext(int dProgress) {
			SongInfo tempSongInfo = MediaManage.getMediaManage(
					MainActivity.this).getPlaySongInfo();
			if (tempSongInfo != null) {
				long progress = tempSongInfo.getPlayProgress();
				long maxProgress = tempSongInfo.getDuration();
				progress = progress + dProgress;
				if (progress >= maxProgress) {
					progress = maxProgress;
				}
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.SEEKTO);
				songMessage.setProgress((int) progress);
				ObserverManage.getObserver().setMessage(songMessage);
			}
		}

		private void playPrevious() {
			SongMessage songMessage = new SongMessage();
			songMessage.setType(SongMessage.PREVMUSIC);
			ObserverManage.getObserver().setMessage(songMessage);
		}

		private void playNext() {
			SongMessage songMessage = new SongMessage();
			songMessage.setType(SongMessage.NEXTMUSIC);
			ObserverManage.getObserver().setMessage(songMessage);
		}

		private void playOrPause() {
			SongMessage songMessage = new SongMessage();
			songMessage.setType(SongMessage.PLAYORSTOPMUSIC);
			ObserverManage.getObserver().setMessage(songMessage);
		}

	};

	/**
	 * 
	 * @author wwj 电话监听器类
	 */
	private class MobliePhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE: // 挂机状态
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK: // 通话状态
			case TelephonyManager.CALL_STATE_RINGING: // 响铃状态
				logger.i("接收到来电-->");
				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.STOPPLAY);
				ObserverManage.getObserver().setMessage(songMessage);
				break;
			default:
				break;
			}
		}
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
				// tabFragmentPagerAdapter.removeFragment(fragmentList.get(1));
				fragmentList.remove(1);
				// tabFragmentPagerAdapter.notifyDataSetChanged();
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

	// /**
	// * 得到按键的消息，不显示音量对话框。 // * KEYCODE_VOLUME_MUTE 扬声器静音键 164 KEYCODE_VOLUME_UP
	// * 音量增加键 24 // * KEYCODE_VOLUME_DOWN 音量减小键 25
	// */
	// @Override
	// public boolean dispatchKeyEvent(KeyEvent event) {
	// if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
	//
	// int currentVolume = mAudioManager
	// .getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
	// currentVolume = currentVolume - 10;
	// if (currentVolume <= 0) {
	// currentVolume = 0;
	// }
	// mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
	// currentVolume, 0);
	// getVolumePopupWindowInstance();
	//
	// return true;
	// } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
	//
	// // 音乐音量
	// int max = mAudioManager
	// .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	// int currentVolume = mAudioManager
	// .getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
	// currentVolume = currentVolume + 10;
	// if (currentVolume >= max) {
	// currentVolume = max;
	// }
	//
	// mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
	// currentVolume, 0);
	//
	// getVolumePopupWindowInstance();
	//
	// return true;
	// } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_MUTE) {
	// mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
	// getVolumePopupWindowInstance();
	// return true;
	// }
	// return super.dispatchKeyEvent(event);
	// }

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	// if (TAB_INDEX == 0) {
	// if ((System.currentTimeMillis() - mExitTime) > 2000) {
	// Toast.makeText(this, R.string.exit_tip, Toast.LENGTH_SHORT)
	// .show();
	// mExitTime = System.currentTimeMillis();
	// } else {
	// close();
	// }
	// } else {
	// viewPager.setCurrentItem(0);
	// }
	// }
	// return false;
	// }

	@Override
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
			return false;
		}
		/**
		 * KEYCODE_VOLUME_MUTE 扬声器静音键 164 KEYCODE_VOLUME_UP 音量增加键 24
		 * KEYCODE_VOLUME_DOWN 音量减小键 25
		 */

		else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

			// Toast.makeText(this, "KEYCODE_VOLUME_DOWN", Toast.LENGTH_SHORT)
			// .show();
			// // 降低音量
			// // mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
			// // AudioManager.ADJUST_LOWER,
			// // AudioManager.FX_FOCUS_NAVIGATION_UP);
			//
			// int currentVolume = mAudioManager
			// .getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
			// currentVolume = currentVolume - 10;
			// if (currentVolume <= 0) {
			// currentVolume = 0;
			// }
			//
			// mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
			// currentVolume, 0);

			getVolumePopupWindowInstance();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			// Toast.makeText(this, "KEYCODE_VOLUME_UP", Toast.LENGTH_SHORT)
			// .show();
			// 增加音量
			// mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
			// AudioManager.ADJUST_RAISE,
			// AudioManager.FX_FOCUS_NAVIGATION_UP);

			// // 音乐音量
			// int max = mAudioManager
			// .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			// int currentVolume = mAudioManager
			// .getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
			// currentVolume = currentVolume + 10;
			// if (currentVolume >= max) {
			// currentVolume = max;
			// }
			//
			// mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
			// currentVolume, 0);

			getVolumePopupWindowInstance();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
			// mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			getVolumePopupWindowInstance();
			// Toast.makeText(this, "KEYCODE_VOLUME_MUTE", Toast.LENGTH_SHORT)
			// .show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private HBaseSeekBar volumeSizeSeekBar = null;
	/**
	 * 显示面板倒计时
	 */
	public int volumeEndTime = -1;

	private void getVolumePopupWindowInstance() {
		if (volumePopupWindow == null) {
			initVolumePopupWindow();
		} else {
			if (volumePopupWindow.isShowing()) {
				volumeEndTime = 2000;
				mVolumeHandler.sendEmptyMessage(0);
			} else {

				int[] location = new int[2];
				mMenu.getLocationOnScreen(location);
				volumePopupWindow.showAtLocation(mMenu, Gravity.NO_GRAVITY,
						location[0], location[1] - mMenu.getHeight());

				mVolumeHandler.sendEmptyMessage(0);

				if (volumeEndTime < 0) {
					volumeEndTime = 2000;
					mVolumeHandler.post(upDateVol);
				} else {
					volumeEndTime = 2000;
				}
			}
		}
	}

	private Handler mVolumeHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int currentVolume = mAudioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
			volumeSizeSeekBar.setProgress(currentVolume);
		}

	};

	Runnable upDateVol = new Runnable() {

		@Override
		public void run() {
			if (volumeEndTime >= 0) {
				volumeEndTime -= 200;
				mVolumeHandler.postDelayed(upDateVol, 200);
			} else {
				if (volumePopupWindow != null && volumePopupWindow.isShowing()) {
					volumePopupWindow.dismiss();
				}
			}

		}
	};

	private void initVolumePopupWindow() {
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View popupWindow = layoutInflater.inflate(R.layout.volume_menu, null);

		popupWindow.setFocusableInTouchMode(true);

		popupWindow.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
					// Toast.makeText(MainActivity.this, "KEYCODE_VOLUME_MUTE",
					// Toast.LENGTH_SHORT).show();

					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
							0);
					getVolumePopupWindowInstance();
					return true;

				} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
					// Toast.makeText(MainActivity.this, "KEYCODE_VOLUME_DOWN",
					// Toast.LENGTH_SHORT).show();

					int currentVolume = mAudioManager
							.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
					currentVolume = currentVolume - 1;
					if (currentVolume <= 0) {
						currentVolume = 0;
					}

					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							currentVolume, 0);

					getVolumePopupWindowInstance();

					return true;

				} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
					// Toast.makeText(MainActivity.this, "KEYCODE_VOLUME_UP",
					// Toast.LENGTH_SHORT).show();
					int max = mAudioManager
							.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 3 * 2;
					int currentVolume = mAudioManager
							.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
					currentVolume = currentVolume + 1;
					if (currentVolume >= max) {
						currentVolume = max;
					}

					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							currentVolume, 0);

					getVolumePopupWindowInstance();
					return true;

				}

				return false;
			}

		});

		volumeSizeSeekBar = (HBaseSeekBar) popupWindow
				.findViewById(R.id.volumeSizeSeekBar);

		// 音乐音量
		int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 3 * 2;
		int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / 3 * 2;
		volumeSizeSeekBar.setMax(max);
		volumeSizeSeekBar.setProgress(current);

		volumeSizeSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar arg0, int progress,
							boolean arg2) {
						volumeEndTime = 2000;
						mAudioManager.setStreamVolume(
								AudioManager.STREAM_MUSIC, progress, 0);
						int currentVolume = mAudioManager
								.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
						volumeSizeSeekBar.setProgress(currentVolume);
					}

					@Override
					public void onStartTrackingTouch(SeekBar arg0) {
					}

					@Override
					public void onStopTrackingTouch(SeekBar arg0) {
					}
				});

		volumePopupWindow = new PopupWindow(popupWindow,
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, true);
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		// 设置SelectPicPopupWindow弹出窗体的背景
		volumePopupWindow.setBackgroundDrawable(new BitmapDrawable());
		// mPopupWindowDialog.setFocusable(true);
		volumePopupWindow.setOutsideTouchable(true);

		int[] location = new int[2];
		mMenu.getLocationOnScreen(location);
		volumePopupWindow.showAtLocation(mMenu, Gravity.NO_GRAVITY,
				location[0], location[1] - mMenu.getHeight());

		if (volumeEndTime < 0) {
			volumeEndTime = 2000;
			mVolumeHandler.post(upDateVol);
		} else {
			volumeEndTime = 2000;
		}
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
				// tabFragmentPagerAdapter.setFragments(fragmentList);
				// tabFragmentPagerAdapter = new TabFragmentPagerAdapter(
				// getSupportFragmentManager());
				// viewPager.setAdapter(tabFragmentPagerAdapter);
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
					|| songMessage.getType() == SongMessage.PLAY
					|| songMessage.getType() == SongMessage.PLAYING
					|| songMessage.getType() == SongMessage.STOPING
					|| songMessage.getType() == SongMessage.ERROR
					|| songMessage.getType() == SongMessage.LASTPLAYFINISH) {
				Message msg = new Message();
				msg.obj = songMessage;
				songHandler.sendMessage(msg);
				if (songMessage.getType() == SongMessage.INIT
						|| songMessage.getType() == SongMessage.PLAY
						|| songMessage.getType() == SongMessage.STOPING
						|| songMessage.getType() == SongMessage.LASTPLAYFINISH) {
					Message msg2 = new Message();
					msg2.what = 0;
					msg2.obj = songMessage;
					notifyHandler.sendMessage(msg2);
				}

			} else if (songMessage.getType() == SongMessage.EXIT) {
				close();
			} else if (songMessage.getType() == SongMessage.DES_LRC) {
				notificationManager.cancel(1);
				if (Constants.SHOWDESLRC) {
					createNotifiLrcView();
				}
			} else if (songMessage.getType() == SongMessage.DEL_NUM) {
				popHandler.sendEmptyMessage(0);
			} else if (songMessage.getType() == SongMessage.DELALLMUSICED) {
				popHandler.sendEmptyMessage(1);
			} else if (songMessage.getType() == SongMessage.DESLRCMOVEED) {
				notificationManager.cancel(1);
				createNotifiLrcView();
			}
		} else if (data instanceof Message) {
			Message msg = (Message) data;
			phoneHandler.sendMessage(msg);
		}
	}

	//
	private Handler popHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (null != mPopupWindow) {
				List<SongInfo> playlist = MediaManage.getMediaManage(
						MainActivity.this).getPlaylist();
				popPlaysumTextTextView.setText("播放列表(" + playlist.size() + ")");

				if (msg.what == 1) {
					if (adapter != null) {
						ObserverManage.getObserver().deleteObserver(adapter);
					}
					adapter = new PopupPlayListAdapter(MainActivity.this,
							playlist, popPlayListView, mPopupWindow);

					popPlayListView.setAdapter(adapter);

					int playIndex = MediaManage.getMediaManage(
							MainActivity.this).getPlayIndex();
					if (playIndex != -1) {
						popPlayListView.setSelection(playIndex);
					}
				}
			}
		}
	};
}
