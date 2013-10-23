package com.lechat.camera.data;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lechat.camera.widget.BottomView;
import com.lechat.camera.widget.RotateLinearLayout;
import com.lechat.camera.widget.SlidBar;
import com.lechat.utils.CommonUtil;

public class SlidBarBuildHelper implements OnClickListener{

	private List<SlidBarItem> mSlidBarItems;
	private SlidBar mSlidBar; 
	private int mHeight = 50;
	private int mItemWidth;
	private int mShowNum = 5;
	private int mScreenHeight;
	private View rootView;
	private Context mContext;
	private ClickListener mClickListener;
	private PopupWindow mPopupWindow;
	
	public void setContext(Context context){
		mContext = context;
	}
	
	public void setSlidBar(SlidBar slidBar){
		mSlidBar = slidBar;
	}
	
	public void setSlidBarData(List<SlidBarItem> slidBarItems){
		mSlidBarItems = slidBarItems;
	}
	
	public void showSlidBar(BottomView view,LinearLayout layout){
		
		if(mPopupWindow == null){
			generatePopup();
		}
		System.out.println(" "+view.getTop());
		if(!mPopupWindow.isShowing()){
			mPopupWindow.showAtLocation((View)view.getParent(), Gravity.NO_GRAVITY, view.getWidth(), view.getTop() - CommonUtil.dip2px(mContext, mHeight));
		}
	}
	
	public void closeSlidBar(){
		if(mPopupWindow != null && mPopupWindow.isShowing()){
			mPopupWindow.dismiss();
		}
	}
	
	private void generatePopup(){
		if(rootView != null){
			((ViewGroup) rootView).removeAllViews();
		}
		int width = mContext.getResources().getDisplayMetrics().widthPixels;
		mScreenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
		mItemWidth = width / mShowNum;
		generateLayout();
		mPopupWindow = new PopupWindow(mSlidBar, WindowManager.LayoutParams.MATCH_PARENT, CommonUtil.dip2px(mContext, mHeight));
		mPopupWindow.setTouchInterceptor(new OnTouchListener() {
  
	            @Override
	            public boolean onTouch(View v, MotionEvent event) {
	                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
	                    mPopupWindow.dismiss();
	                    return true;
	                }
	                return false;
	            }
	    });
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setTouchable(true);
	    mPopupWindow.setFocusable(true);
	    mPopupWindow.setOutsideTouchable(true);
	}
	
	private void generateLayout(){
		
		if(mSlidBar == null){
			mSlidBar = new SlidBar(mContext);
			rootView = mSlidBar.getRootView();			
		}
		if(mSlidBarItems == null){
			return;
		}
		int size = mSlidBarItems.size();
		for(int i = 0; i < size;i++){
			RotateLinearLayout rlayout = generateItemLayout(i);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(mItemWidth, CommonUtil.dip2px(mContext, mHeight));
			layoutParams.gravity = Gravity.CENTER;
			((ViewGroup) rootView).addView(rlayout, layoutParams);
		}
	}
	
	private RotateLinearLayout generateItemLayout(int position){
		RotateLinearLayout rlayout = new RotateLinearLayout(mContext);
		rlayout.setOrientation(LinearLayout.VERTICAL);
		SlidBarItem item = mSlidBarItems.get(position);
		/*ImageView rotateImageView = new ImageView(mContext);
		rotateImageView.setImageResource(item.getItemNRes());
		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(mItemWidth,LayoutParams.WRAP_CONTENT);
		imageParams.weight = 1;		
		imageParams.gravity = Gravity.CENTER;
		rlayout.addView(rotateImageView,imageParams);*/
		TextView textView = new TextView(mContext);
		textView.setBackgroundColor(Color.RED);
		textView.setText(item.getItemName());
		textView.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(mItemWidth,LayoutParams.WRAP_CONTENT);
		textParams.topMargin = CommonUtil.dip2px(mContext, 2);
		rlayout.addView(textView,textParams);
		rlayout.setOnClickListener(this);
		rlayout.setTag(item.getItemNRes());
		return rlayout;
	}
	
	public void setRotate(int rotate){
	    if(mSlidBar != null)
		   mSlidBar.setRotate(rotate);
	}
	
	public void setListener(ClickListener clickListener){
		mClickListener = clickListener;
	}
	
	public interface ClickListener{
		void onClick(int pos);
	}

	@Override
	public void onClick(View v) {
		
		if(mClickListener != null){
			int pos = (Integer) v.getTag();
			mClickListener.onClick(pos);
		}
	}
}
