package com.lechat.ui.activity;

import com.lechat.R;
import com.lechat.client.XmppManager;
import com.lechat.interfaces.ILoginListener;
import com.lechat.interfaces.IRegisterListener;
import com.lechat.utils.Logger;
import com.lechat.utils.MyToast;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;



public class RegisterActivity extends BaseActivity implements OnClickListener{

	private static final String TAG = "RegisterActivity";
	
	private EditText mEtAccount, mEtPassword, mEtConfirm;
	
	private Button mBtnLogin, mBtnRegister;
	
	private XmppManager mXmppManager;
	
	private Handler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.register);
		
		initView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initView(){
		
		mHandler = new MyHandler();
		
		mEtAccount = (EditText) findViewById(R.id.et_account);
		mEtPassword = (EditText) findViewById(R.id.et_password);
		mEtConfirm = (EditText) findViewById(R.id.et_confirm);
		mBtnLogin = (Button) findViewById(R.id.btn_login);
		mBtnRegister = (Button) findViewById(R.id.btn_register);
		
		mBtnLogin.setOnClickListener(this);
		mBtnRegister.setOnClickListener(this);
		
		mXmppManager = XmppManager.getInstance(this);
		
		mXmppManager.setRegisterListener(new IRegisterListener() {
			
			@Override
			public void registerSuccess() {
				Logger.debugPrint(TAG, "注册成功");
				
				mHandler.sendEmptyMessage(XmppManager.REGISTER_SUCCESS);
			}
			
			@Override
			public void registerFail() {
				Logger.debugPrint(TAG, "注册失败");
				
				mHandler.sendEmptyMessage(XmppManager.REGISTER_FAIL);
			}
		});
		
		mXmppManager.setLoginListener(new ILoginListener() {
			
			@Override
			public void logintFail() {
				Logger.debugPrint(TAG, "登录失败");
				mHandler.sendEmptyMessage(XmppManager.LOGIN_FAIL);
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
		
		String account = mEtAccount.getText().toString();
		String password = mEtPassword.getText().toString();
		
		switch (v.getId()) {
		case R.id.btn_login:
			
			mXmppManager.connect(account, password);
			
			break;
			
		case R.id.btn_register:
			
			String confirm = mEtConfirm.getText().toString();
			
			mXmppManager.submitRegisterTask(account, password);
			break;

		}
	}
	
	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case XmppManager.REGISTER_SUCCESS:

				MyToast.showToast(RegisterActivity.this, "注册成功，请登录");
				
				break;
			case XmppManager.REGISTER_FAIL:
				MyToast.showToast(RegisterActivity.this, "注册失败");
				break;
			case XmppManager.LOGIN_SUCCESS:
				MyToast.showToast(RegisterActivity.this, "登录成功，跳转下一界面");
				break;
			case XmppManager.LOGIN_FAIL:
				MyToast.showToast(RegisterActivity.this, "登录失败");
				break;

			}

		}

	}
	
	
}
