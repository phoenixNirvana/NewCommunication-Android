package com.lechat.camera.data;

import android.graphics.drawable.Drawable;

public class WaterMarkPosition {

	private int positionX;
	private int positionY;
	private Drawable drawable;
	private String str;
	
	public WaterMarkPosition(int positionX,int positionY){
		this.positionX = positionX;
		this.positionY = positionY;
	}
	
	public WaterMarkPosition(int positionX,int positionY,Drawable drawable){
		this(positionX,positionY);
	    this.drawable = drawable;
	}
	
	public WaterMarkPosition(int positionX,int positionY,String str){
		this(positionX,positionY);
	    this.str = str;
	}
	
	public Drawable getDrawable() {
		return drawable;
	}

	public void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public int getPositionX() {
		return positionX;
	}
	
	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}
	public int getPositionY() {
		return positionY;
	}
	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}
}
