package com.lechat.camera.data;

import java.util.Timer;

public class AutoFocusManager {

	private static AutoFocusManager instance;
	private Timer timer;
	
	private MyTimerTask myTimerTask;
	
	private AutoFocusManager(){
		
	}
	
	public static synchronized AutoFocusManager getAutoFocusManager(){
		if(instance == null)
			instance = new AutoFocusManager();
		return instance;
	}
	
	public void startAutoFocus(MyTimerTask timerTask){
		cancel();
		myTimerTask = timerTask;
		timer = new Timer();
		timer.schedule(myTimerTask, 0, 2000);
	}
	
	public void cancel(){
		try{
			if(myTimerTask != null){
				myTimerTask.close();
			}
			if(timer != null){
				timer.cancel();
				timer = null;
			}
			myTimerTask = null;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
