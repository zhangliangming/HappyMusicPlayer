package com.happyplayer.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class FloatLyricRelativeLayout extends RelativeLayout {

	public FloatLyricRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public FloatLyricRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FloatLyricRelativeLayout(Context context) {
		super(context);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return false;
	}
}
