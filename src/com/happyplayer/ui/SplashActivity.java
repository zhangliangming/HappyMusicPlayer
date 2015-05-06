package com.happyplayer.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.happyplayer.common.Constants;
import com.happyplayer.util.ActivityManager;
import com.happyplayer.util.DataUtil;

public class SplashActivity extends Activity {
	/**
	 * 默认时
	 */
	private final static int UPDATE = -1;
	/**
	 * 跳转到主页页面
	 */
	private final static int GO_HOME = 0;

	/**
	 * 跳转到页面导航
	 */
	private final static int GO_GUIDE = 1;
	/**
	 * 设置页面跳转时的延迟时间
	 */
	private static final int SPLASH_DELAY_MILLIS = 5000;

	private ImageView splashImageView = null;

	private TextView timeTextView = null;

	/**
	 * 根据msg.what来跳转到不同的页面
	 */
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GO_HOME:
				goHome();
				break;
			case GO_GUIDE:
				goGuide();
				break;
			case UPDATE:
				int ctime = (Integer) msg.obj;
				int stime = (SPLASH_DELAY_MILLIS - ctime) / 1000;
				timeTextView.setVisibility(View.VISIBLE);
				timeTextView.setText(stime + "");
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		init();
		ActivityManager.getInstance().addActivity(this);
	}

	/**
	 * 初始化
	 */
	private void init() {
		splashImageView = (ImageView) findViewById(R.id.splash);
		timeTextView = (TextView) findViewById(R.id.time);
		timeTextView.setVisibility(View.INVISIBLE);

		Thread thread = new Thread(new TimeTipRunnable());
		thread.start();
	}

	class TimeTipRunnable implements Runnable {

		@Override
		public void run() {

			DataUtil.init(SplashActivity.this);

			// 获取是否是第一次使用的参数
			boolean isTheFirst = Constants.THE_FIRST;

			if (isTheFirst) {
				handler.sendEmptyMessageDelayed(GO_GUIDE, 1000);
			} else {
				int time = 0;
				Message msg = null;
				while (time <= SPLASH_DELAY_MILLIS) {
					try {
						Thread.sleep(1000);
						msg = new Message();
						msg.what = UPDATE;
						msg.obj = time;
						handler.sendMessage(msg);
						msg = null;
						time = time + 1000;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				handler.sendEmptyMessage(GO_HOME);
			}
		}
	}

	/**
	 * 跳转到主页面
	 */
	public void goHome() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
		finish();
	}

	/**
	 * 跳转到应用导航页面
	 */
	public void goGuide() {
		Intent intent = new Intent(this, GuideActivity.class);
		startActivity(intent);
		finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
				|| keyCode == KeyEvent.KEYCODE_HOME) {
			ActivityManager.getInstance().exit();
		}
		return false;
	}
}
