package cza.app;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;
import cza.file.FileInfo;
import cza.file.FileInfos;
import cza.file.FileUtils;

public class DeleteConfirm extends ListDialog {
	private FileInfos mInfos;
	private LinkedList<String> list;
	
	public DeleteConfirm(Context c){
		super(c);
		setTitle("确认删除");
		list = new LinkedList<String>();
		setList(list);
		setItems(null, null);
		setConfirm();
	}
	
	public void setInfos(FileInfos infos){
		mInfos = infos;
		list.clear();
		Iterator<FileInfo> itr = mInfos.iterator();
		while (itr.hasNext()){
			FileInfo info = itr.next();
			File file = new File(mInfos.path, info.name);
			StringBuilder sb = new StringBuilder(info.name);
			if (info.isDir){
				if (file.canRead()){
					sb.append("\t" + file.list().length + "项目");
				}
			} else {
				sb.append("\t" + info.size);
			}
			if (!file.canWrite()){
				sb.append("\t拒绝");
				info.checked = false;
				itr.remove();
			}
			list.add(sb.toString());
		}
	}

	@Override
	public boolean triggerClick(int which) {
		if(which == BUTTON_POSITIVE) {
			for (int i = 0, len = mInfos.size(); i < len; i++) {
				File file = mInfos.getFileAt(i);
				FileUtils.delete(file);
			}
		}
		return super.triggerClick(which);
	}
}
