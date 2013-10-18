package com.lechat.camera.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class RotateLinearLayout extends LinearLayout {

	private int mRotate;
	
	public RotateLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RotateLinearLayout(Context context) {
		super(context);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}
	
	public void setRotate(int rotate){
		mRotate = rotate;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		System.out.println("RotateLinearLayout "+getMeasuredWidth()+" "+getMeasuredHeight());
		canvas.rotate(mRotate, getMeasuredWidth() / 2, getMeasuredHeight() / 2);
		super.onDraw(canvas);
		canvas.restore();
	}
	
	
}
