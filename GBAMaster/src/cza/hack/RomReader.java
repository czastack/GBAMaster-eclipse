package cza.hack;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cza.file.FileUtils;

public class RomReader {
	public String mPath;
	public BufferedInputStream mStream;
	private ZipFile mZip;
	
	public RomReader(String path) throws Exception{
		load(path);
	}
	
	public void load(String path) throws Exception{
		mPath = path;
		InputStream is = null;
		if ("gba".equals(FileUtils.getType(path))) {
			is = new FileInputStream(path);
		} else {
			mZip = new ZipFile(path);
			Enumeration<?extends ZipEntry> entrys = mZip.entries();
			while (entrys.hasMoreElements()) {
				ZipEntry entry = entrys.nextElement();
				if (!entry.isDirectory() && entry.getName().endsWith(".gba")){
					is = mZip.getInputStream(entry);
					break;
				}
			}
		}
		mStream = new BufferedInputStream(is);
	}
	
	public void read(long start, byte[] buffer) throws Exception{
		mStream.skip(start);
		mStream.read(buffer);
	}
	
	public void close() throws Exception{
		if (mStream != null) {
			mStream.close();
			mStream = null;
		}
		if (mZip != null) {
			mZip.close();
			mZip = null;
		}
	}
}
