package com.leyingke.paizhao.camera;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.ViewTreeObserver;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.leyingke.paizhao.R;
import com.leyingke.paizhao.camera.CameraManager.OnPreviewListener;
import com.leyingke.paizhao.data.AutoFocusManager;
import com.leyingke.paizhao.ui.BaseActivity;
import com.leyingke.paizhao.utils.BitmapUtil;
import com.leyingke.paizhao.utils.CommonUtil;
import com.leyingke.paizhao.utils.Logger;
import com.leyingke.paizhao.widget.CameraLayout;
import com.leyingke.paizhao.widget.CameraPreview;

public class CameraActivity extends BaseActivity implements OnClickListener,PictureCallback
														,OnPageChangeListener{
	private static final int PHOTO_ALBUM = 1;
	private int mCurrentPosition = 2; 
	private boolean isInitlized = false;
	private List<View> mViews;
	private MyAdapter mAdapter;
	private Button mBtnRotate;
	private Button btnChoicePic;
	private Button btnTaskPic, mBtnSave, mBtnCancel;
	private CameraHandler mHandler;
	private ImageView mShowPic;
	private ImageView mBtnReplace;
	private LinearLayout mContainer;
	private LinearLayout mContainer2;
	private LinearLayout mFlashOff, mFlashAuto, mFlashOpen, mFlashLight;
	private RelativeLayout mRlTakePicture, mRlSave;
	private CameraLayout mCameraLayout;
	private ViewPager mViewPager;
	private CameraPreview mCameraPreview;
	private CameraOrientationEventListener mCameraOrientationEventListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		initView();
	}
	
	private void initView(){
		
		mFlashOff = (LinearLayout) findViewById(R.id.layout_flash_close);
		mFlashAuto = (LinearLayout) findViewById(R.id.layout_flash_auto);
		mFlashOpen = (LinearLayout) findViewById(R.id.layout_flash_open);
		mFlashLight = (LinearLayout) findViewById(R.id.layout_light);
		
		mContainer2 = (LinearLayout) findViewById(R.id.button5);
		mContainer = (LinearLayout) findViewById(R.id.container);
		mBtnReplace = (ImageView) mContainer2.getChildAt(0);
		
		mShowPic = (ImageView) findViewById(R.id.iv_pic);
		
		btnTaskPic = (Button) findViewById(R.id.btn_take);
		btnChoicePic = (Button) findViewById(R.id.btn_choice);
		mBtnSave = (Button) findViewById(R.id.btn_save);
		mBtnCancel = (Button) findViewById(R.id.btn_cancel);
		mBtnRotate = (Button) findViewById(R.id.btn_rotate);
		mViewPager = (ViewPager) findViewById(R.id.vp_effect);
		mCameraLayout = (CameraLayout) findViewById(R.id.cl_cameralayout);
		mCameraPreview = (CameraPreview) findViewById(R.id.view_camerapreview);
		mViewPager.setOnPageChangeListener(this);
		mLocation = new int[2];
		
		mRlTakePicture = (RelativeLayout) btnTaskPic.getParent();
		mRlSave = (RelativeLayout) mBtnSave.getParent();
		
		mFlashOff.setOnClickListener(this);
		mFlashAuto.setOnClickListener(this);
		mFlashOpen.setOnClickListener(this);
		mFlashLight.setOnClickListener(this);
		mContainer2.setOnClickListener(this);
		
		btnTaskPic.setOnClickListener(this);
		btnChoicePic.setOnClickListener(this);
		mBtnSave.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		mBtnRotate.setOnClickListener(this);
		
		CameraManager.init(getApplication());
		CameraManager.get().setPictureCallback(this);
		CameraManager.get().setOnPreviewListener(new OnPreviewListener() {
			@Override
			public void onPreview(boolean preview) {
				updateBtnState(preview);
			}
		});
		mCameraOrientationEventListener = new CameraOrientationEventListener(this);
		
		mViews = new ArrayList<View>();
		View view = LayoutInflater.from(this).inflate(R.layout.watermark_fragment, null);
		ImageView iv = (ImageView) view.findViewById(R.id.iv_watermark);
		iv.setImageResource(R.drawable.guilai);
		mViews.add(view);
		mAdapter = new MyAdapter();
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(0);
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				initFirstPageView();
			}
		});
	}
	
	private void updateBtnState(boolean preview){
		
		if(preview){
			mRlTakePicture.setVisibility(View.VISIBLE);
			mRlSave.setVisibility(View.GONE);
		}else{
			mRlTakePicture.setVisibility(View.GONE);
			mRlSave.setVisibility(View.VISIBLE);
		}
	}
	
	private void initFirstPageView(){
		View view = mViews.get(0);
	/*	final ImageView iv = (ImageView) view.findViewById(R.id.iv_watermark);
		ViewTreeObserver vto = iv.getViewTreeObserver();   
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
		    @Override   
		    public void onGlobalLayout() { 
		    	iv.getViewTreeObserver().removeGlobalOnLayoutListener(this);   
		    	if(mLocation != null){
		    		mLocation[0] = 0;
		    		mLocation[1] = 0;
		    	}
		    	mLocation[0] = iv.getLeft();
		    	mLocation[1] = iv.getTop();
		    	mCurrentBitmap = ((BitmapDrawable)iv.getDrawable()).getBitmap();
		    }   
		});*/
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Logger.debugPrint("onResume");
		mCameraOrientationEventListener.enable();
		if(!isInitlized){
			switchCamera(true);
			isInitlized = true;
			if(mCameraPreview != null){
				mCameraPreview.updataViewSize();
			}
			if (mHandler == null) {
				mHandler = new CameraHandler(this);
				CameraManager.get().setHandler(mHandler);
			}
		}
	}
	
	public void clearCusorFocus(){
		if(mCameraLayout != null)
			mCameraLayout.clearShapeFocus();
	}
	
	public void resetCusorFocus(){
		if(mCameraLayout != null)
			mCameraLayout.resetShapeFocus();
	}
	
/*	private void initCamera() {
		try {
			Logger.debugPrint("initCamera");
			CameraManager.get().openDriver();
			new Thread(){
				@Override
				public void run() {
					try {
						CameraManager.get().openDriver();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		} catch (RuntimeException e) {
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (mHandler == null) {
			mHandler = new CameraHandler(this);
		}
	}*/
	
	private void switchCamera(final boolean isCallback) {
		try {
			new Thread(){
				@Override
				public void run() {
					try {
						CameraManager.get().openDriver();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								CameraManager.get().setPreviewDisplay(mCameraPreview.getSurfaceHolder());
								CameraManager.get().startPreview(isCallback);
								resetCusorFocus();
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		} catch (RuntimeException e) {
			return;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Logger.debugPrint("onPause");
		mCameraOrientationEventListener.disable();
		isInitlized = false;
		clearCusorFocus();
		CameraManager.get().stopPreview(false);
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mHandler != null) {
			mHandler.quitSynchronously();
			mHandler = null;
		}
	}
	
	public void startFocusAni(){
		if(mCameraLayout != null){
			mCameraLayout.doFocusAni();
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	public void openPhoto() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		intent.putExtra("return-data", true);
		startActivityForResult(intent, PHOTO_ALBUM);
	}
	
	private void setCapabilitie(int position){
		
		mCurrentPosition = position;
		int bgId = 0;
		switch (mCurrentPosition) {
		case 1:  
			bgId = R.drawable.flash_close;
			break;
		case 2:  
			bgId = R.drawable.flash_auto;
			break;
		case 3:  
			bgId = R.drawable.flash_open;
			break;
		case 4:  
			bgId = R.drawable.light;
			break;
		}
		mBtnReplace.setImageResource(bgId);
		mContainer.setVisibility(View.GONE);
		mContainer2.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		
		int position = -1;
		switch(v.getId()){
		case R.id.layout_flash_close:
			position = 1;
			CameraManager.get().setFlash(Camera.Parameters.FLASH_MODE_OFF);
			break;
		case R.id.layout_flash_auto:
			position = 2;
			CameraManager.get().setFlash(Camera.Parameters.FLASH_MODE_AUTO);
			break;
		case R.id.layout_flash_open: 
			position = 3;
			CameraManager.get().setFlash(Camera.Parameters.FLASH_MODE_ON);
			break;
		case R.id.layout_light:
			position = 4;
			CameraManager.get().setFlash(Camera.Parameters.FLASH_MODE_TORCH);
			break;
		case R.id.button5:
			mContainer.setVisibility(View.VISIBLE);
			mContainer2.setVisibility(View.GONE);
			break;
		case R.id.btn_take:  
		//	AutoFocusManager.getAutoFocusManager().cancel();
			if(mCameraLayout != null){
				mCameraLayout.resetShapeFocus(Color.GREEN);
			}
			Logger.debugPrint("btn_take");
			CameraManager.get().takePicture();
		//	CameraManager.get().requestAutoFocus(mHandler, CameraHandler.AUTOFOCUS);
			break;
		case R.id.btn_choice:  
			openPhoto();
			break;
		case R.id.btn_cancel:  
			resetCusorFocus();
			CameraManager.get().startPreview(true);
			mShowPic.setVisibility(View.GONE);
			break;
		case R.id.btn_save:     
			showProgressDialog();
			new Thread(new Runnable() {
				@Override
				public void run() {
					generateWatermarkData(0);
					 float xRatio = (float)mLocation[0] / mCameraPreview.getmWidth();
					 float yRatio = (float)mLocation[1] / mCameraPreview.getmHeight();
					 float scale = (float)mCurrentBitmap.getWidth() / mCameraPreview.getmWidth();
					 
					mLocation[0] = (int) (mOriginalBitmap.getWidth() * xRatio);
					mLocation[1] = (int) (mOriginalBitmap.getHeight() * yRatio);
					Logger.debugPrint("onPictureTaken"+" locx="+mLocation[0]+" locy="+mLocation[1]);
					 
					Logger.debugPrint("onPictureTaken"+" save mOriginalBitmap width="+mOriginalBitmap.getWidth()+" height="+mOriginalBitmap.getHeight());
					Logger.debugPrint("onPictureTaken"+" save mCurrentBitmap width="+mCurrentBitmap.getWidth()+" height="+mCurrentBitmap.getHeight());
					Bitmap newBitmap = BitmapUtil.createBitmap(mOriginalBitmap, mCurrentBitmap,mLocation[0],mLocation[1], scale);
					Logger.debugPrint("onPictureTaken"+" save width="+newBitmap.getWidth()+" height="+newBitmap.getHeight());
					ContentResolver resolver = getContentResolver();
					String fileName = System.currentTimeMillis() + "";
					String filePath = CommonUtil.writeFile(fileName + "", CommonUtil.Bitmap2Bytes(newBitmap));
					int fileLength = (int)(new File(filePath).length());
					int what = CameraHandler.SAVE_BITMAP_SUCCESS;
				
					Uri uri = CommonUtil.insertBitmap(getApplicationContext(), resolver, fileName, filePath, fileLength, mOrientation);
					if(uri == null){
						filePath = null;
						what = CameraHandler.SAVE_BITMAP_FAIL;
					}else{
						Message msg = Message.obtain();
						Bundle bundle = new Bundle();
						bundle.putString("file_path", filePath);
						bundle.putString("file_name", fileName);
						msg.setData(bundle);
						msg.what = what;
						mHandler.sendMessage(msg);
					}
				}
			}).start();
			break;
		case R.id.btn_rotate:  
			try {
				if(CameraManager.get().getOppositeCameraFace() != -1){
					CameraManager.get().stopPreview(false);
					CameraManager.get().closeDriver();
				}
				switchCamera(false);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		if(position != -1){
			setCapabilitie(position);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	private Bitmap mOriginalBitmap;
	
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		  Logger.debugPrint("onPictureTaken");
		  CameraManager.get().stopPreview(true);
		  CameraManager.get().setOrientation(mOrientation);
		  clearCusorFocus();
		  Options options = new Options();
		//  options. = Bitmap.Config.ARGB_8888;
		  Parameters parameters = camera.getParameters();
		  Bitmap bitmap = BitmapUtil.decodeSampledBitmap(data, options, parameters.getPictureSize().height, parameters.getPictureSize().width);
		//  Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		  Logger.debugPrint("onPictureTaken"+"bitmap w="+bitmap.getWidth()+" h="+bitmap.getHeight()+" width="+parameters.getPictureSize().width+" height="+parameters.getPictureSize().height);
		  
		 int tag = ExifInterface.ORIENTATION_ROTATE_90;//getExifOrientation(CameraManager.get().getOrientation());
			int degree=0;
			if (tag == ExifInterface.ORIENTATION_ROTATE_90) {
				degree = 90;
			} else if (tag == ExifInterface.ORIENTATION_ROTATE_180) {
				degree = 180;
			} else if (tag == ExifInterface.ORIENTATION_ROTATE_270) {
				degree = 270;
			}
			if (degree != 0 && bitmap != null) {
				Matrix m = new Matrix();
				m.setRotate(degree, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
				mOriginalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
				bitmap.recycle();
			}
	}
	
	private static int getExifOrientation(int orientation) {
        orientation = (orientation + 360) % 360;
        switch (orientation) {
            case 0:
                return ExifInterface.ORIENTATION_NORMAL;
            case 90:
                return ExifInterface.ORIENTATION_ROTATE_90;
            case 180:
                return ExifInterface.ORIENTATION_ROTATE_180;
            case 270:
                return ExifInterface.ORIENTATION_ROTATE_270;
            default:
                throw new AssertionError("invalid: " + orientation);
        }
    }
	
	private int mOrientation;

	class CameraOrientationEventListener extends OrientationEventListener{

		public CameraOrientationEventListener(Context context) {
			super(context);
		}
		@Override
		public void onOrientationChanged(int orientation) {
		    if (orientation == ORIENTATION_UNKNOWN) {
                return;
            }
		    mOrientation = roundOrientation(orientation, mOrientation);
            Configuration config = getResources().getConfiguration();
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
		}
	}
	
	// Orientation hysteresis amount used in rounding, in degrees
    public static final int ORIENTATION_HYSTERESIS = 5;
	
	/**
     * Rounds the orientation so that the UI doesn't rotate if the user
     * holds the device towards the floor or the sky
     * @param orientation        New orientation
     * @param orientationHistory Previous orientation
     * @return Rounded orientation
     */
    public static int roundOrientation(int orientation, int orientationHistory) {
        boolean changeOrientation = false;
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            dist = Math.min(dist, 360 - dist);
            changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
        }
        if (changeOrientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        }
        return orientationHistory;
    }

	
	private static int mRotation = 90;
	
	/**
     * Returns the orientation of the display
     * In our case, since we're locked in Landscape, it should always
     * be 90
     *
     * @param activity
     * @return Orientation angle of the display
     */
    public static int getDisplayRotation(Activity activity) {
        if (activity != null) {
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                    mRotation = 0;
                    break;
                case Surface.ROTATION_90:
                    mRotation = 90;
                    break;
                case Surface.ROTATION_180:
                    mRotation = 180;
                    break;
                case Surface.ROTATION_270:
                    mRotation = 270;
                    break;
            }
        }
        return mRotation;
    }
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		generateWatermarkData(position);
	}
	
	private Bitmap mCurrentBitmap;
	private int[] mLocation;
	
	private void generateWatermarkData(int position) {
		
		View view = mViews.get(position);
		ImageView iv = (ImageView) view.findViewById(R.id.iv_watermark);
		if(mLocation != null){
			mLocation[0] = 0;
			mLocation[1] = 0;
		}
		mLocation[0] = iv.getLeft();
		mLocation[1] = iv.getBottom();
		Log.i("aaaaaaaaaaaaaaaaabc", "mLocation: "+mLocation[1]);
		mCurrentBitmap = ((BitmapDrawable)iv.getDrawable()).getBitmap();
		mLocation[1] = mLocation[1] - iv.getDrawable().getBounds().bottom;
		Log.i("aaaaaaaaaaaaaaaaabc", "mLocation: "+mLocation[1]);
		Log.i("aaaaaaaaaaaaaaaaabc", "x: "+mCurrentBitmap.getWidth()+"y: "+mCurrentBitmap.getHeight());
		Log.i("aaaaaaaaaaaaaabc", "ix: "+iv.getWidth()+"iy: "+iv.getHeight());
	}
	
	public class MyAdapter extends PagerAdapter {

		@Override  
        public boolean isViewFromObject(View arg0, Object arg1) {  
            return arg0 == arg1;  
        }  
        @Override  
        public int getCount() {  
            return mViews.size();  
        }  
        @Override  
        public void destroyItem(ViewGroup container, int position, Object object) {  
            container.removeView(mViews.get(position));  
        }  
        
        @Override  
        public int getItemPosition(Object object) {  
            return super.getItemPosition(object);  
        }  

        @Override  
        public Object instantiateItem(ViewGroup container, int position) {  
        	container.addView(mViews.get(position), 0);//���ҳ��  
            return mViews.get(position);  
        } 
	}
	
}
