package cza.widget;

import cza.app.ListDialog;


public abstract class SelectCallback implements ListDialog.Helper {
	public boolean multiple;
	
	public abstract String getTitle(int position);

	public void onShow() {}

	public void onSubmit(boolean multiple, int[] checkedIndexs) {}
}
