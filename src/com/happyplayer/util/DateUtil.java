package com.happyplayer.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 最近修改时间2013年12月10日
 * 
 * @author Administrator 时间处理类。
 */
public class DateUtil {
	/**
	 * 尝试转换日期
	 * 
	 * @param dateStr
	 * @return
	 */
	public static Date tryParseDate(String dateStr) {
		return tryParseDate(dateStr, null);
	}

	/**
	 * 日期转字符串 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToString(Date date) {
		if (date == null) {
			return null;
		}
		try {
			DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return dateformat.format(date);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 尝试转换日期 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param dateStr
	 * @param defaultValue
	 * @return
	 */
	public static Date tryParseDate(String dateStr, Date defaultValue) {
		if (dateStr == null) {
			return defaultValue;
		}
		DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = defaultValue;
		try {
			date = dateformat.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 当前时间的整形格式
	 * 
	 * @return
	 */
	public static long getNowDateLong() {
		return getNowDate().getTime();
	}

	/**
	 * 获取当前时间
	 * 
	 * @return
	 */
	public static Date getNowDate() {
		return new Date();
	}

	/**
	 * yyyyMMdd
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToIntString(Date date) {
		DateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
		return dateformat.format(date);
	}

	/**
	 * yyyy-MM-dd
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToOtherString(Date date) {
		DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		return dateformat.format(date);
	}

	public static int dateToInt(Date date) {
		String str = dateToIntString(date);
		return new Integer(str);
	}

	public static int dateToInt() {
		return dateToInt(new Date());
	}

	/* ==============================时间计算工具================================== */

	/**
	 * 获取时间差-小时
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static float getHourDiff(long date1, long date2) {
		return (float) ((date2 - date1) / 3600000.0);
	}

	/**
	 * 获取时间差-小时
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static float getHourDiff(Date date1, Date date2) {
		return getHourDiff(date1.getTime(), date2.getTime());
	}

	/**
	 * 获取时间差-小时
	 * 
	 * @param date
	 * @return
	 */
	public static float getHourDiff(Date date) {
		return getHourDiff(date, new Date());
	}

	/**
	 * 获取时间差-小时
	 * 
	 * @param date
	 * @return
	 */
	public static float getHourDiff(long date) {
		return getHourDiff(date, new Date().getTime());
	}

	/**
	 * 获取时间差-天
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static float getDayDiff(long date1, long date2) {
		return (date2 - date1) / 3600000 / 24;
	}

	/**
	 * 获取时间差-天
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static float getDayDiff(Date date1, Date date2) {
		return getDayDiff(date1.getTime(), date2.getTime());
	}

	/**
	 * 获取时间差-天
	 * 
	 * @param date
	 * @return
	 */
	public static float getDayDiff(Date date) {
		return getDayDiff(date, new Date());
	}

	/**
	 * 获取时间差-天
	 * 
	 * @param date
	 * @return
	 */
	public static float getDayDiff(long date) {
		return getDayDiff(date, new Date().getTime());
	}
}
