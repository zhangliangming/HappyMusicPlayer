package com.happyplayer.widget;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
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

	/**
	 * 时间歌词
	 */
	private TextView timeLrc = null;

	private class SeekBarMessage {
		String timeTip;
		String timeLrc;
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			SeekBarMessage sm = (SeekBarMessage) msg.obj;
			timeLrc.setText(sm.timeLrc);
			timeTip.setText(sm.timeTip);
		}
	};

	private Context context;

	private Bitmap bmp;

	private Bitmap baseBitmap;

	private Canvas pCanvas;

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
		paint.setColor(Color.rgb(255, 255, 255));
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

		baseBitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.progress_dot_default);

		bmp = Bitmap.createBitmap(baseBitmap.getWidth(),
				baseBitmap.getHeight(), baseBitmap.getConfig());
		initbm();

		ObserverManage.getObserver().addObserver(this);
	}

	private void initbm() {
		Paint paint = new Paint();
		pCanvas = new Canvas(bmp);
		int color = Constants.BLACK_GROUND[Constants.DEF_COLOR_INDEX];
		float progressR = Color.red(color) / 255f;
		float progressG = Color.green(color) / 255f;
		float progressB = Color.blue(color) / 255f;
		float progressA = Color.alpha(color) / 255f;

		// 根据SeekBar定义RGBA的矩阵
		float[] src = new float[] { progressR, 0, 0, 0, 0, 0, progressG, 0, 0,
				0, 0, 0, progressB, 0, 0, 0, 0, 0, progressA, 0 };
		// 定义ColorMatrix，并指定RGBA矩阵
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.set(src);
		// 设置Paint的颜色
		paint.setColorFilter(new ColorMatrixColorFilter(src));
		// 通过指定了RGBA矩阵的Paint把原图画到空白图片上
		pCanvas.drawBitmap(baseBitmap, new Matrix(), paint);
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
	private void initPopuptWindow(String timeStr, View v, String lrc) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View popupWindow = layoutInflater.inflate(
				R.layout.seekbar_progress_dialog, null);
		timeTip = (TextView) popupWindow.findViewById(R.id.time_tip);
		timeTip.setText(timeStr);
		timeLrc = (TextView) popupWindow.findViewById(R.id.time_lrc);
		timeLrc.setText(lrc);
		mPopupWindow = new PopupWindow(popupWindow, screenWidth, 80, true);
		// mPopupWindow = new PopupWindow(popupWindow, LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT, true);
		// int[] location = new int[2];
		// this.getLocationInWindow(location); // 获取在当前窗口内的绝对坐标
		// this.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
		mPopupWindow.showAsDropDown(v, 0, 0 - v.getHeight() - 80);
	}

	/**
	 * 获取PopupWindow实例
	 * 
	 * @param lrc
	 */
	public void popupWindowShow(int timeLongStr, View v, String lrc) {
		String timeStr = MediaUtils.formatTime(timeLongStr);
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			Message msg = new Message();
			SeekBarMessage sm = new SeekBarMessage();
			sm.timeTip = timeStr;
			sm.timeLrc = lrc;
			msg.obj = sm;
			handler.sendMessage(msg);
		} else {
			initPopuptWindow(timeStr, v, lrc);
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
				initbm();
				invalidate();
			}
		}
	}
}
