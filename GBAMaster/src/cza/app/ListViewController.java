package cza.app;

import java.util.Arrays;
import java.util.List;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.TextView;
import cza.app.Dialog.OnClickListener;
import cza.gbamaster.R;
import cza.widget.MyAdapter;

public class ListViewController implements 
		ListViewControllerInterface,
		AdapterView.OnItemClickListener, 
		MyAdapter.Helper {

	public ListView mListView;
	private int mLayoutRes;
	private BaseAdapter mAdapter;
	private OnClickListener mOnClickListener;
	private OnModeChangeListener mOnModeChangeListener;
	private OnSubmitListener mOnSubmitListener;
	private int mMode;
	//简易模式
	private boolean simple;
	private boolean advanced; //启用内部Helper
	private CharSequence[] mItems;
	List<String> mList;
	public int mCheckedIndex = -1;
	//多选
	private boolean[] mCheckedList;
	private int mCheckedCount;
	public Dialog mDialog; // 依附的对话框
	
	public ListViewController(ListView listView){
		mListView = listView;
	}
	
	/**
	 * 设置模式改变监听
	 * @param listener
	 */
	public void setOnModeChangeListener(OnModeChangeListener listener){
		mOnModeChangeListener = listener;
	}
	
	@Override
	public void setOnSubmitListener(OnSubmitListener listener){
		mOnSubmitListener = listener;
	}
	
	//填充数据
	@Override
	public int getCount() {
		return simple ? simpleGetCount() : helperGetCount();
	}

	@Override
	public View getView(int position, View item) {
		return simple ? 
			simpleGetView(position, item): 
			helperGetView(position, item);
	}

	/**
	 * 列表点击事件
	 */
	@Override
	public void onItemClick(AdapterView<?> adpt, View item, int position, long id) {
		onCheck(position);
	}

	@Override
	public void toTop() {
		mListView.setSelection(0);
	}

	@Override
	public void toBottom() {
		mListView.setSelection(getCount());
	}

	@Override
	public int getCheckCount(){
		return mCheckedCount;
	}

	@Override
	public boolean isChecked(int position){
		return mMode == MODE_MULTI ? mCheckedList[position] : (mCheckedIndex == position);
	}

	/**
	 * 是否可选
	 * @return
	 */
	public boolean isCheckable(){
		return mMode != MODE_ITEM;
	}

	@Override
	public void onCheck(int position) {
		if (isCheckable()){
			//针对过滤
			if (advanced)
				position = ((Helper)mHelper).getRealPosition(position);
			if (mMode == MODE_MULTI){
				//多选
				if (simple || (advanced && ((Helper)mHelper).isMultiCheckable(position))){
					if (mCheckedList[position] = !mCheckedList[position])
						mCheckedCount++;
					else 
						mCheckedCount--;
				}
			} else {
				//单选
				if (mMode == MODE_SINGLE) {
					mCheckedIndex = position;
					submit();
				} else if (mCheckedIndex == position)
					submit();
				else 
					mCheckedIndex = position;
			}
			if(mAdapter instanceof MyAdapter)
				((MyAdapter) mAdapter).updateView(position, mListView);
			else
				refresh();
		}
		if (mOnClickListener != null)
			mOnClickListener.onClick(mDialog, position);
	}
	
	@Override
	public void checkAll(boolean checked){
		Arrays.fill(mCheckedList, checked);
		mCheckedCount = checked ? mCheckedList.length : 0;
		refresh();
	}
	
	@Override
	public boolean[] getCheckedList() {
		return mCheckedList;
	}
	
	@Override
	public void setCheckedList(boolean[] list){
		mCheckedList = list;
		countChecked();
		refresh();
	}
	
	@Override
	public void countChecked(){
		int count = 0;
		for (int i = 0; i < mCheckedList.length; i++){
			if (mCheckedList[i])
				count++;
		}
		mCheckedCount = count;
	}
	
	/**
	 * 获取标题
	 * @return
	 */
	public CharSequence[] getItems(){
		return mItems;
	}
	
	@Override
	public int[] getCheckedIndexs(){
		int[] indexs;
		if (mMode != MODE_MULTI){
			indexs = new int[]{mCheckedIndex};
		} else {
			indexs = new int[mCheckedCount];
			if (mCheckedCount > 0){
				int key = 0;
				boolean[] arr = mCheckedList;
				for (int index = 0; index < arr.length; index++){
					if (arr[index])
						indexs[key++] = index;
				}
			}
		}
		return indexs;
	}
	
	@Override
	public void submit(){
		if (mOnSubmitListener != null) 
			mOnSubmitListener.onSubmit(this, getCheckedIndexs());
	}
	
	@Override
	public void refresh(){
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void ensureCapacity(int size){
		mCheckedList = new boolean[size];
		Arrays.fill(mCheckedList, false);
	}

	/**
	 * 注册监听
	 */
	private void register(){
		mListView.setAdapter(mAdapter);
		if (isCheckable() || mOnClickListener != null){
			mListView.setOnItemClickListener(this);
		}
	}
	
	/**
	 * 初始化
	 * @param mode
	 */
	private void onModeChange(int mode){
		mMode = mode;
		if (mOnModeChangeListener != null)
			mOnModeChangeListener.onModeChange(this, mode);
	}

	
	/*
	 * 简易模式
	 */
	
	/**
	 * 列表
	 * @param items
	 * @param listener
	 */
	private void simpleInit(CharSequence[] items, final OnClickListener listener){
		simple = true;
		mItems = items;
		mOnClickListener = listener;
		MyAdapter adpt = new MyAdapter();
		mAdapter = adpt;
		adpt.setHelper(this);
		register();
	}
	
	@Override
	public void setItems(CharSequence[] items, final OnClickListener listener){
		onModeChange(MODE_ITEM);
		mLayoutRes = android.R.layout.simple_list_item_1;
		simpleInit(items, listener);
	}

	@Override
	public void setItems(CharSequence[] items, int checkedIndex, OnClickListener listener, boolean singleClose){
		onModeChange(singleClose ? MODE_SINGLE : MODE_DOUBLE);
		mLayoutRes = android.R.layout.simple_list_item_single_choice;
		mCheckedIndex = checkedIndex;
		simpleInit(items, listener);
	}

	@Override
	public void setItems(CharSequence[] items, boolean[] checkedList, OnClickListener listener){
		onModeChange(MODE_MULTI);
		mLayoutRes = android.R.layout.simple_list_item_multiple_choice;
		if (checkedList != null){
			mCheckedList = checkedList;
			countChecked();
		} else {
			ensureCapacity(items.length);
		}
		simpleInit(items, listener);
	}
	
	@Override
	public void setList(List<String> list){
		mList = list;
	}
	
	/**
	 * 简易模式获取选项
	 * @param position
	 * @return
	 */
	private CharSequence simpleGetItem(int position){
		return mItems != null ? mItems[position] : mList.get(position);
	}
	
	/**
	 * 简易模式获取数量
	 * @return
	 */
	private int simpleGetCount() {
		Log.d("测试", mList.size() + "");
		return mItems != null ? mItems.length : mList.size();
	}

	/**
	 * 简易模式获取视图
	 * @param position
	 * @param item
	 * @return
	 */
	private View simpleGetView(int position, View item) {
		if (item == null) 
			item = LayoutInflater.from(mListView.getContext()).inflate(mLayoutRes, null);
		((TextView) item).setText(simpleGetItem(position));
		if (isCheckable())
			((Checkable)item).setChecked(isChecked(position));
		return item;
	}
	
	/* 代理 开始 */
	private MyAdapter.Helper mHelper;

	@Override
	public void setItems(MyAdapter.Helper helper, final OnClickListener listener, int mode){
		simple = false;
		onModeChange(mode);
		switch (mode){
			case MODE_ITEM:
				mLayoutRes = 0;
				break;
			case MODE_SINGLE:
			case MODE_DOUBLE:
				mLayoutRes = R.layout.list_singlechoice;
				break;
			case MODE_MULTI:
				mLayoutRes = R.layout.list_multichoice;
				ensureCapacity(helper.getCount());
				break;
		}
		MyAdapter adpt = new MyAdapter();
		mAdapter = adpt;
		mHelper = helper;
		advanced = helper instanceof Helper;
		mOnClickListener = listener;
		adpt.setHelper(this);
		register();
	}

	/**
	 * 代理模式获取数量
	 * @return
	 */
	private int helperGetCount() {
		return mHelper.getCount();
	}

	/**
	 * 代理简易模式获取视图
	 * @return
	 */
	private View helperGetView(int position, View item) {
		//针对过滤
		if (advanced)
			position = ((Helper)mHelper).getRealPosition(position);
		if (!isCheckable())
			return mHelper.getView(position, item);
		else if (advanced && ((Helper)mHelper).unCheckable(position))
			return mHelper.getView(position, null);
		View btn, view;
		if (item == null || !(item.getTag() instanceof View)) {
			ViewGroup container = (ViewGroup) LayoutInflater.from(mListView.getContext()).inflate(mLayoutRes, null);
			ViewGroup content = (ViewGroup) container.findViewById(R.id.content);
			view = mHelper.getView(position, null);
			content.addView(view, -1, -2);
			btn = container.findViewById(R.id.btn);
			item = container;
			item.setTag(btn);
			btn.setTag(view);
		} else {
			btn = (View) item.getTag();
			view = (View) btn.getTag();
			mHelper.getView(position, view);
		}
		((Checkable)btn).setChecked(isChecked(position));
		return item;
	}
	/* 代理 结束 */
	
}
