package com.happyplayer.ui;

import java.util.Observable;
import java.util.Observer;

import com.happyplayer.common.Constants;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.util.ActivityManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

public class LrcViewActivity extends Activity implements Observer {

	private RelativeLayout parent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lrc_view);
		init();
		setBackground();
		ObserverManage.getObserver().addObserver(this);
		ActivityManager.getInstance().addActivity(this);
	}

	private void init() {
		parent = (RelativeLayout) findViewById(R.id.parent);
	}

	public void back(View v) {
		finish();
	}

	public void down(View v) {
	}

	public void like(View v) {
	}

	public void prevSong(View v) {
	}

	public void play(View v) {
	}

	public void pause(View v) {
	}

	public void nextSong(View v) {
	}

	public void playlist(View v) {
	}

	private void setBackground() {
		parent.setBackgroundResource(Constants.PICIDS[Constants.DEF_PIC_INDEX]);
	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SkinMessage) {
			SkinMessage msg = (SkinMessage) data;
			if (msg.type == SkinMessage.PIC) {
				setBackground();
			}
		}
	}
}
