package cza.gbamaster.batchrename;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import cza.file.FileInfo;
import cza.gbamaster.R;

public class FromListNamer implements Namer {
	private EditText mListInput;
	private int mIndex;
	private CheckBox mResetCheckBox;
	private boolean mAutoReset;
	private String[] mNameList;

	@Override
	public boolean withoutType() {
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater) {
		View layout = inflater.inflate(R.layout.batch_rename_fromlist, null);
		mListInput = (EditText) layout.findViewById(R.id.input_list);
		mResetCheckBox = (CheckBox) layout.findViewById(R.id.resetAfter);
		return layout;
	}

	@Override
	public boolean onStart() {
		onRecursionReset();
		mAutoReset = mResetCheckBox.isChecked();
		mNameList = mListInput.getText().toString().split("\n");
		return true;
	}

	@Override
	public String getName(FileInfo info) {
		if (mIndex < mNameList.length)
			return mNameList[mIndex++];
		else
			return info.name;
	}

	@Override
	public void onRecursionReset() {
		if(mAutoReset)
			mIndex = 0;
	}

}
