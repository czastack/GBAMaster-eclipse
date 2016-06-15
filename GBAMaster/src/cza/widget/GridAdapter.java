package cza.widget;

public class GridAdapter extends MyAdapter {
	private int rowCount;
	public int colCount;
	public boolean isVertical;


	int getIndex(int index){
		if (isVertical){
			int col = index % colCount;
			int row = (index - col) / colCount;
			index = col * rowCount + row;
		}
		return index;
	}

	public void setRowCount(int count){
		int len = getCount();
		rowCount = count;
		colCount = (int) Math.ceil((float)len / rowCount);
	}
}
