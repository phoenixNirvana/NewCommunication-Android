package com.lechat.ui.activity;

import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.androidpn.bean.ChatBean;
import com.lechat.R;
import com.lechat.adapter.MyBaseAdapter;
import com.lechat.client.MyChatManager;
import com.lechat.client.XmppManager;
import com.lechat.dao.proxy.IChatDAOProxy;
import com.lechat.interfaces.IChatMessageListener;
import com.lechat.utils.Logger;
import com.lechat.utils.MyToast;

public class ChatActivity extends BaseActivity implements OnClickListener{

	private static final String TAG = "ChatActivity";
	
	private EditText mEtContent;
	private Button mBtnSend;
	private ListView mListView;
	
	private XmppManager mXmppManager;
	
	private String mTargetJid;
	private IChatDAOProxy mIChatDAOProxy;
	
	private MyChatAdapter mAdapter;
	
	private LayoutInflater mInflater;
	
	private Handler mHandler;
	
	private static final int REC_MESSAGE = 0x001;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.chat);
		
		mTargetJid = getIntent().getStringExtra("target_jid");
		
		mIChatDAOProxy = new IChatDAOProxy(this);
		
		mHandler = new MyHandler();
		
		mInflater = LayoutInflater.from(this);
		
		mXmppManager = MyChatManager.getInstance(this).getXmppManager();
		
		mEtContent = (EditText) findViewById(R.id.et_chat_content);
		mBtnSend = (Button) findViewById(R.id.btn_chat_send);
		mListView = (ListView) findViewById(R.id.lv_chat);
		
		mBtnSend.setOnClickListener(this);
		
		mXmppManager.addChatMessageListener(mTargetJid, new IChatMessageListener() {
			
			@Override
			public void processMessage(Message msg) {
				
				Logger.debugPrint(TAG, "from:"+msg.getFrom() + " body: "+msg.getBody());
				
				ChatBean chat = new ChatBean();
				chat.setBody(msg.getBody());
				chat.setFrom(msg.getFrom());
				chat.setDirection(0);
				android.os.Message message = android.os.Message.obtain();
				message.what = REC_MESSAGE;
				message.obj = chat;
				mHandler.sendMessage(message);
				
			}
		});
		
		new RefreshList().execute();
	}
	
	private void addChat(ChatBean chat){
		
		if(chat != null){
			try {
				if(mIChatDAOProxy.doCreate(chat)){
					mAdapter.appendData(chat);
				}else{
					MyToast.showToast(this, "聊天记录添加失败");
				}
			} catch (Exception e) {
				e.printStackTrace();
				MyToast.showToast(this, "聊天记录添加失败");
			}
		}
		
	}
	
//	private Cursor findAll(){
//		Cursor cursor = mIChatDAOProxy.findAll();
//		return cursor;
//	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_chat_send:
			try {
				//发送信息
				Chat chat = mXmppManager.createChat(mTargetJid);
				chat.sendMessage(mEtContent.getText().toString());
				
				//保存到数据库
				ChatBean chatBean = new ChatBean();
				chatBean.setBody(mEtContent.getText().toString());
				chatBean.setFrom("wo");
				chatBean.setDirection(1);
				addChat(chatBean);
				
				mEtContent.getEditableText().clear();
				
			} catch (XMPPException e) {
				e.printStackTrace();
			}
			break;

		}
	}
	
	class MyChatAdapter extends MyBaseAdapter<ChatBean>{

		public MyChatAdapter(Context context, List<ChatBean> list) {
			super(context, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ChatBean chat = (ChatBean) getItem(position);
			ChatListItemCache cache = null;
			if(convertView == null || (convertView.getTag() != null
					&& ((ChatListItemCache)convertView.getTag()).direction != chat.getDirection())){
				cache = new ChatListItemCache();
				if(chat.getFrom().equals(mTargetJid)){  //对方发送的消息
					convertView = mInflater.inflate(R.layout.chat_item_left, null);
					cache.direction = 0;
				}else{										//自己发送的
					convertView = mInflater.inflate(R.layout.chat_item_right, null);
					cache.direction = 1;
				}
				cache.tvBody = (TextView) convertView.findViewById(R.id.chat_body);
				cache.photoView = (ImageView) convertView.findViewById(R.id.chat_protrait);
				convertView.setTag(cache);
			}else{
				cache = (ChatListItemCache) convertView.getTag();
			}
			
			cache.tvBody.setText(chat.getBody());
			
			return convertView;
		}
		
	}

//	class MyChatAdapter extends CursorAdapter{
//		
//		public MyChatAdapter(Context context, Cursor c) {
//			super(context, c);
//		}
//
//		@Override
//		public View newView(Context context, Cursor cursor, ViewGroup parent) {
//			View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, null);
////			if(from.equals(mTargetJid)){  //对方发送的
////			}else{						  //自己发送的
////				view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, null);
////			}
//			ChatListItemCache cache = new ChatListItemCache();
//			cache.tvBody = (TextView) view.findViewById(R.id.chat_body);
//			cache.photoView = (ImageView) view.findViewById(R.id.chat_protrait);
//			view.setTag(cache);
//			
//			return view;
//		}
//
//		@Override
//		public void bindView(View view, Context context, Cursor cursor) {
//			
//			ChatListItemCache cache = (ChatListItemCache) view.getTag();
//			cache.tvBody.setText(cursor.getString(cursor.getColumnIndex("body")));
//		}
//		
//	}
	
	final static class ChatListItemCache {
        public TextView tvBody;
        public ImageView photoView;
        public int direction;
    }
	
	private class RefreshList extends AsyncTask<Void, Void, List<ChatBean>>{
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected List<ChatBean> doInBackground(Void... params) {
			return mIChatDAOProxy.findAll2();
		}

		@Override
		protected void onPostExecute(List<ChatBean> result) {
			super.onPostExecute(result);
			
			if(mAdapter == null){
				mAdapter = new MyChatAdapter(ChatActivity.this, result);
				mListView.setAdapter(mAdapter);
			}else{
//				mAdapter.changeCursor(cursor);// 网上看到很多问如何更新ListView的信息，采用CusorApater其实很简单，换cursor就可以
			}
		}

	}
	
	class MyHandler extends Handler{

		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			
			switch (msg.what) {
			case REC_MESSAGE:
				
				ChatBean chat = (ChatBean) msg.obj;
				addChat(chat);
				
				break;

			default:
				break;
			}
			
		}
		
	}
	
}
