package com.happyplayer.ui;

import com.happyplayer.util.ActivityManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class SkinPicActivity extends Activity {

	private ImageButton deleteSkinImageButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_skinpic);
		init();
		ActivityManager.getInstance().addActivity(this);
	}

	private void init() {
		deleteSkinImageButton = (ImageButton) findViewById(R.id.delete_skin);
		deleteSkinImageButton.setVisibility(View.INVISIBLE);
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
