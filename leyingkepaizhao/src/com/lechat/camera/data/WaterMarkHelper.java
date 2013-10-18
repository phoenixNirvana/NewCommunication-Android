package com.lechat.camera.data;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class WaterMarkHelper {

	
	public List<WaterMarkPosition> getWaterMarkPosition(View view){
		
		int n = ((ViewGroup)view).getChildCount();
		List<WaterMarkPosition> list = new ArrayList<WaterMarkPosition>();
		for(int i = 0 ; i < n ; i++){
			View child = ((ViewGroup)view).getChildAt(i);
			System.out.println("WaterMarkHelper  left="+child.getLeft()+"  top="+child.getTop());
			WaterMarkPosition waterMarkPos = null;
			if(child instanceof TextView){
				String text = (String) ((TextView)child).getText();
				waterMarkPos = new WaterMarkPosition(child.getLeft(), child.getTop(),text);
			}else if(child instanceof ImageView){
				Drawable drawable =((ImageView)child).getDrawable();
				waterMarkPos = new WaterMarkPosition(child.getLeft(), child.getTop(),drawable);
			}
			list.add(waterMarkPos);
		}
		return list;
	}
	
	
	
	
}
