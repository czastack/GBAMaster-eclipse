package cza.hack;

import java.io.File;
import java.util.ArrayList;

public class ChtInfos extends ArrayList<ChtInfo> {
	private static final long serialVersionUID = -1831659616254638302L;
	public static String DEFAULT = "默认";
	public String path;

	public String getChtPath(String key){
		return path + (DEFAULT.equals(key) ? "" : key + ".cht");
	}

	public String getChtPath(int position){
		String key = get(position).name;
		return path + (DEFAULT.equals(key) ? "" : key + ".cht");
	}

	public File getCht(String key){
		return new File(getChtPath(key));
	}
}
