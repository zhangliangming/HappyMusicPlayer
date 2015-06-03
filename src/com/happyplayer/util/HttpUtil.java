package com.happyplayer.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.happyplayer.logger.MyLogger;

public class HttpUtil {
	/**
	 * 设置请求超时20秒钟
	 */
	private static final int REQUEST_TIMEOUT = 20 * 1000;
	private static DefaultHttpClient httpclient = new DefaultHttpClient();
	/**
	 * 设置等待数据超时时间20秒钟
	 */
	private static final int SO_TIMEOUT = 20 * 1000;

	/**
	 * 
	 * @param artist
	 *            歌手名
	 * @return 歌手的相关图片列表
	 */
	public static String getArtistAlbum(String artist) {
		String url = "http://image.haosou.com/j?q=" + artist
				+ "&src=srp&query_tag=壁纸&sn=30&pn=30";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		String temp = getResponseText(url, params);
		return temp;
	}

	/**
	 * 获取http请求后发送过来的数据
	 * 
	 * @param url
	 *            请求地址
	 * @param params
	 *            传参数
	 * @return返回从地址发送过来的数据
	 */
	private static String getResponseText(String url, List<NameValuePair> params) {
		int flag = -1;
		String result = "{";
		HttpPost httpost = null;
		try {
			httpost = new HttpPost(url);
			httpost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			httpclient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, REQUEST_TIMEOUT);
			httpclient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);
			HttpResponse response = httpclient.execute(httpost);
			flag = response.getStatusLine().getStatusCode();
			if (flag == 200) {
				HttpEntity entity = response.getEntity();
				result = result + "\"comment\":"
						+ EntityUtils.toString(entity, HTTP.UTF_8) + ",";
			}
		} catch (IOException e) {
			e.printStackTrace();
			flag = 404;
			result = result + "\"comment\":" + e.toString() + ",";
		} finally {
			if (httpost != null) {
				httpost.abort();
			}
		}
		return result + "\"flag\":" + flag + "}";
	}
}
