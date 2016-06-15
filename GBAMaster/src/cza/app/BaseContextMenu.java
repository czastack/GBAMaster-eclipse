package cza.app;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import cza.gbamaster.R;
import cza.widget.StringArrayAdapter;

public class BaseContextMenu extends Dialog implements AdapterView.OnItemClickListener {

	public ListView listView;
	private String[] texts;
	private BaseAdapter mAdpt;
	private Callback mCallback;

	public BaseContextMenu(Context c){
		super(c);
		setView(R.layout.dialog_list);
		listView = (ListView) findView(R.id.list);
		listView.setOnItemClickListener(this);
	}

	/**
	 * 设置列表项
	 * @param list
	 */
	public void setList(String...list){
		mAdpt = new StringArrayAdapter(getContext(), texts = list, StringArrayAdapter.TYPE_LIST);
		listView.setAdapter(mAdpt);
	}

	/**
	 * 从数组资源设置列表项
	 * @param list
	 */
	public void setList(int resId){
		setList(listView.getResources().getStringArray(resId));
	}

	/**
	 * 获取指定项
	 * @param i
	 * @return
	 */
	public String getText(int i){
		return texts[i];
	}

	/**
	 * 显示菜单
	 * @return
	 */
	public boolean onLongClick() {
		if ((mCallback != null) && mCallback.onShowMenu(this)) {
			show();
			return true;
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View item, int index, long itemId) {
		if (mCallback != null) {
			mCallback.onItemClick(this, index);
			dismiss();
		}
	}
	
	/**
	 * 设置回调
	 * @param callback
	 */
	public void setCallback(Callback callback) {
		mCallback = callback;
	}
	
	public interface Callback {
		public boolean onShowMenu(BaseContextMenu dialog);
		public void onItemClick(BaseContextMenu dialog, int position);
	}
}






