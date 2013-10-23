package com.lechat.camera.imagefilter.api;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class GrayFilter {
	// 黑白效果函数
	public static Bitmap changeToGray(Bitmap bitmap) {
		
		int width, height;
		width = bitmap.getWidth();
		height = bitmap.getHeight();
			
		Bitmap grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(grayBitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true); // 设置抗锯齿
			
		//test
		/*float[] array = {1, 0, 0, 0, 100,
						 0, 1, 0, 0, 100,
						 0, 0, 1, 0, 0,
						 0, 0, 0, 1, 0};
		ColorMatrix colorMatrix = new ColorMatrix(array);*/
		
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0);
			
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
			
		paint.setColorFilter(filter);
		canvas.drawBitmap(bitmap, 0, 0, paint);
			
		return grayBitmap;
	}
}
