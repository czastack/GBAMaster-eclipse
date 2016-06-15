package cza.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import cza.app.Dialog;
import cza.app.ListDialog;
import cza.app.ListViewController;
import cza.gbamaster.R;
import cza.util.ArrayUtils;
import cza.util.ViewUtils;

public class MultipleSelect extends Select implements 
		ListDialog.OnClickListener,
		MyAdapter.Helper,
		RadioGroup.OnCheckedChangeListener,
		DragGridView.Callback {

	public int mSelectLimit;
	private TextView mStateView;
	private ViewGroup mPagesLayout;
	private View[] mPageArray;
	private boolean[] mTempCheckedArray;
	//排序
	private boolean mUseSort;
	private int[] mTempCheckedIndexs;
	private DragGridView mGridView;
	private GridAdapter mGridAdapter;

	public MultipleSelect(Context c, String title, SelectCallback callback){
		super(c, title, callback);
	}

	/**
	 * 初始化
	 */
	protected void init(){
		mStateView = (TextView) mListDialog.findView(R.id.mulChkState);
		ViewUtils.registerClick(mListDialog.findView(R.id.mulChkBar), this, 
				R.id.btn_clear, R.id.btn_toTop, R.id.btn_toBottom);
		mPageArray = new View[2];
		mPageArray[0] = mListDialog.getMainView();
		mPageArray[1] = mListDialog.inflateView(R.layout.cheat_select_page_sort);
		//换成主视图
		mListDialog.setView(R.layout.cheat_select_main);
		RadioGroup pageSwitch = (RadioGroup)mListDialog.findView(R.id.pageSwitch);
		mPagesLayout = (ViewGroup)mListDialog.findView(R.id.cheat_select_pages);
		pageSwitch.setOnCheckedChangeListener(this);
		//默认是选取视图
		pageSwitch.check(R.id.cheat_select_list);
		//初始化排序模块
		mGridView = (DragGridView)mPageArray[1].findViewById(R.id.gridView);
		mGridAdapter = new GridAdapter();
		mGridAdapter.isVertical = true;
		mGridAdapter.setHelper(this);
		mGridView.setCallback(this);
		mGridView.setAdapter(mGridAdapter);
		ViewUtils.registerClick(mPageArray[1], this, R.id.btn_reset, R.id.btn_inverse);
		//对话框调整
		mListDialog.setOnClickListener(this);
		stateBar.setVisibility(View.VISIBLE);
		mListDialog.setItems(mCallback, this, ListDialog.MODE_MULTI);
	}

	/**
	 * 设置多选数量
	 * @param limit
	 */
	public void setSelectLimit(int size){
		mSelectLimit = size;
		mTempCheckedIndexs = new int[size];
		Arrays.fill(mTempCheckedIndexs, -1);
		changeData();
	}

	/**
	 * 设置选中状态
	 * @param list
	 */
	public void setCheckedList(boolean[] list){
		mListDialog.setCheckedList(list);
		changeData();
	}

	/**
	 * 设置选中项
	 */
	public void setCheckedList(int[] list){
		mListDialog.ensureCapacity(mCallback.getCount()); //会清空原来的选中
		for (int i = 0, index; i < list.length; i++) {
			index = list[i];
			manualSelect(index);
			mTempCheckedIndexs[i] = index;
		}
		mUseSort = true;
		changeData();
	}

	/**
	 * 清空选中
	 */
	public void clearSelected(){
		mListDialog.checkAll(false);
		changeData();
	}

	/**
	 * 刷新选中数
	 */
	public void changeData(){
		mStateView.setText(mListDialog.getCheckCount() + "/" + mSelectLimit);
	}
	
	/**
	 * 设置排序界面每列行数
	 * @param row
	 */
	public void setSortRow(int row){
		if (row <= 0)
			row = mSelectLimit;
		mGridAdapter.setRowCount(row);
		mGridView.setNumColumns(mGridAdapter.colCount);
	}
	
	/**
	 * 去除占位
	 * @return 实际长度
	 */
	private int removePlaceholders(){
		int[] checkedIndexs = mTempCheckedIndexs;
		int i, len = 0;
		for (i = 0; i < checkedIndexs.length; i++){
			if (checkedIndexs[i] != -1){
				if (i != len)
					checkedIndexs[len] = checkedIndexs[i];
				len++;
			}
		}
		for (i = len; i < checkedIndexs.length; i++)
			checkedIndexs[i] = -1;
		return len;
	}
	
	/**
	 * 初始化排序
	 */
	private void initSort(){
		int[] checkedIndexs = mListDialog.getCheckedIndexs();
		ArrayList<Integer> origin, added;
		TreeSet<Integer> originKeys = new TreeSet<Integer>();
		int[] main = mTempCheckedIndexs;
		removePlaceholders();
		origin = new ArrayList<Integer>(mSelectLimit);
		added  = new ArrayList<Integer>(mSelectLimit);
		for (int index : checkedIndexs){
			int key = ArrayUtils.indexOf(main, index);
			if (key != -1)
				originKeys.add(key);
			else 
				added.add(index);
		}
		for (int key : originKeys)
			origin.add(main[key]);
		int i, length;
		int index = 0;
		Arrays.fill(main, -1);
		for (i = 0, length = origin.size(); i < length; i++)
			main[index++] = origin.get(i);
		for (i = 0, length = added.size(); i < length; i++)
			main[index++] = added.get(i);
		refreshSortList();
	}
	
	/**
	 * 重置排序
	 */
	private void resetSort(){
		int[] array = mListDialog.getCheckedIndexs();
		int[] checkedIndexs = mTempCheckedIndexs;
		int i;
		for (i = 0; i < array.length; i++)
			checkedIndexs[i] = array[i];
		for (i = array.length; i < checkedIndexs.length; i++)
			checkedIndexs[i] = -1;
		refreshSortList();
	}
	
	/**
	 * 逆转排序
	 */
	private void reverseSort(){
		int[] checkedIndexs = mTempCheckedIndexs;
		int i, len = removePlaceholders();
		int temp;
		for (i = 0; i < len / 2; i++){
			temp = checkedIndexs[i];
			int key = len - 1 - i;
			checkedIndexs[i] = checkedIndexs[key];
			checkedIndexs[key] = temp;
		}
		refreshSortList();
	}
	
	@Override
	public boolean isAvailablePosition(int index){
		return true;
	}

	@Override
	public void onFinishDrag(int before, int after) {
		before = mGridAdapter.getIndex(before);
		after = mGridAdapter.getIndex(after);
		if (ArrayUtils.moveTo(mTempCheckedIndexs, before, after))
			refreshSortList();
	}

	//填充排序
	@Override
	public int getCount() {
		return mSelectLimit;
	}

	@Override
	public View getView(int position, View item) {
		if (item == null) {
			item = mListDialog.inflateView(R.layout.cheat_select_sort_item);
		}
		String title = null;
		int keyAtArray = mGridAdapter.getIndex(position);
		int keyAtData;
		keyAtData = mTempCheckedIndexs[keyAtArray];
		if (keyAtData != -1)
			title = mCallback.getTitle(keyAtData);
		((TextView)item).setText(title);
		return item;
	}

	/**
	 * 刷新排序列表
	 */
	public void refreshSortList(){
		mGridAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.btn_clear:
				clearSelected();
				break;
			case R.id.btn_toTop:
				mListDialog.toTop();
				break;
			case R.id.btn_toBottom:
				mListDialog.toBottom();
				break;
			case R.id.btn_reset:
				resetSort();
				break;
			case R.id.btn_inverse:
				reverseSort();
				break;
			default: 
				super.onClick(v);
				//弹出列表前备份状态
				mTempCheckedArray = Arrays.copyOf(mListDialog.getCheckedList(), 
					mListDialog.getCheckedList().length);
				break;
		}
	}

	@Override
	public boolean onClick(Dialog dialog, int which) {
		if (dialog == mDialog)
			changeData();
		else if (dialog == mListDialog){
			if (which == Dialog.BUTTON_NEGATIVE){
				setCheckedList(mTempCheckedArray);
				initSort();
			}
		}
		return true;
	}

	/**
	 * 提交
	 */
	@Override
	public void onSubmit(ListViewController dialog, int[] checkedIndexs){
		if (mUseSort && checkedIndexs.length > 0)
			checkedIndexs = mTempCheckedIndexs;
		mCallback.onSubmit(true, checkedIndexs);
	}

	/**
	 * 切换视图
	 */
	@Override
	public void onCheckedChanged(RadioGroup v, int id) {
		mPagesLayout.removeAllViews();
		//初始化排序
		if (id == R.id.cheat_select_sort){
			initSort();
			mUseSort = true;
			mPagesLayout.addView(mPageArray[1], LP.FILL);
		} else {
			mUseSort = false;
			mPagesLayout.addView(mPageArray[0], LP.FILL);
		}
	}
}
