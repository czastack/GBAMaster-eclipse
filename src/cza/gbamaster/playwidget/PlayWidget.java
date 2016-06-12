package cza.gbamaster.playwidget;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import cza.gbamaster.PlayActivity;

public class PlayWidget implements View.OnTouchListener {

	protected View mLayout;
	protected ViewGroup mParent;
	protected PlayActivity mOwner;
	private RelativeLayout.LayoutParams mLayoutParams;
	
	public PlayWidget(PlayActivity owner, int resId){
		this(owner, owner.inflateView(resId));
	}
	
	public PlayWidget(PlayActivity owner, View view){
		mOwner = owner;
		mParent = owner.getContentView();
		mLayout = view;
		mLayoutParams = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT);
		view.setLayoutParams(mLayoutParams);
		mParent.addView(view);
	}

	/**
	 * 拖动
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		if (action == MotionEvent.ACTION_MOVE)
			setPosition(-1, (int)event.getRawY() - v.getHeight());
		return true;
	}
	
	/**
	 * 设置大小
	 * @param left
	 * @param top
	 */
	public void setSize(int width, int height){
		mLayoutParams.width = width;
		mLayoutParams.height = height;
		updateLayout();
	}
	
	/**
	 * 设置位置
	 * @param left
	 * @param top
	 */
	public void setPosition(int left, int top){
		if (left != -1)
			mLayoutParams.leftMargin = left;
		if (top != -1)
			mLayoutParams.topMargin = top;
		updateLayout();
	}
	
	/**
	 * 更新布局
	 */
	public void updateLayout(){
		mLayout.setLayoutParams(mLayoutParams);
	}

	/**
	 * 销毁
	 */
	protected void destroy(){
		mParent.removeView(mLayout);
	}
}
