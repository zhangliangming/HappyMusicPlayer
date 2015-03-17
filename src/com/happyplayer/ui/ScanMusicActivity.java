package com.happyplayer.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class ScanMusicActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanmusic);
	}

	public void back(View v) {
		finish();
	}
}
