package com.lechat.camera.widget;

import com.lechat.camera.data.CameraManager;
import com.lechat.utils.Logger;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback{

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
    private Size mPreviewSize;
	private boolean isFirstInitlizeSize = true;
    
	public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CameraPreview(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		mSurfaceView = new SurfaceView(context);
		addView(mSurfaceView);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public int getmWidth(){
		return mWidth;
	}
	
	public int getmHeight(){
		return mHeight;
	}
	
	public SurfaceHolder getSurfaceHolder(){
		return mSurfaceHolder;
	}
	
	public void updataViewSize(){
		new Thread(){
			@Override
			public void run() {
				super.run();
				int retry = 0;
				while(true){
					Camera camera = CameraManager.get().getCamera();
					if(camera != null){
						post(new Runnable() {
							@Override
							public void run() {
								mPreviewSize = CameraManager.get().getOptimalPreviewSize(mHeight,mWidth);
						        Logger.debugPrint("CameraPreview", "  surfaceChanged width="+mWidth+"  height="+mHeight+"  "+mPreviewSize.width+"  "+mPreviewSize.height);
						        CameraManager.get().setPreviewSize(mPreviewSize);
						        CameraManager.get().startPreview(false);
							}
						});
						break;
					}else{
						try {
							if(retry > 4){
								break;
							};
							sleep(300);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally{
							retry++;
						}
					}
				}
			}
		}.start();
	}
	
	private int mWidth;
	private int mHeight;
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		 if (changed && getChildCount() > 0) {
	            final View child = getChildAt(0);
	            final int width = r - l;
	            final int height = b - t;
	            int previewWidth = width;
	            int previewHeight = height;
	            if (mPreviewSize != null) {
	                previewWidth = mPreviewSize.height;
	                previewHeight = mPreviewSize.width;
	            }
	            // Center the child SurfaceView within the parent.
	            if (width * previewHeight > height * previewWidth) {
	                final int scaledChildWidth = previewWidth * height / previewHeight;
	                child.layout((width - scaledChildWidth) / 2, 0,
	                        (width + scaledChildWidth) / 2, height);
	            } else {
	                final int scaledChildHeight = previewHeight * width / previewWidth;
	                child.layout(0, (height - scaledChildHeight) / 2,
	                        width, (height + scaledChildHeight) / 2);
	            }
	        }
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		 CameraManager.get().setPreviewDisplay(holder);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		    Logger.debugPrint("CameraPreview", "  surfaceChanged width="+width+"  height="+height);
		    mWidth = width;
			mHeight = height;
		    if(CameraManager.get().getCamera() != null){
		    	CameraManager.get().stopPreview(false);
		    }
		    updataViewSize();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		CameraManager.get().stopPreview(false);
		CameraManager.get().closeDriver();
		isFirstInitlizeSize = true;
	}
	
}
