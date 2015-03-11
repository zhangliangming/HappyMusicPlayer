package com.happyplayer.ui;

import com.happyplayer.widget.ListViewRelativeLayout;
import com.happyplayer.widget.NavPlayImageButton;

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
	}

	/**
	 * 播放
	 */
	private void navPlayImageButton() {
	}
}
