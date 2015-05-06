package com.happyplayer.ui;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.happyplayer.util.ActivityManager;

/**
 * 导航页面
 * 
 * @author Administrator
 * 
 */
public class GuideActivity extends FragmentActivity {
	private ViewPager viewPager;

	private TabFragmentPagerAdapter tabFragmentPagerAdapter;
	/**
	 * 页面列表
	 */
	private ArrayList<Fragment> fragmentList;

	private GuideFragment firstGuideFragment;
	private GuideFragment secondGuideFragment;
	private GuideFragment thirdGuideFragment;
	private GuideFragment fourthGuideFragment;

	private long mExitTime;

	private int TAB_INDEX = 0;

	/** 将小圆点的图片用数组表示 */
	private ImageView[] imageViews;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		init();
		ActivityManager.getInstance().addActivity(this);
	}

	private void init() {
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		fragmentList = new ArrayList<Fragment>();

		//
		firstGuideFragment = new GuideFragment();
		firstGuideFragment.setParentColor(Color.rgb(163, 161, 212));
		firstGuideFragment.setMainTitleImage(R.drawable.guide_first_top_image);
		firstGuideFragment
				.setSecondTitleImage(R.drawable.guide_first_middle_image);
		firstGuideFragment.setCentPICImage(R.drawable.guide_first_person);
		firstGuideFragment.setInitAni(true);
		//
		secondGuideFragment = new GuideFragment();
		secondGuideFragment.setParentColor(Color.rgb(254, 153, 153));
		secondGuideFragment
				.setMainTitleImage(R.drawable.guide_second_top_image);
		secondGuideFragment
				.setSecondTitleImage(R.drawable.guide_second_middle_image);
		secondGuideFragment.setCentPICImage(R.drawable.guide_second_person);
		//
		thirdGuideFragment = new GuideFragment();
		thirdGuideFragment.setParentColor(Color.rgb(225, 184, 94));
		thirdGuideFragment.setMainTitleImage(R.drawable.guide_four_top_image);
		thirdGuideFragment
				.setSecondTitleImage(R.drawable.guide_four_middle_image);
		thirdGuideFragment.setCentPICImage(R.drawable.guide_four_bottom_person);
		//
		fourthGuideFragment = new GuideFragment();
		fourthGuideFragment.setVisibility(true);
		fourthGuideFragment.setParentColor(Color.rgb(77, 199, 255));
		fourthGuideFragment.setMainTitleImage(R.drawable.guide_third_top_image);
		fourthGuideFragment
				.setSecondTitleImage(R.drawable.guide_third_middle_image);
		fourthGuideFragment
				.setCentPICImage(R.drawable.guide_third_bottom_person);

		fragmentList.add(firstGuideFragment);
		fragmentList.add(secondGuideFragment);
		fragmentList.add(thirdGuideFragment);
		fragmentList.add(fourthGuideFragment);

		// 创建imageviews数组，大小是要显示的图片的数量
		imageViews = new ImageView[fragmentList.size()];

		int i = 0;
		imageViews[i++] = (ImageView) findViewById(R.id.point_1);
		imageViews[i++] = (ImageView) findViewById(R.id.point_2);
		imageViews[i++] = (ImageView) findViewById(R.id.point_3);
		imageViews[i++] = (ImageView) findViewById(R.id.point_4);

		for (int j = 0; j < imageViews.length; j++) {
			if (j != 0) {
				imageViews[j]
						.setBackgroundResource(R.drawable.music_zone_indicator_common);
			} else {
				imageViews[j]
						.setBackgroundResource(R.drawable.music_zone_indicator_current);
			}
		}

		tabFragmentPagerAdapter = new TabFragmentPagerAdapter(
				getSupportFragmentManager());
		viewPager.setAdapter(tabFragmentPagerAdapter);
		// 设置viewpager的缓存页面
		// viewPager.setOffscreenPageLimit(fragmentList.size());
		viewPager.setOnPageChangeListener(new TabOnPageChangeListener());
		viewPager.setCurrentItem(0);
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

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			int position = (Integer) msg.obj;

			for (int i = 0; i < imageViews.length; i++) {
				// 不是当前选中的page，其小圆点设置为未选中的状态
				if (position != i) {
					imageViews[i]
							.setBackgroundResource(R.drawable.music_zone_indicator_common);
				} else {
					imageViews[position]
							.setBackgroundResource(R.drawable.music_zone_indicator_current);
				}
			}
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

		public void onPageSelected(int position) {
			Message msg = new Message();
			msg.obj = position;
			mHandler.sendMessage(msg);

			boolean isRightToLeft = false;
			if (TAB_INDEX < position) {
				isRightToLeft = true;
			}
			TAB_INDEX = position;
			switch (position) {
			case 0:
				firstGuideFragment.setMainTitleImageAnimation(isRightToLeft);
				firstGuideFragment.setSecondTitleImageAnimation(isRightToLeft);

				secondGuideFragment.setAnimationStop();
				thirdGuideFragment.setAnimationStop();
				fourthGuideFragment.setAnimationStop();
				break;
			case 1:
				secondGuideFragment.setMainTitleImageAnimation(isRightToLeft);
				secondGuideFragment.setSecondTitleImageAnimation(isRightToLeft);

				firstGuideFragment.setAnimationStop();
				thirdGuideFragment.setAnimationStop();
				fourthGuideFragment.setAnimationStop();
				break;
			case 2:
				thirdGuideFragment.setMainTitleImageAnimation(isRightToLeft);
				thirdGuideFragment.setSecondTitleImageAnimation(isRightToLeft);

				firstGuideFragment.setAnimationStop();
				secondGuideFragment.setAnimationStop();
				fourthGuideFragment.setAnimationStop();
				break;
			case 3:
				fourthGuideFragment.setMainTitleImageAnimation(isRightToLeft);
				fourthGuideFragment.setSecondTitleImageAnimation(isRightToLeft);

				firstGuideFragment.setAnimationStop();
				secondGuideFragment.setAnimationStop();
				thirdGuideFragment.setAnimationStop();
				break;
			default:
				break;
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, R.string.exit_tip, Toast.LENGTH_SHORT)
						.show();
				mExitTime = System.currentTimeMillis();
			} else {
				ActivityManager.getInstance().exit();
			}
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
}
