package com.happyplayer.service;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.common.Constants;
import com.happyplayer.model.KscLyricsLineInfo;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.player.MediaManage;
import com.happyplayer.util.DataUtil;
import com.happyplayer.util.KscLyricsManamge;
import com.happyplayer.util.KscLyricsParser;
import com.happyplayer.widget.FloatLyricsView;

public class FloatLrcService extends Service implements Observer {

	private WindowManager wm = null;
	private WindowManager.LayoutParams wmParams = null;
	private Context context;

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
		super.onCreate();
		init();
		handler.post(myRunnable);
		ObserverManage.getObserver().addObserver(this);
	}

	private void init() {
		context = FloatLrcService.this.getBaseContext();

		// 获取WindowManager
		wm = (WindowManager) getApplicationContext().getSystemService("window");
		// 设置LayoutParams(全局变量）相关参数
		wmParams = new WindowManager.LayoutParams();
		wmParams.type = 2002;
		wmParams.format = 1;

		if (Constants.DESLRCMOVE) {
			wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		} else {
			wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		}

		wmParams.gravity = Gravity.LEFT | Gravity.TOP;// 调整悬浮窗口至左上角
		// 以屏幕左上角为原点，设置x、y初始值
		wmParams.x = Constants.LRCX;
		wmParams.y = Constants.LRCY;
		// 设置悬浮窗口长宽数据
		wmParams.width = wm.getDefaultDisplay().getWidth();

		floatLyricsView = new FloatLyricsView(context);

		wmParams.height = floatLyricsView.getSIZEWORDDEF() * 2
				+ floatLyricsView.getINTERVAL() * 3;

		floatLyricsView.setLayoutParams(new LayoutParams(wmParams.width,
				wmParams.height));

		floatLyricsView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (!Constants.DESLRCMOVE)
					return false;

				// 获取相对屏幕的坐标，即以屏幕左上角为原点
				x = event.getRawX();
				y = event.getRawY() - 25; // 25是系统状态栏的高度
				switch (event.getAction()) {
				// 手指按下时
				case MotionEvent.ACTION_DOWN:
					// 获取相对View的坐标，即以此View左上角为原点
					mTouchStartX = event.getX();
					mTouchStartY = event.getY();

					break;
				// 手指移动时
				case MotionEvent.ACTION_MOVE:
					// 更新视图
					updateViewPosition();
					break;
				// 手指松开时
				case MotionEvent.ACTION_UP:

					updateViewPosition();
					mTouchStartX = mTouchStartY = 0;
					break;
				}
				return true;
			}
		});
	}

	private void updateViewPosition() {

		// 更新浮动窗口位置参数
		wmParams.x = (int) (x - mTouchStartX);
		wmParams.y = (int) (y - mTouchStartY);
		wm.updateViewLayout(floatLyricsView, wmParams);

		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {

			}

			protected Object doInBackground() throws Exception {
				Constants.LRCX = wmParams.x;
				Constants.LRCY = wmParams.y;

				DataUtil.save(context, Constants.LRCX_KEY, Constants.LRCX);
				DataUtil.save(context, Constants.LRCY_KEY, Constants.LRCY);
				return null;
			}
		}.execute();
	}

	private Handler floatViewHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (!Constants.SHOWDESLRC)
				return;
			switch (msg.what) {
			// 程序后台运行
			case 0:
				if (floatLyricsView.getParent() == null) {
					wm.addView(floatLyricsView, wmParams);
					SongInfo songInfo = MediaManage.getMediaManage(context)
							.getPlaySongInfo();
					if (songInfo != null) {
						loadFloatLyricsData(songInfo);
					}
				}
				break;
			case 1:
				if (floatLyricsView.getParent() != null) {
					wm.removeView(floatLyricsView);
				}
				break;
			}

		}
	};

	private Handler handler = new Handler();
	private Runnable myRunnable = new Runnable() {
		public void run() {
			Message msg = new Message();
			if (!isBackground(context)) {
				msg.what = 1;
			} else {
				msg.what = 0;
			}
			floatViewHandler.sendMessage(msg);
			handler.postDelayed(this, 10);
		}
	};

	@Override
	public void onDestroy() {
		handler.removeCallbacks(myRunnable);
		super.onDestroy();
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
				if (Constants.DESLRCMOVE) {
					wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
							| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
				} else {
					wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
							| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
							| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
				}

				if (floatLyricsView.getParent() != null) {
					wm.updateViewLayout(floatLyricsView, wmParams);
				}
			}
		}
	}
}
