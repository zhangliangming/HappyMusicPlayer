//package com.happyplayer.receiver;
//
//import android.app.KeyguardManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//
//import com.happyplayer.common.Constants;
//import com.happyplayer.ui.ShowLockActivity;
//
//public class LockSreenReceiver extends BroadcastReceiver {
//
//	private KeyguardManager keyguardManager;
//	private KeyguardManager.KeyguardLock keyguardLock;
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		 String action = intent.getAction();
//		 if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
//		 if (Constants.SHOWLOCK) {
//		 Intent lockIntent = new Intent(context, ShowLockActivity.class);
//		 lockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		
//		 keyguardManager = (KeyguardManager) context
//		 .getSystemService(Context.KEYGUARD_SERVICE);
//		 keyguardLock = keyguardManager.newKeyguardLock("");
//		 keyguardLock.disableKeyguard();
//		
//		 context.startActivity(lockIntent);
//		 }
//		
//		 }
//
//	}
//
// }
