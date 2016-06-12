package cza.gbamaster.batchrename;

import cza.file.FileInfo;
import android.view.LayoutInflater;
import android.view.View;

public interface Namer {
	/**
	 * 不需要输入新类型
	 * @return
	 */
	public boolean withoutType();
	public View onCreateView(LayoutInflater inflater);
	public boolean onStart();
	/**
	 * 生成新文件名
	 */
	public String getName(FileInfo info);
	public void onRecursionReset();
}
