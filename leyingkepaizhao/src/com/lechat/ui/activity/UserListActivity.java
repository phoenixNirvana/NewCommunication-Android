package com.lechat.ui.activity;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidpn.bean.UserInfo;
import com.lechat.R;
import com.lechat.adapter.MyBaseAdapter;
import com.lechat.client.MyChatManager;
import com.lechat.client.XmppManager;

public class UserListActivity extends BaseActivity implements OnItemClickListener{

	private GridView mGridView;
	
	private XmppManager mXmppManager;
	
	private LayoutInflater mInflater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.user_list);
		
		mInflater = LayoutInflater.from(this);
		
		mXmppManager = MyChatManager.getInstance(this).getXmppManager();
		
		List<UserInfo> users = mXmppManager.getUsers();
		
		mGridView = (GridView) findViewById(R.id.gv_userlist);
		
		mGridView.setOnItemClickListener(this);
		
		UserListAdapter adapter = new UserListAdapter(this, users);
		
		mGridView.setAdapter(adapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
	class UserListAdapter extends MyBaseAdapter<UserInfo>{

		public UserListAdapter(Context context, List<UserInfo> list) {
			super(context, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			UserListCache cache = null;
			
			if(convertView == null){
				cache = new UserListCache();
				convertView = mInflater.inflate(R.layout.user_list_item, null);
				cache.tvName = (TextView) convertView.findViewById(R.id.user_name);
				cache.ivProtrait = (ImageView) convertView.findViewById(R.id.user_protrait);
				convertView.setTag(cache);
			}else{
				cache = (UserListCache) convertView.getTag();
			}
			
			UserInfo user = (UserInfo) getItem(position);
			
			cache.tvName.setText(user.getUser()); 
			
			return convertView;
		}
		
	}
	
	final class UserListCache{
		TextView tvName;
		ImageView ivProtrait;
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		UserInfo user = (UserInfo) parent.getAdapter().getItem(position);
		
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra("target_jid", user.getFrom() + "/Spark 2.6.3");
		
		startActivity(intent);
		
	}
}
