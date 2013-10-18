package com.lechat.camera.data;

import java.util.ArrayList;
import java.util.List;

import com.lechat.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class WaterMarkBuildHelp {

	private List<View> mWaterMarkView;
	private Context mContext;
    
	
	public WaterMarkBuildHelp(Context context) {
		mContext = context;
	}

	public List<View> getWaterMarkView(){
		generateView();
		return mWaterMarkView;
	}
	
	private void generateView(){
		mWaterMarkView = new ArrayList<View>();
		mWaterMarkView.add(LayoutInflater.from(mContext).inflate(R.layout.layout_water_mark_one,null));
		mWaterMarkView.add(LayoutInflater.from(mContext).inflate(R.layout.layout_water_mark_two,null));
		mWaterMarkView.add(LayoutInflater.from(mContext).inflate(R.layout.layout_water_mark_three,null));
		mWaterMarkView.add(LayoutInflater.from(mContext).inflate(R.layout.layout_water_mark_four,null));
		mWaterMarkView.add(LayoutInflater.from(mContext).inflate(R.layout.layout_water_mark_five,null));
		mWaterMarkView.add(LayoutInflater.from(mContext).inflate(R.layout.layout_water_mark_six,null));
	}
}
