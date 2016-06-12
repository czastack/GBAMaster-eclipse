package cza.app;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ListView;
import cza.gbamaster.R;
import cza.widget.MyAdapter;

public class ListDialog extends Dialog implements 
	ListViewControllerInterface,
	ListViewController.OnModeChangeListener,
	ListViewController.OnSubmitListener {
	
	public final static byte FULL_SCREEN_WITH_PADDING = 0x01;
	public final static byte FULL_SCREEN_NO_PADDING = 0x02;
	
	public ListViewController controller;
	private OnSubmitListener mOnSubmitListener;
	
	public ListView mListView;

	public ListDialog(Context c){
		super(c);
		setView(R.layout.dialog_list);
	}
	
	public ListDialog(Context c, int flags){
		super(c);
		if ((flags & FULL_SCREEN_WITH_PADDING) != 0)
			setFullScreenWithPadding();
		else if ((flags & FULL_SCREEN_NO_PADDING) != 0)
			setFullScreenNoPadding();
		setView(R.layout.dialog_list);
	}

	@Override
	public void setView(int resId) {
		super.setView(resId);
		View view = findView(R.id.list);
		if (view != null) {
			final ListView listView = mListView = (ListView)view;
			ViewGroup.LayoutParams params = listView.getLayoutParams();
			if(isFullScreen()){
				params.height = 1;
				listView.setLayoutParams(params);
			
				ViewTreeObserver observer = listView.getViewTreeObserver();
				observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						ViewGroup.LayoutParams params = listView.getLayoutParams();
						params.height = getParentRestHeight(listView);
						listView.setLayoutParams(params);
					}
				});
			}
			controller = new ListViewController(listView);
			controller.mDialog = this;
			controller.setOnModeChangeListener(this);
			controller.setOnSubmitListener(this);
		}
	}
	
	@Override
	public void onModeChange(ListViewController controller, int mode) {
		switch (mode){
		case ListViewController.MODE_SINGLE:
			setButton(BUTTON_NEGATIVE, R.string.cancel);
			break;
		case ListViewController.MODE_DOUBLE:
		case ListViewController.MODE_MULTI:
			setConfirm();
			break;
		}
	}

	@Override
	public boolean triggerClick(int which) {
		if(which == BUTTON_POSITIVE && controller.isCheckable()) {
			submit();
			return true;
		}
		return super.triggerClick(which);
	}
	
	@Override
	public void setOnSubmitListener(OnSubmitListener listener){
		mOnSubmitListener = listener;
	}

	@Override
	public void toTop() {
		controller.toTop();
	}

	@Override
	public void toBottom() {
		controller.toBottom();
	}

	@Override
	public int getCheckCount(){
		return controller.getCheckCount();
	}

	@Override
	public boolean isChecked(int position){
		return controller.isChecked(position);
	}

	@Override
	public void checkAll(boolean checked){
		controller.checkAll(checked);
	}
	
	@Override
	public boolean[] getCheckedList() {
		return controller.getCheckedList();
	}

	@Override
	public void setCheckedList(boolean[] list){
		controller.setCheckedList(list);
	}
	
	@Override
	public void countChecked(){
		controller.countChecked();
	}

	@Override
	public int[] getCheckedIndexs(){
		return controller.getCheckedIndexs();
	}
	
	@Override
	public void onCheck(int position) {
		controller.onCheck(position);
	}

	@Override
	public void submit(){
		controller.submit();
	}

	@Override
	public void onSubmit(ListViewController controller, int[] checkedIndexs) {
		if (mOnSubmitListener != null)
			mOnSubmitListener.onSubmit(controller, checkedIndexs);
		dismiss();
	}

	@Override
	public void refresh(){
		controller.refresh();
	}

	@Override
	public void ensureCapacity(int size){
		controller.ensureCapacity(size);
	}

	@Override
	public void setItems(CharSequence[] items, final OnClickListener listener){
		controller.setItems(items, listener);
	}

	@Override
	public void setItems(CharSequence[] items, int checkedIndex, OnClickListener listener, boolean singleClose){
		controller.setItems(items, checkedIndex, listener, singleClose);
	}

	@Override
	public void setItems(CharSequence[] items, boolean[] checkedList, OnClickListener listener){
		controller.setItems(items, checkedList, listener);
	}

	@Override
	public void setList(List<String> list){
		controller.setList(list);
	}

	@Override
	public void setItems(MyAdapter.Helper helper, final OnClickListener listener, int mode){
		controller.setItems(helper, listener, mode);
	}
	
	/**
	 * 获取父控件的剩余高度
	 */
	public static int getParentRestHeight(ListView listView){
		int height = 0;
		ViewGroup parent = (ViewGroup)listView.getParent();
		if (parent != null){
			height = parent.getHeight();
			for (int i = 0, length = parent.getChildCount(); i < length; i++){
				if (parent.getChildAt(i) != listView)
					height -= parent.getChildAt(i).getHeight();
			}
		}
		return height;
	}

}
