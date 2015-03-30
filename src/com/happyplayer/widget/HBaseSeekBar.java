package com.happyplayer.widget;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.happyplayer.common.Constants;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.ui.R;
import com.happyplayer.util.MediaUtils;

public class HBaseSeekBar extends SeekBar implements Observer {
	/**
	 * 弹出提示信息窗口
	 */
	private PopupWindow mPopupWindow;
	/**
	 * 弹出窗口显示文本
	 */
	private TextView timeTip = null;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			timeTip.setText((String) msg.obj);
		}
	};

	private Context context;

	private Bitmap bmp;

	public HBaseSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public HBaseSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public HBaseSeekBar(Context context) {
		super(context);
		init(context);
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(Color.rgb(185, 185, 185));
		Rect r = new Rect(6, getHeight() / 2 - bmp.getHeight() / 20,
				getWidth() - 10, getHeight() / 2 + bmp.getHeight() / 20);
		canvas.drawRect(r, paint);
		// ///////////////////////////////////////////////////////////////
		Paint paint2 = new Paint();
		Rect r2 = new Rect(6, getHeight() / 2 - bmp.getHeight() / 20,
				getProgress() * (getWidth() - 10) / getMax(), getHeight() / 2
						+ bmp.getHeight() / 20);
		paint2.setColor(Constants.BLACK_GROUND[Constants.DEF_COLOR_INDEX]);
		canvas.drawRect(r2, paint2);
		//
		if (getProgress() == getMax()) {
			canvas.drawBitmap(bmp, getProgress() * (getWidth() - 10) / getMax()
					- bmp.getWidth() / 3 - 6, getHeight() / 2 - bmp.getHeight()
					/ 2, paint);
		} else {
			canvas.drawBitmap(bmp, getProgress() * (getWidth() - 10) / getMax()
					- bmp.getWidth() / 3 - 4, getHeight() / 2 - bmp.getHeight()
					/ 2, paint);
		}
	}

	@Override
	public void setPressed(boolean pressed) {
		if (pressed && ((View) getParent()).isPressed()) {
			return;
		}
		super.setPressed(pressed);
	}

	@Override
	public void setThumb(Drawable thumb) {
		super.setThumb(thumb);
	}

	/**
	 * 初始化
	 */
	private void init(Context context) {
		this.context = context;

		bmp = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.progress_dot_default);

		ObserverManage.getObserver().addObserver(this);
	}

	/**
	 * 滑动开始
	 */
	public void startTrackingTouch() {
	}

	/**
	 * 滑动结束
	 */
	public void stopTrackingTouch() {
	}

	/**
	 * 创建PopupWindow
	 */
	private void initPopuptWindow(String timeStr) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View popupWindow = layoutInflater.inflate(
				R.layout.seekbar_progress_dialog, null);
		timeTip = (TextView) popupWindow.findViewById(R.id.time_tip);
		timeTip.setText(timeStr);
		mPopupWindow = new PopupWindow(popupWindow, 100, 50, true);
		// mPopupWindow = new PopupWindow(popupWindow, LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT, true);
		// int[] location = new int[2];
		// this.getLocationInWindow(location); // 获取在当前窗口内的绝对坐标
		// this.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
		mPopupWindow.showAsDropDown(this, (screenWidth) / 2 - 100 - 100 / 3,
				0 - this.getHeight() - 50);
	}

	/**
	 * 获取PopupWindow实例
	 */
	public void popupWindowShow(int timeLongStr) {
		String timeStr = MediaUtils.formatTime(timeLongStr);
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			Message msg = new Message();
			msg.obj = timeStr;
			handler.sendMessage(msg);
		} else {
			initPopuptWindow(timeStr);
		}
	}

	/**
	 * 关闭窗口
	 */
	public void popupWindowDismiss() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
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
