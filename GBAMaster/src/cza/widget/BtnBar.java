package cza.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class BtnBar extends LinearLayout {
	public int mId;
	public LP btnLP;
	private boolean showAsBar;

	public BtnBar(Context c, AttributeSet attr) {
		super(c, attr);
		btnLP = getOrientation() == 0 ? LP.HLine : LP.FILL_WRAP;
		showAsBar = true;
	}

	public BtnBar(Context c, int o, boolean showAsBar) {
		super(c, null, showAsBar ? android.R.attr.buttonBarStyle : 0);
		setOrientation(o);
		setLayoutParams(LP.FILL_WRAP);
		btnLP = o == HORIZONTAL ? LP.HLine : LP.FILL_WRAP;
		this.showAsBar = showAsBar;
	}

	public void addButton(View.OnClickListener l, String...texts) {
		Context c = getContext();
		for (String text : texts) {
			Button btn;
			if (showAsBar) 
				btn = new Button(c, null, android.R.attr.buttonBarButtonStyle);
			else 
				btn = new Button(c);
			btn.setText(text);
			btn.setTag(mId++);
			btn.setOnClickListener(l);
			addView(btn, btnLP);
		}
	}
	
	public void addButton(View.OnClickListener l, int resId){
		addButton(l, getResources().getStringArray(resId));
	}
}
