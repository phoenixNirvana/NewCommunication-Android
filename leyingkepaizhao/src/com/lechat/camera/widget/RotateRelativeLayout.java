package com.lechat.camera.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class RotateRelativeLayout extends RelativeLayout {

	private int mRotate;
	
	public RotateRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public RotateRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		init();
	}
	
	public RotateRelativeLayout(Context context) {
		super(context);
		init();
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	private void init(){
		
	}
	
	public void setRotate(int rotate){
		mRotate = rotate;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		canvas.rotate(mRotate, getMeasuredWidth() / 2, getMeasuredHeight() / 2);
		super.onDraw(canvas);
		canvas.restore();
	}
	
	
}
