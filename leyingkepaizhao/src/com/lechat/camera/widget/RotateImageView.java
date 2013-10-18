package com.lechat.camera.widget;

import com.lechat.animation.ObjectAnimator;
import com.lechat.animation.ValueAnimator;
import com.lechat.animation.ValueAnimator.AnimatorUpdateListener;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RotateImageView extends ImageView {

	public RotateImageView(Context context) {
		super(context);
		init();
	}
	public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	public RotateImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	private ObjectAnimator mObjAnimator;
	private Rotate mRotate;
	private int mOritation = 0;
	
	private void init(){
		mRotate = new Rotate();
	}
	
	public void setRotate(int rotate){
		
		if(mObjAnimator != null){
			if(mObjAnimator.isRunning())
				return;
		}
		mObjAnimator = ObjectAnimator.ofInt(mRotate,"rotate",0,rotate);
		if(rotate < 0){
			mObjAnimator = ObjectAnimator.ofInt(mRotate,"rotate",-rotate,0);
		}else{
			mObjAnimator = ObjectAnimator.ofInt(mRotate,"rotate",0,rotate);
		}
		mObjAnimator.setRepeatCount(0);
		mObjAnimator.setDuration(500);
		mObjAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				  invalidate();
				  System.out.println("  "+mRotate.getRotate());
				  mOritation = mRotate.getRotate();
				  
			}
		});
		mObjAnimator.start();
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.rotate(mOritation, canvas.getWidth() / 2, canvas.getHeight() / 2);
		super.draw(canvas);
		canvas.restore();
	}
	
	class Rotate{
		private int rotate;
		public int getRotate() {
			return rotate;
		}

		public void setRotate(int rotate) {
			this.rotate = rotate;
		}
	}
}
