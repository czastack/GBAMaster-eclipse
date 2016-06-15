package cza.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

public class MyAdapter extends BaseAdapter {
	private Helper mHelper;

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getCount() {
		return mHelper.getCount();
	}

	@Override
	public View getView(int position, View item, ViewGroup parent) {
		return mHelper.getView(position, item);
	}

	public void setHelper(Helper helper){
		mHelper = helper;
	}
	
	public void updateView(int position, AbsListView listView) {  
		// 得到第一个可显示控件的位置，
		int visiblePosition = listView.getFirstVisiblePosition();
		// 只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
		if (position - visiblePosition >= 0) 
			mHelper.getView(position, listView.getChildAt(position - visiblePosition));
    }  

	public interface Helper{
		public int getCount();
		public View getView(int position, View item);
	}
}
