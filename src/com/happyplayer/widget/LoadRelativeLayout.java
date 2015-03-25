package com.happyplayer.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.happyplayer.ui.R;

public class LoadRelativeLayout extends RelativeLayout {

	/**
	 * 加载完成后显示的页面
	 */
	private View contentView;
	/**
	 * 正在加载页面
	 */
	private View loadingView;
	/**
	 * 旋转动画
	 */
	private Animation rotateAnimation;
	private LoadingImageView loadingImageView;

	public LoadRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LoadRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LoadRelativeLayout(Context context) {
		super(context);
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		if (getChildCount() == 0) {
			return;
		}
		contentView = getChildAt(0);
		contentView.setVisibility(View.INVISIBLE);

		LayoutInflater inflater = LayoutInflater.from(context);
		LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		loadingView = inflater.inflate(R.layout.view_loading, null, false);
		loadingImageView = (LoadingImageView) loadingView
				.findViewById(R.id.loadingImageView);
		loadingView.setVisibility(View.INVISIBLE);
		loadingView.setLayoutParams(params);
		rotateAnimation = AnimationUtils.loadAnimation(context,
				R.anim.anim_rotate);
		rotateAnimation.setInterpolator(new LinearInterpolator());// 匀速
		addView(loadingView);
	}

	/**
	 * 显示正在加载页面
	 */
	public void showLoadingView() {
		if (contentView == null)
			return;
		loadingImageView.clearAnimation();
		loadingImageView.startAnimation(rotateAnimation);
		contentView.setVisibility(View.INVISIBLE);
		loadingView.setVisibility(View.VISIBLE);
	}

	/**
	 * 显示加载成功页面
	 */
	public void showSuccessView() {
		if (contentView == null)
			return;
		//停止动画
		loadingImageView.clearAnimation();
		contentView.setVisibility(View.VISIBLE);
		loadingView.setVisibility(View.INVISIBLE);
	}

}
