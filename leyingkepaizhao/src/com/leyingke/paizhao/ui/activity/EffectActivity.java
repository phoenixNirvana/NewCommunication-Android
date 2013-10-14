package com.leyingke.paizhao.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.leyingke.paizhao.R;
import com.leyingke.paizhao.common.SinaWeiboHelper;
import com.leyingke.paizhao.common.WeiXinHelper;
import com.leyingke.paizhao.widget.PhotoFrame;

public class EffectActivity extends Activity implements OnClickListener {

	private String filePath;
	private Button mBtnContinue; 
	private TextView mShareWeibo, mShareFriend, mShareAllFriends;
	private PhotoFrame photoFrame;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.effect);
		
		mShareWeibo = (TextView) findViewById(R.id.tv_share_weibo);
		mShareFriend = (TextView) findViewById(R.id.tv_share_friend);
		mShareAllFriends = (TextView) findViewById(R.id.tv_share_all_friends);
		
		mShareWeibo.setOnClickListener(this);
		mShareFriend.setOnClickListener(this);
		mShareAllFriends.setOnClickListener(this);
		
		filePath = getIntent().getStringExtra("file_path");
		photoFrame = (PhotoFrame) findViewById(R.id.view_photoframe);
		mBtnContinue = (Button) findViewById(R.id.btn_continue_take_photos);
		mBtnContinue.setOnClickListener(this);
		
	//	Bitmap bitmap = BitmapFactory.decodeFile(filePath);
	}

	@Override
	protected void onResume() {
		super.onResume();
		photoFrame.setPhoto(filePath);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.tv_share_weibo:
			
			SinaWeiboHelper.getInstance().shareSinaWeibo(EffectActivity.this, "sdfsdf", filePath, null);
			break;
		case R.id.tv_share_friend:
			WeiXinHelper.getInstance().shareWXToFriend(EffectActivity.this, "", "", "www.baidu.com");
			break;
		case R.id.tv_share_all_friends:
			WeiXinHelper.getInstance().shareWXToAllFriend(EffectActivity.this, "", "", "www.baidu.com");
			break;
		case R.id.btn_continue_take_photos:
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(SinaWeiboHelper.getInstance().getSsoHandler() != null){
        	SinaWeiboHelper.getInstance().getSsoHandler().authorizeCallBack(requestCode, resultCode, data);
        }
	}

	
}
