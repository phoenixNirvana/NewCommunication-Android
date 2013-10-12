package com.leyingke.paizhao.ui;

import java.io.IOException;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.leyingke.paizhao.R;
import com.leyingke.paizhao.common.SinaWeiboHelper;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.net.RequestListener;

public class SinaShareActivity extends BaseActivity implements OnClickListener, RequestListener{

	private EditText mEtShare;
	
	private Button mBtnShare;
	
	private String mFilePath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);
		
		mFilePath = getIntent().getStringExtra("img_path");
		
		mEtShare = (EditText) findViewById(R.id.et_share);
		
		mBtnShare = (Button) findViewById(R.id.btn_share);
		
		mBtnShare.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_share:
			
			showProgressDialog();
			SinaWeiboHelper.getInstance().shareMessage(this, mEtShare.getText().toString(), mFilePath, this);
			
			break;

		default:
			break;
		}
		
	}

	@Override
	public void onComplete(String arg0) {
		
		closeProgressDialog();
		Log.e("i", "微博返回的数据：" + arg0);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(SinaShareActivity.this, "微博发送成功", 3000).show();
			}
		});
		this.finish();
	}

	@Override
	public void onError(WeiboException arg0) {
		Toast.makeText(SinaShareActivity.this, "发送失败", 3000).show();
		closeProgressDialog();
	}

	@Override
	public void onIOException(IOException arg0) {
		Toast.makeText(SinaShareActivity.this, "发送失败", 3000).show();
		closeProgressDialog();
	}

	
	
}
