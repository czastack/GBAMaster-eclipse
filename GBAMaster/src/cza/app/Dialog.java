package cza.app;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import cza.gbamaster.R;
import cza.util.ViewUtils;
import cza.widget.LP;

public class Dialog extends android.app.Dialog implements View.OnClickListener  {
	public ViewGroup mContainer;
	private Button mPositivaButton;
	private Button mNegativeButton;
	private Button mNeutralButton;
	private OnClickListener mListener;
	public final static byte 
	FLAG_BUTTON_POSITIVE = 1, 
	FLAG_BUTTON_NEGATIVE = 2;
	
	public Dialog(Context context, int theme){
		super(context, theme);
		setContentView(R.layout.dialog_alert_layout);
		mContainer = (ViewGroup)findViewById(R.id.container);
		mPositivaButton = initButton(android.R.id.button1);
		mNegativeButton = initButton(android.R.id.button2);
		mNeutralButton = initButton(android.R.id.button3);
	}

	public Dialog(Context context){
		this(context, 0);
	}

	public void setView(int layoutResID) {
		setView(inflateView(layoutResID));
	}
	
	public void setView(View view){
		mContainer.removeAllViews();
		mContainer.addView(view, LP.FILL);
	}
	
	/**
	 * 获取主内容视图
	 * @return
	 */
	public View getMainView(){
		return mContainer.getChildAt(0);
	}

	public View inflateView(int resId){
		return getLayoutInflater().inflate(resId, null);
	}

	public View findView(int id){
		return mContainer.findViewById(id);
	}
	
	/**
	 * 设置主视图
	 * @param id
	 */
	public void setContainer(int id){
		View view = findViewById(id);
		if (view != null)
			try {
				mContainer = (ViewGroup) view;
			} catch (Exception e) { }
	}
	
	/**
	 * 初始化底部按钮
	 * @param id
	 * @return
	 */
	private Button initButton(int id){
		Button button = (Button)findViewById(id);
		button.setOnClickListener(this);
		button.setVisibility(View.GONE);
		return button;
	}

	/**
	 * 添加顶部控件
	 * @param layoutId
	 * @return
	 */
	public View addHeader(int layoutId){
		View header = null;
		View layout = findView(R.id.layout);
		if (layout instanceof ViewGroup){
			header = inflateView(layoutId);
			((ViewGroup) layout).addView(header, 0);
		}
		return header;
	}

	@Override
	public void onClick(View v) {
		if (v == mPositivaButton)
			onButtonClick(BUTTON_POSITIVE);
		else if (v == mNegativeButton)
			onButtonClick(BUTTON_NEGATIVE);
		else if (v == mNeutralButton)
			onButtonClick(BUTTON_NEUTRAL);
	}
	
	private void onButtonClick(int which){
		if (triggerClick(which))
			dismiss();
	}
	
	public boolean triggerClick(int which){
		return mListener == null || mListener.onClick(this, which);
	}

	public Button getButton(int which){
		switch (which) {
			default:
			case BUTTON_POSITIVE:
				return mPositivaButton;
			case BUTTON_NEGATIVE:
				return mNegativeButton;
			case BUTTON_NEUTRAL:
				return mNeutralButton;
		}
	}
	
	/**
	 * 设置按钮
	 * @param which
	 * @param text
	 */
	public void setButton(int which, CharSequence text){
		Button button = getButton(which);
		button.setText(text);
		ViewUtils.hide(button, text == null);
	}

	/**
	 * 设置按钮
	 * @param which
	 * @param resId 字符串id
	 */
	public void setButton(int which, int resId){
		setButton(which, getContext().getString(resId));
	}
	
	/**
	 * 设置按钮
	 * @param flags
	 */
	public void setButton(byte flags){
		if((flags & FLAG_BUTTON_POSITIVE) != 0)
			setButton(BUTTON_POSITIVE, R.string.ok);
		if((flags & FLAG_BUTTON_NEGATIVE) != 0)
			setButton(BUTTON_NEGATIVE, R.string.cancel);
	}
	
	/**
	 * 确认取消按钮
	 */
	public void setConfirm(){
		setButton(BUTTON_POSITIVE, R.string.ok);
		setButton(BUTTON_NEGATIVE, R.string.cancel);
	}
	
	/**
	 * 设置全屏
	 * @param fx
	 * @param fy
	 */
	public void setFullScreen(boolean fx, boolean fy, boolean nopadding){
		Window win = getWindow();
		win.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if(nopadding){
			win.getDecorView().setPadding(0, 0, 0, 0);
			win.setBackgroundDrawable(new ColorDrawable(App.getThemeBackground(getContext())));
		}		
		WindowManager.LayoutParams lp = win.getAttributes();
		lp.width = fx ? WindowManager.LayoutParams.MATCH_PARENT : WindowManager.LayoutParams.WRAP_CONTENT;
		lp.height = fy ? WindowManager.LayoutParams.MATCH_PARENT : WindowManager.LayoutParams.WRAP_CONTENT;
		win.setAttributes(lp);
	}
	
	/**
	 * 设置全屏，有边距
	 */
	public void setFullScreenWithPadding(){
		setFullScreen(true, true, false);
	}
	
	/**
	 * 设置全屏，无边距
	 */
	public void setFullScreenNoPadding(){
		setFullScreen(true, true, true);
	}
	
	/**
	 * 是否全屏
	 * @return
	 */
	public boolean isFullScreen(){
		return (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
	}
	
	/**
	 * 确认取消按钮
	 */
	public void setOnClickListener(OnClickListener listener){
		mListener = listener;
	}
	
	public interface OnClickListener {
		public boolean onClick(Dialog dialog, int which);
	}
}
