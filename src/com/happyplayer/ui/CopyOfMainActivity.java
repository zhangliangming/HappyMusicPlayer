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
import com.happyplayer.swipelibrary.SwipeLayout;
import com.happyplayer.util.ActivityManager;

public class CopyOfMainActivity extends FragmentActivity {
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

	private SwipeLayout playerBarSwipeLayout;

	private ImageView flagImageView;
	private TextView timeTextView;

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

		viewPager.setAdapter(new TabFragmentPagerAdapter(
				getSupportFragmentManager()));
		viewPager.setCurrentItem(0);

		// 设置viewpager的缓存页面
		viewPager.setOffscreenPageLimit(fragmentList.size());
		viewPager.setOnPageChangeListener(new TabOnPageChangeListener());

		viewPager.setBackgroundResource(R.drawable.splash);

		playerBarSwipeLayout = (SwipeLayout) findViewById(R.id.player_bar_bg);
		// playerBarSwipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
		playerBarSwipeLayout.setDragEdge(SwipeLayout.DragEdge.Left);

		//
		flagImageView = (ImageView) findViewById(R.id.flag);
		timeTextView = (TextView) findViewById(R.id.time);
		timeTextView.setVisibility(View.INVISIBLE);

		// playerBarSwipeLayout.addRevealListener(R.id.delete,
		// new SwipeLayout.OnRevealListener() {
		// @Override
		// public void onReveal(View child, SwipeLayout.DragEdge edge,
		// float fraction, int distance) {
		//
		// }
		// });

		playerBarSwipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
			@Override
			public void onClose(SwipeLayout layout) {
				flagImageView
						.setBackgroundResource(R.drawable.kg_ic_playing_bar_drag_closed);
				timeTextView.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onUpdate(SwipeLayout layout, int leftOffset,
					int topOffset) {

			}

			@Override
			public void onOpen(SwipeLayout layout) {
				flagImageView
						.setBackgroundResource(R.drawable.kg_ic_playing_bar_drag_opened);
				timeTextView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

			}
		});
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
