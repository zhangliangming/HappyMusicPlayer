package com.happyplayer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.happyplayer.model.KscLyricsLineInfo;

/**
 * 
 * @author Administrator
 * @功能 ksc歌词解析器
 * 
 */
public class KscLyricsParser {
	/**
	 * 歌曲名 字符串
	 */
	private final static String LEGAL_SONGNAME_PREFIX = "karaoke.songname";
	/**
	 * 歌手名 字符串
	 */
	private final static String LEGAL_SINGERNAME_PREFIX = "karaoke.singer";
	/**
	 * 歌词 字符串
	 */
	public final static String LEGAL_LYRICS_LINE_PREFIX = "karaoke.add";
	/**
	 * 歌曲名
	 */
	private String songName = "";
	/**
	 * 歌手名
	 */
	private String singerName = "";
	/**
	 * TreeMap，用于封装每行的歌词信息
	 */
	TreeMap<Integer, KscLyricsLineInfo> lyricsLineTreeMap = null;

	/**
	 * 
	 * @param kscFileName
	 *            文件名
	 */
	public KscLyricsParser(String kscFileName) {
		lyricsLineTreeMap = new TreeMap<Integer, KscLyricsLineInfo>();
		File kscFile = new File(kscFileName);
		if (!kscFile.exists()) {
			return;
		}
		String dataLine = "";
		try {
			FileInputStream stream = new FileInputStream(kscFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					stream, "GB2312"));
			int index = 0;
			while ((dataLine = br.readLine()) != null) {
				if (dataLine.startsWith(LEGAL_SONGNAME_PREFIX)) {
					// karaoke.songname :='爱就一个字';
					String temp[] = dataLine.split("\'");
					songName = temp[1];
					// System.out.println(songName);
				} else if (dataLine.startsWith(LEGAL_SINGERNAME_PREFIX)) {
					// karaoke.singer :='张信哲';
					String temp[] = dataLine.split("\'");
					singerName = temp[1];
					// System.out.println(singerName);
				} else if (dataLine.startsWith(LEGAL_LYRICS_LINE_PREFIX)) {

					KscLyricsLineInfo info = new KscLyricsLineInfo();

					int left = LEGAL_LYRICS_LINE_PREFIX.length() + 1;
					int right = dataLine.length();
					String[] dataLineComment = dataLine.substring(left + 2,
							right - 3).split("', '");

					// 获取开始时间
					info.setStartTimeStr(dataLineComment[0]);
					info.setStartTime(timeStrParserInteger(dataLineComment[0]));

					// 获取结束时间
					info.setEndTimeStr(dataLineComment[1]);
					info.setEndTime(timeStrParserInteger(dataLineComment[1]));

					// 获取歌词
					// karaoke.add('00:22.000', '00:25.000',
					// '拨开天空的乌云','480,240,330,440,270,480,760');
					// karaoke.add('00:36.810', '00:40.780', '我想[你 ]身不由己',
					// '480,360,1200,240,320,320,1050');
					// karaoke.add('00:28.380', '00:31.500',
					// '(女:)问你一句话','500,440,370,500,1310');
					// karaoke.add('00:16.460', '00:17.710',
					// '[Pretty][woman]','440,810');
					// karaoke.add('02:15.590', '02:18.220',
					// '与世界分享更[好][oho]','260,310,260,220,260,270,310,740');
					info.setLineLyrics(getLyricsComment(dataLineComment[2]));
					// System.out.println("====="
					// + getLyricsComment(dataLineComment[2]));

					// 1.先将()的内容转换成[]，如：(女:)问你一句话 将其转换成[女:问]你一句话
					// String newLrc = removeGuoHao(dataLineComment[2]);

					String newLrc = dataLineComment[2];

					List<String> lrcList = new ArrayList<String>();
					// 将[]里的歌词分离出来
					lrcList = removeZhongGuoHao(newLrc);

					String lyricsWords[] = new String[lrcList.size()];
					for (int i = 0; i < lrcList.size(); i++) {
						lyricsWords[i] = lrcList.get(i);
					}
					info.setLyricsWords(lyricsWords);

					// 获取每个歌词的时间
					String wordsDisIntervalStr[] = dataLineComment[3]
							.split(",");
					int[] wordsDisInterval = new int[wordsDisIntervalStr.length];
					for (int i = 0; i < wordsDisIntervalStr.length; i++) {
						wordsDisInterval[i] = Integer
								.parseInt(wordsDisIntervalStr[i]);
					}
					info.setWordsDisInterval(wordsDisInterval);

					//
					lyricsLineTreeMap.put(index, info);
					index++;
				}
			}
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public KscLyricsParser() {
	}

	/**
	 * 去除中括号，并将中括号或者歌词放进list中
	 * 
	 * @param newLrc
	 * @return
	 */
	private List<String> removeZhongGuoHao(String tempLrc) {
		List<String> lrcList = new ArrayList<String>();
		String newtempLrc = "";
		boolean isEnter = false;
		for (int i = 0; i < tempLrc.length(); i++) {
			char c = tempLrc.charAt(i);
			if (isChinese(c) || (!isWord(c) && c != '[' && c != ']')) {
				if (isEnter) {
					newtempLrc += String.valueOf(tempLrc.charAt(i));
				} else {
					lrcList.add(String.valueOf(tempLrc.charAt(i)));
				}
			} else if (c == '[') {
				isEnter = true;
			} else if (c == ']') {
				isEnter = false;
				lrcList.add(newtempLrc);
				newtempLrc = "";
			} else {
				newtempLrc += String.valueOf(tempLrc.charAt(i));
			}
		}

		return lrcList;
	}

	/**
	 * 判断该歌词是不是字母
	 * 
	 * @param c
	 * @return
	 */
	private boolean isWord(char c) {
		if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) {
			return true;
		}
		return false;
	}

	/**
	 * 去除括号，并用[]替代，如：(女:)问你一句话 将其转换成[女:问]你一句话
	 * 
	 * @param tempLrc
	 * @return
	 */
	// private String removeGuoHao(String tempLrc) {
	// String newtempLrc = "";
	// for (int i = 0; i < tempLrc.length(); i++) {
	// switch (tempLrc.charAt(i)) {
	// case '(':
	// newtempLrc += '[';
	// break;
	// case ')':
	// i++;
	// if (i < tempLrc.length()) {
	// newtempLrc += String.valueOf(tempLrc.charAt(i)) + "]";
	// break;
	// }
	// default:
	// newtempLrc += String.valueOf(tempLrc.charAt(i));
	// break;
	// }
	// }
	// return newtempLrc;
	// }

	/**
	 * 判断字符是不是中文，中文字符标点都可以判断
	 * 
	 * @param c
	 *            字符
	 * @return
	 */
	private boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * @功能 将行歌词中的[]和()分离开，得到一行完整的歌词
	 * @param orgLyrics
	 *            还没有分离的行歌词
	 * @return
	 */
	private String getLyricsComment(String orgLyrics) {
		String ly = "";
		for (int i = 0; i < orgLyrics.length(); i++) {
			switch (orgLyrics.charAt(i)) {
			case ']':
			case '[':
				// case '(':
				// case ')':
				break;
			default:
				ly += String.valueOf(orgLyrics.charAt(i));
				break;
			}
		}
		return ly;
	}

	/**
	 * @功能 将时间字符串转换成整数
	 * @param timeString
	 *            时间字符串
	 * @return
	 */
	public int timeStrParserInteger(String timeString) {
		timeString = timeString.replace(":", ".");
		timeString = timeString.replace(".", "@");
		String timedata[] = timeString.split("@");
		// Pattern pattern = Pattern.compile("\\d{2}");
		// Matcher matcher = pattern.matcher(timedata[0]);
		// if (timedata.length == 3 && matcher.matches()) {
		if (timedata.length == 3) {
			int m = Integer.parseInt(timedata[0]); // 分
			int s = Integer.parseInt(timedata[1]); // 秒
			int ms = Integer.parseInt(timedata[2]); // 毫秒
			int currTime = (m * 60 + s) * 1000 + ms;
			return currTime;
		}
		return 0;
	}

	/**
	 * 时间格式转换
	 * 
	 * @param time
	 * @return
	 */
	public String timeParserString(int time) {

		time /= 1000;
		int minute = time / 60;
		// int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

	/**
	 * 通过播放的进度，获取所唱歌词行数
	 * 
	 * @param msec
	 *            歌曲的当前时间值
	 * @return
	 */
	public int getLineNumberFromCurPlayingTime(int msec) {
		for (int i = 0; i < lyricsLineTreeMap.size(); i++) {
			if (msec >= lyricsLineTreeMap.get(i).getStartTime()
					&& msec <= lyricsLineTreeMap.get(i).getEndTime()) {
				return i;
			}
			if (msec > lyricsLineTreeMap.get(i).getEndTime()
					&& i + 1 < lyricsLineTreeMap.size()
					&& msec < lyricsLineTreeMap.get(i + 1).getStartTime()) {
				return i;
			}
		}
		if (msec >= lyricsLineTreeMap.get(lyricsLineTreeMap.size() - 1)
				.getEndTime()) {
			return lyricsLineTreeMap.size() - 1;
		}
		return -1;
	}

	/**
	 * 获取当前时间正在唱的歌词的第几个字
	 * 
	 * @param lyricsLineNum
	 *            行数
	 * @param msec
	 *            歌曲的当前时间值
	 * @return
	 */
	public int getDisWordsIndexFromCurPlayingTime(int lyricsLineNum, int msec) {
		if (lyricsLineNum == -1)
			return -1;
		KscLyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
		int elapseTime = lyrLine.getStartTime();
		for (int i = 0; i < lyrLine.getLyricsWords().length; i++) {
			elapseTime += lyrLine.wordsDisInterval[i];
			if (msec < elapseTime) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 获取当前歌词的第几个歌词的播放进度
	 * 
	 * @param lyricsLineNum
	 * @param msec
	 * @return
	 */
	public int getLenFromCurPlayingTime(int lyricsLineNum, int msec) {
		if (lyricsLineNum == -1)
			return 0;
		KscLyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
		int elapseTime = lyrLine.getStartTime();
		for (int i = 0; i < lyrLine.getLyricsWords().length; i++) {
			elapseTime += lyrLine.wordsDisInterval[i];
			if (msec < elapseTime) {
				return lyrLine.wordsDisInterval[i] - (elapseTime - msec);
			}
		}
		return 0;
	}

	/**
	 * 获取当前上下移动的距离
	 * 
	 * @param lyricsLineNum
	 *            当前 歌词行
	 * @param msec
	 *            当前播放的进度
	 * @param sy
	 *            要上下移动的总距离
	 * @return 要上下移动的距离
	 */
	public float getOffsetDYFromCurPlayingTime(int lyricsLineNum, int msec,
			int sy) {
		if (lyricsLineNum == -1)
			return sy;
		KscLyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
		int elapseTime = lyrLine.getStartTime();
		// int endTime = lyrLine.getEndTime();
		// // 以整行歌词的1/4时间 作为上下移动的时间
		// int dTime = (endTime - elapseTime) / 4;
		// if (msec < elapseTime + dTime) {
		// float dy = (float) sy / dTime;
		// return dy * (dTime - (elapseTime + dTime - msec));
		// }
		if (lyrLine.getLyricsWords().length != 0) {
			int dTime = lyrLine.wordsDisInterval[0];
			float dy = (float) sy / dTime;
			if (msec < elapseTime + dTime) {
				return dy * (dTime - (elapseTime + dTime - msec));
			}
		}

		return sy;
	}

	/**
	 * 
	 * @param lyricsLineNum
	 * @param msec
	 * @param height
	 *            上下移动的总长度
	 * @return 需要移动的距离
	 */
	public float getoffsetYFromCurPlayingTime(int lyricsLineNum, int msec,
			float height) {
		if (lyricsLineNum == -1)
			return 0;
		KscLyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
		int elapseTime = lyrLine.getStartTime();
		int startTime = lyrLine.getStartTime();
		int endTime = lyrLine.getEndTime();
		for (int i = 0; i < lyrLine.getLyricsWords().length; i++) {
			elapseTime += lyrLine.wordsDisInterval[i];
			if (msec < elapseTime) {
				int time = lyrLine.wordsDisInterval[i] - (elapseTime - msec);
				return height / (endTime - startTime) * time;
			}
		}
		return 0;
	}

	/**
	 * 获取当前所在行歌词的间隔时间长度
	 * 
	 * @param lyricsLineNum
	 * @return
	 */
	public int getOffsetYTimeFromCurPlayingTime(int lyricsLineNum) {
		if (lyricsLineNum == -1)
			return 0;
		KscLyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
		int startTime = lyrLine.getStartTime();
		int endTime = lyrLine.getEndTime();
		return (endTime - startTime);
	}

	public TreeMap<Integer, KscLyricsLineInfo> getLyricsLineTreeMap() {
		return lyricsLineTreeMap;
	}

	public void setLyricsLineTreeMap(
			TreeMap<Integer, KscLyricsLineInfo> lyricsLineTreeMap) {
		this.lyricsLineTreeMap = lyricsLineTreeMap;
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public String getSingerName() {
		return singerName;
	}

	public void setSingerName(String singerName) {
		this.singerName = singerName;
	}

}
