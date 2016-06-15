package cza.file;

import java.io.File;
import java.io.Serializable;

import cza.app.App;
import cza.gbamaster.MyApplication;
import cza.util.Checkable;

public class FileInfo extends Checkable implements Serializable {
	private static final long serialVersionUID = 1L;
	public String name, size, type = "";
	public boolean isDir;
	public int icon;

	public FileInfo(){}

	public FileInfo(File file){
		name = file.getName();
		if (file.isDirectory()) {
			isDir = true;
			icon = MyApplication.ICON_DIR;
		} else {
			size = FileUtils.size(file);
			type = FileUtils.getType(name);
			icon = MyApplication.getIcon(type);
		}
	}

	boolean after(FileInfo f) {
		return App.COMPARATOR.compare(name, f.name) > 0;
	}
}
