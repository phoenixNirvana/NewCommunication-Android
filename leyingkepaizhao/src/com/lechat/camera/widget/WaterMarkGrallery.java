package com.lechat.camera.widget;

import com.lechat.camera.utils.BitmapUtil;
import com.lechat.ui.activity.CameraActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WaterMarkGrallery extends Gallery {

	public static final int KEY_INVALID = -1;
	private int kEvent = KEY_INVALID;
	private int count;
	private int mScreenWidth;
	private int mScreenHeight;
	private int mLastMotionX;
	private int mLastMotionY;
	private boolean isMove = false;
	private boolean isFlip = false;
	private boolean isLongClick = false;
	private Bitmap mDragBitmap;
	private Rect mOutRect;
	private Context mContext;
	private WindowManager mWindowManager;
	private ImageView mDragView;
	private View mSelectView;
    
	public WaterMarkGrallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public WaterMarkGrallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public WaterMarkGrallery(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		mContext = context;
		mWindowManager = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
		mScreenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		float xdistance = calXdistance(e1, e2);
		float min_distance = mScreenWidth / 4f;
		if (isScrollingLeft(e1, e2) && xdistance > min_distance) {
			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else if (!isScrollingLeft(e1, e2) && xdistance > min_distance) {
			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		int dx = (int) e1.getX() - (int) e2.getX();
		int dy = (int) e1.getY() - (int) e2.getY();
		if (Math.abs(dx) >= 10 || Math.abs(dy) >= 10) {
			removeCallbacks(mRunnable);
			isFlip = true;
		}
		if(isFlip){
			super.onScroll(e1, e2, distanceX, distanceY);
		}
		return false;
	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		return e2.getX() > e1.getX();
	}

	private float calXdistance(MotionEvent e1, MotionEvent e2) {
		return Math.abs(e2.getX() - e1.getX());
	}

	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if(--count > 0)
				return;
			isLongClick = true;
			isFlip = false;
			try{
			mDragView = new ImageView(mContext);
			if(mDragBitmap != null)
				mDragBitmap.recycle();
			mDragBitmap = BitmapUtil.convertViewToBitmap(mSelectView,mSelectView.getWidth(),mSelectView.getHeight());
			System.out.println("runnable width="+mDragBitmap.getWidth()+"  height="+mDragBitmap.getHeight());
			mDragView.setImageBitmap(mDragBitmap);
			addDragViewToWindow(mDragView,
					(RelativeLayout.LayoutParams) mSelectView.getLayoutParams());
			mSelectView.setVisibility(View.GONE);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(ev.getAction() == MotionEvent.ACTION_DOWN){
			mLastMotionX = (int) ev.getX();
			mLastMotionY = (int) ev.getY();
			ViewGroup view = (ViewGroup) ((CameraActivity) mContext).getSelectView();
			int n = view.getChildCount();
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				for (int i = 0; i < n; i++) {
					mSelectView = view.getChildAt(i);
					mOutRect = new Rect();
					mSelectView.getHitRect(mOutRect);
					System.out.println("dispatchTouchEvent mOutRect left="+mOutRect.left+" mOutRect right="+mOutRect.right+" x="+ev.getX()
							+"mOutRect t="+mOutRect.top+" mOutRect bottom="+mOutRect.bottom+" x="+ev.getY());
					if (mOutRect.contains(mLastMotionX, mLastMotionY)) {
						System.out.println(" postDelayed ");
						count++;
						postDelayed(mRunnable, 1000);
					}
				}
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	private void addDragViewToWindow(View view, RelativeLayout.LayoutParams params) {
		WindowManager.LayoutParams wParams = new WindowManager.LayoutParams();
		wParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		/*System.out
		.println("onInterceptTouchEvent HitRect wParams.x="
				+ wParams.x
				+ "wParams.y="
				+ wParams.y
				+"mOutRect.left="
				+mOutRect.left
				+"mOutRect.top="
				+mOutRect.top);*/
		wParams.x = mOutRect.left;
		wParams.y = mOutRect.top;
		wParams.gravity = Gravity.LEFT | Gravity.TOP;
		wParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		wParams.format = PixelFormat.TRANSLUCENT;
		mWindowManager.addView(view, wParams);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			int dx = (int) ev.getX() - mLastMotionX;
			int dy = (int) ev.getY() - mLastMotionY;
		//	System.out.println("ACTION_MOVE dx="+dx+" dy="+dy);
			if(!isFlip){
				if (Math.abs(dx) >= 10 || Math.abs(dy) >= 10) {
					isMove = true;
					removeCallbacks(mRunnable);
				}
				if (isMove && isLongClick) {
					drag(dx, dy);
				}
			}
			mLastMotionX = (int) ev.getX();
			mLastMotionY = (int) ev.getY();
		}else if (ev.getAction() == MotionEvent.ACTION_UP) {
			removeCallbacks(mRunnable);
			if (isMove && isLongClick) {
				if(mDragView != null){
					WindowManager.LayoutParams dragParams = (WindowManager.LayoutParams) mDragView
							.getLayoutParams();
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mOutRect.width(),mOutRect.height());
					params.leftMargin = dragParams.x;
					params.topMargin = dragParams.y;
					mSelectView.setLayoutParams(params);
					mSelectView.setVisibility(View.VISIBLE);
				}
				mWindowManager.removeView(mDragView);
			}else{
				if(!isFlip){
					System.out.println("onClick!");
				}else if(kEvent != KEY_INVALID) { // 是否切换上一页或下一页
					onKeyDown(kEvent, null);
					kEvent = KEY_INVALID;
				}
			}
			isLongClick = false;
			isMove = false;
			mLastMotionX = 0;
			mLastMotionY = 0;
			mDragView = null;
			isFlip = false;
			count = 0;
		}
		return super.onTouchEvent(ev);
	}

	private void drag(int dx, int dy) {
		System.out.println("drag"+"  dx="+dx+"  dy="+dy);
		if(mDragView != null){
			WindowManager.LayoutParams params = (WindowManager.LayoutParams) mDragView
					.getLayoutParams();
			params.x += dx;
			params.y += dy;
	//		System.out.println("ddddddddrag"+"  mScreenWidth="+mScreenWidth+"  mScreenHeight="+mScreenHeight);
			if(params.x + mDragBitmap.getWidth() >= mScreenWidth){
				params.x = mScreenWidth - mDragBitmap.getWidth();
			}else if(params.x <= 0){
				params.x = 0;
			}
			if(params.y <= 0){
				params.y = 0;
			}else if(params.y + mDragBitmap.getHeight() >= mScreenHeight){
				params.y = mScreenHeight - mDragBitmap.getHeight();
			}
		//	System.out.println("ddddddddrag"+"  dx="+dx+"  X="+params.x+"  BTW="+mDragBitmap.getWidth());
	//		System.out.println("ddddddddrag"+"  dy="+dy+"  Y="+params.y+"  BTH="+mDragBitmap.getHeight());
			mWindowManager.updateViewLayout(mDragView, params);
		}
	}
}
