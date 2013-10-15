package com.lechat.client;

import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import com.lechat.client.XmppManager.IChatMessageListener;

import android.content.Context;

public class MyChatManager {
	
	private Context mContext;
	
	public MyChatManager(Context context){
		this.mContext = context;
	}
	
	public void addChatMessageListener(String jid, IChatMessageListener listener){
		XmppManager.getInstance(mContext).addChatMessageListener(jid, listener);
	}
	
	public List<Message> getOfflineMsg() throws XMPPException{
		return XmppManager.getInstance(mContext).getOfflineMsg();
	}
	
	public Chat createChat(String jid) throws XMPPException{
		return XmppManager.getInstance(mContext).createChat(jid);
	}
	
	public void output(String jid) throws XMPPException{
		XmppManager.getInstance(mContext).output(jid);
	}
	
	public void getFile(){
		XmppManager.getInstance(mContext).getFile();
	}

}
