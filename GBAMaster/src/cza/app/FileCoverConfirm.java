package cza.app;

import android.content.Context;

import java.io.File;

import cza.file.FileUtils;

public class FileCoverConfirm extends TextDialog {
	private File from, to;
	public FileCoverConfirm(Context context) {
		super(context, 0);
		setTitle("确认覆盖");
		setConfirm();
		setMessage("此操作会覆盖原有文件，确定继续？");
	}
	
	public Dialog setFile(File f, File t){
		from = f;
		to = t;
		return this;
	}

	@Override
	public boolean triggerClick(int which) {
		if(which == BUTTON_POSITIVE) {
			FileUtils.copy(from, to);
			return true;
		}
		return super.triggerClick(which);
	}
}
