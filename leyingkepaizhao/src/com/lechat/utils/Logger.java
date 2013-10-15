package com.lechat.utils;

import android.util.Log;

public class Logger {

	private static int LOG = 6;
	private static int E = 5;
	
	public static void e(String log, String msg) {
		if (LOG > E && Constant.isDebug)
			Log.e(log, msg);
	}

	public static void e(String log, String msg, Throwable tr) {
		if (LOG > E && Constant.isDebug)
			Log.e(log, msg, tr);
	}

	public static void debugPrint(String content){
		if(Constant.isDebug){
			Log.i("debug", content);
		}
	}
	
	public static void debugPrint(String tag, String content){
		if(Constant.isDebug){
			Log.i(tag, content);
		}
	}
}
