package cza.widget;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import cza.gbamaster.R;

public class PrefLayout extends LinearLayout {

	public static int margin, marginX;
	protected TextView titleView;
	public String mTitle;

	public PrefLayout(Context c, String title, boolean clickable){
		super(c, null, clickable ? R.attr.clickableBar : 0);
		vertical(this);
		mTitle = title;
		TextView view = new TextView(c);
		view.setTextSize(18);
		view.setText(title);
		titleView = view;
		LP lp = new LP();
		lp.rightMargin = cza.app.App.dip2px(18);
		addView(view, lp);
	}

	@Override
	public void setOrientation(int o) {
		super.setOrientation(o);
		int px = o == 0 ? margin : marginX;
		setPadding(px, margin, px, margin);
	}

	public static void initMargin(View v){
		if (margin == 0){
			Resources res = v.getResources();
			margin = res.getDimensionPixelSize(R.dimen.selectsty_padding);
			marginX = res.getDimensionPixelSize(R.dimen.selectsty_padding_x);
		}
	}

	public static void vertical(LinearLayout layout){
		layout.setOrientation(VERTICAL);
		initMargin(layout);
		layout.setPadding(marginX, margin, marginX, margin);
	}
}
