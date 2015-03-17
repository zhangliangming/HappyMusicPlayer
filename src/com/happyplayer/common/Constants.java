package com.happyplayer.common;

import android.graphics.Color;

/**
 * 记录一些基本的信息 如：第一次使用、颜色、是否开启桌面歌词等等。
 * 
 * @author Administrator
 * 
 */
public class Constants {
	/**
	 * 配置文件的名称
	 */
	public static String SHARE_PREFERENCE_NAME = "happy.player.sharepreference.name";
	/**
	 * 是否是第一次使用，默认值是true
	 */
	public static String THE_FIRST_KEY = "THE_FIRST_KEY";
	public static boolean THE_FIRST = true;

	/**
	 * 判断底部播放器的歌词是否显示
	 */
	public static String BAR_LRC_IS_OPEN_KEY = "BAR_LRC_IS_OPEN_KEY";
	public static boolean BAR_LRC_IS_OPEN = false;

	/**
	 * 背景颜色
	 */
	public static int BLACK_GROUND[] = { Color.rgb(26, 89, 154),
			Color.rgb(234, 84, 84), Color.rgb(240, 90, 154),
			Color.rgb(192, 80, 26), Color.rgb(148, 83, 237),
			Color.rgb(75, 104, 228), Color.rgb(44, 162, 249),
			Color.rgb(4, 188, 205), Color.rgb(26, 89, 154),
			Color.rgb(242, 116, 77), Color.rgb(249, 169, 42),
			Color.rgb(105, 200, 78), Color.rgb(30, 186, 118),
			Color.rgb(31, 190, 158), Color.rgb(161, 161, 161),
			Color.rgb(214, 117, 213), Color.rgb(242, 106, 138),
			Color.rgb(211, 173, 114), Color.rgb(211, 173, 114),
			Color.rgb(191, 199, 112), Color.rgb(120, 213, 214),
			Color.rgb(52, 145, 120) };
	/**
	 * 背景颜色面板索引
	 */
	public static String DEF_COLOR_INDEX_KEY = "COLOR_INDEX_KEY";
	public static int DEF_COLOR_INDEX = 5;
	/**
	 * 文本被点击后的颜色
	 */
	public static int TEXT_COLOR_PRESSED = Color.rgb(255, 255, 255);
	/**
	 * 文本默认颜色
	 */
	public static int TEXT_COLOR = Color.rgb(185, 185, 185);
}
