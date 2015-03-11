package com.happyplayer.util;

import java.util.LinkedList;
import java.util.List;


import android.app.Activity;

/**
 * activity的管理:退出时，遍历所有的activity，并finish,最后退出系统。
 * 
 * @author Administrator 最近修改时间2013年12月10日
 */
public class ActivityManager {

	/**
	 * activity列表
	 */
	private List<Activity> activityList = new LinkedList<Activity>();
	private static ActivityManager instance = null;

	private ActivityManager() {

	}

	public static ActivityManager getInstance() {
		if (instance == null) {
			instance = new ActivityManager();
		}
		return instance;
	}

	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	public void exit() {
		for (Activity activity : activityList) {
			if (!activity.isFinishing() && activity != null) {
				activity.finish();
			}
		}
		int id = android.os.Process.myPid();
		if (id != 0) {
			android.os.Process.killProcess(id);
		}
	}
}
