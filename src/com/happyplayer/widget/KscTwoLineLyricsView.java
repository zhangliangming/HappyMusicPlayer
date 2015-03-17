package com.happyplayer.widget;

import com.happyplayer.common.Constants;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.View;

public class KscTwoLineLyricsView extends View {
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
	private int SIZEWORD = 35;

	public KscTwoLineLyricsView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public KscTwoLineLyricsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public KscTwoLineLyricsView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		paint = new Paint();
		paint.setColor(Color.rgb(51, 51, 51));
		paint.setDither(true);
		paint.setAntiAlias(true);
		paint.setTextSize(SIZEWORD);

		paintHL = new Paint();
		paintHL.setColor(Constants.BLACK_GROUND[Constants.DEF_COLOR_INDEX]);
		paintHL.setDither(true);
		paintHL.setAntiAlias(true);
		paintHL.setTextSize(SIZEWORD);
	}

	@Override
	public void draw(Canvas canvas) {

		if (!blLrc) {
			String tip = "乐乐音乐，传播好的音乐";
			float tipTextWidth = paint.measureText(tip);
			FontMetrics fm = paintHL.getFontMetrics();
			int height = (int) Math.ceil(fm.descent - fm.top) + 2;
			
			canvas.drawText(tip, (getWidth() - tipTextWidth) / 2,
					(getHeight() + height) / 2, paint);

			canvas.clipRect((getWidth() - tipTextWidth) / 2,
					(getHeight() + height) / 2 + height, (getWidth() - tipTextWidth) / 2
							+ tipTextWidth / 2 + 5,  height);

			canvas.drawText(tip, (getWidth() - tipTextWidth) / 2,
					(getHeight() + height) / 2, paintHL);
		} else {

		}
		super.draw(canvas);
	}
}
