package cza.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cza.gbamaster.R;

public class MenuAdapter extends BaseAdapter {

	private Menu mMenu;
	protected LayoutInflater mInflater;
	
	public MenuAdapter(Context context, Menu menu){
		mInflater = LayoutInflater.from(context);
		mMenu = menu;
	}
	
	@Override
	public int getCount() {
		return mMenu != null ? mMenu.size() : 0;
	}
	
	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return mMenu.getItem(position).getItemId();
	}
	

	@Override
	public View getView(int position, View item, ViewGroup parent) {
		Holder holder;
		if (item == null) {
			holder = new Holder();
			item = mInflater.inflate(R.layout.list_menu, null);
			holder.findView(item);
			item.setTag(holder);
		} else {
			holder = (Holder) item.getTag();
		}
		holder.set(mMenu.getItem(position));
		return item;
	}

	private class Holder {
		public ImageView iconView;
		public TextView titleView;

		private void findView(View item) {
			iconView = (ImageView) item.findViewById(R.id.iconView);
			titleView = (TextView) item.findViewById(R.id.title);
		}

		private void set(MenuItem item) {
			iconView.setImageDrawable(item.getIcon());
			titleView.setText(item.getTitle());
		}
	}
}
