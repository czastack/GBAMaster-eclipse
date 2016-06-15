package cza.widget;

import android.content.Context;
import android.view.View;
import cza.app.ListDialog;
import cza.app.ListViewController;
import cza.gbamaster.R;

public class Select extends TextPrefLayout implements ListDialog.OnSubmitListener {

	public int mIndex;
	public ListDialog mListDialog;
	public BtnBar btnBar;
	protected View stateBar;
	protected SelectCallback mCallback;

	public Select(Context context, String title, SelectCallback callback){
		super(context, title);
		ListDialog dialog = new ListDialog(context);
		dialog.addHeader(R.layout.dialog_select);
		btnBar = (BtnBar) dialog.findView(R.id.btnBar);
		stateBar = dialog.findView(R.id.mulChkBar);
		dialog.setTitle(title);
		mDialog = mListDialog = dialog;
		dialog.setOnSubmitListener(this);
		mCallback = callback;
		init();
		select(0);
	}

	protected void init(){
		stateBar.setVisibility(View.GONE);
		mListDialog.setItems(mCallback, null, ListDialog.MODE_SINGLE);
	}

	public void select(int i){
		setHint(mCallback.getTitle(mIndex = i));
		mListDialog.mListView.setSelection(i);
	}
	
	/**
	 * 手动选中
	 */
	public void manualSelect(int position){
		mListDialog.onCheck(position);
	}

	@Override
	public void onClick(View v) {
		mCallback.onShow();
		super.onClick(v);
	}
	
	public void onSubmit(ListViewController controller, int[] checkedIndexs){
		select(checkedIndexs[0]);
		mCallback.onSubmit(false, checkedIndexs);
	}
}
