package com.happyplayer.ui;

import com.happyplayer.util.ActivityManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SettingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		ActivityManager.getInstance().addActivity(this);
	}

	public void back(View v) {
		finish();
	}

	public void showdesLrc(View v) {
	}

	public void goSettingSkinDialog(View v) {
		Intent intent = new Intent(this, SkinPicActivity.class);
		startActivity(intent);
		finish();
	}

}
