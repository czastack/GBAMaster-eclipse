package cza.gbamaster;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import cza.app.BaseContextMenu;
import cza.app.Dialog;
import cza.app.ListDialog;
import cza.file.FileInfos;
import cza.gbamaster.batchrename.BatchRenameCore;
import cza.widget.MyAdapter;

public class BatchRename extends BaseContextMenu implements 
		BaseContextMenu.Callback,
		CompoundButton.OnCheckedChangeListener,
		Dialog.OnClickListener,
		MyAdapter.Helper {

	private Dialog mDialog;
	private ViewGroup mViewParent;
	private ListDialog mConfirmDialog;
	//公共控件
	private EditText mTypeInput;
	private CheckBox mRecursionCheckBox;
	private TextView mRecursionHintView;
	//核心
	private BatchRenameCore mCore;
	private boolean mFinished;

	public BatchRename(Context c) {
		super(c);
		mCore = new BatchRenameCore();
		mConfirmDialog = new ListDialog(c);
		mConfirmDialog.setOnClickListener(this);
		mConfirmDialog.setItems(this, null, ListDialog.MODE_ITEM);
		mConfirmDialog.setConfirm();
		setTitle("批量重命名");
		setList(BatchRenameCore.TYPES);
		setCallback(this);
	}

	/**
	 * 显示/隐藏递归提示
	 */
	@Override
	public void onCheckedChanged(CompoundButton v, boolean show) {
		mRecursionHintView.setVisibility(show ? 0 : 8);
	}

	/**
	 * 切换重命名的方式
	 */
	@Override
	public void onItemClick(BaseContextMenu d, int which) {
		if (mDialog == null){
			mDialog = new Dialog(getContext());
			mDialog.setView(R.layout.dialog_layout);
			mViewParent = (ViewGroup)mDialog.getMainView();
			mDialog.setConfirm();
			mDialog.setOnClickListener(this);
		}
		if (mCore.changeType(which)){
			View layout = mCore.mNamer.onCreateView(getLayoutInflater());
			mDialog.setTitle(mCore.getTitle());
			mViewParent.removeAllViews();
			mViewParent.addView(layout);
			mTypeInput = (EditText) layout.findViewById(R.id.input_type);
			mRecursionCheckBox = (CheckBox) layout.findViewById(R.id.ableR);
			mRecursionHintView = (TextView) layout.findViewById(R.id.aboutR);
			mRecursionCheckBox.setOnCheckedChangeListener(this);
			if (mCore.mNamer.withoutType())
				((View) mTypeInput.getParent()).setVisibility(View.GONE);
		}
		mDialog.show();
	}

	@Override
	public boolean onShowMenu(BaseContextMenu d) {
		return true;
	}

	/**
	 * 设置数据源
	 */
	public void setInfos(FileInfos infos){
		mCore.setInfos(infos);
	}
	
	/**
	 * 撤销重命名
	 */
	public void undo() {
		mCore.undo();
		preview();
	}
	
	/**
	 * 对话框确认事件
	 * confirm 要确认两次
	 * <ol>
	 * <li>预览，确认执行重命名 finished -> true</li>
	 * <li>确认结果，关闭对话框</li></ol>
	 * 其它对话框，记录输入的参数,并生成预览结果
	 */
	@Override
	public boolean onClick(Dialog dialog, int which) {
		if (dialog == mConfirmDialog){
			if (which == BUTTON_POSITIVE) {
				if (!mFinished) {
					mCore.start();
					//回调
					triggerClick(BUTTON_POSITIVE);
					mConfirmDialog.setTitle(R.string.finished);
					mConfirmDialog.refresh();
					mFinished = true;
				} else 
					mConfirmDialog.cancel();
				return !mFinished;
			} else {
				mFinished = false;
				return true;
			}
		} else if (dialog == mDialog){
			if (which == BUTTON_POSITIVE) {
				mCore.mNamer.onStart();
				mCore.newType = mTypeInput.getText().toString();
				mCore.ableR = mRecursionCheckBox.isChecked();
				mCore.build();
				preview();
			}
		}
		return true;
	}

	/**
	 * 预览
	 */
	private void preview(){
		mConfirmDialog.setTitle(R.string.preview);
		mConfirmDialog.show();
	}

	@Override
	public int getCount() {
		return mCore.mDatas != null ? mCore.mDatas.size() : 0;
	}

	@Override
	public View getView(int position, View item) {
		BatchRenameCore.Data data = mCore.mDatas.get(position);
		Holder holder;
		if (data.isDir){
			TextView textView = (TextView)inflateView(R.layout.batch_rename_item_dir);
			textView.setText(data.origin);
			item = textView;
		} else {
			if (item == null || item.getTag() == null) {
				holder = new Holder();
				item = inflateView(R.layout.batch_rename_item);
				holder.findView(item);
				item.setTag(holder);
			} else {
				holder = (Holder) item.getTag();
			}
			holder.set(data);
		}
		return item;
	}

	private class Holder {
		private TextView originalView, currentView, stateView;

		public void findView(View item){
			originalView = (TextView) item.findViewById(R.id.originalView);
			currentView = (TextView) item.findViewById(R.id.currentView);
			stateView = (TextView) item.findViewById(R.id.stateView);
		}

		public void set(BatchRenameCore.Data data) {
			originalView.setText(data.origin);
			currentView.setText(data.current);
			stateView.setText(data.state);
		}
	}
}
