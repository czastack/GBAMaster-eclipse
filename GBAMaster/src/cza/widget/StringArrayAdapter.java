package cza.widget;

import android.content.Context;
import android.widget.ArrayAdapter;
import java.util.List;

public class StringArrayAdapter extends ArrayAdapter<String> {
	public static final int 
	TYPE_LIST = 1,
	TYPE_SPINNER = 2;

	public StringArrayAdapter(Context c, List<String> list, int type) {
		super(c, getView(type), list);
	}

	public StringArrayAdapter(Context c, String[] list, int type) {
		super(c, getView(type), list);
		if (TYPE_SPINNER == type)
			setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}

	public StringArrayAdapter(Context c, int resId, int type) {
		this(c, c.getResources().getStringArray(resId), type);
	}
	
	private static int getView(int type){
		switch (type){
			default:
			case TYPE_LIST:
				return android.R.layout.simple_list_item_1;
			case TYPE_SPINNER:
				return android.R.layout.simple_spinner_item;
		}
	}
}
