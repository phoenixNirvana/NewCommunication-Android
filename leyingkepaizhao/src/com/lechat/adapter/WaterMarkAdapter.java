package com.lechat.adapter;

import java.util.List;

import com.lechat.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WaterMarkAdapter extends CommonAdapter<View>{

	public WaterMarkAdapter(Context context, List<View> datas) {
		super(context, datas);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Gallery.LayoutParams gparams = new Gallery.LayoutParams(Gallery.LayoutParams.MATCH_PARENT, Gallery.LayoutParams.MATCH_PARENT);
		View view = mDatas.get(position);
		view.setLayoutParams(gparams);
		return view;
	}

}
