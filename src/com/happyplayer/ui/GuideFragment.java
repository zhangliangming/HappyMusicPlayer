package com.happyplayer.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.happyplayer.common.Constants;
import com.happyplayer.util.DataUtil;

public class GuideFragment extends Fragment {
	private View mMainView;

	// 初始化后执行动画
	private boolean isInitAni = false;

	private RelativeLayout parentRelativeLayout;
	private int color = Color.rgb(163, 161, 212);

	/**
	 * 标题图片
	 */
	private ImageView mainTitleImageView;
	private int mainTitleImage = R.drawable.guide_first_top_image;

	private TranslateAnimation imageViewTranslateAnimation;

	/**
	 * 副标题图片
	 */
	private ImageView secondTitleImageView;
	private int secondTitleImage = R.drawable.guide_first_middle_image;
	/**
	 * 中部图片
	 */
	private ImageView centPICImageView;
	private int centPICImage = R.drawable.guide_first_person;

	/**
	 * 底问按钮图片
	 */
	private ImageView bottonImageView;
	private boolean visibility = false;

	public GuideFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initComponent();
	}

	private void initComponent() {

		LayoutInflater inflater = getActivity().getLayoutInflater();
		mMainView = inflater.inflate(R.layout.fragement_guide, null, false);

		mainTitleImageView = (ImageView) mMainView
				.findViewById(R.id.main_title);

		mainTitleImageView.setBackgroundResource(mainTitleImage);
		mainTitleImageView.setVisibility(View.INVISIBLE);

		secondTitleImageView = (ImageView) mMainView
				.findViewById(R.id.second_title);
		secondTitleImageView.setBackgroundResource(secondTitleImage);
		secondTitleImageView.setVisibility(View.INVISIBLE);

		centPICImageView = (ImageView) mMainView.findViewById(R.id.cent_pic);
		centPICImageView.setBackgroundResource(centPICImage);

		bottonImageView = (ImageView) mMainView.findViewById(R.id.botton);
		if (visibility) {
			bottonImageView.setVisibility(View.VISIBLE);
		} else {
			bottonImageView.setVisibility(View.INVISIBLE);
		}

		bottonImageView.setOnClickListener(new ItemOnClick());

		parentRelativeLayout = (RelativeLayout) mMainView
				.findViewById(R.id.parent);
		parentRelativeLayout.setBackgroundColor(color);

		if (isInitAni) {
			setMainTitleImageAnimation(true);
			setSecondTitleImageAnimation(true);
		}
	}

	// @Override
	// public void setUserVisibleHint(boolean isVisibleToUser) {
	//
	// if (mainTitleImageView != null && secondTitleImageView != null) {
	// mainTitleImageView.setVisibility(View.INVISIBLE);
	// secondTitleImageView.setVisibility(View.INVISIBLE);
	// }
	// }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup viewGroup = (ViewGroup) mMainView.getParent();
		if (viewGroup != null) {
			viewGroup.removeAllViewsInLayout();
		}
		return mMainView;
	}

	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}

	public void setParentColor(int color) {
		this.color = color;
	}

	public void setMainTitleImage(int mainTitleImage) {
		this.mainTitleImage = mainTitleImage;
	}

	public void setSecondTitleImage(int secondTitleImage) {
		this.secondTitleImage = secondTitleImage;
	}

	public void setCentPICImage(int centPICImage) {
		this.centPICImage = centPICImage;
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			boolean isRightToLeft = (Boolean) msg.obj;
			switch (msg.what) {
			case 0:
				if (isRightToLeft) {
					imageViewTranslateAnimation = new TranslateAnimation(
							Animation.RELATIVE_TO_PARENT, +1.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f);
				} else {
					imageViewTranslateAnimation = new TranslateAnimation(
							Animation.RELATIVE_TO_PARENT, -1.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f);
				}

				imageViewTranslateAnimation
						.setInterpolator(new LinearInterpolator());
				imageViewTranslateAnimation.setFillAfter(true);
				imageViewTranslateAnimation.setDuration(1000);

				mainTitleImageView.clearAnimation();
				mainTitleImageView.startAnimation(imageViewTranslateAnimation);
				break;
			case 1:
				if (isRightToLeft) {
					imageViewTranslateAnimation = new TranslateAnimation(
							Animation.RELATIVE_TO_PARENT, +1.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f);
				} else {
					imageViewTranslateAnimation = new TranslateAnimation(
							Animation.RELATIVE_TO_PARENT, -1.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f);
				}

				imageViewTranslateAnimation
						.setInterpolator(new LinearInterpolator());
				imageViewTranslateAnimation.setFillAfter(true);
				imageViewTranslateAnimation.setDuration(1000);
				secondTitleImageView.clearAnimation();
				secondTitleImageView
						.startAnimation(imageViewTranslateAnimation);
				break;
			case 2:
				if (mainTitleImageView != null && secondTitleImageView != null) {
					mainTitleImageView.clearAnimation();
					mainTitleImageView.setVisibility(View.INVISIBLE);
					secondTitleImageView.clearAnimation();
					secondTitleImageView.setVisibility(View.INVISIBLE);
				}
				break;
			default:
				break;
			}

		}

	};

	/**
	 * 设置第一个标题动画
	 */
	public void setMainTitleImageAnimation(boolean isRightToLeft) {
		Message msg = new Message();
		msg.what = 0;
		msg.obj = isRightToLeft;
		mHandler.sendMessage(msg);
	}

	/**
	 * 设置第二个标题动画
	 */
	public void setSecondTitleImageAnimation(boolean isRightToLeft) {
		Message msg = new Message();
		msg.what = 1;
		msg.obj = isRightToLeft;
		mHandler.sendMessage(msg);
	}

	public void setInitAni(boolean isInitAni) {
		this.isInitAni = isInitAni;
	}

	public void setAnimationStop() {
		Message msg = new Message();
		msg.what = 2;
		msg.obj = true;
		mHandler.sendMessage(msg);
	}

	class ItemOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.botton:
				goHome();
				break;
			}
		}

		private void goHome() {
			DataUtil.save(getActivity(), Constants.THE_FIRST_KEY, false);
			Intent intent = new Intent(getActivity(), MainActivity.class);
			startActivity(intent);
			getActivity().finish();
		}
	}
}
