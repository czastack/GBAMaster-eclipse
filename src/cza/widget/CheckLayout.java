package cza.widget;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

public class CheckLayout extends TextPrefLayout {
	private CheckBox mCheckBox;
	public boolean mChecked;

	public CheckLayout(Context context, String title, String hint){
		super(context, title);
		MyLayout layout = new MyLayout(HORIZONTAL, context);
		removeView(titleView);
		addView(layout, 0, LP.FILL);
		layout.addView(titleView, LP.HLine);
		mCheckBox = new CheckBox(context);
		layout.addView(mCheckBox, LP.WRAP);
		if (hint == null)
			hintView.setVisibility(View.GONE);
		else 
			setHint(hint);
		setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		trigger();
	}
	
	public void trigger(){
		mCheckBox.setChecked(mChecked = !mChecked);
	}
}
