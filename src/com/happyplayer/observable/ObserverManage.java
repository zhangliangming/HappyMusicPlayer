package com.happyplayer.observable;

import java.util.Observable;

/**
 * 观察者，用来观察一些操作，如主题颜色的改变，何时弹出更新的窗口等等。
 * 
 * @author Administrator 最近修改时间2013年12月10日
 */
public class ObserverManage extends Observable {

	private static ObserverManage myobserver = null;

	public static ObserverManage getObserver() {
		if (myobserver == null) {
			myobserver = new ObserverManage();
		}
		return myobserver;
	}

	public void setMessage(Object data) {
		myobserver.setChanged();
		myobserver.notifyObservers(data);
	}
}
