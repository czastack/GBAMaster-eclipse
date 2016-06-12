package cza.file;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZOS extends ZipOutputStream {
	public ZOS(File to) throws Exception{
		super(new FileOutputStream(to));
	}

	public void put(String name, File file) throws Exception{
		put(name, new FileInputStream(file), file.lastModified());
	}

	public void put(String name, CharSequence text) throws Exception{
		putNextEntry(new ZipEntry(name));
		BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(this, "UTF-8"));
		fw.append(text).flush();
		fw.close();
	}

	public void put(String name, InputStream in, long time) throws Exception{
		ZipEntry entry = new ZipEntry(name);
		entry.setTime(time);
		putNextEntry(entry);
		FileUtils.write(new BufferedInputStream(in), this);
		closeEntry();
	}
}
