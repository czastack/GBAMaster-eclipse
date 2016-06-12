package cza.file;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class FileSearcher {
	public static final int DEFAULT_LEVEL = 3;
	private int tLv = DEFAULT_LEVEL;
	public ArrayList<File> list;
	public FileFilter mFilter;

	public FileSearcher(){}

	public FileSearcher(FileFilter filter){
		resetList();
		mFilter = filter;
	}

	public void setSearchLv(int lv){
		tLv = lv;
	}

	public void resetList(){
		list = new ArrayList<File>();
	}

	public void find(File folder){
		find(folder, 0);
	}

	private void find(File folder, int mLv) {
		if (!folder.canRead()) return;  
		File[] files = folder.listFiles(mFilter);  
		for (File file: files) {
			if (file.isDirectory() && mLv < tLv) {
				if (file.canRead()) {
					find(file, mLv + 1); //递归查找  
				}
			} else {
				action(file);
			}
		}
	}

	protected void action(File file){
		list.add(file);
	}
}

