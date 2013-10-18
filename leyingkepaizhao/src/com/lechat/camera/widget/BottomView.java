package com.lechat.camera.widget;

import java.util.ArrayList;
import java.util.List;

import com.lechat.R;
import android.view.OrientationEventListener;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class BottomView extends LinearLayout{

	private int mWidth;
	private int mHeight;
	private int mCurRotate;
	private Context mContext;
	private RotateImageView mBtnTakePic;
	private RotateImageView mBtnSave;
    private RotateImageView mBtnCancel;
    private RotateImageView mBtnChoicePic;
	private RotateImageView mBtnSelectWaterMark;
	private RelativeLayout rlTake;
	private RelativeLayout rlSave;
	private List<RotateImageView> mBtnList;
	
	public BottomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public BottomView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		mContext = context;
		View view = LayoutInflater.from(mContext).inflate(R.layout.layout_bottom,this);
	//	addView(view);
		mBtnTakePic = (RotateImageView) findViewById(R.id.btn_take);
		mBtnChoicePic = (RotateImageView) findViewById(R.id.btn_choice);
		mBtnSave = (RotateImageView) findViewById(R.id.btn_save);
		mBtnCancel = (RotateImageView) findViewById(R.id.btn_cancel);
		mBtnSelectWaterMark = (RotateImageView) findViewById(R.id.btn_watermark);
		
		rlTake = (RelativeLayout) findViewById(R.id.rl_take);
		rlSave = (RelativeLayout) findViewById(R.id.rl_save);
		mBtnList = new ArrayList<RotateImageView>();
		
		mBtnList.add(mBtnTakePic);
		mBtnList.add(mBtnChoicePic);
		mBtnList.add(mBtnSave);
		mBtnList.add(mBtnCancel);
		mBtnList.add(mBtnSelectWaterMark);
	}

	public int getViewHeight(){
		System.out.println("getViewHeight height="+mHeight);
		return mHeight;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mHeight = b - t;
	}

	public void setViewListener(OnClickListener onClickListener){
		mBtnTakePic.setOnClickListener(onClickListener);
		mBtnChoicePic.setOnClickListener(onClickListener);
		mBtnSave.setOnClickListener(onClickListener);
		mBtnCancel.setOnClickListener(onClickListener);
		mBtnSelectWaterMark.setOnClickListener(onClickListener);
	}
	
	public void changeLayout(boolean isNormal){
		
		if(isNormal){
			rlSave.setVisibility(View.VISIBLE);
			rlTake.setVisibility(View.GONE);
		}else{
			rlTake.setVisibility(View.VISIBLE);
			rlSave.setVisibility(View.GONE);
		}
	}
	
	public boolean isSaveState(){
		if(rlSave != null){
			return rlSave.getVisibility() == View.VISIBLE;
		}
		return false;
	}
	
	public void setRotate(int rotate){
		if(mCurRotate == rotate || mCurRotate == OrientationEventListener.ORIENTATION_UNKNOWN)
			return;
		mCurRotate = rotate;
		if(mBtnList != null){
			for(int i = 0; i < mBtnList.size();i++){
				RotateImageView rotateImageView = mBtnList.get(i);
				rotateImageView.setRotate(rotate);
			}
		}
	}
}
