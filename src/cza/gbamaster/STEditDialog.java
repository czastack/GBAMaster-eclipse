package cza.gbamaster;

import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import cza.hack.Emu;
import cza.hack.GameST;
import cza.widget.BtnBar;

public class STEditDialog extends STDialog implements 
	Comparator<GameST> {

	private int dir;
	private boolean top = true;

	public STEditDialog(Context c, Emu emulator) {
		super(c, emulator);
		setTitle(R.string.setST);
		setConfirm();
		BtnBar bar = new BtnBar(c, BtnBar.HORIZONTAL, true);
		bar.addButton(this, R.array.STEditDialog_btnText);
		((ViewGroup)findView(R.id.layout)).addView(bar, 0);
		listView.setOnItemClickListener(null);
	}

	@Override
	public void onClick(View v) {
		switch ((Integer)v.getTag()) {
			default:
				super.onClick(v);
				return;
			case 0:
				fix(top = true);
				break;
			case 1:
				fix(top = false);
				break;
			case 2:
				sort(true);
				break;
			case 3:
				sort(false);
				break;
		}
		fix(top);
		onDataChange();
	}

	private void fix(boolean top) {
		int i = top ? 0 : 10 - list.size();
		for (GameST st : list) {
			st.mIndex = i++;
		}
	}

	@Override
	public boolean triggerClick(int which) {
		if(which == BUTTON_POSITIVE) {
			emu.adjustSTSTo(list);
		}
		return super.triggerClick(which);
	}

	public void sort(boolean up) {
		dir = up ? 1 : -1;
		Collections.sort(list, this);
	}

	@Override
	public int compare(GameST s1, GameST s2) {
		return (s1.time < s2.time ? -1 : 1) * dir;
	}
}
