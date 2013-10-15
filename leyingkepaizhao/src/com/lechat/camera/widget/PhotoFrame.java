package com.lechat.camera.widget;

import com.lechat.R;
import com.lechat.animation.ObjectAnimator;
import com.lechat.animation.ValueAnimator;
import com.lechat.animation.ValueAnimator.AnimatorUpdateListener;
import com.lechat.camera.utils.BitmapUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class PhotoFrame extends View{

	private int mViewWidth;
	private int mViewHeight;
	private int mContentWidth;
	private int mContentHeight;
	private DrawBitmap mLTDDrawBitmap;
	private DrawBitmap mRBDrawBitmap;
	private DrawBitmap mDrawPhotoBitmap;
	private Paint mPaint;
	private Paint mBgPaint;
	private Degree mDegree;

	public PhotoFrame(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public PhotoFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PhotoFrame(Context context) {
		super(context);
		init(context);
	}

	private int mScreenWidth;
	private int mScreenHeight;
	
	private void init(Context context){
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mBgPaint = new Paint();
		mBgPaint.setAntiAlias(true);
		mBgPaint.setStyle(Paint.Style.FILL);
		mBgPaint.setColor(Color.WHITE);
		mLTDDrawBitmap = initDrawBitmap(R.drawable.tiao_up);
		mRBDrawBitmap = initDrawBitmap(R.drawable.tiao_down);
		
		DisplayMetrics dm = getResources().getDisplayMetrics();
		mScreenWidth = dm.widthPixels;
		mScreenHeight = dm.heightPixels;
		System.out.println("屏幕宽 "+mScreenWidth+" 屏幕高 "+mScreenHeight+" width="+mRBDrawBitmap.mBitmap.getWidth()+" height="+mRBDrawBitmap.mBitmap.getHeight());
		mDegree = new Degree();
	}
	
	private DrawBitmap initDrawBitmap(int drawId){
		DrawBitmap drawBitmap = new DrawBitmap();
		drawBitmap.mBitmap = BitmapFactory.decodeResource(getResources(), drawId);
		drawBitmap.setScrRect();
		return drawBitmap;
	}
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	};
	
	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			mContentWidth = (int) (mViewWidth * (2.0 / 3));
			mContentHeight = (int) (mViewHeight * (2.0 / 3));
			int left = (mViewWidth - mContentWidth) / 2;
			int top = (mViewHeight - mContentHeight) / 2;
			synchronized (Rect.class) {
				if(contentRect == null){
					contentRect = new Rect(left,top,left + mContentWidth,top + mContentHeight);
				}else{
					contentRect.set(left,top,left + mContentWidth,top + mContentHeight);
					setPhoto(null);
				}
			}
		}
	};
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		System.out.println("onLayout   sdfsdfdsfc changed="+changed);
		if(changed || mContentWidth == 0){
			mContentWidth = (int) (mViewWidth * (2.0 / 3));
			mContentHeight = (int) (mViewHeight * (2.0 / 3));
			int left = (mViewWidth - mContentWidth) / 2;
			int top = (mViewHeight - mContentHeight) / 2;
			synchronized (Rect.class) {
				if(contentRect == null){
					contentRect = new Rect(left,top,left + mContentWidth,top + mContentHeight);
				}else{
					contentRect.set(left,top,left + mContentWidth,top + mContentHeight);
					setPhoto(null);
				}
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	//	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		System.out.println("onMeasure   sdfsdfdsfc onMeasure");
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		setMeasuredDimension(width, height);
		mViewWidth = width;
		mViewHeight = height;
	}

	private Rect contentRect;
	private ObjectAnimator mDegreeAni;
	private String mFilePath;
	public void setPhoto(final String filePath){
		synchronized (String.class) {
			if(!TextUtils.isEmpty(filePath)){
				mFilePath = filePath;
			}
		}
		synchronized (Rect.class) {
	      if(contentRect == null){
	    	  postDelayed(new Runnable() {
				@Override
				public void run() {
					setPhoto(filePath);
				}
			},300);
	    	  return ;
	      }
		}
   //     System.out.println("setPhoto  "+mContentHeight);
        if(mDrawPhotoBitmap == null){
        	mDrawPhotoBitmap = new DrawBitmap();
        	mDrawPhotoBitmap.mBitmap = produceBitmap(mFilePath);
        	mDrawPhotoBitmap.setScrRect();
        }
		/*if(mDrawPhotoBitmap.mWidth >= mDrawPhotoBitmap.mHeight){
			ratio = (float)mContentWidth / mDrawPhotoBitmap.mWidth;
		}else {
			ratio = (float)mContentHeight / mDrawPhotoBitmap.mHeight;
		}*/
	//	int scaleWidth = (int)(mDrawPhotoBitmap.mWidth);
	//	int scaleHeight = (int)(mDrawPhotoBitmap.mHeight * ratio);
		
	    synchronized (Rect.class) {
	    	int left = contentRect.left + 10;
	    	int top = contentRect.top + 10;
	    	mDrawPhotoBitmap.setDstRect(left, top,contentRect.width() - 20,contentRect.height() - 20);
	    }
		invalidate();
		if(mDegreeAni == null){
			mDegreeAni = ObjectAnimator.ofFloat(mDegree, "degree", 0.0f, 2.0f,5,0f,12.0f,15.0f);
			mDegreeAni.setDuration(500);
			mDegreeAni.setRepeatCount(0);
			mDegreeAni.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					invalidate();
				}
			});
			mDegreeAni.start();
		}
	}
	
	private Bitmap produceBitmap(String path){
	  Options op = new Options();
	  op.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(path, op);
      int simpleSize = BitmapUtil.calculateInSampleSize(op,mContentWidth - 20,mContentHeight - 20);
      op.inSampleSize = simpleSize;
      op.inJustDecodeBounds = false;
      return BitmapFactory.decodeFile(path, op);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		try{
			if(contentRect == null)
				return;
			canvas.save();
			canvas.rotate(mDegree.getDegree(), mViewWidth /2, mViewHeight /2);
			System.out.println(" "+(mViewWidth /2)+" "+(mViewWidth /2));
			canvas.drawRect(contentRect, mBgPaint);
			System.out.println(" onDraw  角度  "+mDegree.getDegree());
			if(mDrawPhotoBitmap != null){
				mDrawPhotoBitmap.drawBitmap(canvas);
			}
			if(mLTDDrawBitmap != null){
				mLTDDrawBitmap.setDstRect(contentRect.left, contentRect.top);
				mLTDDrawBitmap.drawBitmap(canvas);
			}
			if(mRBDrawBitmap != null){
				mRBDrawBitmap.setDstRect(contentRect.right - mRBDrawBitmap.mWidth, contentRect.bottom - mRBDrawBitmap.mHeight);
				mRBDrawBitmap.drawBitmap(canvas);
			}
			canvas.restore();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	class Degree{
		
		private float degree = 0;

		public float getDegree() {
			return degree;
		}

		public void setDegree(float degree) {
			this.degree = degree;
		}
	}
	
	class DrawBitmap {
		protected Rect mScr;
		protected Rect mDst;
		protected Bitmap mBitmap;
		protected int mWidth;
		protected int mHeight;
		protected int mRatio = 1;
		protected int mScaleWidth;
		protected int mScaleHeight;

		public void setDstRect(int left, int top) {
			mDst = new Rect(left, top, left + mWidth, top + mHeight);
		}
		public void setScrRect() {
			mWidth = mBitmap.getWidth();
			mHeight = mBitmap.getHeight();
			mScr = new Rect(0, 0, mWidth, mHeight);
		}

		public void setDstRect(int left, int top, int right, int bottom) {
			mDst = new Rect(left, top, left + right, top + bottom);
		}

		public void drawBitmap(Canvas canvas, Paint paint) {
			System.out.println(""+mDst.toShortString());
			canvas.drawBitmap(mBitmap, mScr, mDst, paint);
		}

		public void drawBitmap(Canvas canvas) {
			canvas.drawBitmap(mBitmap, mScr, mDst, mPaint);
		}
	}
}
