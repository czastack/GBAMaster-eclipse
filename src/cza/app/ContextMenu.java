package cza.app;

import android.view.View;

public class ContextMenu extends BaseContextMenu implements View.OnLongClickListener {
	public View mView;

	public ContextMenu(View v){
		super(v.getContext());
		v.setOnLongClickListener(this);
	}

	@Override
	public boolean onLongClick(View v) {
		mView = v;
		return onLongClick();
	}
}
