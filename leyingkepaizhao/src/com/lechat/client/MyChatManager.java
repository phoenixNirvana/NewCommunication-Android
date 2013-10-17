package com.lechat.client;


import android.content.Context;

import com.lechat.interfaces.IConnectListener;
import com.lechat.interfaces.ILoginListener;
import com.lechat.interfaces.IRegisterListener;

public class MyChatManager{
	
	private XmppManager xmppManager;
	
	private static MyChatManager chatManager;
	
	public XmppManager getXmppManager() {
		return xmppManager;
	}
    
    public void setRegisterListener(IRegisterListener mRegisterListener) {
    	xmppManager.setRegisterListener(mRegisterListener);
	}

	public void setConnectListener(IConnectListener mConnectListener) {
		xmppManager.setConnectListener(mConnectListener);
	}

	public void setLoginListener(ILoginListener mLoginListener) {
		xmppManager.setLoginListener(mLoginListener);
	}
	
	public MyChatManager(Context context){
		
		xmppManager = new XmppManager(context);
	}
	
	public static MyChatManager getInstance(Context context){
		
		synchronized (XmppManager.class) {
			if(chatManager == null){
				chatManager = new MyChatManager(context);
			}
		}
		
		return chatManager;
	}

}
