package com.happyplayer.service;

import java.util.Observable;
import java.util.Observer;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.happyplayer.common.Constants;
import com.happyplayer.logger.MyLogger;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.ui.MainActivity;
import com.happyplayer.ui.ShowLockActivity;

public class LockService extends Service implements Observer {

	private Context context;

	private Intent lockIntent = null;

	public static Boolean isServiceRunning = false;
	private MyLogger logger = MyLogger.getLogger(Constants.USERNAME);

	private KeyguardManager mKeyguardManager = null;

	private KeyguardLock mKeyguardLock = null;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init();
	}

	private void init() {
		context = LockService.this.getBaseContext();

		lockIntent = new Intent(LockService.this, ShowLockActivity.class);
		lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		mKeyguardManager = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);

		// 屏蔽手机内置的锁屏
		mKeyguardLock = mKeyguardManager.newKeyguardLock("");

		// 屏蔽手机内置的锁屏
		mKeyguardLock.disableKeyguard();

		/* 注册广播 */
		// IntentFilter mScreenOnFilter = new IntentFilter(
		// "android.intent.action.SCREEN_ON");
		// LockService.this.registerReceiver(mScreenOnReceiver,
		// mScreenOnFilter);

		/* 注册广播 */
		IntentFilter mScreenOnOrOffFilter = new IntentFilter();
		mScreenOnOrOffFilter.addAction("android.intent.action.SCREEN_ON");
		mScreenOnOrOffFilter.addAction("android.intent.action.SCREEN_OFF");
		LockService.this.registerReceiver(mScreenOnOrOffReceiver,
				mScreenOnOrOffFilter);
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		logger.i("----LockService被启动了----");
		isServiceRunning = true;
		ObserverManage.getObserver().addObserver(this);
		run();
	}

	private void run() {

		// if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
		// logger.i("屏幕屏锁了--->");
		// if (!ShowLockActivity.active)
		// startActivity(lockIntent);
		// }
	}

	public void onDestroy() {
		isServiceRunning = false;
		mKeyguardLock.reenableKeyguard();
		super.onDestroy();
		logger.i("----LockService被回收了----");
		ObserverManage.getObserver().deleteObserver(this);
		this.unregisterReceiver(mScreenOnOrOffReceiver);
		if (!Constants.APPCLOSE && !MainActivity.SCREEN_OFF) {
			// 在此重新启动,使服务常驻内存当然如果系统资源不足，android系统也可能结束服务。
			startService(new Intent(this, LockService.class));
			logger.i("----LockService被重新启动了----");
		}
	}

	// private KeyguardManager mKeyguardManager = null;
	// private KeyguardManager.KeyguardLock mKeyguardLock = null;
	// // 屏幕变亮的广播,我们要隐藏默认的锁屏界面
	// private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
	// }
	// }
	// };

	// 屏幕变暗/变亮的广播 ， 我们要调用KeyguardManager类相应方法去解除屏幕锁定
	private BroadcastReceiver mScreenOnOrOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("android.intent.action.SCREEN_OFF")
					|| action.equals("android.intent.action.SCREEN_ON")) {
				// if (!ShowLockActivity.active)
				// startActivity(lockIntent);
			}
			// String action = intent.getAction();
			//
			// /**
			// * ACTION_SCREEN_OFF表示按下电源键，屏幕黑屏 ACTION_SCREEN_ON 屏幕黑屏情况下，按下电源键
			// */
			//
			// if (action.equals("android.intent.action.SCREEN_OFF")) {
			// // // 屏蔽手机内置的锁屏
			// // KeyguardManager mKeyguardManager = (KeyguardManager) context
			// // .getSystemService(Context.KEYGUARD_SERVICE);
			// // KeyguardLock mKeyguardLock = mKeyguardManager
			// // .newKeyguardLock("");
			// // // 屏蔽手机内置的锁屏
			// // mKeyguardLock.disableKeyguard();
			// // startActivity(lockIntent);
			// // 屏锁
			//
			// } else if (action.equals("android.intent.action.SCREEN_ON")) {
			// // 屏锁
			// if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
			// logger.i("屏幕屏锁了--->");
			// startActivity(lockIntent);
			// }
			// //
			// // KeyguardManager mKeyguardManager = (KeyguardManager) context
			// // .getSystemService(Context.KEYGUARD_SERVICE);
			// // // 屏锁
			// // if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
			// // logger.i("屏幕屏锁了--->");
			// // startActivity(lockIntent);
			// // }
			// }
		}
	};

	@Override
	public void update(Observable arg0, Object data) {

	}
}
