package cza.gbamaster.batchrename;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import cza.file.FileInfo;
import cza.gbamaster.MyApplication;
import cza.gbamaster.R;

public class InsertNamer implements Namer {
	private RadioGroup mDirView;
	private EditText mStartInput, mEndInput, mReplacementInput;
	private int mStart, mEnd;
	private boolean mIsEnd;
	private String mReplacement;

	@Override
	public boolean withoutType() {
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater) {
		View layout = inflater.inflate(R.layout.batch_rename_insert, null);
		mDirView = (RadioGroup) layout.findViewById(R.id.dirSwitch);
		mStartInput = (EditText) layout.findViewById(R.id.input_start);
		mEndInput = (EditText) layout.findViewById(R.id.input_end);
		mReplacementInput = (EditText) layout.findViewById(R.id.input_replacement);
		return layout;
	}

	@Override
	public boolean onStart() {
		mIsEnd = mDirView.getCheckedRadioButtonId() == R.id.z_a;
		String Start = mStartInput.getText().toString(),
			End = mEndInput.getText().toString();
		if (Start.isEmpty()) 
			mStart = 0;
		else 
			mStart = Integer.parseInt(Start);
		if (End.isEmpty()) 
			mEnd = 0;
		else 
			mEnd = Integer.parseInt(End);
		if (mEnd < mStart){
			MyApplication.toast(mDirView.getContext(), "位置错误");
			return false;
		}
		mReplacement = mReplacementInput.getText().toString();
		return true;
	}

	@Override
	public String getName(FileInfo info) {
		String main = BatchRenameCore.getMain(info);
		int start, end;
		if (mIsEnd) {
			int len = main.length();
			start = len - mEnd;
			end = len - mStart;
		} else {
			start = mStart;
			end = mEnd;
		}
		return main.substring(0, start)
			+ mReplacement + main.substring(end)
			+ BatchRenameCore.getType(info);
	}

	@Override
	public void onRecursionReset() {
		
	}

}
