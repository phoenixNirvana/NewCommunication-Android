package com.lechat.camera.widget;

import android.content.Context;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.Gallery;

public class WaterMarkGrallery extends Gallery {

	public WaterMarkGrallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public WaterMarkGrallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WaterMarkGrallery(Context context) {
		super(context);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return super.onFling(e1, e2, velocityX * 0.2f, velocityY);
	}

	/*@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		int keyCode = 0;
		if(e1.getX() - e2.getX() <= 0){
			keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
		}else{
			keyCode = KeyEvent.KEYCODE_DPAD_LEFT;
		}
		if(velocityX > 100){
			onKeyDown(keyCode, null);
		}
		return true;
	}*/
	
}
