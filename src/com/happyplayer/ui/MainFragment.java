package com.happyplayer.ui;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.happyplayer.common.Constants;
import com.happyplayer.iface.PageAction;

public class MainFragment extends Fragment implements PageAction {
	private View mMainView;
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

	private TabFragmentPagerAdapter tabFragmentPagerAdapter;

	private int TAB_INDEX = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initComponent();
	}

	private void initComponent() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mMainView = inflater.inflate(R.layout.fragment_main, null, false);

		viewPager = (ViewPager) mMainView.findViewById(R.id.viewpager);
		fragmentList = new ArrayList<Fragment>();
		fragmentList.add(new MyFragment(this));
		fragmentList.add(new FindFragment());
		fragmentList.add(new SearchFragment());

		tabButton = new RadioButton[fragmentList.size()];
		tabButton[0] = (RadioButton) mMainView.findViewById(R.id.tab_my);
		tabButton[1] = (RadioButton) mMainView.findViewById(R.id.tab_find);
		tabButton[2] = (RadioButton) mMainView.findViewById(R.id.tab_search);

		group = (RadioGroup) mMainView.findViewById(R.id.tab);
		group.setOnCheckedChangeListener(new TabOnCheckedChangeListener());

		viewPager.setAdapter(new TabFragmentPagerAdapter(getFragmentManager()));
		viewPager.setCurrentItem(0);

		// 设置viewpager的缓存页面
		viewPager.setOffscreenPageLimit(fragmentList.size());
		viewPager.setOnPageChangeListener(new TabOnPageChangeListener());

		// viewPager.setBackgroundResource(R.drawable.skin_def);

		setTabButtonBackground(TAB_INDEX);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup viewGroup = (ViewGroup) mMainView.getParent();
		if (viewGroup != null) {
			viewGroup.removeAllViewsInLayout();
		}
		return mMainView;
	}

	class ItemOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			// case R.id.setting_system_style:
			// goTheme();
			// break;
			}
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
				}
			}
			setTabButtonBackground(arg0);
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

	@Override
	public void addPage(Fragment fragment) {
		if (!fragmentList.contains(fragment)) {
			if (fragmentList.size() == 2) {
				fragmentList.remove(1);
			}
			fragmentList.add(fragment);
			tabFragmentPagerAdapter.notifyDataSetChanged();
		}
		viewPager.setCurrentItem(fragmentList.size());
	}

	@Override
	public void finish() {
		viewPager.setCurrentItem(0);
		fragmentList.remove(1);
	}
}
