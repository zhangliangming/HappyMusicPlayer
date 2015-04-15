package com.happyplayer.widget;

import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.View;

import com.happyplayer.common.Constants;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.KscLyricsLineInfo;
import com.happyplayer.model.SongInfo;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.util.KscLyricsParser;

public class KscTwoLineMLyricsView extends View implements Observer {
	/**
	 * 是否有歌词
	 */
	private boolean blLrc = false;
	/**
	 * 默认画笔
	 */
	private Paint paint;

	/**
	 * 已读歌词画笔
	 */
	private Paint paintHL;

	/**
	 * 显示默认歌词文字的大小值
	 */
	private int SIZEWORDDEF = 35;

	/**
	 * 歌词每行的间隔
	 */
	private int INTERVAL = 20;

	/**
	 * 歌词解析
	 */
	private KscLyricsParser kscLyricsParser;

	/**
	 * 歌词列表数据
	 */
	private TreeMap<Integer, KscLyricsLineInfo> lyricsLineTreeMap;

	/**
	 * 当前歌词的行数
	 */
	private int lyricsLineNum = -1;

	/**
	 * 当前歌词的第几个字
	 */
	private int lyricsWordIndex = -1;

	/**
	 * 当前歌词第几个字 已经播放的时间
	 */
	private int lyricsWordHLEDTime = 0;

	/**
	 * 当前歌词第几个字 已经播放的长度
	 */
	private float lineLyricsHLWidth = 0;

	private Context context;

	public KscTwoLineMLyricsView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public KscTwoLineMLyricsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public KscTwoLineMLyricsView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {

		this.context = context;

		paint = new Paint();
		paint.setColor(Color.rgb(255, 255, 255));
		paint.setDither(true);
		paint.setAntiAlias(true);
		paint.setTextSize(SIZEWORDDEF);

		paintHL = new Paint();
		paintHL.setDither(true);
		paintHL.setAntiAlias(true);
		paintHL.setTextSize(SIZEWORDDEF);

		ObserverManage.getObserver().addObserver(this);
	}

	@Override
	public void draw(Canvas canvas) {

		// 由于slidingmenu有些奇怪，所以在这里要先判断
		// 打开该页面时，当前播放器是否是正在暂停
		// 如果是暂停则要重新设置该页面的歌词

		int status = MediaManage.getMediaManage(context).getPlayStatus();
		switch (status) {
		case MediaManage.STOP:
			SongInfo tempSongInfo = MediaManage.getMediaManage(context)
					.getPlaySongInfo();
			if (blLrc && tempSongInfo != null) {
				showLrc((int) tempSongInfo.getPlayProgress());
			}
			break;
		case MediaManage.PLAYING:
			break;
		}

		paintHL.setColor(Constants.LRCCOLORS[Constants.LRC_COLOR_INDEX]);
		// paintHL.setColor(Color.rgb(250, 218, 131));
		if (!blLrc) {
			String tip = "乐乐音乐，传播好的音乐";
			float tipTextWidth = paint.measureText(tip);
			FontMetrics fm = paintHL.getFontMetrics();
			int height = (int) Math.ceil(fm.descent - fm.top) + 2;

			canvas.drawText(tip, (getWidth() - tipTextWidth) / 2,
					(getHeight() + height) / 2, paint);

			canvas.clipRect((getWidth() - tipTextWidth) / 2,
					(getHeight() + height) / 2 + height,
					(getWidth() - tipTextWidth) / 2 + tipTextWidth / 2 + 5,
					height);

			canvas.drawText(tip, (getWidth() - tipTextWidth) / 2,
					(getHeight() + height) / 2, paintHL);
		} else {

			// 画之前的歌词
			if (lyricsLineNum == -1) {
				String lyricsLeft = lyricsLineTreeMap.get(0).getLineLyrics();
				canvas.drawText(lyricsLeft, 10, SIZEWORDDEF + INTERVAL, paint);
				if (lyricsLineNum + 2 < lyricsLineTreeMap.size()) {
					String lyricsRight = lyricsLineTreeMap.get(
							lyricsLineNum + 2).getLineLyrics();

					float lyricsRightWidth = paint.measureText(lyricsRight);

					canvas.drawText(lyricsRight, getWidth() - lyricsRightWidth
							- 10, (SIZEWORDDEF + INTERVAL) * 2, paint);
				}
			} else {
				if (lyricsLineNum % 2 == 0) {
					if (lyricsLineNum + 1 < lyricsLineTreeMap.size()) {
						String lyricsRight = lyricsLineTreeMap.get(
								lyricsLineNum + 1).getLineLyrics();

						float lyricsRightWidth = paint.measureText(lyricsRight);
						canvas.drawText(lyricsRight, getWidth()
								- lyricsRightWidth - 10,
								(SIZEWORDDEF + INTERVAL) * 2, paint);
					}

					KscLyricsLineInfo kscLyricsLineInfo = lyricsLineTreeMap
							.get(lyricsLineNum);

					// 整行歌词
					String lineLyrics = kscLyricsLineInfo.getLineLyrics();

					// 画当前歌词
					canvas.drawText(lineLyrics, 10, SIZEWORDDEF + INTERVAL,
							paint);

					if (lyricsWordIndex != -1) {

						String lyricsWords[] = kscLyricsLineInfo
								.getLyricsWords();
						int wordsDisInterval[] = kscLyricsLineInfo
								.getWordsDisInterval();
						// 当前歌词之前的歌词
						String lyricsBeforeWord = "";
						for (int i = 0; i < lyricsWordIndex; i++) {
							lyricsBeforeWord += lyricsWords[i];
						}
						// 当前歌词
						String lyricsNowWord = lyricsWords[lyricsWordIndex]
								.trim();// 去掉空格
						// 当前歌词之前的歌词长度
						float lyricsBeforeWordWidth = paint
								.measureText(lyricsBeforeWord);

						// 当前歌词长度
						float lyricsNowWordWidth = paint
								.measureText(lyricsNowWord);

						float len = lyricsNowWordWidth
								/ wordsDisInterval[lyricsWordIndex]
								* lyricsWordHLEDTime;
						lineLyricsHLWidth = lyricsBeforeWordWidth + len;
					} else {

						// 整行歌词
						lineLyricsHLWidth = paint.measureText(lineLyrics);
					}

					FontMetrics fm = paint.getFontMetrics();
					int height = (int) Math.ceil(fm.descent - fm.top) + 2;
					canvas.clipRect(10, INTERVAL, 10 + lineLyricsHLWidth,
							SIZEWORDDEF + INTERVAL + height);
					// /////////////////////////////////////////////////////////////////////////////////////////

					canvas.drawText(lineLyrics, 10, SIZEWORDDEF + INTERVAL,
							paintHL);

				} else {

					// 画之前的歌词
					if (lyricsLineNum + 1 != lyricsLineTreeMap.size()) {
						String lyricsLeft = lyricsLineTreeMap.get(
								lyricsLineNum + 1).getLineLyrics();
						canvas.drawText(lyricsLeft, 10, SIZEWORDDEF + INTERVAL,
								paint);
					}

					KscLyricsLineInfo kscLyricsLineInfo = lyricsLineTreeMap
							.get(lyricsLineNum);
					// 整行歌词
					String lineLyrics = kscLyricsLineInfo.getLineLyrics();
					float lyricsRightWidth = paint.measureText(lineLyrics);

					// 画当前歌词
					canvas.drawText(lineLyrics, getWidth() - lyricsRightWidth
							- 10, (SIZEWORDDEF + INTERVAL) * 2, paint);

					if (lyricsWordIndex != -1) {
						String lyricsWords[] = kscLyricsLineInfo
								.getLyricsWords();
						int wordsDisInterval[] = kscLyricsLineInfo
								.getWordsDisInterval();
						// 当前歌词之前的歌词
						String lyricsBeforeWord = "";
						for (int i = 0; i < lyricsWordIndex; i++) {
							lyricsBeforeWord += lyricsWords[i];
						}
						// 当前歌词
						String lyricsNowWord = lyricsWords[lyricsWordIndex]
								.trim();// 去掉空格
						// 当前歌词之前的歌词长度
						float lyricsBeforeWordWidth = paint
								.measureText(lyricsBeforeWord);

						// 当前歌词长度
						float lyricsNowWordWidth = paint
								.measureText(lyricsNowWord);

						float len = lyricsNowWordWidth
								/ wordsDisInterval[lyricsWordIndex]
								* lyricsWordHLEDTime;
						lineLyricsHLWidth = lyricsBeforeWordWidth + len;
					} else {

						// 整行歌词
						lineLyricsHLWidth = paint.measureText(lineLyrics);
					}

					FontMetrics fm = paint.getFontMetrics();
					int height = (int) Math.ceil(fm.descent - fm.top) + 2;
					canvas.clipRect(getWidth() - lyricsRightWidth - 10,
							SIZEWORDDEF + INTERVAL * 2,
							getWidth() - lyricsRightWidth - 10
									+ lineLyricsHLWidth, SIZEWORDDEF + INTERVAL
									* 2 + height);
					// /////////////////////////////////////////////////////////////////////////////////////////

					canvas.drawText(lineLyrics, getWidth() - lyricsRightWidth
							- 10, (SIZEWORDDEF + INTERVAL) * 2, paintHL);
				}
			}
		}
		super.draw(canvas);
	}

	@Override
	public void update(Observable arg0, Object data) {
		// if (data instanceof SkinMessage) {
		// SkinMessage msg = (SkinMessage) data;
		// if (msg.type == SkinMessage.COLOR) {
		// invalidate();
		// }
		// }
	}

	public void setKscLyricsParser(KscLyricsParser kscLyricsParser) {
		this.kscLyricsParser = kscLyricsParser;
	}

	public boolean getBlLrc() {
		return blLrc;
	}

	public void setBlLrc(boolean blLrc) {
		this.blLrc = blLrc;
	}

	public TreeMap<Integer, KscLyricsLineInfo> getLyricsLineTreeMap() {
		return lyricsLineTreeMap;
	}

	public void setLyricsLineTreeMap(
			TreeMap<Integer, KscLyricsLineInfo> lyricsLineTreeMap) {
		this.lyricsLineTreeMap = lyricsLineTreeMap;
	}

	/**
	 * 显示当前进度的歌词
	 * 
	 * @param playProgress
	 */
	public void showLrc(int playProgress) {

		lyricsLineNum = kscLyricsParser
				.getLineNumberFromCurPlayingTime(playProgress);
		lyricsWordIndex = kscLyricsParser.getDisWordsIndexFromCurPlayingTime(
				lyricsLineNum, playProgress);

		lyricsWordHLEDTime = kscLyricsParser.getLenFromCurPlayingTime(
				lyricsLineNum, playProgress);

		invalidate();
	}

	/**
	 * 获取快进时的时间歌词 供进度条使用
	 * 
	 * @param playProgress
	 */
	public String getTimeLrc(int playProgress) {
		// System.out.println("playProgress:#########" + playProgress);
		String lrc = "";
		if (!blLrc)
			return lrc;
		if (kscLyricsParser == null)
			return lrc;
		int index = kscLyricsParser
				.getLineNumberFromCurPlayingTime(playProgress);
		// System.out.println("index:#########" + index);
		if (lyricsLineTreeMap == null || index >= lyricsLineTreeMap.size())
			return lrc;
		// System.out.println("lyricsLineTreeMap.size():#########"
		// + lyricsLineTreeMap.size());
		KscLyricsLineInfo kscLyricsLineInfo = lyricsLineTreeMap.get(index);
		// System.out.println("kscLyricsLineInfo:--" + kscLyricsLineInfo);
		if (kscLyricsLineInfo == null)
			return lrc;
		lrc = kscLyricsLineInfo.getLineLyrics();
		return lrc;
	}

	/**
	 * 初始化数据
	 */
	public void init() {
		blLrc = false;
		lyricsLineNum = -1;
		lyricsWordIndex = -1;
		lineLyricsHLWidth = 0;
		lyricsWordHLEDTime = 0;
		kscLyricsParser = null;
		lyricsLineTreeMap = null;
	}

}
