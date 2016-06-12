package cza.file;

import java.io.File;
import java.io.FilenameFilter;

public class FileNameFixFilter implements FilenameFilter {
	public String prefix, suffix;
	
	public FileNameFixFilter(String prefix, String suffix){
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return (prefix == null || name.startsWith(prefix)) && (suffix == null || name.endsWith(suffix));
	}
}
