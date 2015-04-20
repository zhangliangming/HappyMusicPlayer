package com.happyplayer.ui;

import java.util.ArrayList;

import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.happyplayer.common.Constants;
import com.happyplayer.slidingmenu.SlidingMenu;
import com.happyplayer.slidingmenu.SlidingMenu.OnClosedListener;
import com.happyplayer.slidingmenu.SlidingMenu.OnOpenedListener;
import com.happyplayer.util.ActivityManager;
import com.happyplayer.util.DataUtil;

public class Copy_2_of_MainActivity extends FragmentActivity {
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

	private ImageView flagImageView;
	private TextView timeTextView;

	private SlidingMenu mMenu;

	private TabFragmentPagerAdapter tabFragmentPagerAdapter;

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
		fragmentList.add(new MyFragment(null));
		fragmentList.add(new FindFragment());
		fragmentList.add(new SearchFragment());

		tabButton = new RadioButton[fragmentList.size()];
		tabButton[0] = (RadioButton) findViewById(R.id.tab_my);
		tabButton[1] = (RadioButton) findViewById(R.id.tab_find);
		tabButton[2] = (RadioButton) findViewById(R.id.tab_search);

		group = (RadioGroup) findViewById(R.id.tab);
		group.setOnCheckedChangeListener(new TabOnCheckedChangeListener());

		tabFragmentPagerAdapter = new TabFragmentPagerAdapter(
				getSupportFragmentManager());
		viewPager.setAdapter(tabFragmentPagerAdapter);

		viewPager.setCurrentItem(0);

		// 设置viewpager的缓存页面
		viewPager.setOffscreenPageLimit(fragmentList.size());
		viewPager.setOnPageChangeListener(new TabOnPageChangeListener());

		// viewPager.setBackgroundResource(R.drawable.skin_def);

		mMenu = (SlidingMenu) findViewById(R.id.player_bar_bg);
		mMenu.setMode(SlidingMenu.LEFT);
		mMenu.setFadeEnabled(false);
		mMenu.setBehindScrollScale(0.8f);
		mMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mMenu.setTouchModeAbove(SlidingMenu.SLIDING_CONTENT);

		View centMenu = LayoutInflater.from(this).inflate(R.layout.cent_menu,
				null, false);
		flagImageView = (ImageView) centMenu.findViewById(R.id.flag);
		timeTextView = (TextView) centMenu.findViewById(R.id.time);
		timeTextView.setVisibility(View.INVISIBLE);

		mMenu.setContent(centMenu);

		mMenu.setMenu(R.layout.left_menu);
		mMenu.setOnOpenedListener(new OnOpenedListener() {

			@Override
			public void onOpened() {
				flagImageView
						.setBackgroundResource(R.drawable.kg_ic_playing_bar_drag_opened);
				timeTextView.setVisibility(View.VISIBLE);
				Constants.BAR_LRC_IS_OPEN = true;
				DataUtil.save(Copy_2_of_MainActivity.this,
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
				DataUtil.save(Copy_2_of_MainActivity.this,
						Constants.BAR_LRC_IS_OPEN_KEY,
						Constants.BAR_LRC_IS_OPEN);
			}
		});
		if (Constants.BAR_LRC_IS_OPEN) {
			mMenu.toggle();
		}
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
				} else {
					setTabButtonBackground(arg0);
				}
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

	/**
	 * 打开歌曲窗口
	 * 
	 * @param v
	 */
	public void openLrcDialog(View v) {
		Intent intent = new Intent(this, LrcViewActivity.class);
		startActivity(intent);
	}

}
