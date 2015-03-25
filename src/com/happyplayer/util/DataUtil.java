package com.happyplayer.util;

import java.io.File;

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
		//
		// 创建相关的文件夹
		File file = new File(Constants.PATH_MP3);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(Constants.PATH_KSC);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(Constants.PATH_ARTIST);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(Constants.PATH_ALBUM);
		if (!file.exists()) {
			file.mkdirs();
		}

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
		// 获取主题颜色
		Constants.DEF_COLOR_INDEX = preferences.getInt(
				Constants.DEF_COLOR_INDEX_KEY, Constants.DEF_COLOR_INDEX);
		// 皮肤图片
		Constants.DEF_PIC_INDEX = preferences.getInt(
				Constants.DEF_PIC_INDEX_KEY, Constants.DEF_PIC_INDEX);

		// 记录上一次的播放歌曲sid
		Constants.PLAY_SID = preferences.getString(Constants.PLAY_SID_KEY,
				Constants.PLAY_SID);

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
