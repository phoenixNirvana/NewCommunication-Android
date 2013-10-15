package com.lechat.dao;

import java.util.List;

import com.androidpn.bean.ChatBean;

public interface IEntityDAO<T> {

	boolean doCreate(T t) throws Exception;
	
	boolean doUpdate(T t) throws Exception;
	
	boolean doDelete(int id) throws Exception;
	
	ChatBean findById(int id) throws Exception;
	
	List<T> findAll(int currentPage, int lineSize, String keyWord) throws Exception;
	
	int getAllCount(String keyWord) throws Exception;
}
