package com.lechat.dao.proxy;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.androidpn.bean.ChatBean;
import com.lechat.dao.IEntityDAO;
import com.lechat.dao.impl.IChatDAOImpl;
import com.lechat.database.DBManager;

public class IChatDAOProxy implements IEntityDAO<ChatBean>{

	private DBManager mDbManager;
	
	private SQLiteDatabase mDatabase;
	
	private IChatDAOImpl mChatDAOImpl;
	
	public IChatDAOProxy(Context context) {
		
		mDbManager = new DBManager(context);
		
		mDatabase = mDbManager.getSqLiteDatabase();
		
		mChatDAOImpl = new IChatDAOImpl(mDbManager);
	}

	@Override
	public boolean doCreate(ChatBean chat) throws Exception {
		
		return mChatDAOImpl.doCreate(chat);
	}

	@Override
	public boolean doUpdate(ChatBean chat) throws Exception {
		return false;
	}

	@Override
	public boolean doDelete(int chatId) throws Exception {
		return false;
	}

	@Override
	public ChatBean findById(int chatId) throws Exception {
		return null;
	}

	@Override
	public List<ChatBean> findAll(int currentPage, int lineSize, String keyWord)
			throws Exception {
		return null;
	}

	@Override
	public int getAllCount(String keyWord) throws Exception {
		return 0;
	}

	@Override
	public Cursor findAll() {
		return mChatDAOImpl.findAll();
	}

	@Override
	public List<ChatBean> findAll2() {
		return mChatDAOImpl.findAll2();
	}

}
