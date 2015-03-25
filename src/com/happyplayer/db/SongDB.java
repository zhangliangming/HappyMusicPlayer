package com.happyplayer.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.happyplayer.model.SongInfo;
import com.happyplayer.model.SongMessage;
import com.happyplayer.observable.ObserverManage;
import com.happyplayer.util.MediaUtils;
import com.happyplayer.util.PingYinUtil;

public class SongDB {
	/**
	 * 表名
	 */
	public static final String TBL_NAME = "playlistTbl";

	/**
	 * 建表语句
	 */
	public static final String CREATE_TBL = "create table " + TBL_NAME + "("
			+ "sid text," + "id int," + "title text," + "album text,"
			+ "albumId long," + "displayName text," + "artist text,"
			+ "duration long," + "size long," + "sizeStr text," + "path text,"
			+ "type int," + "albumUrl text," + "downUrl text,"
			+ "downSize long," + "playProgress long," + "category text,"
			+ "valid int," + "createTime text," + "childCategory text" + ")";

	private static SongDB _SongInfoDB;

	private SQLiteDatabase db;

	private SQLDBHlper mDBHlper;

	public SongDB(Context context) {
		mDBHlper = SQLDBHlper.getSQLDBHlper(context);
	}

	public static SongDB getSongInfoDB(Context context) {
		if (_SongInfoDB == null) {
			_SongInfoDB = new SongDB(context);
		}
		return _SongInfoDB;
	}

	/**
	 * 添加歌曲到本地播放列表
	 * 
	 * @param songInfo
	 */
	public void add(SongInfo songInfo) {
		ContentValues values = new ContentValues();
		songInfo.setSid(getSID());
		values.put("sid", songInfo.getSid());
		values.put("id", songInfo.getId());
		values.put("title", songInfo.getTitle());
		values.put("album", songInfo.getAlbum());
		values.put("albumId", songInfo.getAlbumId());
		values.put("displayName", songInfo.getDisplayName());
		values.put("artist", songInfo.getArtist());
		values.put("duration", songInfo.getDuration());
		values.put("size", songInfo.getSize());
		songInfo.setSizeStr(MediaUtils.getFileSize(songInfo.getSize()));
		values.put("sizeStr", songInfo.getSizeStr());
		values.put("path", songInfo.getPath());
		songInfo.setCreateTime(getCreateTime());
		values.put("createTime", songInfo.getCreateTime());
		values.put("type", songInfo.getType());
		values.put("albumUrl", songInfo.getAlbumUrl());
		values.put("downUrl", songInfo.getDownUrl());
		values.put("downSize", songInfo.getDownSize());
		values.put("playProgress", songInfo.getPlayProgress());
		String category = PingYinUtil.getPingYin(songInfo.getDisplayName())
				.toUpperCase();
		char cat = category.charAt(0);
		if (cat <= 'Z' && cat >= 'A') {
			values.put("category", cat + "");
			songInfo.setCategory(cat + "");
			values.put("childCategory", category);
			songInfo.setChildCategory(category);
		} else {
			values.put("category", "^");
			songInfo.setCategory("^");
			values.put("childCategory", category);
			songInfo.setChildCategory(category);
		}
		values.put("valid", songInfo.getValid());

		insert(values, songInfo);
	}

	/**
	 * 获取创建时间
	 * 
	 * @return
	 */
	private String getCreateTime() {
		String time = new Date().getTime() + "";
		return time;
	}

	/**
	 * 获取单一的id
	 * 
	 * @return
	 */
	private String getSID() {
		String sid = new Date().getTime() + "_" + new Date().getTime();
		return sid;
	}

	/**
	 * 插入ContentValues
	 */
	private void insert(ContentValues values, SongInfo songInfo) {
		db = mDBHlper.getWritableDatabase();
		try {
			db.insert(TBL_NAME, null, values);
			SongMessage songMessage = new SongMessage();
			songMessage.setSongInfo(songInfo);
			songMessage.setType(SongMessage.ADDMUSIC);
			// 通知
			ObserverManage.getObserver().setMessage(songMessage);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取相关的Cursor
	 */
	public Cursor query() {
		db = mDBHlper.getReadableDatabase();
		Cursor c = db.query(TBL_NAME, null, null, null, null, null,
				"category asc , childCategory asc");
		return c;
	}

	/**
	 * 获取歌曲列表
	 * 
	 * @return
	 */
	public List<SongInfo> getAllSong() {
		List<SongInfo> list = new ArrayList<SongInfo>();
		Cursor cursor = query();
		while (cursor.moveToNext()) {
			SongInfo songInfo = getSongInfo(cursor);
			File file = new File(songInfo.getPath());
			if (!file.exists()) {
				delete(songInfo.getSid());
			} else {
				list.add(songInfo);
			}
		}
		cursor.close();
		return list;
	}

	/**
	 * 获取所有的分类
	 * 
	 * @return
	 */
	public List<String> getAllCategory() {
		List<String> list = new ArrayList<String>();
		db = mDBHlper.getReadableDatabase();
		Cursor cursor = db.query(true, TBL_NAME, new String[] { "category" },
				null, null, null, null, "category asc , childCategory asc",
				null);
		while (cursor.moveToNext()) {
			list.add(cursor.getString(cursor.getColumnIndex("category")));
		}
		cursor.close();
		String baseCategory = "^";
		if (!list.contains(baseCategory)) {
			list.add(baseCategory);
		}
		return list;
	}

	/**
	 * 获取所有分类的歌曲列表
	 * 
	 * @param category
	 * @return
	 */
	public List<SongInfo> getAllCategorySong(String category) {
		List<SongInfo> list = new ArrayList<SongInfo>();
		db = mDBHlper.getReadableDatabase();
		Cursor cursor = db.query(TBL_NAME, null, "category= ?",
				new String[] { category }, null, null, "childCategory asc",
				null);
		while (cursor.moveToNext()) {
			SongInfo songInfo = getSongInfo(cursor);
			File file = new File(songInfo.getPath());
			if (!file.exists()) {
				delete(songInfo.getSid());
			} else {
				list.add(songInfo);
			}
		}
		cursor.close();
		return list;
	}

	/**
	 * 获取歌曲总数
	 * 
	 * @return
	 */
	// public int getCount() {
	// db = mDBHlper.getReadableDatabase();
	// Cursor cursor = db.rawQuery("select count(*)from " + TBL_NAME, null);
	// cursor.moveToFirst();
	// int count = cursor.getInt(0);
	// cursor.close();
	// return count;
	// }

	/**
	 * 通过Cursor来提取相关的SongInfo数据
	 * 
	 * @param c
	 * @return
	 */
	private SongInfo getSongInfo(Cursor cursor) {
		SongInfo song = new SongInfo();

		song.setSid(cursor.getString(cursor.getColumnIndex("sid")));
		song.setId(cursor.getInt(cursor.getColumnIndex("id")));
		song.setTitle(cursor.getString(cursor.getColumnIndex("title")));
		song.setAlbum(cursor.getString(cursor.getColumnIndex("album")));
		song.setAlbumId(cursor.getLong(cursor.getColumnIndex("albumId")));
		song.setDisplayName(cursor.getString(cursor
				.getColumnIndex("displayName")));
		song.setArtist(cursor.getString(cursor.getColumnIndex("artist")));
		song.setDuration(cursor.getInt(cursor.getColumnIndex("duration")));
		song.setSize(cursor.getLong(cursor.getColumnIndex("size")));
		song.setSizeStr(cursor.getString(cursor.getColumnIndex("sizeStr")));
		song.setPath(cursor.getString(cursor.getColumnIndex("path")));
		song.setCreateTime(cursor.getString(cursor.getColumnIndex("createTime")));
		song.setType(cursor.getInt(cursor.getColumnIndex("type")));
		song.setAlbumUrl(cursor.getString(cursor.getColumnIndex("albumUrl")));
		song.setDownUrl(cursor.getString(cursor.getColumnIndex("downUrl")));
		song.setDownSize(cursor.getLong(cursor.getColumnIndex("downSize")));
		song.setPlayProgress(cursor.getLong(cursor
				.getColumnIndex("playProgress")));
		song.setCategory(cursor.getString(cursor.getColumnIndex("category")));
		song.setChildCategory(cursor.getString(cursor
				.getColumnIndex("childCategory")));
		song.setValid(cursor.getInt(cursor.getColumnIndex("valid")));

		return song;
	}

	/**
	 * 通过sid来获取歌曲的相关信息
	 * 
	 * @param sid
	 * @return
	 */
	public SongInfo getSongInfo(String sid) {
		db = mDBHlper.getReadableDatabase();
		// 第一个参数String：表名
		// 第二个参数String[]:要查询的列名
		// 第三个参数String：查询条件
		// 第四个参数String[]：查询条件的参数
		// 第五个参数String:对查询的结果进行分组
		// 第六个参数String：对分组的结果进行限制
		// 第七个参数String：对查询的结果进行排序
		Cursor cursor = db.rawQuery("select * from " + TBL_NAME
				+ " where sid=?", new String[] { sid + "" });
		if (!cursor.moveToNext()) {
			return null;
		}
		SongInfo song = getSongInfo(cursor);
		cursor.close();
		return song;
	}

	/**
	 * 删除sid的相关数据
	 */
	public void delete(String sid) {
		db = mDBHlper.getWritableDatabase();
		try {
			db.delete(TBL_NAME, "sid=?", new String[] { sid });

			SongMessage songMessage = new SongMessage();
			SongInfo songInfo = new SongInfo();
			songInfo.setSid(sid);
			songMessage.setSongInfo(songInfo);
			songMessage.setType(SongMessage.DELMUSIC);
			// 通知
			ObserverManage.getObserver().setMessage(songMessage);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除所有的数据
	 */
	public void delete() {
		db = mDBHlper.getWritableDatabase();
		try {
			db.execSQL("drop table if exists " + TBL_NAME);
			db.execSQL(CREATE_TBL);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 通过文件路径来判断其是否在数据库中
	 * 
	 * @param path
	 * @return
	 */
	public boolean songIsExists(String fileName) {
		db = mDBHlper.getReadableDatabase();
		// 第一个参数String：表名
		// 第二个参数String[]:要查询的列名
		// 第三个参数String：查询条件
		// 第四个参数String[]：查询条件的参数
		// 第五个参数String:对查询的结果进行分组
		// 第六个参数String：对分组的结果进行限制
		// 第七个参数String：对查询的结果进行排序
		Cursor cursor = db.query(TBL_NAME, new String[] { "displayName" },
				" displayName=?", new String[] { fileName }, null, null, null);
		if (!cursor.moveToNext()) {
			cursor.close();
			return false;
		}
		cursor.close();
		return true;
	}

	/**
	 * 更新本地歌曲列表的歌曲状态是否是有效文件
	 * 
	 * @param sID
	 * @param valid
	 *            是否有效
	 */
	public void updateSongPlaying(int sID, int valid) {
		db = mDBHlper.getReadableDatabase();
		ContentValues values = new ContentValues();

		values.put("valid", valid);
		try {
			db.update(TBL_NAME, values, "sid=?", new String[] { sID + "" });
		} catch (SQLException e) {
			Log.i("error", "update failed");
		}
	}
}
