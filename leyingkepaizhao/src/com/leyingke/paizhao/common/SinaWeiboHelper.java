package com.leyingke.paizhao.common;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.leyingke.paizhao.R;
import com.leyingke.paizhao.interfaces.ISinaCallBack;
import com.leyingke.paizhao.ui.SinaShareActivity;
import com.leyingke.paizhao.utils.Logger;
import com.leyingke.paizhao.utils.MyToast;
import com.leyingke.paizhao.utils.StringUtils;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

public class SinaWeiboHelper{

	private static final String CONSUMER_KEY = "3408541489";
	private static final String CONSUMER_SECRET = "4425d4db59821901ce005d679674d881";
	private static final String REDIRECT_URL = "http://www.leyingke.com/lykweb/home/info";
	
	private Weibo weibo = null;
	private StatusesAPI  api = null;
	
	private static SinaWeiboHelper sinaWeiboHelper;
	/**
	 * SsoHandler 仅当sdk支持sso时有效，
	 */
	private SsoHandler mSsoHandler;
	
	/**
	 * 新浪微薄用户名称
	 */
	private String	screen_name;
	public SsoHandler getSsoHandler() {
		return mSsoHandler;
	}

	/**
	 * 判断weibo是否为null
	 * @return
	 */
	public boolean isWeiboNull()
	{
		if(weibo == null)
			return true;
		else 
			return false;
	}
	
	public static SinaWeiboHelper getInstance(){
		if(sinaWeiboHelper == null){
			sinaWeiboHelper = new SinaWeiboHelper();
		}
		return sinaWeiboHelper;
	}
	
	/**
	 * 认证新浪微博
	 * 
	 * @param context
	 */
	public void authorize(final Context context, final String message, final String imgPath ,boolean isOpen , ISinaCallBack sinaCallBack){
		if(isWeiboNull()){
			initWeibo();
		}
		try {
            Class sso=Class.forName("com.weibo.sdk.android.sso.SsoHandler");

            initSsoHandler((Activity)context);
            mSsoHandler.authorize(new MyWeiboAuthListener(context, message, imgPath,isOpen , sinaCallBack));
            
        } catch (ClassNotFoundException e) {
        	weibo.authorize(context, new MyWeiboAuthListener(context, message, imgPath,isOpen , sinaCallBack));
        }
	}
	
	private void initSsoHandler(Activity context){
		mSsoHandler = new SsoHandler(context, weibo);
	}
	private boolean isSsoHandlerNull(){
		return mSsoHandler == null;
	}
	
	/**
	 * 初始化weibo
	 */
	public void initWeibo(){
		weibo = Weibo.getInstance(CONSUMER_KEY, REDIRECT_URL);
	}
	
	/**
	 * 写微博
	 * 
	 * @param cont
	 * @param shareMsg
	 * @param shareImg
	 */
	public void shareMessage(final Activity cont, String shareMsg, String shareImg, RequestListener requestListener){
		if(isWeiboNull()){
			initWeibo();
		}
		Oauth2AccessToken oauth2AccessToken = AccessTokenKeeper.readAccessToken(cont);
		if(isStatusesAPINull()){
			initStatusesAPI(oauth2AccessToken);
		}
		if (!TextUtils.isEmpty(oauth2AccessToken.getToken())) {
			
			if (!TextUtils.isEmpty(shareImg)) {
				api.upload(shareMsg, shareImg, null, null, requestListener);
				
			} else {
				// Just update a text weibo!
				api.update(shareMsg, null, null, requestListener);
			}
		} else {
			//MyToast.showToast(cont, cont.getString(R.string.weibosdk_please_login),0);
		}
	}
	
	public void initStatusesAPI(Oauth2AccessToken oauth2AccessToken){
		api = new StatusesAPI(oauth2AccessToken);
	}
	
	public boolean isStatusesAPINull(){
		return api == null;
	}
	
	private class MyWeiboAuthListener implements WeiboAuthListener{
		private Context context;
		private String message;
		private String imgPath;
		private boolean isOpen;
		private ISinaCallBack sinaCallBack;
		
		public MyWeiboAuthListener(Context context, String message, String imgPath,boolean isOpen , ISinaCallBack sinaCallBack) {
			this.context = context;
			this.message = message;
			this.imgPath = imgPath;
			this.isOpen = isOpen;
			this.sinaCallBack = sinaCallBack;
		}

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");

			long uid = Long.parseLong(values.getString("uid"));
			Oauth2AccessToken accessToken = new Oauth2AccessToken(token, expires_in);
			AccessTokenKeeper.keepAccessToken(context, accessToken);
			
			if(isOpen)
				openShareActivity(context, message, imgPath);
			
			UsersAPI usersAPI = new UsersAPI(AccessTokenKeeper.readAccessToken(context)); 
			usersAPI.show(uid, new CommonListener(context, sinaCallBack));

		}
		
		
		class CommonListener implements RequestListener{
			
			private Context context;
			private ISinaCallBack sinaCallBack;
			public CommonListener(Context context , ISinaCallBack sinaCallBack){
				this.context = context;
				this.sinaCallBack = sinaCallBack;
			}

			@Override
			public void onComplete(String arg0) {
				Logger.e("i", "Users UID :" + arg0);
				JSONObject jsonObj;
				try {
					jsonObj = new JSONObject(arg0);
					screen_name = jsonObj.optString("screen_name"); 
					
					if(!StringUtils.isEmpty(screen_name)){
						//保存到本地
						//AppConfig.getInstance(context).saveDataString(AppConfig.SP_USER_SINA_NAME, screen_name);
						//回调
						if(sinaCallBack != null){
							sinaCallBack.authorizeSuccess(screen_name);
						}
					}
					jsonObj = null;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(WeiboException arg0) {
				if(sinaCallBack != null){
					sinaCallBack.authorizeFail();
				}
			}

			@Override
			public void onIOException(IOException arg0) {
				if(sinaCallBack != null){
					sinaCallBack.authorizeFail();
				}
			}
		}
		
		
		@Override
		public void onError(WeiboDialogError e) {
			//MyToast.showToast(context, context.getResources().getString(R.string.sina_oauth_fail),0);
			if(sinaCallBack != null){
				sinaCallBack.authorizeFail();
			}
				
		}
		
		@Override
		public void onCancel() {
			//MyToast.showToast(context, context.getResources().getString(R.string.sina_oauth_fail),0);
			if(sinaCallBack != null)
				sinaCallBack.authorizeFail();
		}
		
		@Override
		public void onWeiboException(WeiboException e) {
			//MyToast.showToast(context, context.getResources().getString(R.string.sina_oauth_fail),0);
			if(sinaCallBack != null)
				sinaCallBack.authorizeFail();
		}
	}
	public void openShareActivity(Context context, String message, String imgPath) {
		Intent intent = new Intent(context, SinaShareActivity.class);
		intent.putExtra("message", message);
		intent.putExtra("img_path", imgPath);
		intent.putExtra("mode", 1);
		context.startActivity(intent);
	}
	
	/**
	 * 分享微博
	 * @param bitmapManager 保存图片
	 * @param content       分享的内容
	 * @param context       上下文对象
	 */
	public void shareSinaWeibo(Context context, String content, String imgPath, ISinaCallBack sinaCallBack) {
		
		if (isWeiboNull()) {
			initWeibo();
		}
		Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(context);
		if (!accessToken.isSessionValid()) { // 认证微博时间是否有效
			authorize(context, content, imgPath, true, sinaCallBack);
		} else {
			openShareActivity(context, content, imgPath);
		}
	}

}
