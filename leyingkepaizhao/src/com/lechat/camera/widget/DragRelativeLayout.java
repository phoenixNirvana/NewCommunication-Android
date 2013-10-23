package com.lechat.camera.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class DragRelativeLayout extends RelativeLayout {

	private Context mContext;
	private WindowManager mWindowManager;
    private int mScreenWidth;
    private int mScreenHeight;
	
	public DragRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public DragRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DragRelativeLayout(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		mWindowManager = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
		mScreenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
	}
}
