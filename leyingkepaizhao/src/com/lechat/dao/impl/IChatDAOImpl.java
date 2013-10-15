package com.lechat.dao.impl;

import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.androidpn.bean.ChatBean;
import com.lechat.dao.IEntityDAO;
import com.lechat.database.DBManager;

public class IChatDAOImpl implements IEntityDAO<ChatBean> {

	private SQLiteDatabase mDatabase;
	
	private DBManager dbManager;
	
	private int count;
	
	private static final String TABLE_NAME = "chatTable";
	
	public IChatDAOImpl(DBManager dbManager) {
		this.dbManager = dbManager;
		this.mDatabase = this.dbManager.getSqLiteDatabase();
	}

	@Override
	public boolean doCreate(ChatBean chat) throws Exception {
		
		boolean flag = false;
		ContentValues values = new ContentValues();
		values.put("from", chat.getFrom());
		values.put("to", chat.getTo());
		values.put("body", chat.getBody());
		values.put("direction", chat.getDirection());
		values.put("type", chat.getType());
		
		long count = mDatabase.insert(TABLE_NAME, null, values);
		if(count > 0){
			flag = true;
		}

		return flag;
	}

	@Override
	public boolean doUpdate(ChatBean chat) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		ContentValues values = new ContentValues();
		values.put("read_status", chat.getReadStatus());
		values.put("send_status", chat.getSendStatus());
		
		long count = mDatabase.update(TABLE_NAME, values, "_id=?", new String[]{chat.getId()} );
		if(count > 0){
			flag = true;
		}

		return flag;
	}

	@Override
	public boolean doDelete(int chatId) throws Exception {
		
		boolean flag = false;
		
		int count = mDatabase.delete(TABLE_NAME, "_id=?", new String[]{chatId + ""});
		
		if(count > 0){
			flag = true;
		}
		
		return flag;
	}

	@Override
	public ChatBean findById(int chatId) throws Exception {
		
		return null;
	}

	@Override
	public List<ChatBean> findAll(int currentPage, int lineSize, String keyWord) throws Exception {
		
//		mDatabase.query(TABLE_NAME, null, "from=?", new String[]{"zoushuai"}, groupBy, having, orderBy, limit)
		
		return null;
	}

	@Override
	public int getAllCount(String keyWord) throws Exception {
		
		return 0;
	}

}
