package com.lechat.camera.utils;

import com.lechat.utils.Logger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

public class BitmapUtil {

	/**
	 * 
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createBitmap(Bitmap src, Bitmap dst,int x,int y,float scale) {
		if (src == null || dst == null) {
			return null;
		}
		// 分别获取宽和高
		int srcWidth = src.getWidth();
		int srcHeight = src.getHeight();

		int dstWidth = dst.getWidth();
		int dstHeight = dst.getHeight();
		int targetW = (int) (scale * srcWidth);
		float newScale = (float)targetW / dstWidth;
        System.out.println(" ");
		/*float widthRatio = (float)(srcWidth - x)/ dstWidth;
		float heightRatio = (float)(srcHeight - y)/ dstHeight;
		float ratio = widthRatio > heightRatio ? widthRatio : heightRatio;*/
		Matrix matrix = new Matrix();
		matrix.postScale(newScale, newScale);
		matrix.postTranslate(x, y);
		
		// create the new blank bitmap
		Bitmap newb = Bitmap.createBitmap(srcWidth, srcHeight, Config.ARGB_8888);
		Canvas cv = new Canvas(newb);
		// draw src into
		cv.drawBitmap(src, 0, 0, null);
		// draw watermark into
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		cv.drawBitmap(dst, matrix, paint);
	//	cv.drawBitmap(dst, 0, srcHeight - dstHeight, null);
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);
		// store
		cv.restore();
		return newb;
	}

	public static Bitmap decodeYUV422P(byte[] yuv422p, int width, int height)
			throws NullPointerException, IllegalArgumentException {
		final int frameSize = width * height;
		int[] rgb = new int[frameSize];
		for (int j = 0, yp = 0; j < height; j++) {
			int up = frameSize + (j * (width / 2)), u = 0, v = 0;
			int vp = ((int) (frameSize * 1.5) + (j * (width / 2)));
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv422p[yp])) - 16;
				if (y < 0) {
					y = 0;
				}
				if ((i & 1) == 0) {
					u = (0xff & yuv422p[up++]) - 128;
					v = (0xff & yuv422p[vp++]) - 128;
				}
				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);
				if (r < 0) {
					r = 0;
				} else if (r > 262143) {
					r = 262143;
				}
				if (g < 0) {
					g = 0;
				} else if (g > 262143) {
					g = 262143;
				}
				if (b < 0) {
					b = 0;
				} else if (b > 262143) {
					b = 262143;
				}
				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
		return Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888);
	}

	public static Bitmap decodeSampledBitmap(byte[] bytes, Options options,
			int reqWidth, int reqHeight) {
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		Logger.debugPrint("abc", "图片缩放的比列：" + options.inSampleSize);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		Logger.debugPrint("abc", "图片原始宽 高：" + height + "  ----  " + width);
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		}
		return inSampleSize;
	}

}
