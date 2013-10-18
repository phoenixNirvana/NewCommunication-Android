package com.lechat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "chatDB.db";  
    private static final int DATABASE_VERSION = 1;
    
    public static final String TABLE_USERINFO = "userinfo";
    public static final String TABLE_FRIENDS = "friends";
    public static final String TABLE_MESSAGE = "messages";
    public static final String TABLE_NOTSEND = "notsend";
//    public static final String TABLE_EXPRESSION = "expression";
	
	public DBHelper(Context context) {
		super(context , DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		initTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		

	}
	
	private void initTable(SQLiteDatabase db){	
		
		db.beginTransaction();
		try{
			
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FRIENDS +  
					"(_id INTEGER PRIMARY KEY AUTOINCREMENT)");
			
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERINFO +  
					"(name VARCHAR, portrait_b_url VARCHAR, portrait_s_url VARCHAR, " +
					"active_state INTEGER, sex INTEGER, birthday VARCHAR, qq_num VARCHAR, friend_id VARCHAR, " +
					"weibo_num VARCHAR, FOREIGN KEY(friend_id) REFERENCES " + TABLE_FRIENDS + "(_id))");
			
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGE +  
					"(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
					"source_user_id VARCHAR, date DATE, body VARCHAR, type INTEGER, from_id VARCHAR, direction INTEGER)");
			
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTSEND +  
					"(msg_id VARCHAR, target_user_id VARCHAR, send_count INTEGER)");
			
//			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EXPRESSION +  
//					"(type INTEGER, content VARCHAR)");
			
			db.setTransactionSuccessful();
		
		}catch (Exception e) {
			
			e.printStackTrace();
			
		}finally{
			db.endTransaction();
		}
		
	}

}
