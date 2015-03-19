package com.happyplayer.async;

import android.os.Handler;
import android.os.Message;

/**
 * 最近修改时间2013年12月10日
 * 
 * @author Administrator
 *         <p>
 *         实现AsyncTask相关的异步加载功能
 *         </p>
 *         <p>
 *         1.execute(Params... params)，执行一个异步任务，需要我们在代码中调用此方法，触发异步任务的执行。
 *         </p>
 *         <p>
 *         2.onPreExecute()，在execute(Params...
 *         params)被调用后立即执行，一般用来在执行后台任务前对UI做一些标记。
 *         </p>
 *         <p>
 *         3.doInBackground(Params...params)，在onPreExecute()完成后立即执行，用于执行较为费时的操作，
 *         此方法将接收输入参数和返回计算结果。 在执行过程中可以调用publishProgress(Progress...
 *         values)来更新进度信息。
 *         </p>
 *         <p>
 *         4.onProgressUpdate(Progress... values)，在调用publishProgress(Progress...
 *         values)时，此方法被执行，直接将进度信息更新到UI组件上。 5.onPostExecute(Result
 *         result)，当后台操作结束时，此方法将会被调用，计算结果将做为参数传递到此方法中，直接将结果显示到UI组件上。
 *         </p>
 *         <p>
 *         知道相关的功能后，便可以用handler来简单实现
 *         </p>
 */
public abstract class AsyncTaskHandler {

	private Handler mHandler;

	private final static int ACTION_ONPREEXECUTE = 1;
	private final static int ACTION_ONPROGRESS = 2;
	private final static int ACTION_ONSUCCESS = 3;
	private final static int ACTION_ONERROR = 4;

	public AsyncTaskHandler() {

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ACTION_ONPREEXECUTE:
					onPreExecute();
					break;
				case ACTION_ONPROGRESS:
					onProgressUpdate((Integer) msg.obj);
					break;
				case ACTION_ONSUCCESS:
					onPostExecute(msg.obj);
					break;
				case ACTION_ONERROR:
					onError((Exception) msg.obj);
					break;
				}
			}
		};
	}

	/**
	 * 执行异步任务
	 */
	public void execute() {

		new Thread(new Runnable() {

			public void run() {
				try {
					sendOnPreExecute();
					Object result = doInBackground();
					sendOnSuccess(result);
				} catch (Exception e) {
					e.printStackTrace();
					sendOnError(e);
				}
			}
		}).start();

	}

	/**
	 * 任务执行前
	 */
	protected void onPreExecute() {
	}

	/**
	 * 在onPreExecute()完成后立即执行，用于执行较为费时的操作，此方法将接收输入参数和返回计算结果。
	 * 在执行过程中可以调用publishProgress(int progress)来更新进度信息。
	 * 
	 * @return
	 */
	protected abstract Object doInBackground() throws Exception;

	/**
	 * 此方法被执行，直接将进度信息更新到UI组件上。
	 * 
	 * @param progress
	 */
	protected void onProgressUpdate(int progress) {

	}

	/**
	 * 当后台操作结束时，此方法将会被调用，计算结果将做为参数传递到此方法中，直接将结果显示到UI组件上。
	 * 
	 * @param result
	 */
	protected abstract void onPostExecute(Object result);

	/**
	 * 异步任务执行失败
	 * 
	 * @param e
	 */
	protected void onError(Exception e) {
	}

	private void sendOnPreExecute() {
		mHandler.sendEmptyMessage(ACTION_ONPREEXECUTE);
	}

	private void sendOnSuccess(Object result) {
		Message msg = mHandler.obtainMessage();
		msg.what = ACTION_ONSUCCESS;
		msg.obj = result;
		mHandler.sendMessage(msg);
	}

	private void sendOnError(Exception e) {
		Message msg = mHandler.obtainMessage();
		msg.what = ACTION_ONERROR;
		msg.obj = e;
		mHandler.sendMessage(msg);
	}

	protected void publishProgress(long progress) {
		Message msg = mHandler.obtainMessage();
		msg.what = ACTION_ONPROGRESS;
		msg.obj = progress;
		mHandler.sendMessage(msg);
	}

}
