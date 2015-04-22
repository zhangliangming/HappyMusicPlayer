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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.happyplayer.common.Constants;
import com.happyplayer.logger.MyLogger;
import com.happyplayer.manage.MediaManage;
import com.happyplayer.model.KscLyricsLineInfo;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.util.KscLyricsParser;

public class Copy_5_of_KscManyLineLyricsView extends View implements Observer {
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

	/** 画时间线的画时间线 ***/
	private Paint mPaintForTimeLine;

	/**
	 * 显示默认歌词文字的大小值
	 */
	private int SIZEWORDDEF = 30;

	private int SIZEWORD = 40;

	/**
	 * 歌词每行的间隔
	 */
	private int INTERVAL = 30;

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
	private int oldLyricsLineNum = -1;

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

	private MyLogger logger = MyLogger.getLogger(Constants.USERNAME);

	private Scroller mScroller;

	/** 控制文字缩放的因子 **/
	private float mCurFraction = 1.0f;

	/*** 移动歌词的持续时间 **/
	private static final int DURATION_FOR_LRC_SCROLL = 1000;
	/**
	 * 需要画的歌词行数
	 */
	private int mTotleDrawRow;

	/** 是否画时间线 **/
	private boolean mIsDrawTimeLine = false;
	/**
	 * 时间字符串
	 */
	private String timeStr = "";

	/**
	 * 当触摸歌词View时，保存为当前触点的Y轴坐标
	 * 
	 * 滑动的进度
	 */
	private float touchY = 0;

	/** 高亮歌词当前的其实x轴绘制坐标 **/
	private float highLightLrcMoveX;

	/**
	 * 往上滑动的最大滑动进度
	 */
	private int scrollMaxYProgress = 0;

	private boolean blScroll = false;

	private int progress = 0;

	/**
	 * 轮廓画笔
	 */
	private Paint paintBackgruond;

	public Copy_5_of_KscManyLineLyricsView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public Copy_5_of_KscManyLineLyricsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public Copy_5_of_KscManyLineLyricsView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {

		this.context = context;

		mScroller = new Scroller(context);

		paint = new Paint();
		paint.setDither(true);
		paint.setAntiAlias(true);

		paintHL = new Paint();
		paintHL.setColor(Color.rgb(255, 255, 255));
		paintHL.setDither(true);
		paintHL.setAntiAlias(true);

		paintHLED = new Paint();
		paintHLED.setDither(true);
		paintHLED.setAntiAlias(true);

		mPaintForTimeLine = new Paint();
		mPaintForTimeLine.setDither(true);
		mPaintForTimeLine.setAntiAlias(true);
		mPaintForTimeLine.setTextSize(SIZEWORD);

		paintBackgruond = new Paint();
		paintBackgruond.setAlpha(180);
		paintBackgruond.setColor(Color.BLACK);
		paintBackgruond.setDither(true);
		paintBackgruond.setAntiAlias(true);
		paintBackgruond.setTextSize(SIZEWORD);

		ObserverManage.getObserver().addObserver(this);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		paintHLED.setColor(Constants.LRCCOLORS[Constants.LRC_COLOR_INDEX]);
		mPaintForTimeLine
				.setColor(Constants.LRCCOLORS[Constants.LRC_COLOR_INDEX]);

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

		if (!blLrc) {
			paint.setTextSize(SIZEWORDDEF);
			paint.setColor(Color.rgb(255, 255, 255));
			paint.setAlpha(255);
			String tip = "乐乐音乐，传播好的音乐";
			float textWidth = paint.measureText(tip);// 用画笔测量歌词的宽度
			FontMetrics fm = paint.getFontMetrics();
			int height = (int) Math.ceil(fm.descent - fm.top) + 2;
			canvas.drawText(tip, (getWidth() - textWidth) / 2,
					(getHeight() + height) / 2, paint);
		} else {
			if (mTotleDrawRow == 0) {
				// 初始化将要绘制的歌词行数
				mTotleDrawRow = (int) (getHeight() / (SIZEWORDDEF + INTERVAL));
			}
			// 因为不需要将所有歌词画出来
			int minRaw = lyricsLineNum - (mTotleDrawRow - 1) / 2;
			int maxRaw = lyricsLineNum + (mTotleDrawRow - 1) / 2;
			minRaw = Math.max(minRaw, 0); // 处理上边
			maxRaw = Math.min(maxRaw, lyricsLineTreeMap.size() - 1); // 处理下边

			// 实现渐变的最大歌词行
			int count = Math
					.max(maxRaw - lyricsLineNum, lyricsLineNum - minRaw);
			// 两行歌词间字体颜色变化的透明度
			int alpha = (0xFF - 0x11) / count;

			// 画出来的第一行歌词的y坐标
			float rowY = getHeight() / 2 + minRaw * (SIZEWORDDEF + INTERVAL);
			for (int i = minRaw; i <= maxRaw; i++) {
				if (i == lyricsLineNum) {
					// 画高亮歌词行
					// 因为有缩放效果，有需要动态设置歌词的字体大小
					float textSize = SIZEWORDDEF + (SIZEWORD - SIZEWORDDEF)
							* mCurFraction;
					paintHL.setTextSize(textSize);
					paintHLED.setTextSize(textSize);

					KscLyricsLineInfo kscLyricsLineInfo = lyricsLineTreeMap
							.get(lyricsLineNum);
					// 获取到高亮歌词
					String text = kscLyricsLineInfo.getLineLyrics();
					float textWidth = paintHLED.measureText(text);// 用画笔测量歌词的宽度

					if (lyricsWordIndex == -1) {
						lineLyricsHLWidth = textWidth;
					} else {
						String lyricsWords[] = kscLyricsLineInfo
								.getLyricsWords();
						int wordsDisInterval[] = kscLyricsLineInfo
								.getWordsDisInterval();
						// 当前歌词之前的歌词
						String lyricsBeforeWord = "";
						for (int j = 0; j < lyricsWordIndex; j++) {
							lyricsBeforeWord += lyricsWords[j];
						}
						// 当前歌词
						String lyricsNowWord = lyricsWords[lyricsWordIndex]
								.trim();// 去掉空格

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
						// 如果歌词宽度小于view的宽，则让歌词居中显示
						textX = (getWidth() - textWidth) / 2;
					}

					canvas.drawText(text, textX, rowY, paintHL);

					FontMetrics fm = paintHL.getFontMetrics();
					int height = (int) Math.ceil(fm.descent - fm.top) + 2;

					canvas.clipRect(textX, rowY - height, textX
							+ lineLyricsHLWidth, rowY + height);

					canvas.drawText(text, textX, rowY, paintHLED);

					canvas.restore();
				} else {
					if (i == oldLyricsLineNum) {
						// 因为有缩放效果，有需要动态设置歌词的字体大小
						float textSize = SIZEWORD - (SIZEWORD - SIZEWORDDEF)
								* mCurFraction;
						paint.setTextSize(textSize);
					} else {// 画其他的歌词
						paint.setTextSize(SIZEWORDDEF);
					}

					String text = lyricsLineTreeMap.get(i).getLineLyrics();
					float textWidth = paint.measureText(text);
					float textX = (getWidth() - textWidth) / 2;
					// 如果计算出的textX为负数，将textX置为0(实现：如果歌词宽大于view宽，则居左显示，否则居中显示)
					textX = Math.max(textX, 10);
					// 实现颜色渐变 从 0xFFFFFFFF 逐渐变为 0x11FFFFFF(颜色还是白色，只是透明度变度)
					int curAlpha = 255 - (Math.abs(i - lyricsLineNum) - 1)
							* alpha; // 求出当前歌词颜色的透明度?
					paint.setColor(0x1000000 * curAlpha + 0xffffff);
					canvas.drawText(text, textX, rowY, paint);
				}
				// 计算出下一行歌词绘制的y坐标
				rowY += SIZEWORDDEF + INTERVAL;
			}

			// 画时间线和时间线
			if (mIsDrawTimeLine) {
				timeStr = kscLyricsParser.timeParserString(progress);
				FontMetrics fm = mPaintForTimeLine.getFontMetrics();
				int height = (int) Math.ceil(fm.descent - fm.top) + 2;
				float y = getHeight() / 2 + getScrollY();
				drawBackground(canvas, timeStr, 0, y + height);
				canvas.drawText(timeStr, 0, y + height, mPaintForTimeLine);
				canvas.drawLine(0, y, getWidth(), y, mPaintForTimeLine);
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
		if (!blScroll) {
			this.progress = playProgress;

			int newLyricsLineNum = kscLyricsParser
					.getLineNumberFromCurPlayingTime(playProgress);
			if (newLyricsLineNum != lyricsLineNum) {
				oldLyricsLineNum = lyricsLineNum;
				lyricsLineNum = newLyricsLineNum;
				// logger.i("lyricsLineNum:--->" + lyricsLineNum);
				// if (lyricsLineNum - 1 >= 0) {
				// logger.i("lyricsLineNum2:--->" + lyricsLineNum);
				if (!mScroller.isFinished()) {
					mScroller.forceFinished(true);
				}
				smoothScrollTo(getScrollX(),
						(int) (lyricsLineNum * (SIZEWORDDEF + INTERVAL)));
				// }
				highLightLrcMoveX = 0;
				// if (lyricsLineNum != -1) {
				// // 如果高亮歌词的宽度大于View的宽，就开启属性动画，让它水平滚动
				// String lineLyrics = lyricsLineTreeMap.get(lyricsLineNum)
				// .getLineLyrics();
				// float textWidth = paintHLED.measureText(lineLyrics);
				// if (textWidth > getWidth()) {
				// stopScrollLrc();
				// // logger.i("水平滚动歌词 ---- " + lineLyrics);
				// long duration = kscLyricsParser
				// .getOffsetYTimeFromCurPlayingTime(lyricsLineNum) / 2;
				// startScrollLrc(getWidth() - textWidth, duration);
				// }
				// }
			}
			lyricsWordIndex = kscLyricsParser
					.getDisWordsIndexFromCurPlayingTime(lyricsLineNum,
							playProgress);

			lyricsWordHLEDTime = kscLyricsParser.getLenFromCurPlayingTime(
					lyricsLineNum, playProgress);

			invalidate();
		}
	}

	//
	// /** 控制歌词水平滚动的属性动画 ***/
	// private ValueAnimator mAnimator;
	//
	// /**
	// * 水平滚动歌词
	// *
	// * @param endX
	// * 歌词第一个字的最终的x坐标
	// * @param duration
	// * 滚动的持续时间
	// */
	// @SuppressLint("NewApi")
	// private void startScrollLrc(float endX, long duration) {
	// if (mAnimator == null) {
	// mAnimator = ValueAnimator.ofFloat(0, endX);
	// mAnimator.addUpdateListener(updateListener);
	// } else {
	// highLightLrcMoveX = 0;
	// mAnimator.cancel();
	// mAnimator.setFloatValues(0, endX);
	// }
	// mAnimator.setDuration(duration);
	// // mAnimator.setStartDelay((long) (duration * 0.3)); // 延迟执行属性动画
	// mAnimator.start();
	// }
	//
	// /**
	// * 停止歌词的滚动
	// */
	// @SuppressLint("NewApi")
	// private void stopScrollLrc() {
	// if (mAnimator != null) {
	// mAnimator.cancel();
	// }
	// highLightLrcMoveX = 0;
	// }
	//
	// /***
	// * 监听属性动画的改变
	// */
	// @SuppressLint("NewApi")
	// AnimatorUpdateListener updateListener = new AnimatorUpdateListener() {
	//
	// @Override
	// public void onAnimationUpdate(ValueAnimator animation) {
	// highLightLrcMoveX = (Float) animation.getAnimatedValue();
	// // logger.i("highLightLrcMoveX---- " + highLightLrcMoveX);
	// invalidate();
	// }
	// };

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
			int oldY = getScrollY();
			int y = mScroller.getCurrY();
			if (oldY != y && !blScroll) {
				scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			}
			mCurFraction = mScroller.timePassed() * 3f
					/ DURATION_FOR_LRC_SCROLL;
			mCurFraction = Math.min(mCurFraction, 1F);
			// 必须调用该方法，否则不一定能看到滚动效果
			postInvalidate();
		}
		super.computeScroll();
	}

	/**
	 * 描绘轮廓
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

	/* *
	 * 滑动事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float tt = event.getY();
		if (!blLrc) {
			return super.onTouchEvent(event);
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			blScroll = true;
			mIsDrawTimeLine = true;
			break;
		case MotionEvent.ACTION_MOVE:
			touchY = tt - touchY;
			progress = (int) (progress - touchY * 100);
			if (progress < 0) {
				progress = 0;
			}
			if (progress > scrollMaxYProgress) {
				progress = scrollMaxYProgress;
			}
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			blScroll = false;
			mIsDrawTimeLine = false;
			invalidate();

			// SongMessage songMessage = new SongMessage();
			// songMessage.setType(SongMessage.SEEKTO);
			// songMessage.setProgress(progress);
			// ObserverManage.getObserver().setMessage(songMessage);

			break;
		}
		touchY = tt;
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
	public void init(int scrollMaxYProgressT) {
		scrollMaxYProgress = scrollMaxYProgressT;
		highLightLrcMoveX = 0;
		mCurFraction = 1.0f;
		oldLyricsLineNum = -1;
		mTotleDrawRow = 0;
		blLrc = false;
		lyricsLineNum = -1;
		lyricsWordIndex = -1;
		lineLyricsHLWidth = 0;
		lyricsWordHLEDTime = 0;
		kscLyricsParser = null;
		lyricsLineTreeMap = null;

		if (!mScroller.isFinished()) {
			mScroller.forceFinished(true);
		}

		scrollTo(getScrollX(), 0);
		invalidate();

	}

}
