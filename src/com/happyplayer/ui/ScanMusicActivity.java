package com.happyplayer.ui;

import java.io.File;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.db.SongDB;
import com.happyplayer.model.Mp3Info;
import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.util.ActivityManager;
import com.happyplayer.util.AniUtil;
import com.happyplayer.util.MediaUtils;
import com.happyplayer.widget.TitleRelativeLayout;

public class ScanMusicActivity extends Activity {
	/**
	 * 初始界面
	 */
	private RelativeLayout initRelativeLayout;

	/**
	 * 初始按钮，扫描按钮
	 */
	private TitleRelativeLayout initButton;

	/**
	 * 扫描界面
	 */
	private RelativeLayout scaningRelativeLayout;
	/**
	 * 扫描图片
	 */
	private ImageView scaningPICImageView;
	private AnimationDrawable aniLoading;

	/**
	 * 扫描路径
	 */
	private TextView scanPathTextView;
	/**
	 * 扫描结果
	 */
	private TextView scanResultTextView;
	/**
	 * 取消按钮
	 */
	private RelativeLayout cancelButton;
	/**
	 * 取消扫描
	 */
	private boolean cancelScan = false;
	/**
	 * 正在扫描
	 */
	private boolean isScan = false;
	/**
	 * 完成界面
	 */
	private RelativeLayout finishRelativeLayout;
	/**
	 * 扫描完成后的结果
	 */
	private TextView finishResultTextView;
	/**
	 * 完成按钮
	 */
	private RelativeLayout finishButton;
	/**
	 * 歌曲总数
	 */
	private int size = 0;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// 完成
			if (msg.what == -1) {
				isScan = false;
				if (size != 0) {
					SongMessage songMessage = new SongMessage();
					songMessage.setNum(size);
					songMessage.setType(SongMessage.SCAN_NUM);

					ObserverManage.getObserver().setMessage(songMessage);
				}

				initRelativeLayout.setVisibility(View.INVISIBLE);
				initButton.setVisibility(View.INVISIBLE);

				scaningRelativeLayout.setVisibility(View.INVISIBLE);
				cancelButton.setVisibility(View.INVISIBLE);

				finishRelativeLayout.setVisibility(View.VISIBLE);
				finishButton.setVisibility(View.VISIBLE);

				finishResultTextView.setText(size + "首歌曲已添加到本地音乐");

				AniUtil.stopAnimation(aniLoading);

			} else if (msg.what == 0) {
				String path = (String) msg.obj;
				scanPathTextView.setText(path);
				scanResultTextView.setText(size + "首歌曲已添加到本地音乐");
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanmusic);
		init();
		ActivityManager.getInstance().addActivity(this);
	}

	private void init() {

		initRelativeLayout = (RelativeLayout) findViewById(R.id.init);
		initRelativeLayout.setVisibility(View.VISIBLE);
		initButton = (TitleRelativeLayout) findViewById(R.id.initButton);
		initButton.setVisibility(View.VISIBLE);
		initButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				cancelScan = false;
				scanMusic();
			}
		});

		scaningRelativeLayout = (RelativeLayout) findViewById(R.id.scaning);
		scaningRelativeLayout.setVisibility(View.INVISIBLE);

		scaningPICImageView = (ImageView) findViewById(R.id.scaning_pic);
		aniLoading = (AnimationDrawable) scaningPICImageView.getBackground();

		scanPathTextView = (TextView) findViewById(R.id.scanPath);
		scanResultTextView = (TextView) findViewById(R.id.scanResult);

		cancelButton = (TitleRelativeLayout) findViewById(R.id.cancelButton);
		cancelButton.setVisibility(View.INVISIBLE);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				cancel();
			}
		});

		finishRelativeLayout = (RelativeLayout) findViewById(R.id.finish);
		finishRelativeLayout.setVisibility(View.INVISIBLE);
		finishResultTextView = (TextView) findViewById(R.id.finishResult);
		finishButton = (TitleRelativeLayout) findViewById(R.id.finishButton);
		finishButton.setVisibility(View.INVISIBLE);
		finishButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

	}

	protected void cancel() {
		cancelScan = true;
		isScan = false;
	}

	private void scanMusic() {
		initRelativeLayout.setVisibility(View.INVISIBLE);
		initButton.setVisibility(View.INVISIBLE);

		scaningRelativeLayout.setVisibility(View.VISIBLE);
		cancelButton.setVisibility(View.VISIBLE);

		finishRelativeLayout.setVisibility(View.INVISIBLE);
		finishButton.setVisibility(View.INVISIBLE);

		isScan = true;

		AniUtil.startAnimation(aniLoading);

		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {

				handler.sendEmptyMessage(-1);
			}

			@Override
			protected Object doInBackground() throws Exception {
				Thread.sleep(1000);
				scannerMusic();
				if (!cancelScan)
					Thread.sleep(1000);
				return null;
			}
		}.execute();

	}

	/**
	 * 
	 * @param Path
	 *            搜索目录
	 * @param Extension
	 *            扩展名
	 * @param IsIterative
	 *            是否进入子文件夹
	 */
	public void scannerLocalMP3File(String Path, String Extension,
			boolean IsIterative) {
		File[] files = new File(Path).listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File f = files[i];

				if (f.isFile()) {
					if (f.getPath().endsWith(Extension)) // 判断扩展名
					{
						if (cancelScan)
							return;

						if (!f.exists()) {
							continue;
						}

						// 文件名
						String displayName = f.getName();
						if (displayName.endsWith(Extension)) {
							String[] displayNameArr = displayName
									.split(Extension);
							displayName = displayNameArr[0].trim();
						}

						boolean isExists = SongDB.getSongInfoDB(this)
								.songIsExists(displayName);
						if (isExists) {
							continue;
						}
						// 将扫描到的数据保存到播放列表
						Mp3Info mp3Info = MediaUtils.getMp3InfoByFile(f
								.getPath());
						if (mp3Info != null) {
							addMusicList(mp3Info);
							size++;
							//
							// Message msg = new Message();
							// msg.what = 0;
							// msg.obj = f.getPath();
							// handler.sendMessage(msg);

						} else {
							continue;
						}
					}

					if (!IsIterative)
						break;
				} else if (f.isDirectory() && f.getPath().indexOf("/.") == -1) // 忽略点文件（隐藏文件/文件夹）
				{
					Message msg = new Message();
					msg.what = 0;
					msg.obj = f.getPath();
					handler.sendMessage(msg);

					scannerLocalMP3File(f.getPath(), Extension, IsIterative);
				}
			}
		}
	}

	private void scannerMusic() {
		size = 0;
		// scannerLocalMP3File(
		// Environment.getExternalStorageDirectory().getPath(), ".mp3",
		// true);

		// 查询媒体数据库
		Cursor cursor = MediaUtils.getMediaCursor(this);

		for (int i = 0; i < cursor.getCount(); i++) {

			if (cancelScan)
				break;
			Mp3Info mp3Info = MediaUtils.getMp3InfoByCursor(cursor);
			if (mp3Info == null) {
				continue;
			}
			File file = new File(mp3Info.getPath());
			if (!file.exists()) {
				continue;
			}

			boolean isExists = SongDB.getSongInfoDB(this).songIsExists(
					mp3Info.getDisplayName());
			if (isExists) {
				continue;
			}

			// 将扫描到的数据保存到播放列表
			addMusicList(mp3Info);
			//
			size++;

			Message msg = new Message();
			msg.what = 0;
			msg.obj = file.getPath();
			handler.sendMessage(msg);

		}
		cursor.close();

		isScan = false;

	}

	/**
	 * 添加歌曲到本地播放列表
	 * 
	 * @param mp3Info
	 */
	private void addMusicList(Mp3Info mp3Info) {

		SongInfo songInfo = new SongInfo();

		songInfo.setId(mp3Info.getId());
		songInfo.setTitle(mp3Info.getTitle());
		songInfo.setAlbum(mp3Info.getAlbum());
		songInfo.setAlbumId(mp3Info.getAlbumId());
		songInfo.setDisplayName(mp3Info.getDisplayName());
		songInfo.setArtist(mp3Info.getArtist());
		songInfo.setDuration(mp3Info.getDuration());
		songInfo.setSize(mp3Info.getSize());
		songInfo.setPath(mp3Info.getPath());
		songInfo.setType(SongInfo.LOCAL);
		songInfo.setAlbumUrl("");
		songInfo.setDownUrl("");
		songInfo.setDownSize(0);
		songInfo.setPlayProgress(0);
		songInfo.setValid(SongInfo.VALID);

		SongDB.getSongInfoDB(this).add(songInfo);
	}

	public void back(View v) {
		if (isScan)
			cancel();
		finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (isScan)
				cancel();
			finish();
		}
		return false;
	}
}
