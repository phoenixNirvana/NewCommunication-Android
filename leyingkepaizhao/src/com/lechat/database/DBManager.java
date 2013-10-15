package com.lechat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {

	private DBHelper mDbHelper;
	
	private SQLiteDatabase mSqLiteDatabase;
	
	public SQLiteDatabase getSqLiteDatabase() {
		return mSqLiteDatabase;
	}

	public DBManager(Context context) {
		
		mDbHelper = new DBHelper(context);
		
		mSqLiteDatabase = mDbHelper.getWritableDatabase();
	}

	
}
