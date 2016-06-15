package cza.widget;

import cza.app.App;
import android.widget.LinearLayout;

public class LP extends LinearLayout.LayoutParams {
	public static int fit(int i){
		return i > 0 ? App.dip2px(i) : i;
	}

	public LP() {
		super(-2, -2);
	}

	public LP(int w, int h) {
		super(fit(w), fit(h));
	}

	public LP(int w, int h, int wt) {
		this(w, h);
		weight = wt;
	}

	public LP setMargin(int i){
		i = App.dip2px(i);
		setMargins(i, i, i, i);
		return this;
	}

	public LP setGravity(int i){
		gravity = i;
		return this;
	}

	public static final LP
	O = new LP(0, 0),
	FILL = new LP(-1, -1),
	WRAP = new LP(-2, -2),
	FILL_WRAP = new LP(-1, -2),
	WRAP_FILL = new LP(-2, -1),
	VLine = new LP(-1, 0, 1),
	HLine = new LP(0, -2, 1),
	B = new LP(-2, -2).setGravity(80);
}
