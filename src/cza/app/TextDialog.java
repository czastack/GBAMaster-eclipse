package cza.app;

import cza.gbamaster.R;
import android.content.Context;
import android.widget.TextView;

public class TextDialog extends Dialog {
	private TextView mMsgView;
	
	public TextDialog(Context context, int theme) {
		super(context, theme);
		setView(R.layout.dialog_text);
		mMsgView = (TextView)findView(R.id.textView);
	}
	
	/**
	 * 设置消息文本
	 * @param resId
	 */
	public void setMessage(int resId){
		setMessage(getContext().getString(resId));
	}
	
	/**
	 * 设置消息文本
	 * @param text
	 */
	public void setMessage(CharSequence text){
		mMsgView.setText(text);
	}
}
