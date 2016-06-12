package cza.app;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import cza.gbamaster.R;
import cza.util.ViewUtils;

public class RenameDialog extends EditDialog implements TextView.OnEditorActionListener {
	public static final int
	ERROR_EMPTY = R.string.emptyFilename,
	ERROR_EXIST = R.string.fileExists;
	private OnSummitListener mOnSummitListener;
	
	public RenameDialog(Context c){
		super(c, MODE_SHOW);
		setTitle("重命名");
		init();
	}
	
	public RenameDialog(Context c, int layoutId){
		super(c, MODE_EMPTY);
		setView(layoutId);
		textarea = (EditText) findView(R.id.iet);
		init();
	}
	
	private void init(){
		ViewUtils.setOnDown(textarea, this);
		setConfirm();
	}

	public boolean error(int code){
		App.toast(getContext(), ERROR_EMPTY);
		return false;
	}
	
	@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (triggerClick(BUTTON_POSITIVE)) 
			dismiss();
		return true;
	}

	@Override
	public boolean triggerClick(int which) {
		if(which == BUTTON_POSITIVE) {
			return mOnSummitListener.onSummit(this, textarea.getText().toString());
		}
		return super.triggerClick(which);
	}

	public void pre(CharSequence text) {
		setMessage(text);
		textarea.selectAll();
	}

	public void setOnSummitListener(OnSummitListener l) {
		mOnSummitListener = l;
	}

	public static interface OnSummitListener {
		public boolean onSummit(RenameDialog d, String text);
	}
}
