package cza.app;

import android.content.Context;

import java.io.File;

import cza.file.FileUtils;

public class FileDeleteConfirm extends TextDialog {
	private File mFile;
	private Callback mCallback;

	public FileDeleteConfirm(Context context) {
		super(context, 0);
		setTitle("确认删除");
		setConfirm();
	}
	
	public void setFile(File file){
		mFile = file;
	}
	
	public void setHint(){
		setHint(mFile.getName(), FileUtils.size(mFile));
	}
	
	public void setHint(String name, String size){
		setMessage(name + "\t" + size);
	}
	
	public void setCallback(Callback callback){
		mCallback = callback;
	}

	@Override
	public boolean triggerClick(int which) {
		if(which == BUTTON_POSITIVE) {
			if (mFile.delete()){
				if (mCallback != null)
					mCallback.onDelete(this, mFile);
			}
		}
		return super.triggerClick(which);
	}
	
	public interface Callback {
		public void onDelete(FileDeleteConfirm dialog, File file);
	}
}
