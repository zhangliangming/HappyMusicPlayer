package com.happyplayer.widget;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.happyplayer.common.Constants;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.observable.ObserverManage;

public class ListViewRelativeLayout extends RelativeLayout implements Observer {

	private Paint paint;
	private boolean isTouch = false;

	private Map<String, Integer> childsTextColor = null;

	public ListViewRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ListViewRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ListViewRelativeLayout(Context context) {
		super(context);
		init(context);
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	private void init(Context context) {
		paint = new Paint();
		paint.setDither(true);
		paint.setAntiAlias(true);
		ObserverManage.getObserver().addObserver(this);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		invalidateChild(isTouch);
		if (isTouch) {
			paint.setColor(Constants.BLACK_GROUND[Constants.DEF_COLOR_INDEX]);
			paint.setAlpha(150);
			Rect r = new Rect(0, 0, getWidth(), getHeight());
			canvas.drawRect(r, paint);
			paint.setColor(Color.argb(150, 255, 255, 255));
			canvas.drawLine(0, 0, getWidth(), 1, paint);
		} else {
			paint.setColor(Color.argb(150, 255, 255, 255));
			Rect r = new Rect(0, 0, getWidth(), getHeight());
			canvas.drawRect(r, paint);
			paint.setColor(Color.rgb(210, 210, 210));
			canvas.drawLine(10, getHeight() - 1, getWidth() - 10, getHeight(),
					paint);
			childsTextColor = null;
		}
		super.dispatchDraw(canvas);
	}

	public void setPressed(boolean pressed) {
		isTouch = pressed;
		invalidate();
		super.setPressed(pressed);
	}

	private void invalidateChild(boolean pressed) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			if (childsTextColor == null) {
				childsTextColor = new HashMap<String, Integer>();
			}
			View v = getChildAt(i);
			if (v instanceof TextView) {
				TextView temp = (TextView) v;
				int color = temp.getCurrentTextColor();
				if (!childsTextColor.containsKey(temp.getId() + "")) {
					childsTextColor.put(temp.getId() + "", color);
				}
				if (pressed) {
					temp.setTextColor(Color.WHITE);
				} else {
					int tempColor = childsTextColor.get(temp.getId() + "");
					temp.setTextColor(tempColor);
				}
			} else if (v instanceof ImageButton) {
				ImageButton temp = (ImageButton) v;
				temp.setPressed(pressed);
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

}
