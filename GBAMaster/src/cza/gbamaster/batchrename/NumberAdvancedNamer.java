package cza.gbamaster.batchrename;

import android.view.LayoutInflater;
import android.view.View;
import cza.file.FileInfo;
import cza.gbamaster.MyApplication;
import cza.gbamaster.R;

public class NumberAdvancedNamer extends NumberNamer {

	private static final String[] ADVNUM_ARGS = new String[]{"", "d", "f", "n", "e", "E"};
	public String pName;
	
	@Override
	public View onCreateView(LayoutInflater inflater) {
		return onCreateView(inflater, R.layout.batch_rename_advnum);
	}

	@Override
	public boolean onStart() {
		String parts = mFixInput.getText().toString();
		if (parts.isEmpty()){
			MyApplication.toast(mAutoResetCheckBox.getContext(), "格式为空");
			return false;
		}
		check();
		String param = parts.replaceAll("%d", "%1\\$0" + mLength + "d");
		String[] args = ADVNUM_ARGS;
		for (int i = 2; i < args.length; i++){
			param = param.replaceAll("%" + args[i], "%" + i + "\\$s");
		}
		mParam = param;
		return true;
	}

	@Override
	public String getName(FileInfo info) {
		return String.format(mParam, mValue++, pName, 
				BatchRenameCore.getMain(info), info.type, BatchRenameCore.getType(info));
	}	
}
