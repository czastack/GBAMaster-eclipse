package cza.gbamaster.cot;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.content.Context;
import android.widget.Spinner;
import cza.widget.LP;
import cza.widget.PrefLayout;
import cza.widget.StringArrayAdapter;

public class SimpleSelect extends PrefLayout {
	public int[] mValues;
	public Spinner mSelect;
	
	public SimpleSelect(Context context, String title){
		super(context, title, false);
		mSelect = new Spinner(context);
		addView(mSelect, LP.FILL);
	}

	/**
	 * 选中项代码
	 * @return
	 */
	public int getValue(){
		return mValues[mSelect.getSelectedItemPosition()];
	}

	/**
	 * 选中项文本
	 * @return
	 */
	public String getTitle(){
		return mSelect.getSelectedItem().toString();
	}
	
	/**
	 * xml读入数据
	 */
	public void readData(CotResource cotRes, String entryName) {
		try {
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(cotRes.open(entryName), "UTF-8"));
			String str;
			final int count = Integer.parseInt(reader.readLine());
			int[] values = new int[count];
			String[] items = new String[count];
			int index = 0;
			while ((str = reader.readLine()) != null) {
				if (str.isEmpty())
					continue;
				String[] arr = str.split("\t\t");
				values[index] = Integer.parseInt(arr[0], 16);
				items[index] = arr[1];
				index++;
			}
			reader.close();
			mValues = values;
			StringArrayAdapter adapter = new StringArrayAdapter(getContext(), items, StringArrayAdapter.TYPE_SPINNER);
			mSelect.setAdapter(adapter);
		} catch (Exception e) {}
	}
}
