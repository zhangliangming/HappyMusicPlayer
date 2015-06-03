package com.happyplayer.json;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;

import com.happyplayer.common.Constants;
import com.happyplayer.model.SkinMessage;
import com.happyplayer.util.HttpUtil;

public class ArtistAlbumJson {
	/**
	 * 获取该歌手的图片路径
	 * 
	 * @param artist
	 *            歌手名
	 * @param context
	 * @param num
	 *            获取的张数
	 * @return
	 */
	public static List<SkinMessage> getArtistAlbum(String artist, Context context,
			int num) {
		List<SkinMessage> msgList = new ArrayList<SkinMessage>();
		String responseText = HttpUtil.getArtistAlbum(artist);
		JSONObject jsonb;
		try {
			jsonb = new JSONObject(responseText);
			if (jsonb.has("flag")) {
				int flag = jsonb.getInt("flag");
				// 200获取数据成功 201没有相关的数据 202获取数据时出错 404服务器繁忙 其它服务器异常
				switch (flag) {
				case 200:
					if (jsonb.has("comment")) {
						String json = jsonb.getString("comment");
						JSONObject jsonComment = new JSONObject(json);
						if (jsonComment.has("list")) {
							JSONArray listArray = jsonComment
									.getJSONArray("list");
							int length = 0;
							if (listArray.length() < num) {
								length = listArray.length();
							} else {
								length = num;
							}
							for (int i = 0; i < length; i++) {
								JSONObject jSONObject = listArray
										.getJSONObject(i);
								if (jSONObject.has("thumb_bak")) {
									String thumb_bak = jSONObject
											.getString("thumb_bak");
									SkinMessage skinMessage = new SkinMessage();
									skinMessage.setUrl(thumb_bak);
									skinMessage.type = SkinMessage.ART;
									skinMessage.setParentPath(Constants.PATH_ARTIST + File.separator + artist);
									msgList.add(skinMessage);
								}
							}
						}
					}
					break;

				case 404:
					Toast.makeText(context, "获取歌手图片出错!!", Toast.LENGTH_SHORT)
							.show();
					break;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return msgList;
	}
}
