package cza.widget;

import cza.app.App;
import android.content.Context;
import android.widget.LinearLayout;

public class MyLayout extends LinearLayout {
	public MyLayout(int def, Context c) {
		super(c, null, def);
	}

	public MyLayout(Context c, int o){
		super(c);
		set(o, LP.FILL_WRAP);
	}

	public void set(int o, LP lp){
		setOrientation(o);
		setLayoutParams(lp);
	}

	void setPadding(int i){
		i = App.dip2px(i);
		setPadding(i, i, i, i);
	}
}
