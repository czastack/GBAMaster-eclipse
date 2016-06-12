package cza.gbamaster.batchrename;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import cza.file.FileInfo;
import cza.gbamaster.R;

public class ZeroNamer implements Namer {
	private EditText mLengthInput;
	private boolean mIsEnd;
	private String mParam;
	private Pattern mRegex;

	@Override
	public boolean withoutType() {
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater) {
		View layout = inflater.inflate(R.layout.batch_rename_zero, null);
		mLengthInput = (EditText) layout.findViewById(R.id.input_minLength);
		return layout;
	}

	@Override
	public boolean onStart() {
		String r = "[0-9]+";
		String Len = mLengthInput.getText().toString();
		mRegex = Pattern.compile(mIsEnd ? r + "$" : "^" + r);
		if (Len.isEmpty()) 
			Len = "3";
		mParam = "%0" + Len + "d";
		return true;
	}

	@Override
	public String getName(FileInfo info) {
		Matcher m = mRegex.matcher(BatchRenameCore.getMain(info));
		while (m.find()) {
			StringBuffer sb = new StringBuffer();
			int num = Integer.parseInt(m.group());
			String to = String.format(mParam, num);
			m.appendReplacement(sb, to);
			m.appendTail(sb);
			return sb + BatchRenameCore.getType(info);
		}
		return info.name;
	}

	@Override
	public void onRecursionReset() {
		
	}

}
