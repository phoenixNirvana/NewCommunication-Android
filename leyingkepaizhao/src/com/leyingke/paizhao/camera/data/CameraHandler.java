package com.leyingke.paizhao.camera.data;


import com.leyingke.paizhao.ui.CameraActivity;
import com.leyingke.paizhao.ui.EffectActivity;
import com.leyingke.paizhao.utils.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public final class CameraHandler extends Handler {

  private static final String TAG = CameraHandler.class.getSimpleName();

  public static final int AUTOFOCUS = 1;
  public static final int RESTART_PREVIEW = 2;
  public static final int SAVE_BITMAP_SUCCESS = 3;
  public static final int SAVE_BITMAP_FAIL = 4;
  public static final int AUTOFOCUS_CONTINUOUS = 5;
  
  private final CameraActivity activity;

  public CameraHandler(CameraActivity activity) {
    this.activity = activity;
  }

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
    	 CameraManager.get().startPreview(true);
    	  break;
    }
  }

}
