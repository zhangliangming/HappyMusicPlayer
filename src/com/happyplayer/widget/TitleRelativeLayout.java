package com.happyplayer.widget;

import java.util.Observable;
import java.util.Observer;

import com.happyplayer.common.Constants;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.observable.ObserverManage;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class TitleRelativeLayout extends RelativeLayout implements Observer {

	public TitleRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public TitleRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TitleRelativeLayout(Context context) {
		super(context);
		init(context);
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	private void init(Context context) {
		ObserverManage.getObserver().addObserver(this);
		setBackgroundColor(Constants.BLACK_GROUND[Constants.DEF_COLOR_INDEX]);
	}

	@Override
	public void update(Observable arg0, Object data) {
		if (data instanceof SkinMessage) {
			SkinMessage msg = (SkinMessage) data;
			if (msg.type == SkinMessage.COLOR) {
				setBackgroundColor(Constants.BLACK_GROUND[Constants.DEF_COLOR_INDEX]);
			}
		}
	}
}
