package com.happyplayer.util;

import java.util.HashMap;
import java.util.Map;

import com.happyplayer.common.Constants;

public class KscLyricsManamge {

	/**
	 * 保存每个歌词文件的解析器，不能每次去解析歌词文件
	 */
	private static Map<String, KscLyricsParser> kscLyricsParsers = new HashMap<String, KscLyricsParser>();

	public static KscLyricsParser getKscLyricsParser(String fileName) {

		KscLyricsParser kscLyricsParser = null;

		if (kscLyricsParsers.containsKey(fileName)) {
			kscLyricsParser = kscLyricsParsers.get(fileName);
		} else {
			kscLyricsParser = new KscLyricsParser(Constants.PATH_KSC + "/"
					+ fileName + ".ksc");
		}

		return kscLyricsParser;
	}
}
