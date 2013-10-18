package com.lechat.camera.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class SlidBar extends HorizontalScrollView {

	private Context mContext;
	private View mRootView;
	public SlidBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SlidBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SlidBar(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		mContext = context;
		mRootView = new LinearLayout(mContext);
		((LinearLayout) mRootView).setOrientation(LinearLayout.HORIZONTAL);
		addView(mRootView);
		setBackgroundColor(Color.BLACK);
	}
	
	public void setRotate(int rotate){
		
		int count = ((ViewGroup) mRootView).getChildCount();
		for(int i = 0;i < count;i++){
			View view = ((ViewGroup) mRootView).getChildAt(i);
			if (view instanceof RotateLinearLayout) {
				((RotateLinearLayout)view).setRotate(rotate);
			};
		}
	}
	
	public View getRootView(){
		return mRootView;
	}
}
