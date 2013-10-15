package com.lechat.camera.widget;

import com.lechat.animation.Animator;
import com.lechat.animation.ObjectAnimator;
import com.lechat.animation.ValueAnimator;
import com.lechat.animation.Animator.AnimatorListener;
import com.lechat.animation.ValueAnimator.AnimatorUpdateListener;
import com.lechat.utils.CommonUtil;
import com.lechat.utils.Logger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

public class CameraLayout extends RelativeLayout {

	private final int DEFAULTWIDTH = 60;
	private final int DEFAULTHEIGHT = 40;
	private int mWidth = -1;
	private int mHeight = -1;
	private Context mContex;
	private FocusShape mFocusShape;
	private ObjectAnimator yObj;
	
	public CameraLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CameraLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CameraLayout(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		mContex = context;
	//	mFocusShape = new FocusShape();
		resetShapeFocus(Color.LTGRAY);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(changed || mWidth == -1){
			mWidth = r - l;
			mHeight = b - t;
			//initFocusRect();
		//	mFocusShape.initFocusRect();
		}
		Logger.debugPrint("CameraLayout", " mWidth="+mWidth+" mHeight="+mHeight);
	}

	public void doFocusAni(){
		focusAnimate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		try{
			Logger.debugPrint("CameraLayout", "onDraw");	
			super.onDraw(canvas);
			drawFocus(canvas);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	protected void drawFocus(Canvas canvas){
		synchronized (FocusShape.class) {
			if(mFocusShape != null){
				mFocusShape.drawRect(canvas);
			}
		}
	}
	
	public void setShapeFocusColor(int color){
		if(mFocusShape != null){
			mFocusShape.setPaintColor(color);
			invalidate();
		}
	}
	
	public void resetShapeFocus(int color){
	    mFocusShape = new FocusShape();
	    mFocusShape.setScale(1f);
	    mFocusShape.setPaintColor(color);
	    mFocusShape.initFocusRect();
		invalidate();
	}
	
	public void resetShapeFocus(){
	    mFocusShape = new FocusShape();
	    mFocusShape.setScale(1f);
	    mFocusShape.initFocusRect();
		invalidate();
	}
	
	public void cancelAntResetAni(){
		 cancelAni();
		 resetShapeFocus();
	}
	
	public boolean cancelAni(){
		if(yObj != null && yObj.isRunning()){
			yObj.cancel();
			return true;
		}
		return false;
	}
	
	public void clearShapeFocus(){
		  cancelAni();
		  mFocusShape = null;
		  invalidate();
	}
	
	private Runnable mAniEndAction = new Runnable() {
		
		@Override
		public void run() {
		    Logger.debugPrint("CameraLayout", "ANIMATE Runnable");
			if(mFocusShape != null){
				mFocusShape.setPaintColor(Color.WHITE);
				mFocusShape.setScale(1);
				invalidate();
			}
		}
	};
	
	protected void focusAnimate(){
		if(mFocusShape == null){
			return;
		}
		if(cancelAni()){
			return ;
		}
		if(mFocusShape == null){
			mFocusShape = new FocusShape();
		}
		yObj = ObjectAnimator.ofFloat(mFocusShape, "scale",
				mFocusShape.getScale(),1.4f,1.1f,1.0f,1.0f,1.0f);
		yObj.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				if(mFocusShape != null)
				  Logger.debugPrint("CameraLayout", " onAnimationUpdate"+" scale="+mFocusShape.getScale());
				invalidate();
			}
		});
		yObj.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				Logger.debugPrint("CameraLayout", " onAnimationStart");
					if(mFocusShape != null)
						mFocusShape.setPaintColor(Color.WHITE);
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				Logger.debugPrint("CameraLayout", " onAnimationEnd");
					if(mFocusShape != null)
					   mFocusShape.setPaintColor(Color.GREEN);
					invalidate();
				postDelayed(mAniEndAction, 400);
			}
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			@Override
			public void onAnimationCancel(Animator animation) {
				Logger.debugPrint("CameraLayout", " onAnimationCancel");
				if(mFocusShape != null){
				   mFocusShape.setPaintColor(Color.WHITE);
				}
			}
		});
		yObj.setDuration(500);
		DecelerateInterpolator di = new DecelerateInterpolator();
		yObj.setInterpolator(di);
		yObj.start();
	}
    
	
	class FocusShape{
		
		private int mFocusWidth = -1;
		private int mFocusHeight = -1;
		private float scale = 1.4f;
		private Rect focusDRect;
		private Paint focusPaint;
		private int smallLen;
		private int smallWidth;
		
		public FocusShape(){
			mFocusWidth = CommonUtil.dip2px(mContex, DEFAULTWIDTH);
			mFocusHeight = CommonUtil.dip2px(mContex, DEFAULTHEIGHT);
			smallLen = mFocusWidth / 5;
			smallWidth = 3;
			focusPaint = new Paint();
			focusPaint.setStrokeWidth(2);
			focusPaint.setStyle(Style.STROKE);
			focusPaint.setColor(Color.WHITE);
		}
		
		public float getScale() {
			return scale;
		}
		
		public void setScale(float scale) {
			this.scale = scale;
		}

		public Paint getFocusPaint() {
			return focusPaint;
		}

		public void setFocusPaint(Paint focusPaint) {
			this.focusPaint = focusPaint;
		}
		
		public void setPaintColor(int color){
			this.focusPaint.setColor(color);
		}
		
		public void initFocusRect(){
			focusDRect = generateRect((int)(mFocusWidth * scale),(int)(mFocusHeight * scale));
		}
		
		private Rect generateRect(int width,int height){
			int left = (mWidth - width) / 2;
			int top = (mHeight - height) / 2;
			return new Rect(left, top, left + width, top + height);
		}
		
		public Rect getRect(){
			return focusDRect;
		}
		
		public void drawRect(Canvas canvas){
			synchronized (this) {
				initFocusRect();
				Path ltPath = new Path();
				ltPath.moveTo(focusDRect.left, focusDRect.top + smallLen);
				ltPath.lineTo(focusDRect.left, focusDRect.top);
				ltPath.lineTo(focusDRect.left + smallLen, focusDRect.top);
				ltPath.lineTo(focusDRect.left + smallLen, focusDRect.top + smallWidth);
				ltPath.lineTo(focusDRect.left + smallWidth, focusDRect.top + smallWidth);
				ltPath.lineTo(focusDRect.left + smallWidth, focusDRect.top + smallLen);
				ltPath.close();
				Logger.debugPrint("CameraLayout", "drawRect "+ltPath.toString());
			    canvas.drawPath(ltPath, focusPaint);
			    
				Path rtPath = new Path();
				rtPath.moveTo(focusDRect.right - smallLen, focusDRect.top);
				rtPath.lineTo(focusDRect.right, focusDRect.top);
				rtPath.lineTo(focusDRect.right, focusDRect.top + smallLen);
				rtPath.lineTo(focusDRect.right - smallWidth, focusDRect.top + smallLen);
				rtPath.lineTo(focusDRect.right - smallWidth, focusDRect.top + smallWidth);
				rtPath.lineTo(focusDRect.right - smallLen, focusDRect.top + smallWidth);
				rtPath.close();
				canvas.drawPath(rtPath, focusPaint);
				
				Path rbPath = new Path();
				rbPath.moveTo(focusDRect.right - smallLen, focusDRect.bottom - smallWidth);
				rbPath.lineTo(focusDRect.right - smallWidth, focusDRect.bottom - smallWidth);
				rbPath.lineTo(focusDRect.right - smallWidth, focusDRect.bottom - smallLen);
				rbPath.lineTo(focusDRect.right, focusDRect.bottom - smallLen);
				rbPath.lineTo(focusDRect.right, focusDRect.bottom);
				rbPath.lineTo(focusDRect.right - smallLen, focusDRect.bottom);
				rbPath.close();
				canvas.drawPath(rbPath, focusPaint);
				
				Path lbPath = new Path();
				lbPath.moveTo(focusDRect.left, focusDRect.bottom - smallLen);
				lbPath.lineTo(focusDRect.left, focusDRect.bottom);
				lbPath.lineTo(focusDRect.left + smallLen, focusDRect.bottom);
				lbPath.lineTo(focusDRect.left + smallLen, focusDRect.bottom - smallWidth);
				lbPath.lineTo(focusDRect.left + smallWidth, focusDRect.bottom - smallWidth);
				lbPath.lineTo(focusDRect.left + smallWidth, focusDRect.bottom - smallLen);
				lbPath.close();
				canvas.drawPath(lbPath, focusPaint);
			}
		}
	}
}
