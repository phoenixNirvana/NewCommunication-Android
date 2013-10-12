package com.leyingke.paizhao.common;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.leyingke.paizhao.R;
import com.leyingke.paizhao.utils.MyToast;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

public class WeiXinHelper {
	
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

	public static String APP_ID = "wxd67b01a7d45d6faa";
	
	private static final int THUMB_SIZE = 120;
	
	private static WeiXinHelper weiXinHelper;
	
	private boolean isShareAll = false;   //用于判断 true：分享朋友圈    false：分享给朋友

	// IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;
    
    public IWXAPI getApi(Context context) {
    	
    	if(isIWXAPINull()){
			initIWXAPI(context);
		}
    	
		return api;
	}

	public boolean isShareAll() {
		return isShareAll;
	}

	public void setShareAll(boolean isShareAll) {
		this.isShareAll = isShareAll;
	}
	
	public static WeiXinHelper getInstance(){
		if(weiXinHelper == null){
			weiXinHelper = new WeiXinHelper();
		}
		return weiXinHelper;
	}
	
	/**
	 * 把应用注册到微信
	 * 
	 * @return
	 */
	public boolean registerWeiXin(Context context){
		
		if(isIWXAPINull()){
			initIWXAPI(context);
		}

		// 将该app注册到微信
		return api.registerApp(APP_ID);
	}
	
	/**
	 * 是否安装了微信客户端
	 * 
	 * @return
	 */
	public boolean isWXAppInstalled(){
		return api.isWXAppInstalled();
	}
	
	/**
	 * 是否支持朋友圈发送
	 * 
	 * @return
	 */
	public boolean isWXAppSupportAPI(){
		
		int wxSdkVersion = api.getWXAppSupportAPI();
		if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION) {
			return true;
		} else {
			return false;
		}
		
	}
	
	private boolean isIWXAPINull(){
		return api == null;
	}
	
	private void initIWXAPI(Context context){
		api = WXAPIFactory.createWXAPI(context, APP_ID, false);
	}
	
	/**
	 * 发送消息给朋友
	 * 
	 * @return
	 */
	public boolean sendMessageToFriends(){
		return true;
	}
	
	
	
	/**
	 * 发消息到朋友圈
	 * 
	 * @param title        标题
	 * @param description  描述
	 * @param redirectUrl  点击跳转的路径
	 * @param bitmap       图片
	 * @return
	 */
	public boolean sendMessage(Context context, String title, String description, String addressUrl){
		
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
		
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = addressUrl;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		
		if(isShareAll){   //分享给朋友圈
			msg.title = description;
		}else{			  //分享给指定的人
			msg.title = title;
		}

		msg.description = description;
		msg.thumbData = bmpToByteArray(bitmap, true);
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
		req.scene = isShareAll ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		
		// 调用api接口发送数据到微信
		return api.sendReq(req);
	}
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * 分享给指定好友
	 * 
	 * @param context
	 * @param title        分享标题
	 * @param description  分享描述
	 * @param addressUrl   分享点击地址
	 */
	public void shareWXToFriend(Context context, String title, String description, String addressUrl){
		boolean isFSuccess = registerWeiXin(context);
		if(isFSuccess){
			WeiXinHelper.getInstance().setShareAll(false);
			boolean mm = sendMessage(context, title, description, addressUrl);  
		}else{
			MyToast.showToast(context, "注册微信失败");
		}
	}
	
	/**
	 * 分享给全部好友
	 * 
	 * @param context
	 * @param title        分享标题
	 * @param description  分享描述
	 * @param addressUrl   分享点击地址
	 */
	public void shareWXToAllFriend(Context context, String title, String description, String addressUrl){
		boolean isASuccess = registerWeiXin(context);
		if(isASuccess){
			if(WeiXinHelper.getInstance().isWXAppSupportAPI()){
				WeiXinHelper.getInstance().setShareAll(true);
				boolean mmm = sendMessage(context, title, description, addressUrl);
			}else{
				//MyToast.showToast(context, context.getResources().getString(R.string.weixin_disabled));
			}
			
		}else{
			//MyToast.showToast(context, context.getResources().getString(R.string.weixin_version));
		}
	}
}
