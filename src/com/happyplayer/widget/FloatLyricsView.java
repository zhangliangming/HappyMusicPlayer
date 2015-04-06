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
import com.happyplayer.model.KscLyricsLineInfo;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.model.SongInfo;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.player.MediaManage;
import com.happyplayer.util.KscLyricsParser;

public class FloatLyricsView extends View implements Observer {
	/**
	 * 是否有歌词
	 */
	private boolean blLrc = false;
	/**
	 * 默认画笔
	 */
	private Paint paint;

	/**
	 * 普通歌词，颜色为黑色
	 */
	private Paint paintBackgruond;

	/**
	 * 已读歌词画笔
	 */
	private Paint paintHL;

	/**
	 * 显示默认歌词文字的大小值
	 */
	private int SIZEWORDDEF = 40;

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

	public FloatLyricsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public FloatLyricsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FloatLyricsView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {

		this.context = context;

		paint = new Paint();
		paint.setDither(true);
		paint.setAntiAlias(true);
		paint.setTextSize(SIZEWORDDEF);

		paintBackgruond = new Paint();
		paintBackgruond.setAlpha(180);
		paintBackgruond.setColor(Color.BLACK);
		paintBackgruond.setDither(true);
		paintBackgruond.setAntiAlias(true);
		paintBackgruond.setTextSize(SIZEWORDDEF);

		paintHL = new Paint();
		paintHL.setDither(true);
		paintHL.setAntiAlias(true);
		paintHL.setTextSize(SIZEWORDDEF);

		ObserverManage.getObserver().addObserver(this);
	}

	@Override
	public void draw(Canvas canvas) {

		int index = Constants.DEF_DES_COLOR_INDEX;

		paint.setColor(Constants.DESLRCNOREADCOLOR[index]);
		paintHL.setColor(Constants.DESLRCREADEDCOLOR[index]);

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

		if (!blLrc) {

			String tip = "乐乐音乐，传播好的音乐";
			float lineLyricsWidth = paint.measureText(tip);
			drawBackground(canvas, tip, getWidth() / 2 - (int) lineLyricsWidth
					/ 2, getHeight() / 2);
			canvas.drawText(tip, getWidth() / 2 - (int) lineLyricsWidth / 2,
					getHeight() / 2, paint);

		} else {

			// 画之前的歌词
			if (lyricsLineNum == -1) {
				String lyricsLeft = lyricsLineTreeMap.get(0).getLineLyrics();

				drawBackground(canvas, lyricsLeft, 10, SIZEWORDDEF + INTERVAL);
				canvas.drawText(lyricsLeft, 10, SIZEWORDDEF + INTERVAL, paint);
				if (lyricsLineNum + 2 < lyricsLineTreeMap.size()) {
					String lyricsRight = lyricsLineTreeMap.get(
							lyricsLineNum + 2).getLineLyrics();

					float lyricsRightWidth = paint.measureText(lyricsRight);

					drawBackground(canvas, lyricsRight, getWidth()
							- lyricsRightWidth - 10,
							(SIZEWORDDEF + INTERVAL) * 2);

					canvas.drawText(lyricsRight, getWidth() - lyricsRightWidth
							- 10, (SIZEWORDDEF + INTERVAL) * 2, paint);
				}
			} else {
				if (lyricsLineNum % 2 == 0) {
					if (lyricsLineNum + 1 < lyricsLineTreeMap.size()) {
						String lyricsRight = lyricsLineTreeMap.get(
								lyricsLineNum + 1).getLineLyrics();

						float lyricsRightWidth = paint.measureText(lyricsRight);

						drawBackground(canvas, lyricsRight, getWidth()
								- lyricsRightWidth - 10,
								(SIZEWORDDEF + INTERVAL) * 2);

						canvas.drawText(lyricsRight, getWidth()
								- lyricsRightWidth - 10,
								(SIZEWORDDEF + INTERVAL) * 2, paint);
					}

					KscLyricsLineInfo kscLyricsLineInfo = lyricsLineTreeMap
							.get(lyricsLineNum);

					// 整行歌词
					String lineLyrics = kscLyricsLineInfo.getLineLyrics();

					drawBackground(canvas, lineLyrics, 10, SIZEWORDDEF
							+ INTERVAL);
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

					drawBackground(canvas, lineLyrics, 10, SIZEWORDDEF
							+ INTERVAL);

					canvas.drawText(lineLyrics, 10, SIZEWORDDEF + INTERVAL,
							paintHL);

				} else {

					// 画之前的歌词
					if (lyricsLineNum + 1 != lyricsLineTreeMap.size()) {
						String lyricsLeft = lyricsLineTreeMap.get(
								lyricsLineNum + 1).getLineLyrics();

						drawBackground(canvas, lyricsLeft, 10, SIZEWORDDEF
								+ INTERVAL);

						canvas.drawText(lyricsLeft, 10, SIZEWORDDEF + INTERVAL,
								paint);
					}

					KscLyricsLineInfo kscLyricsLineInfo = lyricsLineTreeMap
							.get(lyricsLineNum);
					// 整行歌词
					String lineLyrics = kscLyricsLineInfo.getLineLyrics();
					float lyricsRightWidth = paint.measureText(lineLyrics);

					drawBackground(canvas, lineLyrics, getWidth()
							- lyricsRightWidth - 10,
							(SIZEWORDDEF + INTERVAL) * 2);
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
							(SIZEWORDDEF + INTERVAL) * 2 + height,
							getWidth() - lyricsRightWidth - 10
									+ lineLyricsHLWidth, height);
					// /////////////////////////////////////////////////////////////////////////////////////////

					drawBackground(canvas, lineLyrics, getWidth()
							- lyricsRightWidth - 10,
							(SIZEWORDDEF + INTERVAL) * 2);
					canvas.drawText(lineLyrics, getWidth() - lyricsRightWidth
							- 10, (SIZEWORDDEF + INTERVAL) * 2, paintHL);
				}
			}
		}
		super.draw(canvas);
	}

	/**
	 * 描绘黑色轮廓
	 * 
	 * @param canvas
	 * @param string
	 * @param x
	 * @param y
	 */
	private void drawBackground(Canvas canvas, String string, float x, float y) {
		canvas.drawText(string, x - 1, y, paintBackgruond);
		canvas.drawText(string, x + 1, y, paintBackgruond);
		canvas.drawText(string, x, y + 1, paintBackgruond);
		canvas.drawText(string, x, y - 1, paintBackgruond);
	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SkinMessage) {
			SkinMessage msg = (SkinMessage) data;
			if (msg.type == SkinMessage.COLOR) {
				invalidate();
			}
		}
	}

	public void setKscLyricsParser(KscLyricsParser kscLyricsParser) {
		this.kscLyricsParser = kscLyricsParser;
	}

	public int getSIZEWORDDEF() {
		return SIZEWORDDEF;
	}

	public int getINTERVAL() {
		return INTERVAL;
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
