package com.lechat.common;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.lechat.R;
import com.lechat.utils.MyToast;
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
	
	private boolean isShareAll = false;   //�����ж� true����������Ȧ    false�����������

	// IWXAPI �ǵ���app��΢��ͨ�ŵ�openapi�ӿ�
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
	 * ��Ӧ��ע�ᵽ΢��
	 * 
	 * @return
	 */
	public boolean registerWeiXin(Context context){
		
		if(isIWXAPINull()){
			initIWXAPI(context);
		}

		// ����appע�ᵽ΢��
		return api.registerApp(APP_ID);
	}
	
	/**
	 * �Ƿ�װ��΢�ſͻ���
	 * 
	 * @return
	 */
	public boolean isWXAppInstalled(){
		return api.isWXAppInstalled();
	}
	
	/**
	 * �Ƿ�֧������Ȧ����
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
	 * ������Ϣ������
	 * 
	 * @return
	 */
	public boolean sendMessageToFriends(){
		return true;
	}
	
	
	
	/**
	 * ����Ϣ������Ȧ
	 * 
	 * @param title        ����
	 * @param description  ����
	 * @param redirectUrl  �����ת��·��
	 * @param bitmap       ͼƬ
	 * @return
	 */
	public boolean sendMessage(Context context, String title, String description, String addressUrl){
		
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.back_nor);
		
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = addressUrl;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		
		if(isShareAll){   //���������Ȧ
			msg.title = description;
		}else{			  //�����ָ������
			msg.title = title;
		}

		msg.description = description;
		msg.thumbData = bmpToByteArray(bitmap, true);
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
		req.scene = isShareAll ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		
		// ����api�ӿڷ�����ݵ�΢��
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
	 * �����ָ������
	 * 
	 * @param context
	 * @param title        �������
	 * @param description  ��������
	 * @param addressUrl   ��������ַ
	 */
	public void shareWXToFriend(Context context, String title, String description, String addressUrl){
		boolean isFSuccess = registerWeiXin(context);
		if(isFSuccess){
			WeiXinHelper.getInstance().setShareAll(false);
			boolean mm = sendMessage(context, title, description, addressUrl);  
		}else{
			MyToast.showToast(context, "ע��΢��ʧ��");
		}
	}
	
	/**
	 * �����ȫ������
	 * 
	 * @param context
	 * @param title        �������
	 * @param description  ��������
	 * @param addressUrl   ��������ַ
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
