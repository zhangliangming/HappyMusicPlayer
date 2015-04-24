package com.happyplayer.widget;

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
import com.happyplayer.util.KscLyricsParser;

public class FloatLyricsView extends View {
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
	 * 显示已经缩放歌词文字的大小值
	 */
	private int SCALEIZEWORDDEF = 35;
	/**
	 * 显示默认歌词文字的大小值
	 */
	private int SIZEWORDDEF = 35;

	/**
	 * 歌词每行的间隔
	 */
	private int INTERVAL = 20;
	/**
	 * 歌词默认每行的间隔
	 */
	private int DEFINTERVAL = 20;

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

	/** 高亮歌词当前的其实x轴绘制坐标 **/
	private float highLightLrcMoveX;

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

		paintBackgruond = new Paint();
		paintBackgruond.setAlpha(180);
		paintBackgruond.setColor(Color.BLACK);
		paintBackgruond.setDither(true);
		paintBackgruond.setAntiAlias(true);

		paintHL = new Paint();
		paintHL.setDither(true);
		paintHL.setAntiAlias(true);

	}

	@Override
	public void draw(Canvas canvas) {

		// 打开该页面时，当前播放器是否是正在暂停
		// 如果是暂停则要重新设置该页面的歌词
		int index = Constants.DEF_DES_COLOR_INDEX;

		paint.setColor(Constants.DESLRCNOREADCOLOR[index]);
		paintHL.setColor(Constants.DESLRCREADEDCOLOR[index]);

		// 设置字体大小
		int fontSizeScale = Constants.DESLRCFONTSIZE[Constants.DESLRCFONTSIZEINDEX];
		SCALEIZEWORDDEF = (int) ((float) fontSizeScale / 100 * SIZEWORDDEF);
		INTERVAL = DEFINTERVAL - (SCALEIZEWORDDEF - SIZEWORDDEF);

		// logger.i("SCALEIZEWORDDEF---->" + SCALEIZEWORDDEF);
		// logger.i("INTERVAL---->" + INTERVAL);

		paint.setTextSize(SCALEIZEWORDDEF);
		paintBackgruond.setTextSize(SCALEIZEWORDDEF);
		paintHL.setTextSize(SCALEIZEWORDDEF);

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
			float tipTextWidth = paint.measureText(tip);
			FontMetrics fm = paintHL.getFontMetrics();
			int height = (int) Math.ceil(fm.descent - fm.top) + 2;

			drawBackground(canvas, tip, (getWidth() - tipTextWidth) / 2,
					(getHeight() + height) / 2);

			canvas.drawText(tip, (getWidth() - tipTextWidth) / 2,
					(getHeight() + height) / 2, paint);

			canvas.clipRect((getWidth() - tipTextWidth) / 2,
					(getHeight() + height) / 2 - height,
					(getWidth() - tipTextWidth) / 2 + tipTextWidth / 2 + 5,
					(getHeight() + height) / 2 + height);

			drawBackground(canvas, tip, (getWidth() - tipTextWidth) / 2,
					(getHeight() + height) / 2);
			canvas.drawText(tip, (getWidth() - tipTextWidth) / 2,
					(getHeight() + height) / 2, paintHL);

		} else {

			// 画之前的歌词
			if (lyricsLineNum == -1) {
				String lyricsLeft = lyricsLineTreeMap.get(0).getLineLyrics();

				drawBackground(canvas, lyricsLeft, 10, SCALEIZEWORDDEF
						+ INTERVAL);
				canvas.drawText(lyricsLeft, 10, SCALEIZEWORDDEF + INTERVAL,
						paint);
				if (lyricsLineNum + 2 < lyricsLineTreeMap.size()) {
					String lyricsRight = lyricsLineTreeMap.get(
							lyricsLineNum + 2).getLineLyrics();

					float lyricsRightWidth = paint.measureText(lyricsRight);
					float textRightX = getWidth() - lyricsRightWidth - 10;
					// 如果计算出的textX为负数，将textX置为0(实现：如果歌词宽大于view宽，则居左显示，否则居中显示)
					textRightX = Math.max(textRightX, 10);
					drawBackground(canvas, lyricsRight, textRightX,
							(SCALEIZEWORDDEF + INTERVAL) * 2);

					canvas.drawText(lyricsRight, textRightX,
							(SCALEIZEWORDDEF + INTERVAL) * 2, paint);
				}
			} else {
				if (lyricsLineNum % 2 == 0) {
					if (lyricsLineNum + 1 < lyricsLineTreeMap.size()) {
						String lyricsRight = lyricsLineTreeMap.get(
								lyricsLineNum + 1).getLineLyrics();

						float lyricsRightWidth = paint.measureText(lyricsRight);
						float textRightX = getWidth() - lyricsRightWidth - 10;
						// 如果计算出的textX为负数，将textX置为0(实现：如果歌词宽大于view宽，则居左显示，否则居中显示)
						textRightX = Math.max(textRightX, 10);
						drawBackground(canvas, lyricsRight, textRightX,
								(SCALEIZEWORDDEF + INTERVAL) * 2);

						canvas.drawText(lyricsRight, textRightX,
								(SCALEIZEWORDDEF + INTERVAL) * 2, paint);
					}

					KscLyricsLineInfo kscLyricsLineInfo = lyricsLineTreeMap
							.get(lyricsLineNum);
					// 整行歌词
					String lineLyrics = kscLyricsLineInfo.getLineLyrics();
					float textWidth = paint.measureText(lineLyrics);// 用画笔测量歌词的宽度

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
						lineLyricsHLWidth = textWidth;
					}

					// save和restore是为了剪切操作不影响画布的其它元素
					canvas.save();

					float textX = 0;
					if (textWidth > getWidth()) {
						if (lineLyricsHLWidth >= getWidth() / 2) {
							if ((textWidth - lineLyricsHLWidth) >= getWidth() / 2) {
								highLightLrcMoveX = (getWidth() / 2 - lineLyricsHLWidth);
							} else {
								highLightLrcMoveX = getWidth() - textWidth - 10;
							}
						} else {
							highLightLrcMoveX = 10;
						}
						// 如果歌词宽度大于view的宽，则需要动态设置歌词的起始x坐标，以实现水平滚动
						textX = highLightLrcMoveX;
					} else {
						// 如果歌词宽度小于view的宽
						textX = 10;
					}

					drawBackground(canvas, lineLyrics, textX, SCALEIZEWORDDEF
							+ INTERVAL);
					// 画当前歌词
					canvas.drawText(lineLyrics, textX, SCALEIZEWORDDEF
							+ INTERVAL, paint);

					FontMetrics fm = paint.getFontMetrics();
					int height = (int) Math.ceil(fm.descent - fm.top) + 2;
					canvas.clipRect(textX, INTERVAL, textX + lineLyricsHLWidth,
							SCALEIZEWORDDEF + INTERVAL + height);
					// /////////////////////////////////////////////////////////////////////////////////////////

					drawBackground(canvas, lineLyrics, textX, SCALEIZEWORDDEF
							+ INTERVAL);

					canvas.drawText(lineLyrics, textX, SCALEIZEWORDDEF
							+ INTERVAL, paintHL);
					canvas.restore();
				} else {

					// 画之前的歌词
					if (lyricsLineNum + 1 != lyricsLineTreeMap.size()) {
						String lyricsLeft = lyricsLineTreeMap.get(
								lyricsLineNum + 1).getLineLyrics();

						drawBackground(canvas, lyricsLeft, 10, SCALEIZEWORDDEF
								+ INTERVAL);

						canvas.drawText(lyricsLeft, 10, SCALEIZEWORDDEF
								+ INTERVAL, paint);
					}

					KscLyricsLineInfo kscLyricsLineInfo = lyricsLineTreeMap
							.get(lyricsLineNum);
					// 整行歌词
					String lineLyrics = kscLyricsLineInfo.getLineLyrics();
					float lyricsRightWidth = paint.measureText(lineLyrics);

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
						lineLyricsHLWidth = lyricsRightWidth;
					}

					// save和restore是为了剪切操作不影响画布的其它元素
					canvas.save();

					float textX = 0;
					if (lyricsRightWidth > getWidth()) {
						if (lineLyricsHLWidth >= getWidth() / 2) {
							if ((lyricsRightWidth - lineLyricsHLWidth) >= getWidth() / 2) {
								highLightLrcMoveX = (getWidth() / 2 - lineLyricsHLWidth);
							} else {
								highLightLrcMoveX = getWidth()
										- lyricsRightWidth - 10;
							}
						} else {
							highLightLrcMoveX = 10;
						}
						// 如果歌词宽度大于view的宽，则需要动态设置歌词的起始x坐标，以实现水平滚动
						textX = highLightLrcMoveX;
					} else {
						// 如果歌词宽度小于view的宽
						textX = getWidth() - lyricsRightWidth - 10;
					}

					drawBackground(canvas, lineLyrics, textX,
							(SCALEIZEWORDDEF + INTERVAL) * 2);
					// 画当前歌词
					canvas.drawText(lineLyrics, textX,
							(SCALEIZEWORDDEF + INTERVAL) * 2, paint);

					FontMetrics fm = paint.getFontMetrics();
					int height = (int) Math.ceil(fm.descent - fm.top) + 2;
					canvas.clipRect(textX, SCALEIZEWORDDEF + INTERVAL * 2,
							textX + lineLyricsHLWidth, SCALEIZEWORDDEF
									+ INTERVAL * 2 + height);
					// /////////////////////////////////////////////////////////////////////////////////////////

					drawBackground(canvas, lineLyrics, textX,
							(SCALEIZEWORDDEF + INTERVAL) * 2);
					canvas.drawText(lineLyrics, textX,
							(SCALEIZEWORDDEF + INTERVAL) * 2, paintHL);
					canvas.restore();
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
		int newLyricsLineNum = kscLyricsParser
				.getLineNumberFromCurPlayingTime(playProgress);
		if (newLyricsLineNum != lyricsLineNum) {
			lyricsLineNum = newLyricsLineNum;
			highLightLrcMoveX = 0;
		}
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
		highLightLrcMoveX = 0;
		blLrc = false;
		lyricsLineNum = -1;
		lyricsWordIndex = -1;
		lineLyricsHLWidth = 0;
		lyricsWordHLEDTime = 0;
		kscLyricsParser = null;
		lyricsLineTreeMap = null;
	}

}
