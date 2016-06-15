package cza.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ArrayEditText extends SpinnerEditText implements AdapterView.OnItemClickListener {

	private List<String> list = new ArrayList<String>();
	private StringArrayAdapter adapter;

	public ArrayEditText(Context c) {
		this(c, null);
	}
	
	public ArrayEditText(Context c, AttributeSet attr) {
		super(c, attr);
		adapter = new StringArrayAdapter(c, list, StringArrayAdapter.TYPE_LIST);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> apt, View v, int position, long id) {
		setText(list.get(position));
		closePopUp();
	}

	public void setList(Collection<String> nList){
		list.clear();
		list.addAll(nList);
		checkMoreBtn();
		adapter.notifyDataSetChanged();
	}

	public void setList(String...nList){
		setList(Arrays.asList(nList));
	}

	public void checkMoreBtn() {
		showMoreBtn(!list.isEmpty());
	}

}
