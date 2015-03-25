package com.happyplayer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class CopyOfMainFragment extends Fragment {
	private View mMainView;
	private TabHost tabhost;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initComponent();
	}

	private void initComponent() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mMainView = inflater.inflate(R.layout.fragment_main, null, false);

		tabhost = (TabHost) mMainView.findViewById(R.id.tabhost);
		tabhost.setup();

		TabSpec spec;
		Intent intent;

		intent = new Intent(this.getActivity(), SplashActivity.class);
		spec = tabhost.newTabSpec("我的").setIndicator("我的").setContent(intent);
		tabhost.addTab(spec);

		tabhost.setCurrentTab(0);
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
}
