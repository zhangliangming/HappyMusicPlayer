package com.happyplayer.widget;

import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.happyplayer.common.Constants;
import com.happyplayer.logger.MyLogger;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.KscLyricsLineInfo;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.model.SongInfo;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.util.KscLyricsParser;

public class CopyOfKscManyLineLyricsView extends View implements Observer {
	/**
	 * 是否有歌词
	 */
	private boolean blLrc = false;
	/**
	 * 默认画笔
	 */
	private Paint paint;

	/**
	 * 高亮歌词画笔
	 */
	private Paint paintHL;

	private Paint paintHLED;

	/**
	 * 显示默认歌词文字的大小值
	 */
	private int SIZEWORDDEF = 30;

	private int SIZEWORD = 40;

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
	private int lyricsLineNum = 2;

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

	private float viewHeight = 0;

	/**
	 * 该view的中点，此值固定，
	 */
	private int viewWidth = 0;

	private MyLogger logger = MyLogger.getLogger(Constants.USERNAME);

	private Scroller mScroller;

	public CopyOfKscManyLineLyricsView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CopyOfKscManyLineLyricsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CopyOfKscManyLineLyricsView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {

		this.context = context;

		mScroller = new Scroller(context);

		paint = new Paint();
		paint.setColor(Color.rgb(255, 255, 255));
		paint.setTextAlign(Align.CENTER);
		paint.setDither(true);
		paint.setAntiAlias(true);
		paint.setTextSize(SIZEWORDDEF);

		paintHL = new Paint();
		paintHL.setColor(Color.rgb(255, 255, 255));
		paintHL.setDither(true);
		paintHL.setAntiAlias(true);
		paintHL.setTextSize(SIZEWORD);

		paintHLED = new Paint();
		paintHLED.setDither(true);
		paintHLED.setAntiAlias(true);
		paintHLED.setTextSize(SIZEWORD);

		ObserverManage.getObserver().addObserver(this);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
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

		paintHLED.setColor(Constants.LRCCOLORS[Constants.LRC_COLOR_INDEX]);
		if (!blLrc) {
			String tip = "乐乐音乐，传播好的音乐";
			FontMetrics fm = paintHL.getFontMetrics();
			int height = (int) Math.ceil(fm.descent - fm.top) + 2;
			canvas.drawText(tip, viewWidth / 2, (viewHeight + height) / 2,
					paint);
		} else {
			int offsetY = 1;
			// 画当前歌词之前的歌词
			for (int i = lyricsLineNum - 1; i >= 0; i--) {
				if (getHeight() / 2 - (SIZEWORDDEF + INTERVAL) * offsetY < SIZEWORDDEF) {
					break;
				}
				String lineLyrics = lyricsLineTreeMap.get(i).getLineLyrics();
				canvas.drawText(lineLyrics, viewWidth / 2, viewHeight / 2
						- (SIZEWORDDEF + INTERVAL) * offsetY, paint);
				offsetY++;
			}

			offsetY = 1;
			// 画当前歌词之后的歌词
			for (int i = lyricsLineNum + 1; i < lyricsLineTreeMap.size(); i++) {
				if (getHeight() / 2 + (SIZEWORDDEF + INTERVAL) * offsetY > this
						.getHeight() - SIZEWORDDEF) {
					break;
				}
				String lineLyrics = lyricsLineTreeMap.get(i).getLineLyrics();
				canvas.drawText(lineLyrics, viewWidth / 2, viewHeight / 2
						+ (SIZEWORDDEF + INTERVAL) * offsetY, paint);
				offsetY++;
			}

			if (lyricsLineNum != -1) {
				KscLyricsLineInfo kscLyricsLineInfo = lyricsLineTreeMap
						.get(lyricsLineNum);
				// 整行歌词
				String lineLyrics = kscLyricsLineInfo.getLineLyrics();

				float lineLyricsWidth = paintHL.measureText(lineLyrics);

				// 画当前歌词
				canvas.drawText(lineLyrics,
						viewWidth / 2 - lineLyricsWidth / 2, viewHeight / 2,
						paintHL);

				if (lyricsWordIndex == -1) {
					lineLyricsHLWidth = lineLyricsWidth;
				} else {

					String lyricsWords[] = kscLyricsLineInfo.getLyricsWords();
					int wordsDisInterval[] = kscLyricsLineInfo
							.getWordsDisInterval();
					// 当前歌词之前的歌词
					String lyricsBeforeWord = "";
					for (int i = 0; i < lyricsWordIndex; i++) {
						lyricsBeforeWord += lyricsWords[i];
					}
					// 当前歌词
					String lyricsNowWord = lyricsWords[lyricsWordIndex].trim();// 去掉空格

					// 当前歌词之前的歌词长度
					float lyricsBeforeWordWidth = paintHL
							.measureText(lyricsBeforeWord);

					// 当前歌词长度
					float lyricsNowWordWidth = paintHL
							.measureText(lyricsNowWord);

					float len = lyricsNowWordWidth
							/ wordsDisInterval[lyricsWordIndex]
							* lyricsWordHLEDTime;
					lineLyricsHLWidth = lyricsBeforeWordWidth + len;

				}
				FontMetrics fm = paintHL.getFontMetrics();
				int height = (int) Math.ceil(fm.descent - fm.top) + 2;
				canvas.clipRect(viewWidth / 2 - lineLyricsWidth / 2, viewHeight
						/ 2 - height, viewWidth / 2 - lineLyricsWidth
						/ +lineLyricsHLWidth, viewHeight / 2 + height);
				// /////////////////////////////////////////////////////////////////////////////////////////

				// 画当前歌词
				canvas.drawText(lineLyrics,
						viewWidth / 2 - lineLyricsWidth / 2, viewHeight / 2,
						paintHLED);
			}
		}
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
		// int newLyricsLineNum = kscLyricsParser
		// .getLineNumberFromCurPlayingTime(playProgress);
		// if (newLyricsLineNum != lyricsLineNum) {
		// lyricsLineNum = newLyricsLineNum;
		// // scrollBy(getScrollX(), SIZEWORDDEF + INTERVAL);
		// // viewHeight =viewHeight + getScrollY() - (SIZEWORDDEF + INTERVAL);
		// // try {
		// // Thread.sleep(50);
		// // } catch (InterruptedException e) {
		// // e.printStackTrace();
		// // }
		// // scrollTo(getScrollX(), -(SIZEWORDDEF + INTERVAL*3));
		// // invalidate();
		// // smoothScrollTo(0, SIZEWORD + INTERVAL);
		// // invalidate();
		// } else {
		// if (offsetY <= SIZEWORD) {
		// smoothScrollTo(0, -1);
		// viewHeight -= 1;
		// offsetY++;
		// }
		// }
		// // else {
		// lyricsWordIndex = kscLyricsParser.getDisWordsIndexFromCurPlayingTime(
		// lyricsLineNum, playProgress);
		//
		// lyricsWordHLEDTime = kscLyricsParser.getLenFromCurPlayingTime(
		// lyricsLineNum, playProgress);
		// // scrollTo(getScrollX(), 0);
		// invalidate();
		// // }

		int newLyricsLineNum = kscLyricsParser
				.getLineNumberFromCurPlayingTime(playProgress);
		if (newLyricsLineNum != lyricsLineNum) {
			lyricsLineNum = newLyricsLineNum;
			// viewHeight = getHeight() + (SIZEWORDDEF + INTERVAL);
		}
		lyricsWordIndex = kscLyricsParser.getDisWordsIndexFromCurPlayingTime(
				lyricsLineNum, playProgress);

		lyricsWordHLEDTime = kscLyricsParser.getLenFromCurPlayingTime(
				lyricsLineNum, playProgress);

		float offsetY = kscLyricsParser.getoffsetYFromCurPlayingTime(
				lyricsLineNum, playProgress, (float) SIZEWORDDEF);
		logger.i("offsetY:--------->" + offsetY);

		smoothScrollTo(0, 0 - (int) offsetY);
		//viewHeight += getScrollY();

		invalidate();
	}

	private void smoothScrollTo(int fx, int fy) {
		int dx = fx - mScroller.getFinalX();
		int dy = fy - mScroller.getFinalY();
		smoothScrollBy(dx, dy);
	}

	private void smoothScrollBy(int dx, int dy) {
		mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx,
				dy);
		invalidate();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			invalidate();
		}
	}

	/* *
	 * 滑动事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float tt = event.getY();
		if (!blLrc) {
			return super.onTouchEvent(event);
		}
		return true;
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
	public void init(int viewWidth, int viewHeight) {
		blLrc = false;
		lyricsLineNum = -1;
		lyricsWordIndex = -1;
		lineLyricsHLWidth = 0;
		lyricsWordHLEDTime = 0;
		kscLyricsParser = null;
		lyricsLineTreeMap = null;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;

		if (!mScroller.isFinished()) {
			mScroller.forceFinished(true);
		}

		scrollTo(getScrollX(), 0);
		invalidate();
	}

}
