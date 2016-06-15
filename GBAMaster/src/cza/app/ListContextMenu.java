package cza.app;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class ListContextMenu extends BaseContextMenu implements AdapterView.OnItemLongClickListener {
	public int mIndex;
	
	public ListContextMenu(ListView li){
		super(li.getContext());
		li.setOnItemLongClickListener(this);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> p1, View v, int index, long id) {
		mIndex = index;
		return onLongClick();
	}
}
