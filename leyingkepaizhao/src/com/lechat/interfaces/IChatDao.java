package com.lechat.interfaces;

public interface IChatDao {

	void onConnect();
	
	void onLogin(String account, String password);
	
	void onRegister(String account, String password);
	
	void disconnect();
	
}
