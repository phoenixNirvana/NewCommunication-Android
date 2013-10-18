package com.lechat.adapter;

import java.util.List;

import android.content.Context;
import android.widget.BaseAdapter;

public abstract class MyBaseAdapter<T> extends BaseAdapter {
	
	public List<T> datas;
	public Context context;
	
	public MyBaseAdapter(Context context, List<T> list){
		
		this.context = context;
		this.datas = list;
		
	}
	
	public boolean hasData(){
		
		return this.datas != null && !this.datas.isEmpty();
	}
	
	/**
	 * 追加数据
	 * 
	 * @param list   数据
	 * @param index  加入数据在集合的位置
	 */
	public void appendData(List<T> list){
		this.datas.addAll(list);
		this.notifyDataSetChanged();
	}
	
	/**
	 * 追加单条数据
	 * 
	 * @param t
	 */
	public void appendData(T t){
		this.datas.add(t);
		this.notifyDataSetChanged();
	}
	
	public void refreshData(List<T> list){
		this.datas = list;
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		
		if (datas != null)
			return datas.size();
		else
			return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

}
