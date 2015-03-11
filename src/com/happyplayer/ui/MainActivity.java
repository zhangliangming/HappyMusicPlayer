package com.happyplayer.ui;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.happyplayer.common.Constants;
import com.happyplayer.util.ActivityManager;

public class MainActivity extends FragmentActivity {
	private ViewPager viewPager;
	/**
	 * 页面列表
	 */
	private ArrayList<Fragment> fragmentList;
	/**
	 * 记录tab的几个RadioButton
	 */
	private RadioButton tabButton[];

	private RadioGroup group;

	private View mainView;

	private int TAB_INDEX = 0;
	
	private long mExitTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainView = LayoutInflater.from(this).inflate(R.layout.activity_main,
				null);
		setContentView(mainView);
		init();
		setTabButtonBackground(TAB_INDEX);
		ActivityManager.getInstance().addActivity(this);
	}

	private void init() {
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		fragmentList = new ArrayList<Fragment>();
		fragmentList.add(new MyFragment());
		fragmentList.add(new FindFragment());
		fragmentList.add(new SearchFragment());

		tabButton = new RadioButton[fragmentList.size()];
		tabButton[0] = (RadioButton) findViewById(R.id.tab_my);
		tabButton[1] = (RadioButton) findViewById(R.id.tab_find);
		tabButton[2] = (RadioButton) findViewById(R.id.tab_search);
		
		group = (RadioGroup) findViewById(R.id.tab);
		group.setOnCheckedChangeListener(new TabOnCheckedChangeListener());

		viewPager.setAdapter(new TabFragmentPagerAdapter(
				getSupportFragmentManager()));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new TabOnPageChangeListener());
		
		viewPager.setBackgroundResource(R.drawable.splash);
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
			for (int i = 0; i < tabButton.length; i++) {
				if (i == arg0) {
					tabButton[i].setChecked(true);
				}
			}
			setTabButtonBackground(arg0);
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

	/**
	 * 头标点击监听
	 */
	private class TabOnCheckedChangeListener implements OnCheckedChangeListener {

		public void onCheckedChanged(RadioGroup arg0, int arg1) {
			int index = 0;
			for (int i = 0; i < tabButton.length; i++) {
				if (tabButton[i].getId() == arg1
						&& tabButton[i].isChecked() == true) {
					viewPager.setCurrentItem(i);
					index = i;
				}
			}
			setTabButtonBackground(index);
		}
	}

	/**
	 * 
	 * @param postion
	 */
	private void setTabButtonBackground(int postion) {
		TAB_INDEX = postion;
		for (int i = 0; i < tabButton.length; i++) {
			if (i == postion) {
				tabButton[i].setTextColor(Constants.TEXT_COLOR_PRESSED);
			} else {
				tabButton[i].setTextColor(Constants.TEXT_COLOR);
			}
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, R.string.exit_tip, Toast.LENGTH_SHORT)
						.show();
				mExitTime = System.currentTimeMillis();
			} else {
				ActivityManager.getInstance().exit();
			}
		}
		return false;
	}
}
