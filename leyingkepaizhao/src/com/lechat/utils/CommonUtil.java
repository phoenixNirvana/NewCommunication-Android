package com.lechat.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.OrientationEventListener;


public class CommonUtil {

	private static String generateDCIM() {
        return new File(Environment.getExternalStorageDirectory().toString(), Environment.DIRECTORY_DCIM).toString();
    }

    public static String generateDirectory() {
        return generateDCIM() + "/Camera";
    }

    private static String generateFilepath(String title) {
        return generateDirectory() + '/' + title + ".jpg";
    }
	
	public static String writeFile(String title, byte[] data) {
        String path = generateFilepath(title);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(data);
        } catch (Exception e) {
        	Log.e("abc", "����ͼƬ�쳣");
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }
        return path;
    }
	
	public static Uri insertBitmap(Context context, ContentResolver resolver, String fileName,
			String filePath, int fileLength, int orientation){
		
		Uri uri = null;
		try {
			// Insert into MediaStore.
	        ContentValues values = new ContentValues(9);
	        values.put(ImageColumns.TITLE, fileName);
	        values.put(ImageColumns.DISPLAY_NAME, fileName + ".jpg");
	        values.put(ImageColumns.MIME_TYPE, "image/jpeg");
	        values.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis());
	        // Clockwise rotation in degrees. 0, 90, 180, or 270.
	        values.put(ImageColumns.DATA, filePath);
	        values.put(ImageColumns.ORIENTATION, orientation);
	        values.put(ImageColumns.SIZE, fileLength);
	        try {
	            uri = resolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
	            broadcastNewPicture(context, uri);
	        } catch (Throwable th) {
	        }
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("abc", "����ʧ��!");
		}
		
		return uri;

	}
	
	/**
     * Broadcast an intent to notify of a new picture in the Gallery
     * @param context
     * @param uri
     */
    private static void broadcastNewPicture(Context context, Uri uri) {
        context.sendBroadcast(new Intent("android.hardware.action.NEW_PICTURE", uri));
        // Keep compatibility
        context.sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", uri));
    }
	
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
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
	
	/**
	 *  dp转化成px
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(Context context, float dipValue){ 
        final float scale = context.getResources().getDisplayMetrics().density; 
        return (int)(dipValue * scale + 0.5f); 
    } 
   
  /**
    *   px 转化成dp
    * @param context
    * @param pxValue
    * @return
    */
   public static int px2dip(Context context, float pxValue){ 
        final float scale = context.getResources().getDisplayMetrics().density; 
        return (int)(pxValue / scale + 0.5f); 
   }
}
