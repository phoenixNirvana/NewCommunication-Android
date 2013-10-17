package com.lechat.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;

public class BaseActivity extends Activity {

	
	/**
     * add a keylistener for progress dialog
     */
    private OnKeyListener onKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                closeProgressDialog();
            }
            return false;
        }
    };
	
	/**
	 * ��ʾ��ʾ��
	 */
	public void showProgressDialog() {
		if ((!isFinishing()) && (this.progressDialog == null)) {
			this.progressDialog = new ProgressDialog(this);
			this.progressDialog.setCancelable(false);
			this.progressDialog.setMessage("请稍后...");
			this.progressDialog.setOnKeyListener(onKeyListener);
		}
		if(!isShowing()){
			this.progressDialog.show();
		}
		
	}
	
	private ProgressDialog progressDialog;
	
	private boolean isShowing(){
		return this.progressDialog != null && this.progressDialog.isShowing();
	}
	
	/**
	 * �ر���ʾ��
	 */
	public void closeProgressDialog() {
		if (isShowing()){
			this.progressDialog.dismiss();
		}
	}
	
	/**
     * cancel progress dialog if nesseary
     */
    @Override
    public void onBackPressed() {
        if (isShowing()) {
        	this.progressDialog.dismiss();
        } else {
        	
        	finish();
        	
        }
    }
	
}
