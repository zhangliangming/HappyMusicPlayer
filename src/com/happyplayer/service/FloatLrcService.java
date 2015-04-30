package com.happyplayer.service;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.common.Constants;
import com.happyplayer.logger.MyLogger;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.KscLyricsLineInfo;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.ui.MainActivity;
import com.happyplayer.ui.R;
import com.happyplayer.util.DataUtil;
import com.happyplayer.util.KscLyricsManamge;
import com.happyplayer.util.KscLyricsParser;
import com.happyplayer.widget.FloatLyricRelativeLayout;
import com.happyplayer.widget.FloatLyricsView;

public class FloatLrcService extends Service implements Observer {
	public static Boolean isServiceRunning = false;
	private MyLogger logger = MyLogger.getLogger(Constants.USERNAME);
	private WindowManager wm = null;
	private WindowManager.LayoutParams floatViewParams = null;
	private View floatView;
	private Context context;

	private FloatLyricRelativeLayout floatLyricRelativeLayout;

	private FloatLyricsView floatLyricsView;
	/**
	 * 歌词解析
	 */
	private KscLyricsParser kscLyricsParser;
	/**
	 * 歌词
	 */
	private TreeMap<Integer, KscLyricsLineInfo> lyricsLineTreeMap;
	private float mTouchStartX;
	private float mTouchStartY;
	private float x;
	private float y;

	private float startRawX = 0, startRawY = 0;

	private WindowManager.LayoutParams lrcColorViewParams = null;

	private View lrcColorView = null;

	/**
	 * 显示面板倒计时
	 */
	public int EndTime = -1;

	// 状态栏高度
	private double stateHeight;

	private Handler songHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			SongMessage songMessage = (SongMessage) msg.obj;
			final SongInfo songInfo = songMessage.getSongInfo();
			switch (songMessage.getType()) {
			case SongMessage.INIT:

				loadFloatLyricsData(songInfo);

				break;
			case SongMessage.LASTPLAYFINISH:

				loadFloatLyricsData(songInfo);

				break;
			case SongMessage.PLAYING:
				if (floatLyricsView.getParent() != null) {
					reshLrcView((int) songInfo.getPlayProgress());
				}
				break;
			case SongMessage.STOPING:
				if (floatLyricsView.getParent() != null) {
					reshLrcView((int) songInfo.getPlayProgress());
				}
				break;
			case SongMessage.ERROR:
				break;
			}
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/**
	 * 
	 * @param playProgress
	 *            根据当前歌曲播放进度，刷新歌词
	 */
	private void reshLrcView(int playProgress) {
		// 判断当前的歌曲是否有歌词
		boolean blLrc = floatLyricsView.getBlLrc();
		if (blLrc) {
			floatLyricsView.showLrc(playProgress);
		}
	}

	@Override
	public void onCreate() {
		init();
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		isServiceRunning = true;
		logger.i("FloatLrcService被创建");
		handler.post(myRunnable);
		ObserverManage.getObserver().addObserver(this);
	}

	private void init() {

		context = FloatLrcService.this.getBaseContext();

		stateHeight = Math
				.ceil(25 * context.getResources().getDisplayMetrics().density);

		// 获取WindowManager
		wm = (WindowManager) getApplicationContext().getSystemService("window");
		// 设置LayoutParams(全局变量）相关参数
		floatViewParams = new WindowManager.LayoutParams();
		floatViewParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
				| WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		floatViewParams.format = 1;

		if (Constants.DESLRCMOVE) {
			floatViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		} else {
			floatViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		}

		floatViewParams.gravity = Gravity.LEFT | Gravity.TOP;// 调整悬浮窗口至左上角
		// 以屏幕左上角为原点，设置x、y初始值
		floatViewParams.x = Constants.LRCX;
		floatViewParams.y = Constants.LRCY;

		// 设置悬浮窗口长宽数据
		floatViewParams.width = wm.getDefaultDisplay().getWidth();

		floatView = LayoutInflater.from(context).inflate(R.layout.des_view,
				null);

		floatLyricRelativeLayout = (FloatLyricRelativeLayout) floatView
				.findViewById(R.id.floatLyricRelativeLayout);

		floatLyricRelativeLayout.getBackground().setAlpha(0);

		floatLyricsView = (FloatLyricsView) floatView
				.findViewById(R.id.floatLyricsView);

		floatViewParams.height = 140;

		floatLyricsView.setOnTouchListener(mOnTouchListener);

		initLrcColorView();
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (lrcColorView.getParent() != null) {
				wm.removeView(lrcColorView);
				logger.i("移除lrcColorView------>");
				floatLyricRelativeLayout.getBackground().setAlpha(0);
			}
			floatLyricsView.setOnTouchListener(mOnTouchListener);
			floatLyricsView.setOnClickListener(null);
		}
	};

	private OnTouchListener mOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			if (!Constants.DESLRCMOVE)
				return false;

			// 获取相对屏幕的坐标，即以屏幕左上角为原点
			x = event.getRawX();
			y = (float) (event.getRawY() - stateHeight);

			int sumX = (int) (event.getRawX() - startRawX);
			int sumY = (int) (event.getRawY() - startRawY);

			switch (event.getAction()) {
			// 手指按下时
			case MotionEvent.ACTION_DOWN:
				// 获取相对View的坐标，即以此View左上角为原点
				mTouchStartX = event.getX();
				mTouchStartY = event.getY();

				startRawX = event.getRawX();
				startRawY = event.getRawY();

				break;
			// 手指移动时
			case MotionEvent.ACTION_MOVE:
				if (sumX > -10 && sumX < 10 && sumY > -10 && sumY < 10) {
					if (lrcColorView.getParent() != null) {
						wm.removeView(lrcColorView);
						logger.i("移除lrcColorView------>");
						floatLyricRelativeLayout.getBackground().setAlpha(0);
						floatLyricsView.setOnTouchListener(mOnTouchListener);
						floatLyricsView.setOnClickListener(null);
					}
					return false;
				} else {
					// 更新视图
					updateViewPosition();
				}

				break;
			// 手指松开时
			case MotionEvent.ACTION_UP:
				if (sumX > -5 && sumX < 5 && sumY > -5 && sumY < 5) {
					addDesLrcColorView();
					return false;

				}
				mTouchStartX = mTouchStartY = 0;

				startRawX = 0;
				startRawY = 0;
				break;
			}
			return true;
		}
	};

	/**
	 * 颜色面板
	 */
	private ImageView imageviews[];

	/**
	 * 游标
	 */
	private ImageView flagimageviews[];

	private void initLrcColorView() {
		lrcColorViewParams = new WindowManager.LayoutParams();
		lrcColorViewParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
				| WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		lrcColorViewParams.format = 1;
		lrcColorViewParams.alpha = 1f;
		lrcColorViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		lrcColorViewParams.gravity = Gravity.LEFT | Gravity.TOP;// 调整悬浮窗口至左上角

		lrcColorViewParams.x = 0;
		lrcColorViewParams.y = 0;

		lrcColorViewParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		lrcColorViewParams.height = 110;

		lrcColorView = LayoutInflater.from(context).inflate(
				R.layout.des_lrc_item_view, null);

		ImageButton lycicLock = (ImageButton) lrcColorView
				.findViewById(R.id.lycic_lock);
		lycicLock.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (Constants.DESLRCMOVE) {
					Constants.DESLRCMOVE = false;
				} else {
					Constants.DESLRCMOVE = true;
				}

				SongMessage songMessage = new SongMessage();
				songMessage.setType(SongMessage.DESLRCMOVE);
				ObserverManage.getObserver().setMessage(songMessage);

				new AsyncTaskHandler() {

					@Override
					protected void onPostExecute(Object result) {
						Toast.makeText(context, "桌面歌词已锁", Toast.LENGTH_SHORT)
								.show();
						SongMessage songMessage = new SongMessage();
						songMessage.setType(SongMessage.DESLRCMOVEED);
						ObserverManage.getObserver().setMessage(songMessage);
					}

					protected Object doInBackground() throws Exception {

						DataUtil.save(context, Constants.DESLRCMOVE_KEY,
								Constants.DESLRCMOVE);
						return null;
					}
				}.execute();
			}
		});

		ImageButton lyricShrink = (ImageButton) lrcColorView
				.findViewById(R.id.lyric_shrink);
		lyricShrink.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				EndTime = 4000;
				Constants.DESLRCFONTSIZEINDEX--;
				if (Constants.DESLRCFONTSIZEINDEX < 0) {
					Constants.DESLRCFONTSIZEINDEX = 0;
				}
				floatLyricsView.invalidate();

				new Thread() {

					@Override
					public void run() {
						DataUtil.save(context,
								Constants.DESLRCFONTSIZEINDEX_KEY,
								Constants.DESLRCFONTSIZEINDEX);
					}

				}.start();
			}
		});
		ImageButton lyricScale = (ImageButton) lrcColorView
				.findViewById(R.id.lyric_scale);
		lyricScale.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				EndTime = 4000;
				Constants.DESLRCFONTSIZEINDEX++;
				if (Constants.DESLRCFONTSIZEINDEX >= Constants.DESLRCFONTSIZE.length) {
					Constants.DESLRCFONTSIZEINDEX = Constants.DESLRCFONTSIZE.length - 1;
				}
				floatLyricsView.invalidate();
				new Thread() {

					@Override
					public void run() {
						DataUtil.save(context,
								Constants.DESLRCFONTSIZEINDEX_KEY,
								Constants.DESLRCFONTSIZEINDEX);
					}

				}.start();
			}
		});
		// lrcColorView.setOnTouchListener(new OnTouchListener() {
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// System.out.println("11111111");
		// int topHeight = lrcColorView.findViewById(R.id.lrcparent)
		// .getTop();
		// int bottomHeight = lrcColorView.findViewById(R.id.lrcparent)
		// .getBottom();
		// int y = (int) event.getY();
		// if (event.getAction() == MotionEvent.ACTION_UP) {
		// if (topHeight > y || y > bottomHeight) {
		// if (lrcColorView.getParent() != null) {
		// wm.removeView(lrcColorView);
		// }
		// }
		// }
		// return true;
		// }
		// });

		int length = Constants.DESLRCCOLORS.length;

		imageviews = new ImageView[length];
		flagimageviews = new ImageView[length];

		int i = 0;
		imageviews[i] = (ImageView) lrcColorView.findViewById(R.id.colorpanel0);
		flagimageviews[i] = (ImageView) lrcColorView
				.findViewById(R.id.select_flag0);
		flagimageviews[i].setVisibility(View.INVISIBLE);
		imageviews[i].setOnClickListener(new MyImageViewOnClickListener());
		imageviews[i].setBackgroundColor(Constants.DESLRCCOLORS[i++]);
		imageviews[i] = (ImageView) lrcColorView.findViewById(R.id.colorpanel1);
		flagimageviews[i] = (ImageView) lrcColorView
				.findViewById(R.id.select_flag1);
		flagimageviews[i].setVisibility(View.INVISIBLE);
		imageviews[i].setOnClickListener(new MyImageViewOnClickListener());
		imageviews[i].setBackgroundColor(Constants.DESLRCCOLORS[i++]);
		imageviews[i] = (ImageView) lrcColorView.findViewById(R.id.colorpanel2);
		flagimageviews[i] = (ImageView) lrcColorView
				.findViewById(R.id.select_flag2);
		flagimageviews[i].setVisibility(View.INVISIBLE);
		imageviews[i].setOnClickListener(new MyImageViewOnClickListener());
		imageviews[i].setBackgroundColor(Constants.DESLRCCOLORS[i++]);
		imageviews[i] = (ImageView) lrcColorView.findViewById(R.id.colorpanel3);
		flagimageviews[i] = (ImageView) lrcColorView
				.findViewById(R.id.select_flag3);
		flagimageviews[i].setVisibility(View.INVISIBLE);
		imageviews[i].setOnClickListener(new MyImageViewOnClickListener());
		imageviews[i].setBackgroundColor(Constants.DESLRCCOLORS[i++]);
		imageviews[i] = (ImageView) lrcColorView.findViewById(R.id.colorpanel4);
		flagimageviews[i] = (ImageView) lrcColorView
				.findViewById(R.id.select_flag4);
		flagimageviews[i].setVisibility(View.INVISIBLE);
		imageviews[i].setOnClickListener(new MyImageViewOnClickListener());
		imageviews[i].setBackgroundColor(Constants.DESLRCCOLORS[i++]);
		// imageviews[i] = (ImageView)
		// lrcColorView.findViewById(R.id.colorpanel5);
		// flagimageviews[i] = (ImageView) lrcColorView
		// .findViewById(R.id.select_flag5);
		// flagimageviews[i].setVisibility(View.INVISIBLE);
		// imageviews[i].setOnClickListener(new MyImageViewOnClickListener());
		// imageviews[i].setBackgroundColor(Constants.DESLRCREADEDCOLOR[i++]);

		flagimageviews[Constants.DEF_DES_COLOR_INDEX]
				.setVisibility(View.VISIBLE);

	}

	private class MyImageViewOnClickListener implements OnClickListener {

		public void onClick(View arg0) {
			EndTime = 4000;
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
			// case R.id.colorpanel5:
			// index = 5;
			// break;
			default:
				break;
			}
			Constants.DEF_DES_COLOR_INDEX = index;
			for (int i = 0; i < imageviews.length; i++) {
				if (i == index)
					flagimageviews[i].setVisibility(View.VISIBLE);
				else
					flagimageviews[i].setVisibility(View.INVISIBLE);
			}

			floatLyricsView.invalidate();

			new Thread() {

				@Override
				public void run() {
					DataUtil.save(context, Constants.DEF_DES_COLOR_INDEX_KEY,
							Constants.DEF_DES_COLOR_INDEX);
				}

			}.start();

		}
	}

	/**
	 * 改变歌词颜色
	 */
	protected void addDesLrcColorView() {

		floatLyricsView.setOnTouchListener(null);
		floatLyricsView.setOnClickListener(mOnClickListener);

		if (lrcColorView.getParent() == null) {

			int[] location = new int[2];
			floatLyricsView.getLocationOnScreen(location);

			lrcColorViewParams.x = location[0];

			int heigth = (int) (wm.getDefaultDisplay().getHeight()
					- location[1] - floatViewParams.height);
			// System.out.println("heigth:------->"+heigth);
			// System.out.println("lrcColorViewParams.height:------->"+lrcColorViewParams.height);
			if (heigth >= lrcColorViewParams.height) {
				lrcColorViewParams.y = (int) (location[1]
						+ floatViewParams.height - stateHeight);
			} else {
				lrcColorViewParams.y = (int) (location[1]
						- lrcColorViewParams.height - stateHeight);
			}
			floatLyricRelativeLayout.getBackground().setAlpha(100);
			wm.addView(lrcColorView, lrcColorViewParams);
			logger.i("添加lrcColorView------>");
			if (EndTime < 0) {
				EndTime = 4000;
				handler.post(upDateVol);
			} else {
				EndTime = 4000;
			}
		}
	}

	Runnable upDateVol = new Runnable() {

		@Override
		public void run() {
			if (EndTime >= 0) {
				EndTime -= 200;
				handler.postDelayed(upDateVol, 200);
			} else {
				if (lrcColorView.getParent() != null) {
					wm.removeView(lrcColorView);
					logger.i("移除lrcColorView------>");
					floatLyricRelativeLayout.getBackground().setAlpha(0);
					floatLyricsView.setOnTouchListener(mOnTouchListener);
					floatLyricsView.setOnClickListener(null);
				}
			}

		}
	};

	private void updateViewPosition() {

		// 更新浮动窗口位置参数
		floatViewParams.x = (int) (x - mTouchStartX);
		floatViewParams.y = (int) (y - mTouchStartY);
		wm.updateViewLayout(floatView, floatViewParams);

		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {

			}

			protected Object doInBackground() throws Exception {
				Constants.LRCX = floatViewParams.x;
				Constants.LRCY = floatViewParams.y;

				DataUtil.save(context, Constants.LRCX_KEY, Constants.LRCX);
				DataUtil.save(context, Constants.LRCY_KEY, Constants.LRCY);
				return null;
			}
		}.execute();
	}

	private Handler floatViewHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (!Constants.SHOWDESLRC || !isServiceRunning)
				return;
			switch (msg.what) {
			// 程序后台运行
			case 0:
				if (floatView.getParent() == null) {
					wm.addView(floatView, floatViewParams);
					logger.i("添加floatView------>");
					floatLyricRelativeLayout.getBackground().setAlpha(0);
					floatLyricsView.setOnTouchListener(mOnTouchListener);
					floatLyricsView.setOnClickListener(null);
					SongInfo songInfo = MediaManage.getMediaManage(context)
							.getPlaySongInfo();
					if (songInfo != null) {
						loadFloatLyricsData(songInfo);
					}
				}
				break;
			case 1:
				if (floatView.getParent() != null) {
					wm.removeView(floatView);
					logger.i("移除floatView------>");
				}
				if (lrcColorView.getParent() != null) {
					wm.removeView(lrcColorView);
					logger.i("移除lrcColorView------>");
					floatLyricRelativeLayout.getBackground().setAlpha(0);
					floatLyricsView.setOnTouchListener(mOnTouchListener);
					floatLyricsView.setOnClickListener(null);
				}
				break;
			}

		}
	};

	private Handler handler = new Handler();
	private Runnable myRunnable = new Runnable() {
		public void run() {
			if (isServiceRunning) {
				Message msg = new Message();
				if (!isBackground(context)) {
					msg.what = 1;
				} else {
					msg.what = 0;
				}
				floatViewHandler.sendMessage(msg);
				handler.postDelayed(this, 10);
			}
		}
	};

	@Override
	public void onDestroy() {
		isServiceRunning = false;
		logger.i("----FloatLrcService被回收了----");
		handler.removeCallbacks(myRunnable);
		super.onDestroy();

		if (floatView.getParent() != null) {
			logger.i("onDestroy 移除floatView------>");
			wm.removeView(floatView);
		}
		if (lrcColorView.getParent() != null) {
			logger.i("onDestroy 移除lrcColorView------>");
			wm.removeView(lrcColorView);
			floatLyricRelativeLayout.getBackground().setAlpha(0);
			floatLyricsView.setOnTouchListener(mOnTouchListener);
			floatLyricsView.setOnClickListener(null);
		}

		ObserverManage.getObserver().deleteObserver(this);

		if (!Constants.APPCLOSE && !MainActivity.SCREEN_OFF) {
			// 在此重新启动,使服务常驻内存当然如果系统资源不足，android系统也可能结束服务。
			startService(new Intent(this, FloatLrcService.class));
			logger.i("----FloatLrcService被重新启动了---");
		}
	}

	protected void loadFloatLyricsData(final SongInfo songInfo) {

		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {

				kscLyricsParser = (KscLyricsParser) result;
				lyricsLineTreeMap = kscLyricsParser.getLyricsLineTreeMap();
				floatLyricsView.init();
				if (lyricsLineTreeMap.size() != 0) {
					floatLyricsView.setKscLyricsParser(kscLyricsParser);
					floatLyricsView.setLyricsLineTreeMap(lyricsLineTreeMap);
					floatLyricsView.setBlLrc(true);
					floatLyricsView.invalidate();
				} else {
					floatLyricsView.setBlLrc(false);
					floatLyricsView.invalidate();
				}

			}

			@Override
			protected Object doInBackground() throws Exception {

				if (songInfo != null) {
					return KscLyricsManamge.getKscLyricsParser(songInfo
							.getDisplayName());
				}
				return null;
			}
		}.execute();
	}

	/**
	 * 判断程序是否在后台运行
	 * 
	 * @param context
	 * @return
	 */
	// private boolean isTopActivity(Context context) {
	// String packageName = context.getPackageName();
	// ActivityManager activityManager = (ActivityManager) context
	// .getSystemService(Context.ACTIVITY_SERVICE);
	// List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
	// if (tasksInfo.size() > 0) {
	// // 应用程序位于堆栈的顶层
	// if (packageName.equals(tasksInfo.get(0).topActivity
	// .getPackageName())) {
	// return true;
	// }
	// }
	// return false;
	// }
	public static boolean isBackground(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				/*
				 * BACKGROUND=400 EMPTY=500 FOREGROUND=100 GONE=1000
				 * PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
				 */
				// Log.i(context.getPackageName(), "此appimportace ="
				// + appProcess.importance
				// + ",context.getClass().getName()="
				// + context.getClass().getName());
				if (appProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					// Log.i(context.getPackageName(), "处于后台"
					// + appProcess.processName);
					return true;
				} else {
					// Log.i(context.getPackageName(), "处于前台"
					// + appProcess.processName);
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SongMessage) {
			SongMessage songMessage = (SongMessage) data;
			if (songMessage.getType() == SongMessage.INIT
					|| songMessage.getType() == SongMessage.PLAYING
					|| songMessage.getType() == SongMessage.STOPING
					|| songMessage.getType() == SongMessage.ERROR
					|| songMessage.getType() == SongMessage.LASTPLAYFINISH) {
				Message msg = new Message();
				msg.obj = songMessage;
				songHandler.sendMessage(msg);
			} else if (songMessage.getType() == SongMessage.DESLRCMOVE) {
				System.out.println("Constants.DESLRCMOVE:--->"
						+ Constants.DESLRCMOVE);
				if (Constants.DESLRCMOVE) {
					floatViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
							| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
				} else {
					floatViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
							| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
							| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
				}
				if (lrcColorView.getParent() != null) {
					wm.removeView(lrcColorView);
					floatLyricRelativeLayout.getBackground().setAlpha(0);
					floatLyricsView.setOnTouchListener(mOnTouchListener);
					floatLyricsView.setOnClickListener(null);
				}
				if (floatView.getParent() != null) {
					wm.updateViewLayout(floatView, floatViewParams);
				}
			}
		}
	}
}
