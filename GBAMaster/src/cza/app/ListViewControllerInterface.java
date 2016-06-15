package cza.app;

import java.util.List;

import cza.app.Dialog.OnClickListener;
import cza.widget.MyAdapter;

public interface ListViewControllerInterface {

	public int MODE_ITEM = 0;
	public int MODE_SINGLE = 1;
	public int MODE_DOUBLE = 2; //要双击的单选
	public int MODE_MULTI = 3;
	

	/**
	 * 设置提交监听
	 * @param listener
	 */
	public void setOnSubmitListener(OnSubmitListener listener);
	
	public void toTop();
	
	public void toBottom();
	
	/**
	 * 获取选中数量
	 * @return
	 */
	public int getCheckCount();
	
	/**
	 * 选中状态
	 * @param position
	 * @return
	 */
	public boolean isChecked(int position);
	
	/**
	 * 全选
	 * @param checked
	 */
	public void checkAll(boolean checked);
	
	/**
	 * 获取选中状态
	 * @param list
	 */
	public boolean[] getCheckedList();
	
	/**
	 * 设置选中状态
	 * @param list
	 */
	public void setCheckedList(boolean[] list);
	
	/**
	 * 统计选中数
	 */
	public void countChecked();
	
	/**
	 * 获取选中序号数组
	 * @return
	 */
	public int[] getCheckedIndexs();
	

	/**
	 * 点击回调
	 * @param position
	 */
	public void onCheck(int position);
	
	/**
	 * 提交
	 */
	public void submit();
	
	/**
	 * 刷新数据
	 */
	public void refresh();
	
	/**
	 * 设置多选的容量
	 * @param size
	 */
	public void ensureCapacity(int size);


	/**
	 * 列表
	 * @param items
	 * @param listener
	 */
	public void setItems(CharSequence[] items, final OnClickListener listener);

	/**
	 * 单选
	 * @param items
	 * @param checkedIndex
	 * @param listener
	 * @param singleClose
	 */
	public void setItems(CharSequence[] items, int checkedIndex, OnClickListener listener, boolean singleClose);

	/**
	 * 复选
	 * @param items
	 * @param checkedList
	 * @param listener
	 */
	public void setItems(CharSequence[] items, boolean[] checkedList, OnClickListener listener);
	
	/**
	 * 设置选项
	 * @param list
	 */
	public void setList(List<String> list);

	/**
	 * 代理适配器
	 * @param helper
	 * @param listener
	 * @param mode
	 */
	public void setItems(MyAdapter.Helper helper, final OnClickListener listener, int mode);
	
	/**
	 * 模式改变
	 */
	public interface OnModeChangeListener {
		public void onModeChange(ListViewController controller, int mode);
	}
	
	/**
	 * 提交监听
	 */
	public interface OnSubmitListener {
		public void onSubmit(ListViewController controller, int[] checkedIndexs);
	}

	public interface Helper extends MyAdapter.Helper {
		/** 是否显示选择框 */
		public boolean unCheckable(int position);
		/** 多选检查边界 */
		public boolean isMultiCheckable(int position);
		/** 针对过滤 */
		public int getRealPosition(int position);
	}
	
}
