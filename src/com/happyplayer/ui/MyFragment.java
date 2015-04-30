package com.happyplayer.ui;

import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.common.Constants;
import com.happyplayer.iface.PageAction;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.service.EasytouchService;
import com.happyplayer.service.FloatLrcService;
import com.happyplayer.util.DataUtil;
import com.happyplayer.widget.ListViewRelativeLayout;
import com.happyplayer.widget.NavPlayImageButton;

@SuppressLint("ValidFragment")
public class MyFragment extends Fragment implements Observer {
	private View mMainView;

	private ListViewRelativeLayout local;
	/**
	 * 歌曲个数
	 */
	private TextView numTextView;

	private NavPlayImageButton navPlayImageButton;

	private ListViewRelativeLayout scanmusic;
	// private ListViewRelativeLayout mylove;
	// private ListViewRelativeLayout mydownload;
	// private ListViewRelativeLayout setting;

	private ListViewRelativeLayout showdesLrc;
	private CheckBox showcheckboxCheckBox;

	private ListViewRelativeLayout showEasyTouch;
	private CheckBox showEasyTouchCheckBox;

	private ListViewRelativeLayout showLock;
	private CheckBox showlockCheckBox;

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

	private PageAction action;

	public MyFragment() {

	}

	public MyFragment(PageAction action) {
		this.action = action;
	}

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

		// mylove = (ListViewRelativeLayout)
		// mMainView.findViewById(R.id.mylove);
		// mylove.setOnClickListener(new ItemOnClick());

		showLock = (ListViewRelativeLayout) mMainView
				.findViewById(R.id.showLock);
		showLock.setOnClickListener(new ItemOnClick());
		showlockCheckBox = (CheckBox) mMainView.findViewById(R.id.lockCheckbox);
		showlockCheckBox.setChecked(Constants.SHOWLOCK);

		showdesLrc = (ListViewRelativeLayout) mMainView
				.findViewById(R.id.showdesLrc);
		showdesLrc.setOnClickListener(new ItemOnClick());

		showcheckboxCheckBox = (CheckBox) mMainView
				.findViewById(R.id.showcheckbox);
		showcheckboxCheckBox.setChecked(Constants.SHOWDESLRC);

		showEasyTouch = (ListViewRelativeLayout) mMainView
				.findViewById(R.id.showEasyTouch);
		showEasyTouch.setOnClickListener(new ItemOnClick());
		showEasyTouchCheckBox = (CheckBox) mMainView
				.findViewById(R.id.EasyTouchcheckbox);
		showEasyTouchCheckBox.setChecked(Constants.SHOWEASYTOUCH);

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

				Message msg = new Message();
				msg.what = COUNT;
				msg.obj = mCOUNT;

				handler.sendMessage(msg);
			}

			@Override
			protected Object doInBackground() throws Exception {

				Thread.sleep(50);

				mCOUNT = MediaManage.getMediaManage(
						MyFragment.this.getActivity()).getCount();

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
				showdesLrc();
				break;
			case R.id.showEasyTouch:
				showEasyTouch();
				break;
			case R.id.skinsetting:
				gotoSkinSetting();
				break;
			case R.id.showLock:
				showLock();
				break;
			}
		}

	}

	/**
	 * 本地音乐
	 */
	private void local() {
		// Intent intent = new Intent(getActivity(), LocalMusicActivity.class);
		// getActivity().startActivity(intent);
		action.addPage(new LocalMusicFragment(action));
	}

	public void showEasyTouch() {
		if (showEasyTouchCheckBox.isChecked()) {
			showEasyTouchCheckBox.setChecked(false);
			Constants.SHOWEASYTOUCH = false;

			Intent easytouchServiceIntent = new Intent(getActivity(),
					EasytouchService.class);
			getActivity().stopService(easytouchServiceIntent);

		} else {
			Constants.SHOWEASYTOUCH = true;
			showEasyTouchCheckBox.setChecked(true);

			Intent easytouchServiceIntent = new Intent(getActivity(),
					EasytouchService.class);
			getActivity().startService(easytouchServiceIntent);

		}

		DataUtil.save(getActivity(), Constants.SHOWEASYTOUCH_KEY,
				Constants.SHOWEASYTOUCH);
	}

	private void showLock() {
		if (showlockCheckBox.isChecked()) {
			showlockCheckBox.setChecked(false);
			Constants.SHOWLOCK = false;
		} else {
			Constants.SHOWLOCK = true;
			showlockCheckBox.setChecked(true);
		}

		DataUtil.save(getActivity(), Constants.SHOWLOCK_KEY, Constants.SHOWLOCK);
	}

	public void showdesLrc() {

		if (showcheckboxCheckBox.isChecked()) {
			showcheckboxCheckBox.setChecked(false);
			Constants.SHOWDESLRC = false;

			Intent floatLrcServiceIntent = new Intent(getActivity(),
					FloatLrcService.class);
			getActivity().stopService(floatLrcServiceIntent);

		} else {
			Constants.SHOWDESLRC = true;
			showcheckboxCheckBox.setChecked(true);

			Intent floatLrcServiceIntent = new Intent(getActivity(),
					FloatLrcService.class);
			getActivity().startService(floatLrcServiceIntent);
		}

		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {

			}

			protected Object doInBackground() throws Exception {

				DataUtil.save(getActivity(), Constants.SHOWDESLRC_KEY,
						Constants.SHOWDESLRC);
				return null;
			}
		}.execute();

		SongMessage songMessage = new SongMessage();
		songMessage.setType(SongMessage.DES_LRC);
		ObserverManage.getObserver().setMessage(songMessage);

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

		SongMessage songMessage = new SongMessage();
		songMessage.setType(SongMessage.NEXTMUSIC);
		ObserverManage.getObserver().setMessage(songMessage);

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
			} else if (songMessage.getType() == SongMessage.DEL_NUM
					|| songMessage.getType() == SongMessage.DELALLMUSICED) {
				Message msg = new Message();
				msg.what = UPDATE;
				msg.obj = songMessage.getNum();

				handler.sendMessage(msg);
			}
		}
	}

}
