package cza.file;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GBASearcher extends FileSearcher{
	public GBASearcher(){
		super(new FileTypeFilter("gba", "zip"));
	}

	@Override
	protected void action(File file){
		if (isGBA(file))
			list.add(file);
	}

	public static boolean zipHasGBA(File f) throws IOException{
		ZipFile zip = new ZipFile(f);
		boolean has = false;
		Enumeration<?extends ZipEntry> e = zip.entries();
		while (e.hasMoreElements()) {
			ZipEntry entry = e.nextElement();
			if (!entry.isDirectory() && entry.getName().endsWith(".gba")){
				has = true;
				break;
			}
		}
		zip.close();
		return has;
	}
	
	public static boolean isGBA(File file){
		String type = FileUtils.getType(file);
		if ("gba".equals(type))
			return true;
		else if ("zip".equals(type))
			try {
				if (zipHasGBA(file)) 
					return true;
			} catch (IOException e) {}
		return false;
	}
}
