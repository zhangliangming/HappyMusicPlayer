package com.happyplayer.application;

import com.happyplayer.handler.CrashHandler;

import android.app.Application;

public class CrashApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler catchHandler = CrashHandler.getInstance();
		catchHandler.init(getApplicationContext());
	}
}
