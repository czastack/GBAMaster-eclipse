package cza.gbamaster;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cza.app.BaseContextMenu;
import cza.hack.Emu;
import cza.hack.GameST;
import cza.widget.MyAdapter;

public class STDialog extends BaseContextMenu implements MyAdapter.Helper {
	boolean saveMode;
	private MyAdapter stAdpt;
	protected Emu emu;
	ArrayList<GameST> list;

	STDialog(Context c, Emu emulator){
		super(c);
		emu = emulator;
		list = new ArrayList<GameST>();
		stAdpt = new MyAdapter();
		stAdpt.setHelper(this);
		listView.setAdapter(stAdpt);
	}

	public void setSaveMode(boolean on){
		setTitle((saveMode = on) ? R.string.saveST : R.string.loadST);
	}

	public void onDataChange(){
		stAdpt.notifyDataSetChanged();
	}

	@Override
	public void show() {
		for (GameST state : list)
			state.recycle();
		list.clear();
		for (int i = 0; i < 10; i++) {
			GameST st = emu.getST(i);
			if (saveMode || st.time > 0) {
				list.add(st);
				st.mIndex = i;
			}
		}
		onDataChange();
		super.show();
	}

	public int getCount() {
		return list != null ? list.size() : 0;
	}

	@Override
	public View getView(int position, View item) {
		Holder holder;
		if (item == null) {
			holder = new Holder();
			item = inflateView(R.layout.state_item);
			holder.findView(item);
			item.setTag(holder);
		} else {
			holder = (Holder) item.getTag();
		}
		holder.set(list.get(position));
		return item;
	}

	private class Holder {
		private ImageView img;
		private TextView name, time;

		private void findView(View item) {
			img = (ImageView) item.findViewById(R.id.st_img);
			name = (TextView) item.findViewById(R.id.st_name);
			time = (TextView) item.findViewById(R.id.st_time);
		}

		private void set(GameST st) {
			img.setImageBitmap(st.bp);
			name.setText(st.mIndex == 0 ? "快捷" : "存档" + st.mIndex);
			time.setText(st.timeStr);
		}
	}
}
