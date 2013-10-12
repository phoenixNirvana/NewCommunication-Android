/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.leyingke.paizhao.camera;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.leyingke.paizhao.utils.Logger;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

final class CameraConfigurationManager {

	private static final String TAG = CameraConfigurationManager.class
			.getSimpleName();

	private static final int TEN_DESIRED_ZOOM = 27;
	private static final int DESIRED_SHARPNESS = 30;

	private static final Pattern COMMA_PATTERN = Pattern.compile(",");

	private String mFocusMode;
	private final Context context;
	private String previewFormatString;
	private int numberOfCameras; 
	
	CameraConfigurationManager(Context context) {
		this.context = context;
	}

	/**
	 * Reads, one time, values from the camera that are needed by the app.
	 */
	public void initCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		numberOfCameras = Camera.getNumberOfCameras();
		parameters.setPictureFormat(ImageFormat.JPEG);
		camera.setDisplayOrientation(90);
		setFlash(parameters);
		setZoom(parameters);
		setFocusMode(parameters);
		camera.setParameters(parameters);
	}

   protected void setDisplayOrientation(Camera camera, int angle) {
  	  Method downPolymorphic;
  	  try {
  	   downPolymorphic = camera.getClass().getMethod(
  	     "setDisplayOrientation", new Class[] { int.class });
  	   if (downPolymorphic != null)
  	    downPolymorphic.invoke(camera, new Object[] { angle });
  	  } catch (Exception e1) {
  	  }
   } 
	
   public int getCameraNumbers(){
	   return numberOfCameras;
   }

	String getPreviewFormatString() {
		return previewFormatString;
	}

	private static int findBestMotZoomValue(CharSequence stringValues,
			int tenDesiredZoom) {
		int tenBestValue = 0;
		for (String stringValue : COMMA_PATTERN.split(stringValues)) {
			stringValue = stringValue.trim();
			double value;
			try {
				value = Double.parseDouble(stringValue);
			} catch (NumberFormatException nfe) {
				return tenDesiredZoom;
			}
			int tenValue = (int) (10.0 * value);
			if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom
					- tenBestValue)) {
				tenBestValue = tenValue;
			}
		}
		return tenBestValue;
	}

	public void setFlash(Camera.Parameters parameters) {
		// FIXME: This is a hack to turn the flash off on the Samsung Galaxy.
		// And this is a hack-hack to work around a different value on the
		// Behold II
		// Restrict Behold II check to Cupcake, per Samsung's advice
		// if (Build.MODEL.contains("Behold II") &&
		// CameraManager.SDK_INT == Build.VERSION_CODES.CUPCAKE) {
		if (Build.MODEL.contains("Behold II") && CameraManager.SDK_INT == 3) { // 3
																				// =
																				// Cupcake
			parameters.set("flash-value", 1);
		} else {
			parameters.set("flash-value", 2);
		}
		// This is the standard setting to turn the flash off that all devices
		// should honor.
	//	parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
		parameters.set("flash-mode", "off");
	}
	
	public void setFlash(Camera.Parameters parameters,String mode) {
		
		if (Build.MODEL.contains("Behold II") && CameraManager.SDK_INT == 3) { // 3
																				// =
																				// Cupcake
			parameters.set("flash-value", 1);
		} else {
			parameters.set("flash-value", 2);
		}
		// This is the standard setting to turn the flash off that all devices
		// should honor.
	//	parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
		parameters.set("flash-mode", mode);
	}

	
	private void setZoom(Camera.Parameters parameters) {

		String zoomSupportedString = parameters.get("zoom-supported");
		if (zoomSupportedString != null
				&& !Boolean.parseBoolean(zoomSupportedString)) {
			return;
		}

		int tenDesiredZoom = TEN_DESIRED_ZOOM;

		String maxZoomString = parameters.get("max-zoom");
		if (maxZoomString != null) {
			try {
				int tenMaxZoom = (int) (10.0 * Double
						.parseDouble(maxZoomString));
				if (tenDesiredZoom > tenMaxZoom) {
					tenDesiredZoom = tenMaxZoom;
				}
			} catch (NumberFormatException nfe) {
				Log.w(TAG, "Bad max-zoom: " + maxZoomString);
			}
		}

		String takingPictureZoomMaxString = parameters
				.get("taking-picture-zoom-max");
		if (takingPictureZoomMaxString != null) {
			try {
				int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
				if (tenDesiredZoom > tenMaxZoom) {
					tenDesiredZoom = tenMaxZoom;
				}
			} catch (NumberFormatException nfe) {
				Log.w(TAG, "Bad taking-picture-zoom-max: "
						+ takingPictureZoomMaxString);
			}
		}

		String motZoomValuesString = parameters.get("mot-zoom-values");
		if (motZoomValuesString != null) {
			tenDesiredZoom = findBestMotZoomValue(motZoomValuesString,
					tenDesiredZoom);
		}

		String motZoomStepString = parameters.get("mot-zoom-step");
		if (motZoomStepString != null) {
			try {
				double motZoomStep = Double.parseDouble(motZoomStepString
						.trim());
				int tenZoomStep = (int) (10.0 * motZoomStep);
				if (tenZoomStep > 1) {
					tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
				}
			} catch (NumberFormatException nfe) {
				// continue
			}
		}
		// Set zoom. This helps encourage the user to pull back.
		// Some devices like the Behold have a zoom parameter
		if (maxZoomString != null || motZoomValuesString != null) {
			parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
		}
		// Most devices, like the Hero, appear to expose this zoom parameter.
		// It takes on values like "27" which appears to mean 2.7x zoom
		if (takingPictureZoomMaxString != null) {
			parameters.set("taking-picture-zoom", tenDesiredZoom);
		}
	}

	public void setFocusMode(Camera.Parameters parameters){
		
		List<String> supportFocus = parameters.getSupportedFocusModes();
		if(supportFocus.contains("continuous-picture")){
			parameters.setFocusMode("continuous-picture");
			mFocusMode = "continuous-picture";
		}else if(supportFocus.contains("auto")){
			parameters.setFocusMode("auto");
			mFocusMode = "auto";
		}
	}
	
	public String getFocusMode(){
		return mFocusMode;
	}
	
    private Size mPreviewSize;
    private List<Size> mSupportedPreviewSizes;
	
	public Size getOptimalPreviewSize(Camera camera,int w, int h){
		 Logger.debugPrint(TAG, "  getOptimalPreviewSize   camera="+camera);
		if(mSupportedPreviewSizes == null && camera != null)
			mSupportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
		mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,w,h);
		return mPreviewSize;
	 }
	 
	 private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
	        final double ASPECT_TOLERANCE = 0.1;
	        double targetRatio = (double) w / h;
	        if (sizes == null) return null;
	        Size optimalSize = null;
	        double minDiff = Double.MAX_VALUE;
	        int targetHeight = h;
	        for (Size size : sizes) {
	            double ratio = (double) size.width / size.height;
	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
	            if (Math.abs(size.height - targetHeight) < minDiff) {
	            	if(Math.abs(size.height - targetHeight) > 10){
	            		continue;
	            	}
	                optimalSize = size;
	                minDiff = Math.abs(size.height - targetHeight);
	            }
	        }
	        if (optimalSize == null) {
	            minDiff = Double.MAX_VALUE;
	            for (Size size : sizes) {
	                if (Math.abs(size.height - targetHeight) < minDiff) {
	                    optimalSize = size;
	                    minDiff = Math.abs(size.height - targetHeight);
	                }
	            }
	        }
	        return optimalSize;
	 }
}
