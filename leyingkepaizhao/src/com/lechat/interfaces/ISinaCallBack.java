package com.lechat.interfaces;


/**
 * 绑定新浪微薄成功后更改textView的状�?
 * @author boy
 *
 *	不需要可以为空�?
 *
 *
 */

public interface ISinaCallBack {
	
	/**
	 * 绑定新浪微薄成功后调用的方法
	 */
	void authorizeSuccess(String screenName);
	
	/**
	 * 绑定新浪微薄失败后调用的方法
	 */
	void authorizeFail();
	
}
