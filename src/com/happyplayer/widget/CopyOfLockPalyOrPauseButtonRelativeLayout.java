package com.happyplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 
 * 锁屏按钮
 * 
 */
public class CopyOfLockPalyOrPauseButtonRelativeLayout extends RelativeLayout {
	private Paint progressPaint;
	private Paint paint;
	private boolean isTouch = false;

	private int maxProgress = 0;
	private int playingProgress = 0;

	public CopyOfLockPalyOrPauseButtonRelativeLayout(Context context,
			AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CopyOfLockPalyOrPauseButtonRelativeLayout(Context context,
			AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CopyOfLockPalyOrPauseButtonRelativeLayout(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		paint = new Paint();
		paint.setDither(true);
		paint.setAntiAlias(true);

		progressPaint = new Paint();
		progressPaint.setDither(true);
		progressPaint.setAntiAlias(true);
	}

	public void setPressed(boolean pressed) {
		isTouch = pressed;
		invalidate();
		super.setPressed(pressed);
	}

	private double PI = 3.1416;

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (isTouch) {
			paint.setColor(Color.rgb(37, 158, 247));
			paint.setStyle(Paint.Style.FILL);
			paint.setStrokeWidth(3);
			int cx = getWidth() / 2;
			int cy = getHeight() / 2;
			canvas.drawCircle(cx, cy, getWidth() / 3, paint);
		} else {
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(3);
			int cx = getWidth() / 2;
			int cy = getHeight() / 2;
			int r = getWidth() / 3;
			canvas.drawCircle(cx, cy, r, paint);
			if (maxProgress != 0) {
				// save和restore是为了剪切操作不影响画布的其它元素
				canvas.save();

				double c = 2 * PI * r * playingProgress / maxProgress / r;
				double h = r * Math.cos(c);
				h = Math.abs(h);
				int dRx = 0;
				if (playingProgress <= maxProgress / 4) {
					dRx = (int) (getWidth() / 2 - h);
				} else if (playingProgress <= maxProgress / 2) {
					dRx = (int) (getWidth() / 2 + h);
				} else {
					dRx = getWidth();
				}
				// 右矩形
				int rightRectButtom = dRx;
				canvas.clipRect(getWidth() / 2, 0, getWidth(), rightRectButtom);

				progressPaint.setColor(Color.rgb(37, 158, 247));
				progressPaint.setStyle(Paint.Style.STROKE);
				progressPaint.setStrokeWidth(5);
				canvas.drawCircle(cx, cy, getWidth() / 3, progressPaint);

				canvas.restore();

				// save和restore是为了剪切操作不影响画布的其它元素
				canvas.save();

				int dLx = 0;
				if (playingProgress >= maxProgress / 2 && playingProgress  < maxProgress * 3 / 4) {
					dLx = (int) (getWidth() / 2 + h);
				} else if (playingProgress >= maxProgress * 3 / 4) {
					dLx = (int) (getWidth() / 2 - h);
				} else {
					dLx = getHeight();
				}

				// // 左矩形
				int leftRectButtom = dLx;
				canvas.clipRect(0, leftRectButtom, getWidth() / 2, getHeight());

				canvas.drawCircle(cx, cy, getWidth() / 3, progressPaint);

				canvas.restore();
			}

		}
		super.dispatchDraw(canvas);
	}

	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
	}

	public void setPlayingProgress(int playingProgress) {
		this.playingProgress = playingProgress;
	}

}
