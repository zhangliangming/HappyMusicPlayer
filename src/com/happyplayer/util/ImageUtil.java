package com.happyplayer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
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

import com.happyplayer.common.Constants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtil {
	// 软引用内存缓存
	public static Map<String, SoftReference<Bitmap>> sImageCache = new HashMap<String, SoftReference<Bitmap>>();

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
	 * 根据歌曲文件获取相关的专辑图片
	 * 
	 * @param filePath
	 *            文件路径
	 * @return
	 */
	public static Bitmap getFirstArtwork(String filePath, String fileSid) {
		String fileName = Constants.PATH_ALBUM + File.separator + fileSid
				+ ".jpg";
		Bitmap bm = null;
		if (sImageCache.containsKey(filePath)) {
			bm = sImageCache.get(filePath).get();
			if (bm == null) {
				bm = loadFirstArtwork(filePath, fileName);
			} else {
				return bm;
			}
		} else {
			bm = loadFirstArtwork(filePath, fileName);
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
	private static Bitmap loadFirstArtwork(String filePath, String fileName) {
		File artworkFile = new File(fileName);
		Bitmap bm = null;
		if (artworkFile.exists()) {
			bm = getImageFormFile(filePath);
			if (bm == null) {
				bm = getArtworkFormFile(filePath, fileName);
			} else {
				sImageCache.put(filePath, new SoftReference<Bitmap>(bm));
				saveImage(bm, fileName);
				return bm;
			}
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
	public static void saveImage(Bitmap bm, String fileName) {
		if (bm == null) {
			return;
		}
		try {
			// 两层文件夹建立方法
			// 你要存放的文件
			File file = new File(fileName);
			// file文件的上一层文件夹
			File parentFile = new File(Constants.PATH_ALBUM);
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
	public static Bitmap getArtworkFormFile(String filePath, String fileName) {
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
						saveImage(bm, fileName);
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
	public static Bitmap getImageFormFile(String filePath) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opts);
		// 压缩到640x640
		opts.inSampleSize = computeSampleSize(opts, -1, 640 * 640);
		opts.inJustDecodeBounds = false;
		try {
			return BitmapFactory.decodeFile(filePath, opts);
		} catch (OutOfMemoryError err) {
			err.printStackTrace();
		}
		return null;
	}

	public static int computeSampleSize(BitmapFactory.Options options,
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

	public static int computeInitialSampleSize(BitmapFactory.Options options,
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
}
