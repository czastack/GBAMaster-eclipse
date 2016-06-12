package cza.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class FileTypeFilter extends ArrayList<String> implements FileFilter, FilenameFilter {

	private static final long serialVersionUID = 5561591766943470031L;
	boolean showHidden = false;
	boolean showFolder = true;

    public FileTypeFilter() {}

	public FileTypeFilter(String...types) {
        addTypes(types);
    }

    @Override
    public boolean accept(File f) {
        if(f.isHidden() && !showHidden)
			return false;
		if(f.isDirectory())
			return showFolder;
        return accept(null, f.getName());
    }

	@Override
	public boolean accept(File dir, String name) {
		for (String type: this) {
            if (name.endsWith("." + type))
                return true;
        }
        return false;
	}

    /**
     * 添加指定类型的文件
     * @param type  将添加的文件类型，如"mp3"
     */
	void addTypes(String...types) {
        addAll(Arrays.asList(types));
    }
}
