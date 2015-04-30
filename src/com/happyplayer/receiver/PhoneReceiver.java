package com.happyplayer.receiver;

import com.happyplayer.observable.ObserverManage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;

public class PhoneReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals("android.intent.action.MEDIA_BUTTON")) {
			// 耳机事件 Intent 附加值为(Extra)点击MEDIA_BUTTON的按键码

			KeyEvent event = (KeyEvent) intent
					.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (event == null)
				return;

			boolean isActionUp = (event.getAction() == KeyEvent.ACTION_UP);
			if (!isActionUp)
				return;

			int keyCode = event.getKeyCode();
			long eventTime = event.getEventTime() - event.getDownTime();// 按键按下到松开的时长
			Message msg = Message.obtain();
			msg.what = 100;
			Bundle data = new Bundle();
			data.putInt("key_code", keyCode);
			data.putLong("event_time", eventTime);
			msg.setData(data);
			
			ObserverManage.getObserver().setMessage(msg);
			// phoneHandler.sendMessage(msg);

		}
		// 终止广播(不让别的程序收到此广播，免受干扰)
		// abortBroadcast();
	}
}
