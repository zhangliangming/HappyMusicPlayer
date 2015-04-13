package com.happyplayer.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.happyplayer.adapter.PlayListAdapter;
import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.db.SongDB;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.Category;
import com.happyplayer.model.SongInfo;
import com.happyplayer.util.ActivityManager;

public class LocalMusicActivity extends Activity {

	private ExpandableListView playlistView;

	private List<Category> categorys;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_localmusic);
		init();
		loadData();
		ActivityManager.getInstance().addActivity(this);
	}

	public void back(View v) {
		finish();
	}

	private void init() {
		//playlistView = (ExpandableListView) findViewById(R.id.playlistView);
	}

	private void loadData() {

		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {
				
				// 给ListView添加footView
				View footView = getLayoutInflater().inflate(
						R.layout.playlist_list_foot, null);
				int count = MediaManage.getMediaManage(LocalMusicActivity.this)
						.getCount();
				((TextView) footView.findViewById(R.id.list_size_text))
						.setText("共有" + count + "首歌曲");
				playlistView.addFooterView(footView);

				PlayListAdapter adapter = new PlayListAdapter(
						LocalMusicActivity.this, categorys,playlistView);

				playlistView.setAdapter(adapter);
			}

			@Override
			protected Object doInBackground() throws Exception {

				loadLocalMusic();

				return null;
			}
		}.execute();
	}

	/**
	 * 加载本地歌曲，以类型分类
	 */
	private void loadLocalMusic() {
		categorys = new ArrayList<Category>();
		List<String> categoryList = SongDB.getSongInfoDB(this).getAllCategory();
		for (int i = 0; i < categoryList.size(); i++) {
			String categoryName = categoryList.get(i);
			Category category = new Category(categoryName);
			List<SongInfo> songInfos = SongDB.getSongInfoDB(this)
					.getAllCategorySong(categoryName);
			category.setmCategoryItem(songInfos);
			categorys.add(category);
		}
	}
}
