package com.happyplayer.util;

import com.happyplayer.common.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 用于记录一些基本的配置数据
 * 
 * @author Administrator
 * 
 */
public class DataUtil {
	/**
	 * 初始化数据
	 * 
	 * @param context
	 */
	public static void init(Context context) {

		/***
		 * // 1.进入页面后，判断用户是不是第一次使用该应用，如果是则跳转到应用的导航页面 //
		 */
		SharedPreferences preferences = context.getSharedPreferences(
				Constants.SHARE_PREFERENCE_NAME, 0);

		// 获取是否是第一次使用的参数
		Constants.THE_FIRST = preferences.getBoolean(Constants.THE_FIRST_KEY,
				Constants.THE_FIRST);
		// if (Constants.THE_FIRST) {
		// return;
		// }
		// 获取底部歌词是否显示
		Constants.BAR_LRC_IS_OPEN = preferences.getBoolean(
				Constants.BAR_LRC_IS_OPEN_KEY, Constants.BAR_LRC_IS_OPEN);
	}

	/**
	 * 保存数据
	 * 
	 * @param context
	 */
	public static void save(Context context, String key, Object data) {
		SharedPreferences preferences = context.getSharedPreferences(
				Constants.SHARE_PREFERENCE_NAME, 0);
		Editor editor = preferences.edit();
		if (data instanceof Boolean) {
			editor.putBoolean(key, (Boolean) data);
		} else if (data instanceof Integer) {
			editor.putInt(key, (Integer) data);
		} else if (data instanceof String) {
			editor.putString(key, (String) data);
		}
		// 提交修改
		editor.commit();
	}

	/**
	 * 保存数据
	 * 
	 * @param context
	 */
	public static void save(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				Constants.SHARE_PREFERENCE_NAME, 0);
		Editor editor = preferences.edit();
		// 提交修改
		editor.commit();
	}

}
