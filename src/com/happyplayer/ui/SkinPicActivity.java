package com.happyplayer.ui;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;

import com.happyplayer.adapter.GridViewAdapter;
import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.common.Constants;
import com.happyplayer.util.ActivityManager;
import com.happyplayer.util.ImageUtil;
import com.happyplayer.widget.LoadRelativeLayout;

public class SkinPicActivity extends Activity {

	private ImageButton deleteSkinImageButton;

	private LoadRelativeLayout loadRelativeLayout;

	private GridView gridView;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				loadRelativeLayout.showLoadingView();
				break;
			case 1:
				loadRelativeLayout.showSuccessView();
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_skinpic);
		init();
		loadData();
		ActivityManager.getInstance().addActivity(this);
	}

	private void init() {
		deleteSkinImageButton = (ImageButton) findViewById(R.id.delete_skin);
		deleteSkinImageButton.setVisibility(View.INVISIBLE);

		gridView = (GridView) findViewById(R.id.grid);

		loadRelativeLayout = (LoadRelativeLayout) findViewById(R.id.loadRelativeLayout);

		loadRelativeLayout.init(this);
	}

	private void loadData() {
		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {
				GridViewAdapter adapter = new GridViewAdapter(
						SkinPicActivity.this, gridView);
				gridView.setAdapter(adapter);

				handler.sendEmptyMessage(1);
			}

			@Override
			protected Object doInBackground() throws Exception {
				handler.sendEmptyMessage(0);
				loadPICData();
				return null;
			}
		}.execute();
	}

	private void loadPICData() {
		for (int i = 0; i < Constants.PICIDS.length; i++) {
			ImageUtil.readBitmap(this, Constants.PICIDS[i]);
		}
	}

	public void back(View v) {
		finish();
	}

	public void deleteSkin(View v) {
	}

	public void goColorDialog(View v) {
		Intent intent = new Intent(this, SkinColorActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
	}
}
