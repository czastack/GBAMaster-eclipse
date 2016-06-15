package cza.gbamaster.batchrename;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import cza.file.FileInfo;
import cza.gbamaster.R;

public class NumberNamer implements Namer {
	
	protected Spinner mFixView;
	protected EditText mFixInput, mStartValueInput, mMinLengthInput;
	protected int mStartValue, mValue;
	protected String mLength, mParam;
	protected boolean mAutoReset;
	protected CheckBox mAutoResetCheckBox;
	
	protected void check() {
		String Num = mStartValueInput.getText().toString();
		mLength = mMinLengthInput.getText().toString();
		mAutoReset = mAutoResetCheckBox.isChecked();
		if (Num.isEmpty()) {
			mStartValue = 1;
		} else {
			mStartValue = Integer.parseInt(Num);
		}
		onRecursionReset();
		if (mLength.isEmpty()) {
			mLength = "3";
		}
	}

	@Override
	public boolean withoutType() {
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater) {
		return onCreateView(inflater, R.layout.batch_rename_num);
	}
	
	public View onCreateView(LayoutInflater inflater, int resId) {
		View layout = inflater.inflate(resId, null);
		mFixView = (Spinner) layout.findViewById(R.id.fixSwitch);
		mFixInput = (EditText) layout.findViewById(R.id.input_fix);
		mStartValueInput = (EditText) layout.findViewById(R.id.input_startVal);
		mMinLengthInput = (EditText) layout.findViewById(R.id.input_minLength);
		mAutoResetCheckBox = (CheckBox) layout.findViewById(R.id.resetAfter);
		return layout;
	}

	@Override
	public boolean onStart() {
		check();
		String mPrefix = "";
		String mSuffix = "";
		String fix = mFixInput.getText().toString();
		switch (mFixView.getSelectedItemPosition()) {
			case 0:
				mPrefix = fix;
				break;
			case 1:
				mSuffix = fix;
				break;
		}
		mParam = mPrefix + "%0" + mLength + "d" + mSuffix;
		return true;
	}

	@Override
	public String getName(FileInfo info) {
		return String.format(mParam, mValue++) + BatchRenameCore.getType(info);
	}

	@Override
	public void onRecursionReset() {
		if (mAutoReset)
		mValue = mStartValue;
	}

}
