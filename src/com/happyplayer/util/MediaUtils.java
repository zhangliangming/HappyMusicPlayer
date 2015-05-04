package com.happyplayer.util;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.id3.ID3v23Frame;
import org.jaudiotagger.tag.id3.ID3v23Tag;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.happyplayer.model.Mp3Info;
import com.happyplayer.ui.R;

public class MediaUtils {
	// 获取专辑封面的Uri
	private static final Uri albumArtUri = Uri
			.parse("content://media/external/audio/albumart");

	/**
	 * 获取媒体的游标
	 * 
	 * @return Cursor
	 */
	public static Cursor getMediaCursor(Context context) {
		Cursor cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		return cursor;
	}

	/**
	 * 从数据库中查询歌曲信息保存在List中
	 */
	public static List<Mp3Info> getMp3Infos(Context context) {
		Cursor cursor = getMediaCursor(context);
		List<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
		for (int i = 0; i < cursor.getCount(); i++) {
			Mp3Info mp3Info = getMp3InfoByCursor(cursor);
			mp3Infos.add(mp3Info);
		}
		cursor.close();
		return mp3Infos;
	}

	/**
	 * 通过文件获取mp3的相关数据信息
	 * 
	 * @param filePath
	 * @return
	 */

	public static Mp3Info getMp3InfoByFile(String filePath) {
		File sourceFile = new File(filePath);
		if (!sourceFile.exists())
			return null;
		Mp3Info mp3Info = null;
		try {
			AudioFileIO.logger.setLevel(Level.SEVERE);
			ID3v23Frame.logger.setLevel(Level.SEVERE);
			ID3v23Tag.logger.setLevel(Level.SEVERE);
			MP3File mp3file = new MP3File(sourceFile);
			MP3AudioHeader header = mp3file.getMP3AudioHeader();
			if (header == null)
				return null;
			mp3Info = new Mp3Info();
			// 歌曲时长
			long duration = getTrackLength(header.getTrackLengthAsString());
			// 文件名
			String displayName = sourceFile.getName();
			if (displayName.contains(".mp3")) {
				String[] displayNameArr = displayName.split(".mp3");
				displayName = displayNameArr[0].trim();
			}
			String artist = "";
			String title = "";
			String album = "";
			if (displayName.contains("-")) {
				String[] titleArr = displayName.split("-");
				artist = titleArr[0].trim();
				title = titleArr[1].trim();
			} else {
				title = displayName;
			}
			mp3Info.setId(0);
			mp3Info.setTitle(title);
			mp3Info.setArtist(artist);
			mp3Info.setDuration(duration);
			mp3Info.setDisplayName(displayName);
			mp3Info.setSize(sourceFile.length());
			mp3Info.setPath(filePath);
			mp3Info.setAlbumId(0);
			mp3Info.setAlbum(album);

			mp3file = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mp3Info;

	}

	/**
	 * 获取歌曲长度
	 * 
	 * @param trackLengthAsString
	 * @return
	 */
	private static long getTrackLength(String trackLengthAsString) {

		if (trackLengthAsString.contains(":")) {
			String temp[] = trackLengthAsString.split(":");
			if (temp.length == 2) {
				int m = Integer.parseInt(temp[0]);// 分
				int s = Integer.parseInt(temp[1]);// 秒
				int currTime = (m * 60 + s) * 1000;
				return currTime;
			}
		}
		return 0;
	}

	/**
	 * 通过游标获取Mp3Info
	 */
	public static Mp3Info getMp3InfoByCursor(Cursor cursor) {
		cursor.moveToNext();

		int isMusic = cursor.getInt(cursor
				.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
		if (isMusic == 0)
			return null;

		String url = cursor.getString(cursor
				.getColumnIndex(MediaStore.Audio.Media.DATA));
		if (url.startsWith(".")) {
			return null;
		}
		String extension = ".mp3";
		if (!url.substring(url.length() - extension.length()).equals(extension)) {
			return null;
		}

		// Mp3Info mp3Info = getMp3InfoByFile(url);
		Mp3Info mp3Info = new Mp3Info();
		String artist = "";
		String title = "";

		long id = cursor.getLong(cursor
				.getColumnIndex(MediaStore.Audio.Media._ID));
		String tmpTitle = cursor.getString(cursor
				.getColumnIndex(MediaStore.Audio.Media.TITLE));
		if (tmpTitle.contains("-")) {
			String[] titleArr = tmpTitle.split("-");
			artist = titleArr[0].trim();
			title = titleArr[1].trim();
		} else {
			title = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.TITLE));
			artist = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.ARTIST));
		}
		if (title.equals("<unknown>")) {
			title = tmpTitle;
		}
		if (artist.equals("<unknown>")) {
			artist = "";
		}

		String displayName = cursor.getString(cursor
				.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));

		if (displayName.contains(".mp3")) {
			String[] displayNameArr = displayName.split(".mp3");
			displayName = displayNameArr[0].trim();
		}

		long duration = cursor.getLong(cursor
				.getColumnIndex(MediaStore.Audio.Media.DURATION));
		long size = cursor.getLong(cursor
				.getColumnIndex(MediaStore.Audio.Media.SIZE));
		String album = cursor.getString(cursor
				.getColumnIndex(MediaStore.Audio.Media.ALBUM)); // 专辑

		long albumid = cursor.getLong(cursor
				.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

		if (size < 1024 * 1024) {
			return null;
		}

		mp3Info.setId(id);
		mp3Info.setTitle(title);
		mp3Info.setArtist(artist);
		mp3Info.setDuration(duration);
		mp3Info.setDisplayName(displayName);
		mp3Info.setSize(size);
		mp3Info.setPath(url);
		mp3Info.setAlbumId(albumid);
		mp3Info.setAlbum(album);
		return mp3Info;
	}

	/**
	 * 时间格式转换
	 * 
	 * @param time
	 * @return
	 */
	public static String formatTime(int time) {

		time /= 1000;
		int minute = time / 60;
		// int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

	/**
	 * 计算文件的大小，返回相关的m字符串
	 * 
	 * @param fileS
	 * @return
	 */
	public static String getFileSize(long fileS) {// 转换文件大小
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 获取默认专辑图片
	 * 
	 * @param context
	 * @return
	 */
	public static Bitmap getDefaultArtwork(Context context, boolean small) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		if (small) { // 返回小图片
			return BitmapFactory.decodeStream(context.getResources()
					.openRawResource(R.drawable.playing_bar_default_avatar),
					null, opts);
		}
		return BitmapFactory.decodeStream(context.getResources()
				.openRawResource(R.drawable.playing_bar_default_avatar), null,
				opts);
	}

	/**
	 * 从文件当中获取专辑封面位图
	 * 
	 * @param context
	 * @param songid
	 * @param albumid
	 * @return
	 */
	private static Bitmap getArtworkFromFile(Context context, long songid,
			long albumid) {
		Bitmap bm = null;
		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException(
					"Must specify an album or a song id");
		}
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			FileDescriptor fd = null;
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/"
						+ songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			} else {
				Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			}
			options.inSampleSize = 1;
			// 只进行大小判断
			options.inJustDecodeBounds = true;
			// 调用此方法得到options得到图片大小
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			// 我们的目标是在800pixel的画面上显示
			// 所以需要调用computeSampleSize得到图片缩放的比例
			options.inSampleSize = 2;
			// 我们得到了缩放的比例，现在开始正式读入Bitmap数据
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			// 根据options参数，减少所需要的内存
			bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bm;
	}

	/**
	 * 获取专辑封面位图对象
	 * 
	 * @param context
	 * @param song_id
	 * @param album_id
	 * @param allowdefalut
	 * @return
	 */
	public static Bitmap getArtwork(Context context, long song_id,
			long album_id, boolean allowdefalut, boolean small) {
		if (album_id < 0) {
			if (song_id >= 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					return bm;
				}
			}
			if (allowdefalut) {
				return getDefaultArtwork(context, small);
			}
			return null;
		}
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				BitmapFactory.Options options = new BitmapFactory.Options();
				// 先制定原始大小
				options.inSampleSize = 1;
				// 只进行大小判断
				options.inJustDecodeBounds = true;
				// 调用此方法得到options得到图片的大小
				BitmapFactory.decodeStream(in, null, options);
				/** 我们的目标是在你N pixel的画面上显示。 所以需要调用computeSampleSize得到图片缩放的比例 **/
				/** 这里的target为800是根据默认专辑图片大小决定的，800只是测试数字但是试验后发现完美的结合 **/
				if (small) {
					options.inSampleSize = computeSampleSize(options, 40);
				} else {
					options.inSampleSize = computeSampleSize(options, 600);
				}
				// 我们得到了缩放比例，现在开始正式读入Bitmap数据
				options.inJustDecodeBounds = false;
				options.inDither = false;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				in = res.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, options);
			} catch (FileNotFoundException e) {
				Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefalut) {
							return getDefaultArtwork(context, small);
						}
					}
				} else if (allowdefalut) {
					bm = getDefaultArtwork(context, small);
				}
				return bm;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 对图片进行合适的缩放
	 */
	public static int computeSampleSize(Options options, int target) {
		int w = options.outWidth;
		int h = options.outHeight;
		int candidateW = w / target;
		int candidateH = h / target;
		int candidate = Math.max(candidateW, candidateH);
		if (candidate == 0) {
			return 1;
		}
		if (candidate > 1) {
			if ((w > target) && (w / candidate) < target) {
				candidate -= 1;
			}
		}
		if (candidate > 1) {
			if ((h > target) && (h / candidate) < target) {
				candidate -= 1;
			}
		}
		return candidate;
	}
}
