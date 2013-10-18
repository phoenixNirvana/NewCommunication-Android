package com.lechat.dao.impl;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.androidpn.bean.ChatBean;
import com.lechat.dao.IEntityDAO;
import com.lechat.database.DBHelper;
import com.lechat.database.DBManager;

public class IChatDAOImpl implements IEntityDAO<ChatBean> {

	private SQLiteDatabase mDatabase;
	
	private DBManager dbManager;
	
	private int count;
	
	public IChatDAOImpl(DBManager dbManager) {
		this.dbManager = dbManager;
		this.mDatabase = this.dbManager.getSqLiteDatabase();
	}

	@Override
	public boolean doCreate(ChatBean chat) throws Exception {
		
		boolean flag = false;
		ContentValues values = new ContentValues();
		values.put("from_id", chat.getFrom());
//		values.put("to", chat.getTo());
		values.put("body", chat.getBody());
		values.put("direction", chat.getDirection());
//		values.put("type", chat.getType());
		
		long count = mDatabase.insert(DBHelper.TABLE_MESSAGE, null, values);
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
		
		long count = mDatabase.update(DBHelper.TABLE_MESSAGE, values, "_id=?", new String[]{chat.getId()} );
		if(count > 0){
			flag = true;
		}

		return flag;
	}

	@Override
	public boolean doDelete(int chatId) throws Exception {
		
		boolean flag = false;
		
		int count = mDatabase.delete(DBHelper.TABLE_MESSAGE, "_id=?", new String[]{chatId + ""});
		
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
		
		Cursor c = mDatabase.query(DBHelper.TABLE_MESSAGE, null, null, null, null, null, null, null);
		
		return null;
	}

	@Override
	public int getAllCount(String keyWord) throws Exception {
		
		return 0;
	}

	@Override
	public Cursor findAll() {
		
		Cursor cursor = mDatabase.query(DBHelper.TABLE_MESSAGE, null, null, null, null, null, null);
		
		return cursor;
	}

	@Override
	public List<ChatBean> findAll2() {
		List<ChatBean> chats = new ArrayList<ChatBean>();
		Cursor cursor = mDatabase.query(DBHelper.TABLE_MESSAGE, null, null, null, null, null, null);
		if(cursor != null){
			while (cursor.moveToNext()) {
				ChatBean chatBean = new ChatBean();
				chatBean.setBody(cursor.getString(cursor.getColumnIndex("body")));  
				chatBean.setFrom(cursor.getString(cursor.getColumnIndex("from_id")));  
				chatBean.setDirection(cursor.getInt(cursor.getColumnIndex("direction")));  
				chats.add(chatBean);
			}
			cursor.close();
		}
		return chats;
	}

}
