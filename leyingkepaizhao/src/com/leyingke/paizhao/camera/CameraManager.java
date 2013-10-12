package com.leyingke.paizhao.camera;

import java.io.IOException;
import java.util.TimerTask;

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
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;

import com.leyingke.paizhao.utils.Logger;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraManager {

	private static final String TAG = CameraManager.class.getSimpleName();

	private final Context context;
	private final CameraConfigurationManager configManager;
	private Camera camera;
	private boolean isCameraReady;
	private boolean previewing;
	
	private CameraHandler mHandler;
	private SurfaceHolder mSurfaceHolder;
	private final boolean useOneShotPreviewCallback;

	private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;

	private int mCurrentFacing;

	private OnPreviewListener onPreviewListener;

	private static CameraManager cameraManager;

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

	/**
	 * Preview frames are delivered here, which we pass on to the registered
	 * handler. Make sure to clear the handler so it will only receive one
	 * message.
	 */
	private final PreviewCallback previewCallback;
	/**
	 * Autofocus callbacks arrive here, and are dispatched to the Handler which
	 * requested them.
	 */
	private final AutoFocusCallback autoFocusCallback;

	/**
	 * Initializes this static object with the Context of the calling Activity.
	 * 
	 * @param context
	 *            The Activity which wants to use the camera.
	 */
	public static void init(Context context) {
		if (cameraManager == null) {
			cameraManager = new CameraManager(context);
		}
	}

	public static CameraManager get() {
		return cameraManager;
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
    
	private CameraManager(Context context) {
		this.context = context;
		this.configManager = new CameraConfigurationManager(context);
		mCurrentFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
		// Camera.setOneShotPreviewCallback() has a race condition in Cupcake,
		// so we use the older
		// Camera.setPreviewCallback() on 1.5 and earlier. For Donut and later,
		// we need to use
		// the more efficient one shot callback, as the older one can swamp the
		// system and cause it
		// to run out of memory. We can't use SDK_INT because it was introduced
		// in the Donut SDK.
		// useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) >
		// Build.VERSION_CODES.CUPCAKE;
		useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > 3; // 3
		previewCallback = new PreviewCallback(configManager,
				useOneShotPreviewCallback);
		autoFocusCallback = new AutoFocusCallback();
	}

	/**
	 * Opens the camera driver and initializes the hardware parameters.
	 * @param holder
	 *            The surface object which the camera will draw preview frames
	 *            into.
	 * @throws IOException
	 *             Indicates the camera driver failed to open.
	 */
	public void openDriver() throws IOException {
		
		releaseCamera();
		if (camera == null) {
			camera = Camera.open(mCurrentFacing);
		//	mCurrentFacing = cameraId;
			if (camera == null) {
				throw new IOException();
			}
			configManager.initCameraParameters(camera);
			isCameraReady = true;
			FlashlightManager.enableFlashlight();
		}
	}
	
	public void setPreviewDisplay(SurfaceHolder surfaceHolder){
		if(camera != null){
			mSurfaceHolder = surfaceHolder;
			try {
				camera.setPreviewDisplay(mSurfaceHolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Closes the camera driver if still in use.
	 */
	public void closeDriver() {
		if (camera != null) {
			FlashlightManager.disableFlashlight();
			camera.release();
			camera = null;
		}
	}

	/**
	 * Asks the camera hardware to begin drawing preview frames to the screen.
	 */
	public void startPreview(boolean isChangeBtnState) {
		if (camera != null && !previewing) {
		//	Camera.Parameters parameter = camera.getParameters();
		//	Logger.debugPrint(TAG, parameter.flatten());
		//	camera.setParameters(parameter);
		//	requestPreviewFrame(mHandler,CameraHandler.AUTOFOCUS);
		//	AutoFocusManager.getAutoFocusManager().cancel();
			camera.startPreview();
			previewing = true;
			requestAutoFocus(mHandler);
			if (onPreviewListener != null && isChangeBtnState) {
				onPreviewListener.onPreview(previewing);
			}
		}
	}

	/**
	 * Tells the camera to stop drawing preview frames.
	 */
	public  void stopPreview(boolean isChangeBtnState) {
		if (camera != null && previewing) {
		//	AutoFocusManager.getAutoFocusManager().cancel();
		/*	if(mHandler != null)
				 mHandler.setFirstTime(true);*/
			camera.stopPreview();
			previewing = false;
			if (onPreviewListener != null && isChangeBtnState) {
				onPreviewListener.onPreview(previewing);
			}
		}
	}
	
	Parameters parameters;
	
	private void releaseCamera() {
		if (camera != null) {
            Log.v(TAG, "Releasing camera facing " + mCurrentFacing);
            camera.release();
            parameters = null;
            camera = null;
            previewing = false;
        }
    }
	
	public synchronized void setPreviewSize(Size size){
		  if(camera != null){
			  parameters = camera.getParameters();
			  Logger.debugPrint("aaaaaaaaaaaaaaaaaaaaaa", "  size.height ="+size.height + " size.width="+size.width);
			  parameters.setPreviewSize(size.width, size.height);
			  parameters.setPictureSize(size.width, size.height);
			  camera.setParameters(parameters);
			//  camera.startPreview();
		  }
	}
	
	public Camera getCamera(){
		return camera;
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
 		// Rotate the pictures accordingly (display is kept at 90 degrees)
		Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		Camera.getCameraInfo(mCurrentFacing, info);
		// orientation = (360 - orientation + 45) / 90 * 90;
		int rotation = 0;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			rotation = (info.orientation - orientation + 360) % 360;
		} else { // back-facing camera
			rotation = (info.orientation + orientation) % 360;
		}
		// setParameterAsync("rotation", Integer.toString(rotation));
	}
	
	public Parameters getParameters(){
		return camera.getParameters();
	}

	public int getOrientation() {
		return mOrientation;
	}
	
	
	public interface PreviewPauseListener {
        /**
         * This method is called when the preview is about to pause.
         * This allows the CameraActivity to display an animation when the preview
         * has to stop.
         */
        public void onPreviewPause();

        /**
         * This method is called when the preview resumes
         */
        public void onPreviewResume();
    }

    public interface CameraReadyListener {
        /**
         * Called when a camera has been successfully opened. This allows the
         * main activity to continue setup operations while the camera
         * sets up in a different thread.
         */
        public void onCameraReady();

        /**
         * Called when the camera failed to initialize
         */
        public void onCameraFailed();
    }
	
	private PictureCallback pictureCallback;

	public void setPictureCallback(PictureCallback pictureCallback) {
		this.pictureCallback = pictureCallback;
	}

	private ToneGenerator tone;
	
	public void takePicture() {
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

	/**
	 * A single preview frame will be returned to the handler supplied. The data
	 * will arrive as byte[] in the message.obj field, with width and height
	 * encoded as message.arg1 and message.arg2, respectively.
	 * @param handler
	 *            The handler to send the message to.
	 * @param message
	 *            The what field of the message to be sent.
	 */
	public void requestPreviewFrame(Handler handler, int message) {
		if (camera != null && previewing) {
			previewCallback.setHandler(handler, message);
			if (useOneShotPreviewCallback) {
				camera.setOneShotPreviewCallback(previewCallback);
			} else {
				camera.setPreviewCallback(previewCallback);
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
	
	public void setFlash(String mode){
		if(camera != null){
		  Camera.Parameters parameter = camera.getParameters();
		  configManager.setFlash(parameter, mode);
		  camera.setParameters(parameter);
		}
	}
	
	public interface OnPreviewListener {
		void onPreview(boolean preview);
	}

}
