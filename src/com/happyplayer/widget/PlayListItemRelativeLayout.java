package com.happyplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class PlayListItemRelativeLayout extends RelativeLayout {

	private int defColor;
	private int selectedColor;
	private int pressColor;
	private Paint paint;

	private boolean isPressed = false;
	private boolean isSelected = false;

	public PlayListItemRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public PlayListItemRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PlayListItemRelativeLayout(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		defColor = Color.rgb(255, 255, 255);
		selectedColor = Color.rgb(227, 227, 227);
		pressColor = Color.rgb(247, 247, 247);

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
	
	public void setSelect(boolean selected){
		isSelected = selected;
		invalidate();
	}
}
