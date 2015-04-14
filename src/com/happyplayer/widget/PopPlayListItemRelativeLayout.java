package com.happyplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class PopPlayListItemRelativeLayout extends RelativeLayout {

	private int defColor;
	private int selectedColor;
	private int pressColor;
	private Paint paint;

	private boolean isPressed = false;
	private boolean isSelected = false;

	public PopPlayListItemRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public PopPlayListItemRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PopPlayListItemRelativeLayout(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		defColor = Color.argb(0, 255, 255, 255);
		selectedColor = Color.argb(50, 255, 255, 255);
		pressColor = Color.argb(80, 255, 255, 255);

		paint = new Paint();
		paint.setDither(true);
		paint.setAntiAlias(true);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {

		if (isPressed) {
			paint.setColor(pressColor);
		} else {
			if (isSelected) {
				paint.setColor(selectedColor);
			} else {
				paint.setColor(defColor);
			}
		}

		Rect r = new Rect(0, 0, getWidth(), getHeight());
		canvas.drawRect(r, paint);
		super.dispatchDraw(canvas);
	}

	public void setPressed(boolean pressed) {
		isPressed = pressed;
		invalidate();
		super.setPressed(pressed);
	}

	public void setSelect(boolean selected) {
		isSelected = selected;
		invalidate();
	}
}
