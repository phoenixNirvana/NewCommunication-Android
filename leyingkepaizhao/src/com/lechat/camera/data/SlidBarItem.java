package com.lechat.camera.data;

public class SlidBarItem {

	private String itemName;
	private int itemNRes;
	
	public SlidBarItem(String name,int resId){
		itemName = name;
		itemNRes = resId;
	}
	
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public int getItemNRes() {
		return itemNRes;
	}
	public void setItemNRes(int itemNRes) {
		this.itemNRes = itemNRes;
	}
	
}
