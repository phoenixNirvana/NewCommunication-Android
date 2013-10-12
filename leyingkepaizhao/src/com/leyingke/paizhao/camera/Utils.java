package com.leyingke.paizhao.camera;

import android.content.Context;
import android.content.pm.PackageManager;

public class Utils {

	private static boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        return true;
	    } else {
	        return false;
	    }
	}
}
