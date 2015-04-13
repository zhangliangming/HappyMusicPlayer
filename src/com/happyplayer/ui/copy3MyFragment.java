package com.happyplayer.ui;

import java.util.Observable;
import java.util.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.widget.ListViewRelativeLayout;
import com.happyplayer.widget.NavPlayImageButton;

public class copy3MyFragment extends Fragment implements Observer {
	private View mMainView;

	private ListViewRelativeLayout local;
	/**
	 * 歌曲个数
	 */
	private TextView numTextView;

	private NavPlayImageButton navPlayImageButton;

	private ListViewRelativeLayout scanmusic;
	private ListViewRelativeLayout mylove;
	// private ListViewRelativeLayout mydownload;
	// private ListViewRelativeLayout setting;

	private ListViewRelativeLayout showdesLrc;

	private ListViewRelativeLayout skinsetting;

	/**
	 * 初始时歌曲个数
	 */
	private final int COUNT = 0;
	/**
	 * 更新歌曲个数
	 */
	private final int UPDATE = 1;

	private int mCOUNT = 0;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case COUNT:
				int count = (Integer) msg.obj;
				numTextView.setText(count + "首");
				break;
			case UPDATE:

				int updateCount = (Integer) msg.obj;
				mCOUNT = mCOUNT + updateCount;
				numTextView.setText(mCOUNT + "首");
				break;
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initComponent();
		loadData();
		ObserverManage.getObserver().addObserver(this);
	}

	private void initComponent() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mMainView = inflater.inflate(R.layout.fragment_my, null, false);

		local = (ListViewRelativeLayout) mMainView.findViewById(R.id.local);
		local.setOnClickListener(new ItemOnClick());

		numTextView = (TextView) mMainView.findViewById(R.id.num);

		scanmusic = (ListViewRelativeLayout) mMainView
				.findViewById(R.id.scanmusic);
		scanmusic.setOnClickListener(new ItemOnClick());

		mylove = (ListViewRelativeLayout) mMainView.findViewById(R.id.mylove);
		mylove.setOnClickListener(new ItemOnClick());

		showdesLrc = (ListViewRelativeLayout) mMainView
				.findViewById(R.id.showdesLrc);
		showdesLrc.setOnClickListener(new ItemOnClick());

		skinsetting = (ListViewRelativeLayout) mMainView
				.findViewById(R.id.skinsetting);
		skinsetting.setOnClickListener(new ItemOnClick());

		// mydownload = (ListViewRelativeLayout) mMainView
		// .findViewById(R.id.mydownload);
		// mydownload.setOnClickListener(new ItemOnClick());

		// setting = (ListViewRelativeLayout)
		// mMainView.findViewById(R.id.setting);
		// setting.setOnClickListener(new ItemOnClick());

		navPlayImageButton = (NavPlayImageButton) mMainView
				.findViewById(R.id.navPlayImageButton);
		navPlayImageButton.setOnClickListener(new ItemOnClick());
	}

	private void loadData() {
		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {

			}

			@Override
			protected Object doInBackground() throws Exception {

				mCOUNT = MediaManage.getMediaManage(
						copy3MyFragment.this.getActivity()).getCount();

				Message msg = new Message();
				msg.what = COUNT;
				msg.obj = mCOUNT;

				handler.sendMessage(msg);
				return null;
			}
		}.execute();
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
			case R.id.local:
				local();
				break;
			case R.id.scanmusic:
				scanmusic();
				break;
			case R.id.mylove:
				mylove();
				break;
			case R.id.mydownload:
				mydownload();
				break;
			case R.id.setting:
				setting();
				break;
			case R.id.navPlayImageButton:
				navPlayImageButton();
				break;
			case R.id.showdesLrc:
				break;
			case R.id.skinsetting:
				gotoSkinSetting();
				break;
			}
		}

	}

	/**
	 * 本地音乐
	 */
	private void local() {
		Intent intent = new Intent(getActivity(), LocalMusicActivity.class);
		getActivity().startActivity(intent);
	}

	/**
	 * 扫描本地音乐
	 */
	private void scanmusic() {
		Intent intent = new Intent(getActivity(), ScanMusicActivity.class);
		getActivity().startActivity(intent);
	}

	/**
	 * 我的收藏
	 */
	private void mylove() {
		Intent intent = new Intent(getActivity(), MyLoveActivity.class);
		getActivity().startActivity(intent);
	}

	/**
	 * 我的下载
	 */
	private void mydownload() {
		Intent intent = new Intent(getActivity(), MyDownloadActivity.class);
		getActivity().startActivity(intent);
	}

	/**
	 * 设置页面
	 */
	private void setting() {
		Intent intent = new Intent(getActivity(), SettingActivity.class);
		getActivity().startActivity(intent);
	}

	/**
	 * 皮肤设置页面
	 */
	private void gotoSkinSetting() {
		Intent intent = new Intent(getActivity(), SkinPicActivity.class);
		getActivity().startActivity(intent);
	}

	/**
	 * 播放
	 */
	private void navPlayImageButton() {
	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SongMessage) {
			SongMessage songMessage = (SongMessage) data;
			if (songMessage.getType() == SongMessage.SCAN_NUM) {
				Message msg = new Message();
				msg.what = UPDATE;
				msg.obj = songMessage.getNum();

				handler.sendMessage(msg);
			}
		}
	}

}
