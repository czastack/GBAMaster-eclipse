package cza.widget;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import cza.app.App;
import cza.gbamaster.R;
import cza.util.ViewUtils;

public class ItemMoveInsetDialog extends InsetDialog implements TextView.OnEditorActionListener  {

	public EditText mIndexInput;
	private int mIndex;
	private Callback mCallback;
	
	public ItemMoveInsetDialog(Context context, Callback callback) {
		super(context);
		mCallback = callback;
		setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_move, null));
		setTitle(R.string.move);
		mIndexInput = (EditText)getContentView().findViewById(R.id.iet);
		ViewUtils.setOnDown(mIndexInput, this);
		ViewUtils.registerClick(getContentView(), mButtonOnClickListener, 
			R.id.top, R.id.bottom, R.id.btn_up, R.id.btn_down);
	}

	@Override
	public boolean triggerClick(int which) {
		if(which == BUTTON_SUBMIT){
			onSummit(mIndexInput.getText().toString());
			return true;
		}
		return super.triggerClick(which);
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
	
	@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		onButtonClick(BUTTON_SUBMIT);
		return true;
	}

	@Override
	public boolean onShow() {
		pre(String.valueOf(mIndex));
		return true;
	}

	/**
	 * 设置位置
	 * @param index
	 * @return
	 */
	public boolean setIndex(int index){
		if (-1 < index && index <= mCallback.getMax()){
			mIndex = index;
			return true;
		}
		return false;
	}
	
	/**
	 * 获取当前目标序号
	 * @return
	 */
	public int getIndex(){
		return mIndex;
	}
	
	/**
	 * 显示当前序号
	 * @param text
	 */
	public void pre(CharSequence text) {
		mIndexInput.setText(text);
		mIndexInput.selectAll();
	}

	/**
	 * 提交
	 * @param text
	 * @return
	 */
	public boolean onSummit(String text) {
		int index = Integer.parseInt(text);
		if (mCallback.tryIndex(mIndex, index)) {
			mIndex = index;
			return true;
		} else {
			App.toast(getContext(), "位置无效");
			return false;
		}
	}

	public interface Callback {
		public boolean tryIndex(int from, int to);
		public int getMax();
	}
}
