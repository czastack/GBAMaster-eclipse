package cza.app;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import cza.gbamaster.R;
import cza.util.ViewUtils;

public class ItemMoveDialog extends RenameDialog implements RenameDialog.OnSummitListener {

	private int mIndex;
	private Callback mCallback;

	public ItemMoveDialog(Context context, Callback callback) {
		super(context, R.layout.dialog_move);
		mCallback = callback;
		setOnSummitListener(this);
		setTitle(R.string.move);
		ViewUtils.registerClick(getMainView(), mButtonOnClickListener, 
			R.id.top, R.id.bottom, R.id.btn_up, R.id.btn_down);
		
		Window window = getWindow();
		window.setDimAmount(0f);
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.gravity = Gravity.TOP;
		if (context instanceof BaseActivity)
			lp.y = ((BaseActivity)context).getTitleBarHeight();
		window.setAttributes(lp);
	}

	public boolean setIndex(int index){
		if (-1 < index && index <= mCallback.getMax()){
			mIndex = index;
			return true;
		}
		return false;
	}

	private View.OnClickListener mButtonOnClickListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			int index = mIndex;
			switch(v.getId()){
				case R.id.top:
					index = 0;
					break;
				case R.id.bottom:
					index = mCallback.getMax();
					break;
				case R.id.btn_down:
					index--;
					break;
				case R.id.btn_up:
					index++;
					break;
			}
			if (mCallback.tryIndex(mIndex, index)) {
				mIndex = index;
				onShow();
			}
		}
	};
	
	@Override
	public void show() {
		onShow();
		super.show();
	}

	public void onShow() {
		pre(String.valueOf(mIndex));
	}

	@Override
	public boolean onSummit(RenameDialog d, String text) {
		int index = Integer.parseInt(text);
		if (!mCallback.tryIndex(mIndex, index)) {
			App.toast(getContext(), "位置无效");
			return false;
		} else 
			mIndex = index;
		return true;
	}

	public interface Callback {
		public boolean tryIndex(int from, int to);
		public int getMax();
	}
}
