package com.lechat.ui.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lechat.R;
import com.lechat.adapter.WaterMarkAdapter;
import com.lechat.camera.data.CameraHandler;
import com.lechat.camera.data.CameraManager;
import com.lechat.camera.data.CameraManager.OnPreviewListener;
import com.lechat.camera.data.SlidBarBuildHelper;
import com.lechat.camera.data.SlidBarBuildHelper.ClickListener;
import com.lechat.camera.data.SlidBarItem;
import com.lechat.camera.data.WaterMarkBuildHelp;
import com.lechat.camera.data.WaterMarkHelper;
import com.lechat.camera.data.WaterMarkPosition;
import com.lechat.camera.imagefilter.api.BitmapFilter;
import com.lechat.camera.utils.BitmapUtil;
import com.lechat.camera.widget.BottomView;
import com.lechat.camera.widget.CameraLayout;
import com.lechat.camera.widget.CameraPreview;
import com.lechat.camera.widget.WaterMarkGrallery;
import com.lechat.utils.CommonUtil;
import com.lechat.utils.Logger;

public class CameraActivity extends BaseActivity implements OnClickListener,PictureCallback
														,OnItemSelectedListener{
	private static final int PHOTO_ALBUM = 1;
	public static boolean CONTINUE_PIC = false;
	private int mCurrentPosition = 2; 
	private boolean isInitlized = false;
	private int mOrientation;
	private WaterMarkAdapter mWaterMarkAdapter;
	private CameraHandler mHandler;
	private ImageView mShowPic;
	private ImageView mBtnReplace;
	private BottomView mBottomView;
	private LinearLayout mContainer;
	private LinearLayout mContainer2;
	private LinearLayout mFlashOff, mFlashAuto, mFlashOpen, mFlashLight;
	private CameraLayout mCameraLayout;
	private WaterMarkGrallery mWaterMarkGrallery;
	private WaterMarkBuildHelp mWaterMarkBuildHelp;
	private CameraPreview mCameraPreview;
	private SlidBarBuildHelper mSlidBarBuildHelper;
	private CameraOrientationEventListener mCameraOrientationEventListener;
	private LinearLayout mLayoutMan;
	private WaterMarkHelper mWaterMarkHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.act_camera);
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
		
		mBottomView = (BottomView) findViewById(R.id.view_bottomview);
		mCameraLayout = (CameraLayout) findViewById(R.id.cl_cameralayout);
		mCameraPreview = (CameraPreview) findViewById(R.id.view_camerapreview);
		
		mWaterMarkGrallery = (WaterMarkGrallery) findViewById(R.id.gl_effect);
		mWaterMarkGrallery.setOnItemSelectedListener(this);
		
		mFlashOff.setOnClickListener(this);
		mFlashAuto.setOnClickListener(this);
		mFlashOpen.setOnClickListener(this);
		mFlashLight.setOnClickListener(this);
		mContainer2.setOnClickListener(this);
		
		mBottomView.setViewListener(this);
		
		CameraManager.init(getApplication());
		CameraManager.get().setPictureCallback(this);
		CameraManager.get().setOnPreviewListener(new OnPreviewListener() {
			@Override
			public void onPreview(boolean preview) {
			}
		});
		mHandler = new CameraHandler(this);
		CameraManager.get().setHandler(mHandler);
		
		mCameraOrientationEventListener = new CameraOrientationEventListener(this);
		
		mWaterMarkBuildHelp = new WaterMarkBuildHelp(this);
		mWaterMarkAdapter = new WaterMarkAdapter(this, mWaterMarkBuildHelp.getWaterMarkView());
		mWaterMarkGrallery.setAdapter(mWaterMarkAdapter);
		mWaterMarkGrallery.setSelection(0);
		mSlidBarBuildHelper = new SlidBarBuildHelper();
		
		List<SlidBarItem> items = new ArrayList<SlidBarItem>();
		items.add(new SlidBarItem("原始",-1));
		items.add(new SlidBarItem("黑白",BitmapFilter.GRAY_STYLE));
		items.add(new SlidBarItem("怀旧",BitmapFilter.OLD_STYLE));
		items.add(new SlidBarItem("冰冻",BitmapFilter.ICE_STYLE));
		items.add(new SlidBarItem("版画",BitmapFilter.BLOCK_STYLE));
		items.add(new SlidBarItem("LOMO",BitmapFilter.LOMO_STYLE));
		mLayoutMan = (LinearLayout) findViewById(R.id.layout_main);
		mWaterMarkHelper = new WaterMarkHelper();
		mSlidBarBuildHelper.setSlidBarData(items);
		mSlidBarBuildHelper.setContext(this);
		mSlidBarBuildHelper.setListener(new ClickListener() {
			@Override
			public void onClick(int pos) {
				new ImageFilterTask(CameraActivity.this,pos).execute();
			}
		});
	}
	
	private void updateBtnState(boolean preview){
		mBottomView.changeLayout(preview);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Logger.debugPrint("onResume");
		mCameraOrientationEventListener.enable();
		if(CONTINUE_PIC){
			continuePic();
		}
		if(!isInitlized){
			initCamera();
			isInitlized = true;
		}else{
			if(isPause){
				isPause = false;
				switchCamera(false);
			}
		}
	}
	
	public void hideShowPicView(){
		if(mShowPic != null)
			   mShowPic.setVisibility(View.GONE);
	}
	
	public void clearCusorFocus(){
		if(mCameraLayout != null)
			mCameraLayout.clearShapeFocus();
	}
	
	public void resetCusorFocus(){
		if(mCameraLayout != null)
			mCameraLayout.resetShapeFocus();
	}
	
	private void initCamera(){
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
	}
	
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
	
	private boolean isPause = false;
	
	@Override
	protected void onPause() {
		super.onPause();
		Logger.debugPrint("onPause");
		if(mSlidBarBuildHelper != null)
		    mSlidBarBuildHelper.closeSlidBar();
		mCameraOrientationEventListener.disable();
		isPause = true;
		clearCusorFocus();
		CameraManager.get().stopPreview(true);
		CameraManager.get().closeDriver();
	}

	private void releaseBitmap(){
		if(mOriginalBitmap != null)
			mOriginalBitmap.recycle();
		if(mResultBitmap != null)
			mResultBitmap.recycle();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		isInitlized = false;
	}
	
	public void startFocusAni(){
		if(mCameraLayout != null){
			mCameraLayout.doFocusAni();
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if(mBottomView != null && mBottomView.isSaveState()){
			continuePic();
			return ;
		}
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
			if(mCameraLayout != null){
				mCameraLayout.resetShapeFocus(Color.GREEN);
			}
			Logger.debugPrint("btn_take");
			CameraManager.get().takePicture();
			break;
		case R.id.btn_choice:  
			openPhoto();
			break;
		case R.id.btn_cancel:  
			continuePic();
			break;
		case R.id.btn_save:     
			showProgressDialog();
			savePic();
			break;
		case R.id.btn_watermark:
			if(mSlidBarBuildHelper != null)
			    mSlidBarBuildHelper.showSlidBar(mBottomView,mLayoutMan);
			break;
		case R.id.btn_rotate:  
			try {
				if(CameraManager.get().getOppositeCameraFace() != -1){
					CameraManager.get().stopPreview(false);
					CameraManager.get().closeDriver();
				}
				hideShowPicView();
				switchCamera(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		if(position != -1){
			setCapabilitie(position);
		}
	}
	
	public void continuePic(){
		resetCusorFocus();
		CameraManager.get().startPreview(true);
		hideShowPicView();
		releaseBitmap();
		updateBtnState(false);
	}
	
	public void savePic(){
		final List<WaterMarkPosition> waterMarkPos = mWaterMarkHelper.getWaterMarkPosition((View)mWaterMarkAdapter.getItem(mSelectPos));
		new Thread(new Runnable() {
			@Override
			public void run() {
				int viewWidth = mCameraLayout.getWidth();
				int viewHeight = mCameraLayout.getHeight();
				Bitmap newBitmap = BitmapUtil.composeBitmap(viewWidth,viewHeight,mResultBitmap,waterMarkPos);
				Logger.debugPrint("onPictureTaken"+" save width="+newBitmap.getWidth()+" height="+newBitmap.getHeight());
				ContentResolver resolver = getContentResolver();
				String fileName = System.currentTimeMillis() + "";
				String filePath = CommonUtil.writeFile(fileName + "", CommonUtil.Bitmap2Bytes(newBitmap));
				if(newBitmap != null)
					newBitmap.recycle();
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
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	private Bitmap mOriginalBitmap;
	private Bitmap mResultBitmap;
	
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
				mResultBitmap = mOriginalBitmap;
				bitmap.recycle();
			}
			updateBtnState(true);
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
	
	private void changeOrientation(int rotation){
		/*  if(mBottomView != null){
			  mBottomView.setRotate(rotation);
		  }
		  if(mSlidBarBuildHelper != null){
			  mSlidBarBuildHelper.setRotate(rotation);
			  mSlidBarBuildHelper.closeSlidBar();			  
		  }*/
	}
	
	class CameraOrientationEventListener extends OrientationEventListener{

		public CameraOrientationEventListener(Context context) {
			super(context);
		}
		@Override
		public void onOrientationChanged(int orientation) {
		    
			if (orientation == ORIENTATION_UNKNOWN) {
                return;
            }
		    int or = CommonUtil.roundOrientation(orientation, mOrientation);
		    if(or == mOrientation){
		    	return ;
		    }else{
		    	mOrientation = or;
		    	if(mOrientation == 270){
		    		changeOrientation(90);
		    		System.out.println("mOrientaion="+mOrientation);
		    	}else if(mOrientation == 0){
		    		changeOrientation(-90);
		    		System.out.println("mOrientaion="+mOrientation);
		    	}
		    }
		}
	}
	
	
	private static int mRotation = 90;
	
	/**
     * Returns the orientation of the display
     * In our case, since we're locked in Landscape, it should always
     * be 90
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
	
	private int mSelectPos;
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		synchronized (Object.class) {
			mSelectPos = arg2;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}
	
	public View getSelectView(){
		return (View) mWaterMarkAdapter.getItem(mSelectPos);
	}
	
	public class ImageFilterTask extends AsyncTask<Void, Void, Bitmap> {

		private Activity activity = null;
        private int mPos;
		public ImageFilterTask(Activity activity, int pos) {
			this.activity = activity;
			this.mPos = pos;
		}

		public Bitmap doInBackground(Void... params) {
			try {
				Bitmap bitmap = mOriginalBitmap;
			    return BitmapFilter.changeStyle(bitmap, mPos);
			} catch (Exception e) {
			} finally {
			}
			return null;
		}
		@Override
		protected void onPostExecute(Bitmap result) {
			System.out.println("check result="+result);
			if (result != null) {
				super.onPostExecute(result);
				mResultBitmap = result;
				mShowPic.setVisibility(View.VISIBLE);
				mShowPic.setImageBitmap(result);
				closeProgressDialog();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressDialog();
		}
	}
}
