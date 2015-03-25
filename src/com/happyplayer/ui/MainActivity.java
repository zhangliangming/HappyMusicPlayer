package com.happyplayer.ui;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.common.Constants;
import com.happyplayer.iface.PageAction;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.slidingmenu.SlidingMenu;
import com.happyplayer.slidingmenu.SlidingMenu.OnClosedListener;
import com.happyplayer.slidingmenu.SlidingMenu.OnOpenedListener;
import com.happyplayer.util.ActivityManager;
import com.happyplayer.util.DataUtil;
import com.happyplayer.util.ImageUtil;
import com.happyplayer.util.MediaUtils;

public class MainActivity extends FragmentActivity implements Observer {
	private ViewPager viewPager;
	/**
	 * 页面列表
	 */
	private ArrayList<Fragment> fragmentList;

	private View mainView;

	private long mExitTime;

	private TabFragmentPagerAdapter tabFragmentPagerAdapter;

	private ImageView flagImageView;
	private TextView timeTextView;

	private SlidingMenu mMenu;

	private MainPageAction action;

	private int TAB_INDEX = 0;
	/**
	 * 歌手图片和专辑图片
	 */
	private ImageView singerPicImageView;
	/**
	 * 歌名
	 */
	private TextView songNameTextView;
	/**
	 * 歌手
	 */
	private TextView singerNameTextView;
	/**
	 * 播放按钮
	 */
	private ImageButton playImageButton;
	/**
	 * 暂停按钮
	 */
	private ImageButton pauseImageButton;

	private Handler songHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			SongMessage songMessage = (SongMessage) msg.obj;
			final SongInfo songInfo = songMessage.getSongInfo();
			switch (songMessage.getType()) {
			case SongMessage.INIT:
				// 本地歌曲
				if (songInfo.getType() == SongInfo.LOCAL) {
					new AsyncTaskHandler() {

						@Override
						protected void onPostExecute(Object result) {
							Bitmap bm = (Bitmap) result;
							if (bm != null) {
								singerPicImageView
										.setBackgroundDrawable(new BitmapDrawable(
												bm));
								// 显示专辑封面图片
							} else {
								bm = MediaUtils.getDefaultArtwork(
										MainActivity.this, false);
								singerPicImageView
										.setBackgroundDrawable(new BitmapDrawable(
												bm));// 显示专辑封面图片
							}
						}

						@Override
						protected Object doInBackground() throws Exception {
							return ImageUtil.getFirstArtwork(
									songInfo.getPath(), songInfo.getSid());
						}
					}.execute();

				} else {
					// 网上下载歌曲
				}
				pauseImageButton.setVisibility(View.INVISIBLE);
				playImageButton.setVisibility(View.VISIBLE);
				songNameTextView.setText(songInfo.getDisplayName());
				singerNameTextView.setText(songInfo.getArtist());
				break;
			case SongMessage.PLAYING:
				break;
			case SongMessage.STOPING:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainView = LayoutInflater.from(this).inflate(R.layout.activity_main,
				null);
		setContentView(mainView);
		init();
		setBackground();
		ObserverManage.getObserver().addObserver(this);
		ActivityManager.getInstance().addActivity(this);
	}

	private void init() {
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		fragmentList = new ArrayList<Fragment>();

		action = new MainPageAction();
		fragmentList.add(new MyFragment(action));

		tabFragmentPagerAdapter = new TabFragmentPagerAdapter(
				getSupportFragmentManager());
		viewPager.setAdapter(tabFragmentPagerAdapter);

		viewPager.setCurrentItem(0);

		// 设置viewpager的缓存页面
		// viewPager.setOffscreenPageLimit(fragmentList.size());
		viewPager.setOnPageChangeListener(new TabOnPageChangeListener());

		mMenu = (SlidingMenu) findViewById(R.id.player_bar_bg);
		mMenu.setMode(SlidingMenu.LEFT);
		mMenu.setFadeEnabled(false);
		mMenu.setBehindScrollScale(1f);
		mMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mMenu.setTouchModeAbove(SlidingMenu.SLIDING_CONTENT);

		View centMenu = LayoutInflater.from(this).inflate(R.layout.cent_menu,
				null, false);
		flagImageView = (ImageView) centMenu.findViewById(R.id.flag);
		timeTextView = (TextView) centMenu.findViewById(R.id.time);
		timeTextView.setVisibility(View.INVISIBLE);

		singerPicImageView = (ImageView) centMenu.findViewById(R.id.singer_pic);
		singerNameTextView = (TextView) centMenu.findViewById(R.id.singer_name);
		songNameTextView = (TextView) centMenu.findViewById(R.id.song_name);
		playImageButton = (ImageButton) centMenu.findViewById(R.id.play_buttom);
		playImageButton.setVisibility(View.VISIBLE);
		pauseImageButton = (ImageButton) centMenu
				.findViewById(R.id.pause_buttom);
		pauseImageButton.setVisibility(View.INVISIBLE);

		mMenu.setContent(centMenu);

		mMenu.setMenu(R.layout.left_menu);
		mMenu.setOnOpenedListener(new OnOpenedListener() {

			@Override
			public void onOpened() {
				flagImageView
						.setBackgroundResource(R.drawable.kg_ic_playing_bar_drag_opened);
				timeTextView.setVisibility(View.VISIBLE);
				Constants.BAR_LRC_IS_OPEN = true;
				DataUtil.save(MainActivity.this, Constants.BAR_LRC_IS_OPEN_KEY,
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
				DataUtil.save(MainActivity.this, Constants.BAR_LRC_IS_OPEN_KEY,
						Constants.BAR_LRC_IS_OPEN);
			}
		});
		if (Constants.BAR_LRC_IS_OPEN) {
			mMenu.toggle();
		}

	}

	private void setBackground() {
		viewPager
				.setBackgroundResource(Constants.PICIDS[Constants.DEF_PIC_INDEX]);
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
			TAB_INDEX = arg0;
			if (TAB_INDEX == 0 && fragmentList.size() == 2) {
				fragmentList.remove(1);
				tabFragmentPagerAdapter = new TabFragmentPagerAdapter(
						getSupportFragmentManager());
				viewPager.setAdapter(tabFragmentPagerAdapter);
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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (TAB_INDEX == 0) {
				if ((System.currentTimeMillis() - mExitTime) > 2000) {
					Toast.makeText(this, R.string.exit_tip, Toast.LENGTH_SHORT)
							.show();
					mExitTime = System.currentTimeMillis();
				} else {
					ActivityManager.getInstance().exit();
				}
			} else {
				viewPager.setCurrentItem(0);
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

	private class MainPageAction implements PageAction {

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
		}
	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SkinMessage) {
			SkinMessage msg = (SkinMessage) data;
			if (msg.type == SkinMessage.PIC) {
				setBackground();
			}
		} else if (data instanceof SongMessage) {
			SongMessage songMessage = (SongMessage) data;
			if (songMessage.getType() == SongMessage.INIT) {
				Message msg = new Message();
				msg.obj = songMessage;
				songHandler.sendMessage(msg);
			}
		}
	}
}
