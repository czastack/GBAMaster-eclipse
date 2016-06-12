package cza.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cza.gbamaster.R;

public class Input extends LinearLayout {
	public TextView textView;
	public EditText editView;
	private OnceLayoutListener mListener;

	public Input(Context c, String label){
		super(c, null, R.attr.inputStyle);
		LayoutInflater.from(c).inflate(R.layout.widget_input, this, true);
		textView = (TextView) findViewById(R.id.input_text);
		editView = (EditText) findViewById(R.id.input_edit);
		textView.setText(label);
	}

	public Input(Context c, String label, String text){
		this(c, label);
		editView.setText(text);
	}
	
	public void setOnceLayoutListener(OnceLayoutListener listener){
		mListener = listener;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mListener != null) {
			mListener.onceLayout(this);
			mListener = null;
		}
	}

	public String getTitle(){
		return textView.getText().toString();
	}

	public String getValue(){
		return editView.getText().toString();
	}
	
	public void requesInput(){
		editView.requestFocus();
		InputMethodManager im =
			(InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		im.toggleSoftInput(0, 1);
		im.showSoftInput(editView, 0);
	}
	
	public void numberOnly(){
		editView.setInputType(2);
	}
}
