package com.happyplayer.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PingYinUtil {

	/**
	 * 
	 * @param inputString
	 * @return
	 */
	public static String getPingYin(String inputString) {
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

		char[] input = inputString.trim().toCharArray();
		String output = "";

		try {
			for (int i = 0; i < input.length; i++) {
				if (java.lang.Character.toString(input[i]).matches(
						"[\\u4E00-\\u9FA5]+")) {
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(
							input[i], format);
					output += temp[0];
				} else
					output += java.lang.Character.toString(input[i]);
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		return output;
	}

	/**
	 * 字节转换
	 * 
	 * @param length
	 * @return
	 */
	public static String convertFileSize(long length) {
		int sub_index = 0;
		String show = "";
		if (length >= 1073741824) {
			sub_index = (String.valueOf((float) length / 1073741824))
					.indexOf(".");
			show = ((float) length / 1073741824 + "000").substring(0,
					sub_index + 3) + "GB";
		} else if (length >= 1048576) {
			sub_index = (String.valueOf((float) length / 1048576)).indexOf(".");
			String myshow = ((float) length / 1048576 + "000").substring(0,
					sub_index + 3) + "";
			if (Float.parseFloat(myshow) < 1024
					&& Float.parseFloat(myshow) > 1000) {

				show = String.valueOf(Float.parseFloat(myshow) / 1024)
						.substring(0, 4) + "GB";

			} else {
				show = ((float) length / 1048576 + "000").substring(0,
						sub_index + 3) + "MB";
			}
		} else if (length >= 1024) {
			sub_index = (String.valueOf((float) length / 1024)).indexOf(".");
			show = ((float) length / 1024 + "000").substring(0, sub_index + 3)
					+ "KB";
		} else if (length < 1024) {
			show = String.valueOf(length) + "B";
		}
		return show;
	}
}