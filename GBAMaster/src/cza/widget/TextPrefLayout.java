package cza.widget;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import cza.app.Dialog;

public class TextPrefLayout extends PrefLayout implements View.OnClickListener {
	protected TextView hintView;
	public Dialog mDialog;

	public TextPrefLayout(Context c, String title){
		super(c, title, true);
		hintView = new TextView(c);
		hintView.setTextSize(16);
		addView(hintView);
		setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if (mDialog != null)
			mDialog.show();
	}

	public void setHint(CharSequence text){
		hintView.setText(text);
	}
}
