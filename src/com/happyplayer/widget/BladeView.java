package com.happyplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.happyplayer.common.Constants;
import com.happyplayer.ui.R;

public class BladeView extends View {
	private OnItemClickListener mOnItemClickListener;
	String[] b = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
			"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
			"Z", "#" };
	int choose = -1;
	private boolean isFirstPress = true;
	Paint paint = new Paint();
	boolean showBkg = false;
	private PopupWindow mPopupWindow;
	private TextView mPopupText;
	private Handler handler = new Handler();

	public BladeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public BladeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BladeView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (showBkg) {
			canvas.drawColor(Color.parseColor("#AAAAAA"));
		}

		int height = getHeight();
		int width = getWidth();
		int singleHeight = height / b.length;
		for (int i = 0; i < b.length; i++) {
			paint.setColor(Color.parseColor("#ff2f2f2f"));
			// paint.setTypeface(Typeface.DEFAULT_BOLD); //加粗
			paint.setTextSize(getResources().getDimensionPixelSize(
					R.dimen.bladeview_fontsize));// 设置字体的大小
			paint.setFakeBoldText(true);
			paint.setAntiAlias(true);
			if (i == choose) {
				paint.setColor(Constants.BLACK_GROUND[Constants.DEF_COLOR_INDEX]);
			}
			float xPos = width / 2 - paint.measureText(b[i]) / 2;
			float yPos = singleHeight * i + singleHeight;
			canvas.drawText(b[i], xPos, yPos, paint);
			paint.reset();
		}

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float y = event.getY();
		// final int oldChoose = choose;
		final int c = (int) (y / getHeight() * b.length);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			isFirstPress = false;
			showBkg = true;
			// if (oldChoose != c) {
			if (c >= 0 && c < b.length) { // 让第一个字母响应点击事件
				performItemClicked(c);
				choose = c;
				invalidate();
			}
			// }

			break;
		case MotionEvent.ACTION_MOVE:
			// if (oldChoose != c) {
			if (c >= 0 && c < b.length) { // 让第一个字母响应点击事件
				performItemClicked(c);
				choose = c;
				invalidate();
			}
			// }
			break;
		case MotionEvent.ACTION_UP:
			showBkg = false;
			// choose = -1;
			dismissPopup();
			invalidate();
			break;
		}
		return true;
	}

	private void showPopup(int item) {
		if (mPopupWindow == null) {

			handler.removeCallbacks(dismissRunnable);
			mPopupText = new TextView(getContext());
			mPopupText.setBackgroundColor(Color.GRAY);
			mPopupText.setTextColor(Color.WHITE);
			mPopupText.setTextSize(getResources().getDimensionPixelSize(
					R.dimen.bladeview_popup_fontsize));
			mPopupText.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL);

			int height = getResources().getDimensionPixelSize(
					R.dimen.bladeview_popup_height);

			mPopupWindow = new PopupWindow(mPopupText, height, height);
		}

		String text = b[item];
		mPopupText.setText(text);
		if (mPopupWindow.isShowing()) {
			mPopupWindow.update();
		} else {
			mPopupWindow.showAtLocation(getRootView(),
					Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		}
	}

	private void dismissPopup() {
		handler.postDelayed(dismissRunnable, 800);
	}

	Runnable dismissRunnable = new Runnable() {

		@Override
		public void run() {
			if (mPopupWindow != null) {
				mPopupWindow.dismiss();
			}
		}
	};

	public int getChoose() {
		return choose;
	}

	public void setChoose(int choose) {
		this.choose = choose;
		invalidate();
	}

	public boolean isFirstPress() {
		return isFirstPress;
	}

	public void setFirstPress(boolean isFirstPress) {
		this.isFirstPress = isFirstPress;
	}

	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	private void performItemClicked(int item) {
		if (mOnItemClickListener != null) {
			mOnItemClickListener.onItemClick(b[item]);
			showPopup(item);
		}
	}

	public interface OnItemClickListener {
		void onItemClick(String s);
	}

}
