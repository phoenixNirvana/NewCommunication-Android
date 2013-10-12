/*
 * Copyright (C) 2008 ZXing authors
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


import com.leyingke.paizhao.utils.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraHandler extends Handler {

  private static final String TAG = CameraHandler.class.getSimpleName();

  public static final int AUTOFOCUS = 1;
  public static final int RESTART_PREVIEW = 2;
  public static final int DECODE_SUCCEEDED = 3;
  public static final int DECODE_FAILED = 4;
  public static final int SAVE_BITMAP_SUCCESS = 5;
  public static final int SAVE_BITMAP_FAIL = 6;
  public static final int AUTOFOCUS_CONTINUOUS = 7;
  
  private final CameraActivity activity;
  private State state;

  private enum State {
    PREVIEW,
    SUCCESS,
    DONE
  }

  public CameraHandler(CameraActivity activity) {
    this.activity = activity;
    state = State.SUCCESS;
  }

//  private boolean isFirstTime = true;
  
 /* public void setFirstTime(boolean flag){
	  isFirstTime = flag;
  }
  */
  @Override
  public void handleMessage(Message message) {
    switch (message.what) {
      case AUTOFOCUS:
    	  try{
    		 boolean isfocus = (Boolean) message.obj;
    		 if(isfocus){
    			  if(activity != null)
    				  activity.startFocusAni();
    		 }else{
    			CameraManager.get().requestAutoFocus(this);
    		 }
    	  }catch(Exception e){
    		  e.printStackTrace();
    	  }
        Logger.debugPrint(TAG, "AUTOFOCUS isSuccess="+message.obj);
        break;
      case AUTOFOCUS_CONTINUOUS:
    	  boolean isfocus = (Boolean) message.obj;
 		 if(isfocus){
 			  if(activity != null)
 				  activity.startFocusAni();
 		 }else{
 			CameraManager.get().requestAutoFocus(this);
 		 }
        Logger.debugPrint(TAG, "AUTOFOCUS_CONTINUOUS isSuccess="+message.obj);
        break;  
      case RESTART_PREVIEW:
        Log.d(TAG, "Got restart preview message");
        break;
      case DECODE_SUCCEEDED:
        Log.d(TAG, "Got decode succeeded message");
        state = State.SUCCESS;
        Bundle bundle = message.getData();
        break;
      case DECODE_FAILED:
        state = State.PREVIEW;
        break;
      case SAVE_BITMAP_SUCCESS:
    	  activity.closeProgressDialog();
    	  Bundle data = message.getData();
    	  String filePath = data.getString("file_path");
    	  String fileName = data.getString("file_name");
    	  Intent intent = new Intent(activity, EffectActivity.class);
    	  intent.putExtra("file_path", filePath);
    	  intent.putExtra("file_name", fileName);
    	  activity.startActivity(intent);
    	  break;
      case SAVE_BITMAP_FAIL:
    	  activity.closeProgressDialog();
    	//  CameraManager.get().startPreview(true);
    	  break;
    }
  }

  public void quitSynchronously() {
    state = State.DONE;
  }
  
}
