package com.lechat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.lechat.R;
import com.lechat.client.ServiceManager;
import com.lechat.client.XmppManager;
import com.lechat.interfaces.IConnectListener;
import com.lechat.interfaces.ILoginListener;
import com.lechat.utils.Logger;
import com.lechat.utils.MyToast;
import com.lechat.utils.StringUtils;


public class LoginActivity extends BaseActivity implements OnClickListener{

	private static final String TAG = "LoginActivity";
	
	private EditText mEtAccount, mEtPassword;
	
	private Button mBtnLogin, mBtnRegister;
	
	private XmppManager mXmppManager;
	
	private MyHandler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.login);
		
		init();
		
		// Start the service
        ServiceManager serviceManager = new ServiceManager(this);
        serviceManager.setNotificationIcon(R.drawable.back_nor);
        serviceManager.startService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void init(){
		
		mHandler = new MyHandler();
		
		mEtAccount = (EditText) findViewById(R.id.et_account);
		mEtPassword = (EditText) findViewById(R.id.et_password);
		mBtnLogin = (Button) findViewById(R.id.btn_login);
		mBtnRegister = (Button) findViewById(R.id.btn_register);
		
		mBtnLogin.setOnClickListener(this);
		mBtnRegister.setOnClickListener(this);
		
		mXmppManager = XmppManager.getInstance(this);
		
		mXmppManager.setConnectListener(new IConnectListener() {
			
			@Override
			public void connectSuccess() {
				Logger.debugPrint(TAG, "连接成功");
				
				mHandler.sendEmptyMessage(XmppManager.CONNECT_SUCCESS);
			}
			
			@Override
			public void connectFail() {
				Logger.debugPrint(TAG, "连接失败");
				mHandler.sendEmptyMessage(XmppManager.CONNECT_FAIL);
			}
		});
		
		mXmppManager.setLoginListener(new ILoginListener() {
			
			@Override
			public void logintFail() {
				
				Logger.debugPrint(TAG, "登录失败");
				mHandler.sendEmptyMessage(XmppManager.LOGIN_FAIL);
				
				startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
				finish();
				
			}
			
			@Override
			public void loginSuccess() {
				Logger.debugPrint(TAG, "登录成功");
				mHandler.sendEmptyMessage(XmppManager.LOGIN_SUCCESS);
				
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			
			String account = mEtAccount.getText().toString();
			String password = mEtPassword.getText().toString();
			
			if(StringUtils.isEmpty(account)){
				
				MyToast.showToast(this, "请输入账号！");
				
				return;
			}
			
			if(StringUtils.isEmpty(password)){
				
				MyToast.showToast(this, "请输入密码！");
				return;
			}
			
			mXmppManager.connect(account, password);
			
			break;
			
		case R.id.btn_register:
			
			break;

		}
	}
	
	private void login(){
		
		
	}
	
	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case XmppManager.CONNECT_SUCCESS:

				MyToast.showToast(LoginActivity.this, "连接成功");
				
				break;
			case XmppManager.CONNECT_FAIL:
				MyToast.showToast(LoginActivity.this, "连接失败");
				break;
			case XmppManager.LOGIN_SUCCESS:
				MyToast.showToast(LoginActivity.this, "登录成功，跳转下一界面");
				break;
			case XmppManager.LOGIN_FAIL:
				MyToast.showToast(LoginActivity.this, "登录失败");
				break;

			}

		}

	}
	
}
