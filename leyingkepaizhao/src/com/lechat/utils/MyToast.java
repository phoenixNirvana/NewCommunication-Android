package com.lechat.utils;


import android.content.Context;
import android.widget.Toast;

public class MyToast {
	
	/**
	 * 暂时没有数据
	 */
	static final int NO_DATA = 20101;
	
	/**
	 * 签到过于频繁
	 */
	static final int SIGN_IN_FREQUENTLY = 20102;
	
	/**
	 * 未绑定手�?
	 */
	static final int UN_BUNDING_PHONE   = 20103;
	
	
	public static void showToast(Context context ,String text){
		Toast t = Toast.makeText(context, text, 2000);
		t.show();
	}
	
	public static void showToast(Context context ,int text){
		Toast t = Toast.makeText(context, text, 2000);
		t.show();
	}
	
	public static void showToast(Context context ,String text, int duration){
		Toast t = Toast.makeText(context, text, duration);
		t.show();
	}
	
}
