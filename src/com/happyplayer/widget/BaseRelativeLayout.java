package com.happyplayer.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class BaseRelativeLayout extends RelativeLayout {

	public BaseRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public BaseRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BaseRelativeLayout(Context context) {
		super(context);
	}

	@Override
	public void setPressed(boolean pressed) {
		if (pressed && ((View) getParent()).isPressed()) {
			return;
		}
		super.setPressed(pressed);
	}

}
