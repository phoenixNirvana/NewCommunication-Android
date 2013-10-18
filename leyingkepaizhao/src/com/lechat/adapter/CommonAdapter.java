package com.lechat.adapter;

import java.util.List;

import android.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class CommonAdapter<T> extends BaseAdapter{

	protected Context mContext;
	protected List<T> mDatas; 
	
	public CommonAdapter(Context context,List<T> datas) {
		super();
		mContext = context;
		mDatas = datas;
	}

	@Override
	public int getCount() {
		return mDatas != null ? mDatas.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return mDatas != null ? mDatas.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}

}
