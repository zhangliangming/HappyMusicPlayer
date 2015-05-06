//package com.happyplayer.ui;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Observable;
//import java.util.Observer;
//import java.util.TreeMap;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.happyplayer.async.AsyncTaskHandler;
//import com.happyplayer.manage.MediaManage;
//import com.happyplayer.model.KscLyricsLineInfo;
//import com.happyplayer.model.SongInfo;
//import com.happyplayer.model.SongMessage;
//import com.happyplayer.observable.ObserverManage;
//import com.happyplayer.util.KscLyricsManamge;
//import com.happyplayer.util.KscLyricsParser;
//import com.happyplayer.widget.KscManyLineLyricsView;
//import com.happyplayer.widget.LockButtonRelativeLayout;
//import com.happyplayer.widget.LockPalyOrPauseButtonRelativeLayout;
//
//public class CopyOfLockMenuFragment extends Fragment implements Observer {
//	private View mMainView;
//
//	/**
//	 * 滑动提示图标
//	 */
//	private ImageView[] tipDotImageView;
//
//	/**
//	 * 歌名
//	 */
//	private TextView songNameTextView;
//	/**
//	 * 歌手
//	 */
//	private TextView songerTextView;
//	/**
//	 * 时间
//	 */
//	private TextView timeTextView;
//	/**
//	 * 日期
//	 */
//	private TextView dateTextView;
//	/**
//	 * 星期几
//	 */
//	private TextView dayTextView;
//
//	/**
//	 * 歌词解析
//	 */
//	private KscLyricsParser kscLyricsParser;
//	/**
//	 * 歌词
//	 */
//	private TreeMap<Integer, KscLyricsLineInfo> lyricsLineTreeMap;
//
//	private KscManyLineLyricsView kscManyLineLyricsView;
//
//	private LockButtonRelativeLayout prewButton;
//	private LockButtonRelativeLayout nextButton;
//
//	private LockPalyOrPauseButtonRelativeLayout playOrPauseButton;
//
//	private ImageView playImageView;
//
//	private ImageView pauseImageView;
//
//	private Handler songHandler = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//
//			SongMessage songMessage = (SongMessage) msg.obj;
//			final SongInfo songInfo = songMessage.getSongInfo();
//			switch (songMessage.getType()) {
//			case SongMessage.INIT:
//
//				songNameTextView.setText(songInfo.getDisplayName());
//				songerTextView.setText(songInfo.getArtist());
//
//				initKscLyrics(songInfo);
//
//				break;
//			case SongMessage.LASTPLAYFINISH:
//
//				songNameTextView.setText(songInfo.getDisplayName());
//				songerTextView.setText(songInfo.getArtist());
//
//				initKscLyrics(songInfo);
//				break;
//			case SongMessage.PLAY:
//				if (pauseImageView.getVisibility() != View.VISIBLE) {
//					pauseImageView.setVisibility(View.VISIBLE);
//				}
//				if (playImageView.getVisibility() != View.INVISIBLE) {
//					playImageView.setVisibility(View.INVISIBLE);
//				}
//				break;
//			case SongMessage.PLAYING:
//				reshLrcView((int) songInfo.getPlayProgress());
//				break;
//			case SongMessage.STOPING:
//				pauseImageView.setVisibility(View.INVISIBLE);
//				playImageView.setVisibility(View.VISIBLE);
//				reshLrcView((int) songInfo.getPlayProgress());
//				break;
//			}
//		}
//	};
//
//	private Handler mHandler = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//			SongInfo songInfo = (SongInfo) msg.obj;
//
//			songNameTextView.setText(songInfo.getDisplayName());
//			songerTextView.setText(songInfo.getArtist());
//			int status = MediaManage.getMediaManage(
//					CopyOfLockMenuFragment.this.getActivity()).getPlayStatus();
//			if (status == MediaManage.STOP) {
//				pauseImageView.setVisibility(View.INVISIBLE);
//				playImageView.setVisibility(View.VISIBLE);
//			} else {
//				pauseImageView.setVisibility(View.VISIBLE);
//				playImageView.setVisibility(View.INVISIBLE);
//			}
//			initKscLyrics(songInfo);
//		}
//
//	};
//
//	public CopyOfLockMenuFragment() {
//
//	}
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		initComponent();
//		setDate();
//		loadData();
//		ObserverManage.getObserver().addObserver(this);
//
//		IntentFilter mTimeFilter = new IntentFilter();
//		mTimeFilter.addAction(Intent.ACTION_TIME_TICK);
//		getActivity().registerReceiver(mTimeReceiver, mTimeFilter);
//	}
//
//	/**
//	 * 设置日期
//	 */
//	private void setDate() {
//
//		String str = "";
//		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd");
//		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
//
//		Calendar lastDate = Calendar.getInstance();
//		str = sdfDate.format(lastDate.getTime());
//		dateTextView.setText(str);
//		str = sdfTime.format(lastDate.getTime());
//		timeTextView.setText(str);
//
//		String mWay = String.valueOf(lastDate.get(Calendar.DAY_OF_WEEK));
//		if ("1".equals(mWay)) {
//			mWay = "日";
//		} else if ("2".equals(mWay)) {
//			mWay = "一";
//		} else if ("3".equals(mWay)) {
//			mWay = "二";
//		} else if ("4".equals(mWay)) {
//			mWay = "三";
//		} else if ("5".equals(mWay)) {
//			mWay = "四";
//		} else if ("6".equals(mWay)) {
//			mWay = "五";
//		} else if ("7".equals(mWay)) {
//			mWay = "六";
//		}
//		dayTextView.setText("星期" + mWay);
//
//	}
//
//	/**
//	 * 设置时间
//	 */
//	private void setTime() {
//		String str = "";
//		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
//		Calendar lastDate = Calendar.getInstance();
//		str = sdfTime.format(lastDate.getTime());
//		timeTextView.setText(str);
//	}
//
//	private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
//				setTime();
//			}
//		}
//	};
//
//	private void loadData() {
//		new AsyncTaskHandler() {
//
//			@Override
//			protected void onPostExecute(Object result) {
//
//			}
//
//			@Override
//			protected Object doInBackground() throws Exception {
//				SongInfo songInfo = MediaManage.getMediaManage(getActivity())
//						.getPlaySongInfo();
//				if (songInfo != null) {
//					Message msg = new Message();
//					msg.obj = songInfo;
//					mHandler.sendMessage(msg);
//				}
//				return null;
//			}
//		}.execute();
//	}
//
//	/**
//	 * 初始化歌词
//	 * 
//	 * @param songInfo
//	 *            当前歌曲的信息
//	 */
//	private void initKscLyrics(final SongInfo songInfo) {
//		new AsyncTaskHandler() {
//			@Override
//			protected void onPostExecute(Object result) {
//				kscLyricsParser = (KscLyricsParser) result;
//				lyricsLineTreeMap = kscLyricsParser.getLyricsLineTreeMap();
//				kscManyLineLyricsView.init((int) songInfo.getDuration());
//				playOrPauseButton.setPlayingProgress(0);
//				playOrPauseButton.setMaxProgress((int) songInfo.getDuration());
//				playOrPauseButton.invalidate();
//				if (lyricsLineTreeMap.size() != 0) {
//					kscManyLineLyricsView.setKscLyricsParser(kscLyricsParser);
//					kscManyLineLyricsView
//							.setLyricsLineTreeMap(lyricsLineTreeMap);
//					kscManyLineLyricsView.setCanScroll(false);
//					kscManyLineLyricsView.setBlLrc(true);
//					kscManyLineLyricsView.setOnLrcClickListener(null);
//					kscManyLineLyricsView.invalidate();
//				} else {
//					kscManyLineLyricsView.setCanScroll(false);
//					kscManyLineLyricsView.setBlLrc(false);
//					kscManyLineLyricsView.setOnLrcClickListener(null);
//					kscManyLineLyricsView.invalidate();
//				}
//			}
//
//			@Override
//			protected Object doInBackground() throws Exception {
//
//				return KscLyricsManamge.getKscLyricsParser(songInfo
//						.getDisplayName());
//			}
//		}.execute();
//	}
//
//	/**
//	 * 
//	 * @param playProgress
//	 *            根据当前歌曲播放进度，刷新歌词
//	 */
//	private void reshLrcView(int playProgress) {
//		playOrPauseButton.setPlayingProgress(playProgress);
//		playOrPauseButton.invalidate();
//		// 判断当前的歌曲是否有歌词
//		boolean blLrc = kscManyLineLyricsView.getBlLrc();
//		if (blLrc) {
//			kscManyLineLyricsView.showLrc(playProgress);
//		}
//	}
//
//	private void initComponent() {
//		LayoutInflater inflater = getActivity().getLayoutInflater();
//		mMainView = inflater.inflate(R.layout.lock_menu, null, false);
//
//		tipDotImageView = new ImageView[3];
//
//		int i = 0;
//		tipDotImageView[i++] = (ImageView) mMainView
//				.findViewById(R.id.tip_dot_1);
//		tipDotImageView[i++] = (ImageView) mMainView
//				.findViewById(R.id.tip_dot_2);
//		tipDotImageView[i++] = (ImageView) mMainView
//				.findViewById(R.id.tip_dot_3);
//
//		mHandler.postDelayed(mRunnable, 100);
//
//		songNameTextView = (TextView) mMainView.findViewById(R.id.songName);
//		songerTextView = (TextView) mMainView.findViewById(R.id.songer);
//
//		timeTextView = (TextView) mMainView.findViewById(R.id.time);
//		dateTextView = (TextView) mMainView.findViewById(R.id.date);
//		dayTextView = (TextView) mMainView.findViewById(R.id.day);
//
//		kscManyLineLyricsView = (KscManyLineLyricsView) mMainView
//				.findViewById(R.id.kscManyLineLyricsView);
//
//		prewButton = (LockButtonRelativeLayout) mMainView
//				.findViewById(R.id.prev_button);
//
//		prewButton.setOnClickListener(new ItemOnClick());
//
//		nextButton = (LockButtonRelativeLayout) mMainView
//				.findViewById(R.id.next_button);
//		nextButton.setOnClickListener(new ItemOnClick());
//
//		playOrPauseButton = (LockPalyOrPauseButtonRelativeLayout) mMainView
//				.findViewById(R.id.play_pause_button);
//		playOrPauseButton.setOnClickListener(new ItemOnClick());
//
//		playImageView = (ImageView) mMainView.findViewById(R.id.play);
//		pauseImageView = (ImageView) mMainView.findViewById(R.id.pause);
//	}
//
//	int i = 0;
//	private Runnable mRunnable = new Runnable() {
//
//		@Override
//		public void run() {
//			i++;
//			if (i >= tipDotImageView.length) {
//				i = 0;
//			}
//			for (int j = 0; j < tipDotImageView.length; j++) {
//				if (i == j) {
//					tipDotImageView[j]
//							.setBackgroundResource(R.drawable.kg_navigation_arrow_image_white);
//				} else {
//					tipDotImageView[j]
//							.setBackgroundResource(R.drawable.kg_navigation_arrow_image);
//				}
//			}
//			mHandler.postDelayed(this, 300);
//		}
//	};
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		ViewGroup viewGroup = (ViewGroup) mMainView.getParent();
//		if (viewGroup != null) {
//			viewGroup.removeAllViewsInLayout();
//		}
//		return mMainView;
//	}
//
//	class ItemOnClick implements OnClickListener {
//
//		@Override
//		public void onClick(View v) {
//			switch (v.getId()) {
//			case R.id.prev_button:
//				prev();
//				break;
//			case R.id.next_button:
//				next();
//				break;
//			case R.id.play_pause_button:
//				playOrPause();
//				break;
//			}
//		}
//	}
//
//	@Override
//	public void onDestroy() {
//		getActivity().unregisterReceiver(mTimeReceiver);
//		mHandler.removeCallbacks(mRunnable);
//		ObserverManage.getObserver().deleteObserver(this);
//		super.onDestroy();
//	}
//
//	public void playOrPause() {
//		SongMessage songMessage = new SongMessage();
//		songMessage.setType(SongMessage.PLAYORSTOPMUSIC);
//		ObserverManage.getObserver().setMessage(songMessage);
//	}
//
//	public void next() {
//		SongMessage songMessage = new SongMessage();
//		songMessage.setType(SongMessage.NEXTMUSIC);
//		ObserverManage.getObserver().setMessage(songMessage);
//	}
//
//	public void prev() {
//		SongMessage songMessage = new SongMessage();
//		songMessage.setType(SongMessage.PREVMUSIC);
//		ObserverManage.getObserver().setMessage(songMessage);
//	}
//
//	@Override
//	public void update(Observable observable, Object data) {
//		if (data instanceof SongMessage) {
//			SongMessage songMessage = (SongMessage) data;
//			if (songMessage.getType() == SongMessage.INIT
//					|| songMessage.getType() == SongMessage.PLAYING
//					|| songMessage.getType() == SongMessage.STOPING
//					|| songMessage.getType() == SongMessage.LASTPLAYFINISH
//					|| songMessage.getType() == SongMessage.PLAY) {
//				Message msg = new Message();
//				msg.obj = songMessage;
//				songHandler.sendMessage(msg);
//			}
//		}
//	}
// }
