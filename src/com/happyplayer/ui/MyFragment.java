package com.happyplayer.ui;

import com.happyplayer.widget.ListViewRelativeLayout;
import com.happyplayer.widget.NavPlayImageButton;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class MyFragment extends Fragment {
	private View mMainView;

	private ListViewRelativeLayout local;
	private NavPlayImageButton navPlayImageButton;

	private ListViewRelativeLayout scanmusic;
	private ListViewRelativeLayout mylove;
	private ListViewRelativeLayout mydownload;
	private ListViewRelativeLayout setting;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initComponent();
	}

	private void initComponent() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mMainView = inflater.inflate(R.layout.fragment_my, null, false);

		local = (ListViewRelativeLayout) mMainView.findViewById(R.id.local);
		local.setOnClickListener(new ItemOnClick());

		scanmusic = (ListViewRelativeLayout) mMainView
				.findViewById(R.id.scanmusic);
		scanmusic.setOnClickListener(new ItemOnClick());

		mylove = (ListViewRelativeLayout) mMainView.findViewById(R.id.mylove);
		mylove.setOnClickListener(new ItemOnClick());

		mydownload = (ListViewRelativeLayout) mMainView
				.findViewById(R.id.mydownload);
		mydownload.setOnClickListener(new ItemOnClick());

		setting = (ListViewRelativeLayout) mMainView.findViewById(R.id.setting);
		setting.setOnClickListener(new ItemOnClick());

		navPlayImageButton = (NavPlayImageButton) mMainView
				.findViewById(R.id.navPlayImageButton);
		navPlayImageButton.setOnClickListener(new ItemOnClick());
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
	 * 播放
	 */
	private void navPlayImageButton() {
	}
}
