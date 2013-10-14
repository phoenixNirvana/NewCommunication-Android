package com.leyingke.paizhao.camera.data;

import java.io.IOException;

import com.leyingke.paizhao.interfaces.AutoFocusCallback;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;

import com.leyingke.paizhao.utils.Logger;

public final class CameraManager {

	private static final String TAG = CameraManager.class.getSimpleName();

	private int mCurrentFacing;
	private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
	private boolean previewing;
	private boolean isCameraReady;
	private final boolean useOneShotPreviewCallback;
	
	private Camera camera;
	private CameraHandler mHandler;
	private Parameters parameters;
	private SurfaceHolder mSurfaceHolder;
	private OnPreviewListener onPreviewListener;
	private final AutoFocusCallback autoFocusCallback;
	private static CameraManager cameraManager;
	private final CameraConfigurationManager configManager;
	
	private PictureCallback pictureCallback;
	private ToneGenerator tone;
	
	private Object configObj = new Object();
	private Object mannObj = new Object();
	
	static final int SDK_INT; // Later we can use Build.VERSION.SDK_INT
	static {
		int sdkInt;
		try {
			sdkInt = Integer.parseInt(Build.VERSION.SDK);
		} catch (NumberFormatException nfe) {
			// Just to be safe
			sdkInt = 10000;
		}
		SDK_INT = sdkInt;
	}

	private Handler handler;
	
    public static void init(Context context) {
		if (cameraManager == null) {
			cameraManager = new CameraManager(context);
		}
	}
	
	public static CameraManager get() {
		return cameraManager;
	}
	
    private CameraManager(Context context) {
		this.configManager = new CameraConfigurationManager(context);
		mCurrentFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
		useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > 3; // 3
		autoFocusCallback = new AutoFocusCallback();
	}
	
    public int getCurrentFacing() {
        return mCurrentFacing;
    }

    public int getOppositeCameraFace(){
    	 int numberOfCameras = configManager.getCameraNumbers();
    	 if(numberOfCameras > 1){
    		 mCurrentFacing = (mCurrentFacing + 1) % numberOfCameras;
    	 }else{
    		 return -1;
    	 }
    	 return mCurrentFacing;
    }
    
	public void openDriver() throws IOException {
		synchronized (mannObj) {
			if (camera == null) {
				camera = Camera.open(mCurrentFacing);
				if (camera == null) {
					throw new IOException();
				}
				configManager.initCameraParameters(camera);
				isCameraReady = true;
				FlashlightManager.enableFlashlight();
			}
		}
	}
	
	public void setPreviewDisplay(SurfaceHolder surfaceHolder){
        synchronized (mannObj) {
        	if(camera != null){
        		mSurfaceHolder = surfaceHolder;
        		try {
        			camera.setPreviewDisplay(mSurfaceHolder);
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	}
		}
	}
	
	/**
	 * Closes the camera driver if still in use.
	 */
	public void closeDriver() {
        synchronized (mannObj) {
        	if (camera != null) {
        		FlashlightManager.disableFlashlight();
        		camera.release();
        		camera = null;
        	}
		}
	}

	/**
	 * Asks the camera hardware to begin drawing preview frames to the screen.
	 */
	public void startPreview(boolean isChangeBtnState) {
        synchronized (mannObj) {
        	if (camera != null && !previewing) {
        		camera.startPreview();
        		previewing = true;
        	}else{
        		return;
        	}
		}
        requestAutoFocus(mHandler);
        if (onPreviewListener != null && isChangeBtnState) {
        	onPreviewListener.onPreview(previewing);
        }
	}

	/**
	 * Tells the camera to stop drawing preview frames.
	 */
	public  void stopPreview(boolean isChangeBtnState) {
		 synchronized (mannObj) {
			 if (camera != null && previewing) {
				 camera.stopPreview();
				 previewing = false;
			 }else{
				 return;
			 }
		 }
		 previewing = false;
		 if (onPreviewListener != null && isChangeBtnState) {
			 onPreviewListener.onPreview(previewing);
		 }
	}
	
	public void setPreviewSize(Size size){
		synchronized (mannObj) {
		  if(camera != null){
			  parameters = camera.getParameters();
			  Logger.debugPrint("aaaaaaaaaaaaaaaaaaaaaa", "  size.height ="+size.height + " size.width="+size.width);
			  parameters.setPreviewSize(size.width, size.height);
			  parameters.setPictureSize(size.width, size.height);
			  camera.setParameters(parameters);
		  }else{
			  return;
		  }
		}
	}
	
	public Camera getCamera(){
		synchronized (mannObj) {
			return camera;
		}
	}
	
	public boolean isAutoContinuous(){
		String focusMode = configManager.getFocusMode();
		if("continuous-picture".equals(focusMode)){
			return true;
		}
		return false;
	}

	
	public OnPreviewListener getOnPreviewListener() {
		return onPreviewListener;
	}

	public void setOnPreviewListener(OnPreviewListener onPreviewListener) {
		this.onPreviewListener = onPreviewListener;
	}
	
	public synchronized Size getOptimalPreviewSize(int w,int h){
		Logger.debugPrint("CameraPreview", "  camera="+camera);
		return configManager.getOptimalPreviewSize(camera, w, h);
	}
	
	public void setHandler(CameraHandler handler){
		mHandler = handler;
	}
	
	/**
	 * Sets the current orientation of the device
	 * @param orientation
	 *            The orientation, in degrees
	 */
	public void setOrientation(int orientation) {
		orientation += 90;
		if (mOrientation == orientation)
			return;
		Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		Camera.getCameraInfo(mCurrentFacing, info);
		int rotation = 0;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			rotation = (info.orientation - orientation + 360) % 360;
		} else { // back-facing camera
			rotation = (info.orientation + orientation) % 360;
		}
	}
	
	public Parameters getParameters(){
		return camera.getParameters();
	}

	public int getOrientation() {
		return mOrientation;
	}
	
	
	public interface PreviewPauseListener {
       
        public void onPreviewPause();
        public void onPreviewResume();
    }

    public interface CameraReadyListener {
       
        public void onCameraReady();
        public void onCameraFailed();
    }
	
	public void setPictureCallback(PictureCallback pictureCallback) {
		this.pictureCallback = pictureCallback;
	}

	public void takePicture() {
        synchronized (mannObj) {
        	if (camera != null){
        		camera.takePicture(new ShutterCallback(){
        			@Override
        			public void onShutter() {
        				if(tone == null)
        					//发出提示用户的声音
        					tone = new ToneGenerator(AudioManager.STREAM_DTMF,
        							ToneGenerator.MIN_VOLUME);
        				tone.startTone(ToneGenerator.TONE_PROP_BEEP);
        			}
        		}, null, pictureCallback);
        	}
		}
	}

	/**
	 * Asks the camera hardware to perform an autofocus.
	 * @param handler
	 *            The Handler to notify when the autofocus completes.
	 * @param message
	 *            The message to deliver.
	 */
	public void requestAutoFocus(CameraHandler handler) {
		if(TextUtils.isEmpty(configManager.getFocusMode())){
			return;
		}
	    synchronized (mannObj) {
	    	if (camera != null && previewing) {
	    		if(!isAutoContinuous()){
	    			Logger.debugPrint("aaaaaaaaaaaaaaaaaaaaaa", "requestAutoFocus  AUTOFOCUS" );
	    			autoFocusCallback.setHandler(handler, CameraHandler.AUTOFOCUS);
	    		}else{
	    			Logger.debugPrint("aaaaaaaaaaaaaaaaaaaaaa", "requestAutoFocus  AUTOFOCUS_CONTINUOUS" );
	    			autoFocusCallback.setHandler(handler, CameraHandler.AUTOFOCUS_CONTINUOUS);
	    		}
	    		camera.autoFocus(autoFocusCallback);
	    	}
		}
	}
	
	public void setFlash(String mode){
		Camera.Parameters parameter;
		synchronized (mannObj) {
			if(camera != null){
				parameter = camera.getParameters();
				configManager.setFlash(parameter, mode);
				camera.setParameters(parameter);
			}else{
				return;
			}
		}
	}
	
	public interface OnPreviewListener {
		void onPreview(boolean preview);
	}

}
