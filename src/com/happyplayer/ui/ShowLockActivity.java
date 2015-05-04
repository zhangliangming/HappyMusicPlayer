package com.happyplayer.ui;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.happyplayer.common.Constants;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.util.ActivityManager;

public class ShowLockActivity extends FragmentActivity implements Observer {

	public static boolean active = false;

	private ViewPager viewPager;
	/**
	 * 页面列表
	 */
	private ArrayList<Fragment> fragmentList;

	private TabFragmentPagerAdapter tabFragmentPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		//
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		// showStatusBar();
		setContentView(R.layout.activity_lock);
		init();
		setBackground();
		ObserverManage.getObserver().addObserver(this);
		ActivityManager.getInstance().addActivity(this);
		handler.post(myRunnable);
	}

	private void setBackground() {
		viewPager
				.setBackgroundResource(Constants.PICIDS[Constants.DEF_PIC_INDEX]);
	}

	private void init() {
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		fragmentList = new ArrayList<Fragment>();

		fragmentList.add(new TransparentFragment());
		fragmentList.add(new LockMenuFragment());

		tabFragmentPagerAdapter = new TabFragmentPagerAdapter(
				getSupportFragmentManager());
		viewPager.setAdapter(tabFragmentPagerAdapter);

		viewPager.setCurrentItem(1);

		// 设置viewpager的缓存页面
		// viewPager.setOffscreenPageLimit(fragmentList.size());
		viewPager.setOnPageChangeListener(new TabOnPageChangeListener());
	}

	@Override
	protected void onStart() {
		active = true;
		super.onStart();
	}

	@Override
	protected void onStop() {
		active = false;
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		handler.removeCallbacks(myRunnable);
		super.onDestroy();
	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SkinMessage) {
			SkinMessage msg = (SkinMessage) data;
			if (msg.type == SkinMessage.PIC) {
				setBackground();
			}
		}
	}

	private Handler handler = new Handler();
	private Runnable myRunnable = new Runnable() {
		public void run() {
			// if (active) {
			KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
			// 屏锁
			if (km.inKeyguardRestrictedInputMode()) {
				handler.postDelayed(this, 100);
			} else {
				finish();
			}
			// }
		}
	};

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
			if (arg0 == 0) {
				finish();
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

	public boolean onKeyDown(int keyCode, KeyEvent event) { // 屏蔽按键
		return true;
	}

	// public void onAttachedToWindow() {// 屏蔽home键
	// this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	// super.onAttachedToWindow();
	// }

	// private void hideStatusBar() {
	// WindowManager.LayoutParams attrs = getWindow().getAttributes();
	// attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
	// getWindow().setAttributes(attrs);
	// }
	//
	// private void showStatusBar() {
	// WindowManager.LayoutParams attrs = getWindow().getAttributes();
	// attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
	// getWindow().setAttributes(attrs);
	// }

}
