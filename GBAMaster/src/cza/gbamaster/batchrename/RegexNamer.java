package cza.gbamaster.batchrename;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import cza.file.FileInfo;
import cza.gbamaster.MyApplication;
import cza.gbamaster.R;

public class RegexNamer implements Namer {
	private EditText mFindInput, mReplacementInput;
	private String mReplacement;
	private Pattern mRegex;

	@Override
	public boolean withoutType() {
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater) {
		View layout = inflater.inflate(R.layout.batch_rename_regex, null);
		mFindInput = (EditText) layout.findViewById(R.id.input_find);
		mReplacementInput = (EditText) layout.findViewById(R.id.input_replacement);
		return layout;
	}

	@Override
	public boolean onStart() {
		String Find = mFindInput.getText().toString();
		if (Find.isEmpty()) {
			MyApplication.toast(mFindInput.getContext(), "查找为空");
			return false;
		}
		try {
			mRegex = Pattern.compile(Find);
		} catch (Exception e) {
			MyApplication.toast(mFindInput.getContext(), "正则错误");
			return false;
		}
		mReplacement = mReplacementInput.getText().toString();
		return true;
	}

	@Override
	public String getName(FileInfo info) {
		Matcher m = mRegex.matcher(info.name);
		try {
			return m.replaceAll(mReplacement);
		} catch (Exception e) {}
		return info.name;
	}

	@Override
	public void onRecursionReset() {
		
	}

}
