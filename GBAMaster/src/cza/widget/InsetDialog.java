package cza.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cza.gbamaster.R;

public class InsetDialog implements View.OnClickListener {
	
	private View mView;
	private ViewGroup mContainer;
	private TextView mTitleView;
	private OnClickListener mListener;
	
	public final static int BUTTON_SUBMIT = -1;
	public final static int BUTTON_CANCEL = -2;
	
	public InsetDialog(Context context){
		View layout = LayoutInflater.from(context).inflate(R.layout.popupdialog, null);
		mView = layout;
		mContainer = (ViewGroup)layout.findViewById(R.id.container);
		mTitleView = (TextView)layout.findViewById(R.id.title);
		layout.findViewById(R.id.btn_cancel).setOnClickListener(this);
		layout.findViewById(R.id.btn_ok).setOnClickListener(this);
		dismiss();
	}
	
	public Context getContext(){
		return mView.getContext();
	}
	
	/**
	 * 获取视图
	 * @return
	 */
	public View getView(){
		return mView;
	}
	
	/**
	 * 设置主内容视图
	 * @param view
	 */
	public void setContentView(View view){
		mContainer.removeAllViews();
		mContainer.addView(view);
	}
	
	/**
	 * 获取主内容视图
	 * @return
	 */
	public View getContentView(){
		return mContainer.getChildAt(0);
	}

	/**
	 * 加载布局
	 * @param resId
	 * @return
	 */
	public View inflateView(int resId){
		return LayoutInflater.from(getContext()).inflate(resId, null);
	}
	
	/**
	 * 设置标题
	 * @param resId
	 */
	public void setTitle(int resId){
		setTitle(getContext().getString(resId));
	}
	
	/**
	 * 设置标题
	 * @param text
	 */
	public void setTitle(CharSequence text){
		mTitleView.setText(text);
	}
	
	/**
	 * 显示
	 */
	public void show(){
		if(onShow())
			mView.setVisibility(View.VISIBLE);
	}

	/**
	 * 是否显示
	 * @return
	 */
	public boolean onShow() {
		return true;
	}
	
	/**
	 * 隐藏
	 */
	public void dismiss(){
		mView.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_cancel:
			onButtonClick(BUTTON_CANCEL);
			break;
		case R.id.btn_ok:
			onButtonClick(BUTTON_SUBMIT);
			break;
		}
	}
	
	protected final void onButtonClick(int which){
		if (triggerClick(which))
			dismiss();
	}

	public boolean triggerClick(int which){
		return mListener == null || mListener.onClick(this, which);
	}
	
	/**
	 * 确认取消按钮
	 */
	public void setOnClickListener(OnClickListener listener){
		mListener = listener;
	}
	
	public interface OnClickListener {
		public boolean onClick(InsetDialog popup, int which);
	}
}
