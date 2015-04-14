package com.happyplayer.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.happyplayer.adapter.PlayListAdapter;
import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.common.Constants;
import com.happyplayer.db.SongDB;
import com.happyplayer.iface.PageAction;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.Category;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.widget.BladeView;
import com.happyplayer.widget.BladeView.OnItemClickListener;
import com.happyplayer.widget.LoadRelativeLayout;
import com.happyplayer.widget.MySectionIndexer;

@SuppressLint("ValidFragment")
public class LocalMusicFragment extends Fragment implements Observer,
		OnScrollListener {
	private View mMainView;
	private ImageButton backImageButton;
	private PageAction action;

	private LoadRelativeLayout loadRelativeLayout;

	private ListView playlistView;
	private String ALL_CHARACTER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
	private MySectionIndexer mIndexer;
	private BladeView mLetterListView;
	private List<String> categoryList;
	private String[] sections = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
			"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
			"W", "X", "Y", "Z", "#" };
	private int[] counts = new int[sections.length];

	private List<Category> categorys;
	private PlayListAdapter adapter;

	private View footView;

	private boolean isFirst = true;
	/**
	 * 歌曲定位面板
	 */
	private RelativeLayout localMusicLocateRelativeLayout;
	/**
	 * 显示面板倒计时
	 */
	public int EndTime = -1;

	private int playIndexPosition = -1;

	private Context context;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				loadRelativeLayout.showLoadingView();
				break;
			case 1:
				int count = MediaManage.getMediaManage(getActivity())
						.getCount();
				((TextView) footView.findViewById(R.id.list_size_text))
						.setText("共有" + count + "首歌曲");
				loadRelativeLayout.showSuccessView();

				// 设置playlistView的位置
				if (adapter == null)
					return;
				int playIndexPosition = adapter.getPlayIndexPosition();
				if (playIndexPosition != -1) {
					int firstPosition = playlistView.getFirstVisiblePosition()
							- playlistView.getHeaderViewsCount();
					int lastPosition = playlistView.getLastVisiblePosition()
							- playlistView.getFooterViewsCount();
					int middle = (lastPosition - firstPosition) / 2;
					int position = playIndexPosition - middle;
					if (position > 0) {
						playlistView.setSelection(position);
					} else {
						playlistView.setSelection(0);
					}
				}
				break;
			case 2:
				int newcount = MediaManage.getMediaManage(getActivity())
						.getCount();
				((TextView) footView.findViewById(R.id.list_size_text))
						.setText("共有" + newcount + "首歌曲");
				break;
			}
		}

	};

	public LocalMusicFragment() {

	}

	public LocalMusicFragment(PageAction action) {
		this.action = action;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initComponent();
		loadData();

		ObserverManage.getObserver().addObserver(this);

		isFirst = false;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {

		if (isVisibleToUser && isFirst) {
			isFirst = false;
			loadData();
		}
	}

	private void initComponent() {
		context = getActivity();
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mMainView = inflater.inflate(R.layout.activity_localmusic, null, false);

		backImageButton = (ImageButton) mMainView
				.findViewById(R.id.backImageButton);
		backImageButton.setOnClickListener(new ItemOnClick());

		loadRelativeLayout = (LoadRelativeLayout) mMainView
				.findViewById(R.id.loadRelativeLayout);

		playlistView = (ListView) mMainView.findViewById(R.id.playlistView);

		footView = inflater.inflate(R.layout.playlist_list_foot, null);

		mLetterListView = (BladeView) mMainView
				.findViewById(R.id.mLetterListView);

		mLetterListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(String s) {
				if (s != null && categoryList != null
						&& (categoryList.contains(s) || s.equals("#"))) {

					int section = ALL_CHARACTER.indexOf(s);

					int position = mIndexer.getPositionForSection(section);

					if (position != -1) {
						playlistView.setSelection(position);
					}
				}

			}
		});

		loadRelativeLayout.init(getActivity());

		// 给ListView添加footView
		playlistView.addFooterView(footView);
		playlistView.setOnScrollListener(this);

		localMusicLocateRelativeLayout = (RelativeLayout) mMainView
				.findViewById(R.id.local_music_locate);
		localMusicLocateRelativeLayout.setVisibility(View.GONE);

		localMusicLocateRelativeLayout
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (adapter == null)
							return;
						int playIndexPosition = adapter.getPlayIndexPosition();
						if (playIndexPosition != -1) {
							int firstPosition = playlistView
									.getFirstVisiblePosition()
									- playlistView.getHeaderViewsCount();
							int lastPosition = playlistView
									.getLastVisiblePosition()
									- playlistView.getFooterViewsCount();
							int middle = (lastPosition - firstPosition) / 2;
							int position = playIndexPosition - middle;
							if (position > 0) {
								playlistView.setSelection(position);
							} else {
								playlistView.setSelection(0);
							}
						}
					}
				});

	}

	private void loadData() {
		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {
				mIndexer = new MySectionIndexer(sections, counts);
				// 不能把一个对象重复放到观察者队列里面
				if (adapter != null) {
					ObserverManage.getObserver().deleteObserver(adapter);
				}
				adapter = new PlayListAdapter(context, categorys, playlistView);

				adapter.setPlayIndexPosition(playIndexPosition);

				playlistView.setAdapter(adapter);

				handler.sendEmptyMessage(1);
			}

			@Override
			protected Object doInBackground() throws Exception {
				handler.sendEmptyMessage(0);
				loadLocalMusic();
				return null;
			}
		}.execute();
	}

	/**
	 * 加载本地歌曲，以类型分类
	 */
	private void loadLocalMusic() {
		playIndexPosition = -1;
		categorys = new ArrayList<Category>();
		categoryList = SongDB.getSongInfoDB(getActivity()).getAllCategory();
		int count = 0;
		for (int i = 0; i < categoryList.size(); i++) {
			String categoryName = categoryList.get(i);
			List<SongInfo> songInfos = SongDB.getSongInfoDB(getActivity())
					.getAllCategorySong(categoryName);
			if (categoryName.equals("^")) {
				categoryName = "#";
			}
			Category category = new Category(categoryName);
			category.setmCategoryItem(songInfos);
			//
			int index = ALL_CHARACTER.indexOf(categoryName);
			counts[index] = songInfos.size() + 1;
			//

			for (int j = 0; j < songInfos.size(); j++) {
				if (songInfos.get(j).getSid().equals(Constants.PLAY_SID)) {
					playIndexPosition = count + j + 1;
				}
			}
			count += category.getItemCount();

			categorys.add(category);
		}
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
			case R.id.backImageButton:
				finish();
				break;
			}
		}
	}

	private void finish() {
		action.finish();
	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SongMessage) {
			SongMessage songMessage = (SongMessage) data;
			if (songMessage.getType() == SongMessage.SCAN_NUM) {
				isFirst = true;
			} else if (songMessage.getType() == SongMessage.DELALLMUSICED) {
				loadData();
			} else if (songMessage.getType() == SongMessage.DEL_NUM) {
				handler.sendEmptyMessage(2);
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (adapter != null && mLetterListView.isFirstPress()) {
			int type = adapter.getItemViewType(firstVisibleItem);
			switch (type) {
			case PlayListAdapter.CATEGORYTITLE:
				String mCategoryName = (String) adapter
						.getItem(firstVisibleItem);
				mLetterListView.setChoose(ALL_CHARACTER.indexOf(mCategoryName));
				break;
			case PlayListAdapter.ITEM:
				SongInfo songInfo = (SongInfo) adapter
						.getItem(firstVisibleItem);
				mLetterListView.setChoose(ALL_CHARACTER.indexOf(songInfo
						.getCategory()));
				break;
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// 滑动停止
		if (scrollState == 0) {

		} else {
			if (EndTime < 0) {
				EndTime = 2000;
				localMusicLocateRelativeLayout.setVisibility(View.VISIBLE);
				handler.post(upDateVol);
				Animation mAnimation = AnimationUtils.loadAnimation(
						getActivity(), R.anim.fade_in);
				localMusicLocateRelativeLayout.startAnimation(mAnimation);
			} else {
				EndTime = 2000;
			}
		}
	}

	Runnable upDateVol = new Runnable() {

		@Override
		public void run() {
			if (EndTime >= 0) {
				EndTime -= 200;
				handler.postDelayed(upDateVol, 200);
			} else {
				Animation mAnimation = AnimationUtils.loadAnimation(
						getActivity(), R.anim.fade_out);
				localMusicLocateRelativeLayout.startAnimation(mAnimation);
				localMusicLocateRelativeLayout.setVisibility(View.GONE);
			}

		}
	};
}
