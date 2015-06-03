package com.happyplayer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v23Frame;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.common.Constants;
import com.happyplayer.widget.CircleImageView;

public class ImageUtil {
	// 软引用内存缓存
	public static Map<String, SoftReference<Bitmap>> sImageCache = new HashMap<String, SoftReference<Bitmap>>();

	/**
	 * 加载资源图片
	 * 
	 * @param context
	 * @param imageview
	 * @param resourceID
	 * @param defResourceID
	 */
	public static void loadResourceImage(final Context context,
			final ImageView imageview, final int resourceID,
			final int defResourceID) {
		imageview.setBackgroundResource(defResourceID);
		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {
				Bitmap bm = (Bitmap) result;
				if (bm == null) {
					imageview.setBackgroundResource(defResourceID);
				} else {
					imageview.setBackgroundDrawable(new BitmapDrawable(bm));
				}
			}

			@Override
			protected Object doInBackground() throws Exception {
				return ImageUtil.readBitmap(context, resourceID);
			}
		}.execute();
	}

	/**
	 * 通过资源id获取资源图片
	 * 
	 * @param context
	 * @param id
	 *            资源id
	 * @return
	 */
	public static Bitmap readBitmap(Context context, int id) {
		Bitmap bm = null;
		if (sImageCache.containsKey(id + "")) {
			bm = sImageCache.get(id + "").get();
			if (bm == null) {
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inPreferredConfig = Bitmap.Config.RGB_565;// 表示16位位图
																// 565代表对应三原色占的位数
				opt.inInputShareable = true;
				opt.inPurgeable = true;// 设置图片可以被回收
				InputStream is = context.getResources().openRawResource(id);
				bm = BitmapFactory.decodeStream(is, null, opt);
				sImageCache.put(id + "", new SoftReference<Bitmap>(bm));
			}
		} else {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Bitmap.Config.RGB_565;// 表示16位位图
															// 565代表对应三原色占的位数
			opt.inInputShareable = true;
			opt.inPurgeable = true;// 设置图片可以被回收
			InputStream is = context.getResources().openRawResource(id);
			bm = BitmapFactory.decodeStream(is, null, opt);
			sImageCache.put(id + "", new SoftReference<Bitmap>(bm));
		}
		return bm;
	}

	/**
	 * 加载圆形专辑图片
	 * 
	 * @param context
	 * @param imageview
	 * @param defResourceID
	 * @param filePath
	 * @param fileSid
	 * @param url
	 */
	public static void loadCircleAlbum(final Context context,
			final CircleImageView imageview, final int defResourceID,
			final String filePath, final String fileSid, final String url) {
		final String fileName = Constants.PATH_ALBUM + File.separator + fileSid
				+ ".jpg";
		imageview.setImageResource(defResourceID);
		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {
				final Bitmap bm = (Bitmap) result;
				if (bm == null) {
					imageview.setImageResource(defResourceID);
				} else {
					new Thread() {

						@Override
						public void run() {
							saveImage(bm, fileName);
						}
					}.start();

					imageview.setImageDrawable(new BitmapDrawable(bm));
				}
			}

			@Override
			protected Object doInBackground() throws Exception {
				return getAlbum(context, filePath, fileSid, url, fileName);
			}
		}.execute();
	}

	/**
	 * 加载专辑图片
	 * 
	 * @param imageview
	 * @param defResourceID
	 * @param filePath
	 * @param fileSid
	 * @param url
	 */
	public static void loadAlbum(final Context context,
			final ImageView imageview, final int defResourceID,
			final String filePath, final String fileSid, final String url) {
		final String fileName = Constants.PATH_ALBUM + File.separator + fileSid
				+ ".jpg";
		imageview.setBackgroundResource(defResourceID);
		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {
				final Bitmap bm = (Bitmap) result;
				if (bm == null) {
					imageview.setBackgroundResource(defResourceID);
				} else {
					new Thread() {

						@Override
						public void run() {
							saveImage(bm, fileName);
						}
					}.start();

					imageview.setBackgroundDrawable(new BitmapDrawable(bm));
				}
			}

			@Override
			protected Object doInBackground() throws Exception {
				return getAlbum(context, filePath, fileSid, url, fileName);
			}
		}.execute();
	}

	/**
	 * 获取图片数据
	 * 
	 * @param filePath
	 * @param fileSid
	 * @param url
	 * @return
	 */
	public static Bitmap getAlbum(Context context, String filePath,
			String fileSid, String url, String fileName) {
		if (fileName == null || fileName.equals("")) {
			fileName = Constants.PATH_ALBUM + File.separator + fileSid + ".jpg";
		}
		Bitmap bm = getFirstArtwork(context, filePath, fileSid, fileName);
		if (bm != null) {
			return bm;
		}
		if (url != null && !url.equals("")) {
			bm = getBitmap(url);
			if (bm != null) {
				sImageCache.put(filePath, new SoftReference<Bitmap>(bm));
			}
		}
		return bm;
	}

	/**
	 * 根据歌曲文件获取相关的专辑图片
	 * 
	 * @param filePath
	 *            文件路径
	 * @return
	 */
	private static Bitmap getFirstArtwork(Context context, String filePath,
			String fileSid, String fileName) {

		Bitmap bm = null;
		if (sImageCache.containsKey(filePath)) {
			bm = sImageCache.get(filePath).get();
			if (bm == null) {
				bm = loadFirstArtwork(filePath, fileName, context);
			} else {
				return bm;
			}
		} else {
			bm = loadFirstArtwork(filePath, fileName, context);
		}
		return bm;
	}

	/**
	 * 加载图片
	 * 
	 * @param filePath
	 *            文件路径
	 * @return
	 */
	private static Bitmap loadFirstArtwork(String filePath, String fileName,
			Context context) {
		File artworkFile = new File(fileName);
		Bitmap bm = null;
		if (artworkFile.exists()) {
			bm = getImageFormFile(filePath, context);
			if (bm != null) {
				sImageCache.put(filePath, new SoftReference<Bitmap>(bm));
				return bm;
			}
			bm = getArtworkFormFile(filePath, fileName);
		} else {
			bm = getArtworkFormFile(filePath, fileName);
		}
		return bm;
	}

	/**
	 * 保存图片到本地
	 * 
	 * @param bm
	 * @param fileName
	 */
	private static void saveImage(Bitmap bm, String fileName) {
		if (bm == null) {
			return;
		}
		try {
			// 你要存放的文件
			File file = new File(fileName);
			// file文件的上一层文件夹
			File parentFile = new File(file.getParent());
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}

			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStream outStream = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.flush();
			outStream.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * 从mp3文件获取专辑图片
	 * 
	 * @param filePath
	 * @return
	 */
	private static Bitmap getArtworkFormFile(String filePath, String fileName) {
		File sourceFile = new File(filePath);
		if (!sourceFile.exists())
			return null;
		Bitmap bm = null;
		try {
			AudioFileIO.logger.setLevel(Level.SEVERE);
			ID3v23Frame.logger.setLevel(Level.SEVERE);
			ID3v23Tag.logger.setLevel(Level.SEVERE);
			MP3File mp3file = new MP3File(sourceFile);
			if (mp3file.hasID3v2Tag()) {
				AbstractID3v2Tag tag = mp3file.getID3v2Tag();
				AbstractID3v2Frame frame = (AbstractID3v2Frame) tag
						.getFrame("APIC");
				if (frame != null) {
					FrameBodyAPIC body = (FrameBodyAPIC) frame.getBody();
					if (body != null) {
						byte[] imageData = body.getImageData();
						// 通过BitmapFactory转成Bitmap
						bm = BitmapFactory.decodeByteArray(imageData, 0,
								imageData.length);
						sImageCache
								.put(filePath, new SoftReference<Bitmap>(bm));
						return bm;
					} else {
						return null;
					}

				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 从文件中获取图片
	 */
	private static Bitmap getImageFormFile(String filePath, Context context) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opts);

		/** 这里是获取手机屏幕的分辨率用来处理 图片 溢出问题的。begin */
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		int displaypixels = dm.widthPixels * dm.heightPixels;

		opts.inSampleSize = computeSampleSize(opts, -1, displaypixels);
		opts.inJustDecodeBounds = false;
		try {
			return BitmapFactory.decodeFile(filePath, opts);
		} catch (OutOfMemoryError err) {
			err.printStackTrace();
		}
		return null;
	}

	private static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	/**
	 * 根据一个网络连接(URL)获取bitmap图像
	 * 
	 * @param imageUri
	 * @return
	 */
	public static Bitmap getBitmap(URL imageUri) {
		// 显示网络上的图片
		URL myFileUrl = imageUri;
		Bitmap bitmap = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * 根据一个网络连接(String)获取bitmap图像
	 * 
	 * @param imageUri
	 * @return
	 * @throws MalformedURLException
	 */
	public static Bitmap getBitmap(String imageUri) {
		// 显示网络上的图片
		Bitmap bitmap = null;
		try {
			URL myFileUrl = new URL(imageUri);
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}

	/**
	 * 加载图片
	 * 
	 * @param context
	 * @param imageview
	 * @param defResourceID
	 *            默认的图片id
	 * @param fileParentPath
	 *            图片保存的文件夹
	 * @param url
	 */
	public static void loadImage(final Context context, final View imageview,
			final int defResourceID, final String fileParentPath,
			final String url) {
		final String filePath = fileParentPath + File.separator
				+ url.hashCode() + ".jpg";
		imageview.setBackgroundResource(defResourceID);
		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {
				Bitmap bm = (Bitmap) result;
				if (bm == null) {
					imageview.setBackgroundResource(defResourceID);
				} else {
					imageview.setBackgroundDrawable(new BitmapDrawable(bm));
				}
			}

			@Override
			protected Object doInBackground() throws Exception {
				return ImageUtil.loadImage(context, filePath, url);
			}
		}.execute();
	}

	/**
	 * 加载本地图片
	 * 
	 * @param context
	 * @param imageview
	 * @param defResourceID
	 * @param filePath
	 */
	public static void loadLocalImage(final Context context,
			final View imageview, final int defResourceID, final String filePath) {
		imageview.setBackgroundResource(defResourceID);
		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {
				Bitmap bm = (Bitmap) result;
				if (bm == null) {
					imageview.setBackgroundResource(defResourceID);
				} else {
					imageview.setBackgroundDrawable(new BitmapDrawable(bm));
				}
			}

			@Override
			protected Object doInBackground() throws Exception {
				return ImageUtil.loadLocalImage(context, filePath);
			}
		}.execute();
	}

	/**
	 * 
	 * @param context
	 * @param filePath
	 * @return
	 */
	private static Bitmap loadLocalImage(Context context, String filePath) {
		// 判断内存中是否存在图片
		Bitmap bitmap = null;
		if (sImageCache.containsKey(filePath)) {
			bitmap = sImageCache.get(filePath).get();
		}
		if (bitmap == null) {
			// 判断内存卡里面是否存在图片
			bitmap = getImageFormFile(filePath, context);
		}

		if (bitmap != null) {
			sImageCache.put(filePath, new SoftReference<Bitmap>(bitmap));
		}
		return bitmap;
	}

	/**
	 * 加载图片
	 * 
	 * @param context
	 * @param filePath
	 *            图片路径
	 * @param url
	 *            图片下载路径
	 * @return
	 */
	private static Bitmap loadImage(Context context, String filePath, String url) {
		// 判断内存中是否存在图片
		Bitmap bitmap = null;
		if (sImageCache.containsKey(url)) {
			bitmap = sImageCache.get(url).get();
		}
		if (bitmap == null) {
			// 判断内存卡里面是否存在图片
			bitmap = getImageFormFile(filePath, context);
		}
		if (bitmap == null) {
			// 网络下载图片
			bitmap = getBitmap(url);
			// 保存图片到内存卡和内存
			if (bitmap != null) {
				saveImage(bitmap, filePath);
			}
		}
		if (bitmap != null) {
			sImageCache.put(url, new SoftReference<Bitmap>(bitmap));
		}
		return bitmap;
	}
}
