package com.lechat.dao.factory;

import android.content.Context;

import com.lechat.dao.IEntityDAO;
import com.lechat.dao.proxy.IChatDAOProxy;

public class DaoFactory {

	public enum DAOType {
		CHAT
	};
	
	public static IEntityDAO<?> getIEntityDAO(Context context, DAOType type){ 
		
		IEntityDAO<?> entityDAO = null;
		
		switch (type) {
		
		case CHAT:
			entityDAO = new IChatDAOProxy(context);
			break;

		}
		
		return entityDAO;
		
	}
	
	
}
