package com.happyplayer.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.happyplayer.common.Constants;
import com.happyplayer.util.ActivityManager;

public class SplashActivity extends Activity {
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
	private static final long SPLASH_DELAY_MILLIS = 3000;

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
		/***
		 * // 1.进入页面后，判断用户是不是第一次使用该应用，如果是则跳转到应用的导航页面 //
		 */
		SharedPreferences preferences = getSharedPreferences(
				Constants.SHARE_PREFERENCE_NAME, 0);
		// 获取是否是第一次使用的参数
		boolean isTheFirst = preferences.getBoolean(Constants.THE_FIRST_KEY,
				Constants.THE_FIRST);
		if (!isTheFirst) {
			handler.sendEmptyMessageDelayed(GO_GUIDE, SPLASH_DELAY_MILLIS);
		} else {
			handler.sendEmptyMessageDelayed(GO_HOME, SPLASH_DELAY_MILLIS);
		}
	}

	/**
	 * 跳转到主页面
	 */
	public void goHome() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
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
}
