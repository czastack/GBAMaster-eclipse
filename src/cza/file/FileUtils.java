package cza.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FileUtils {

	public static String getMainName(File f){
		return getMainName(f.getName());
	}

	public static String getMainName(String name){
		int dot = name.lastIndexOf('.');
		int sep = name.lastIndexOf(File.separatorChar);
		if (dot < 0 && sep < 0)
			return name;
		else if (dot < 0)
			dot = name.length();
		return name.substring(sep + 1, dot);
	}

	public static String getType(File f){
		return getType(f.getName());
	}

	public static String getType(String name){
		int n = name.lastIndexOf(46);
		if (n < 0) return "";
		return name.substring(n + 1).toLowerCase(Locale.getDefault());
	}

	public static String withType(String name, String type){
		return getMainName(name) + "." + type;
	}
	
	public static File withType(File file, String type){
		return new File(file.getParentFile(), getMainName(file) + "." + type);
	}

	public static String getParent(String path){
		int end = path.lastIndexOf(File.separator);
		return path.substring(0, end);
	}

	public static String getTime(File file){
		return timer.format(file.lastModified());
	}
	
	public static String getMIMEType(File file) {
		return getMIMEType(getType(file));
	}

	public static String getMIMEType(String type) {
		for (String[] per: MIME_MapTable) {
			if (type.equals(per[0])){
				return per[1];
			}
		}
		return "*/*";
	}

	public static String size(File f) {	
		return size(f.length());
	}

	public static String size(long size) { // 转换文件大小
		if (size < 1024){
			return size + "B";
		}
		DecimalFormat df = new DecimalFormat("#.00");
		int[] base = new int[]{1024, 1048576, 1073741824};
		String [] units = new String[]{"KB", "MB", "GB"};
		for (int i = 2; i >= 0; i--){
			if (size >= base[i]){
				return df.format((double) size / base[i]) + units[i];
			}
		}
		return null;
	}

	public static void copy(File f, File t){
		FileInputStream fi;
		FileOutputStream fo;
		FileChannel in ,
			out;
		try {
			fi = new FileInputStream(f);
			fo = new FileOutputStream(t);
			in =fi.getChannel();
			out = fo.getChannel();
			in.transferTo(0, in.size(), out);
			fi.close();
			in.close();
			fo.close();
			out.close();
			t.setLastModified(f.lastModified());
		 } catch(IOException e) {}
	}

	public static void delete(File file){
		if (file.isDirectory()){
			for (File f : file.listFiles()) {
				delete(f);
			}
		}
		file.delete();
	}

	public static void write(InputStream is, File out) throws Exception {
		OutputStream os = new BufferedOutputStream(new FileOutputStream(out));
		write(is, os);
		os.flush();
		os.close();
	}

	public static void write(InputStream is, OutputStream os) throws Exception {
		byte[] data = new byte[1024 << 2];
		int count;
		while ((count = is.read(data)) != -1) {
			os.write(data, 0, count);
		}
		is.close();
	}
	
	public static boolean writeStringToFile(String str, File file) {
		OutputStream out;
		try {
			out = new FileOutputStream(file);
		} catch (Exception e) {
			return false;
		}
		OutputStreamWriter outw = new OutputStreamWriter(out);
		try {
			outw.write(str);
			outw.close();
			out.close();
		} catch (Exception e){
			return false;
		}
		return true;
	}

	public static SimpleDateFormat timer = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

	private final static String[][] MIME_MapTable = {
		//{后缀名， MIME类型}
		{"apk", "application/vnd.android.package-archive"},
		{"avi", "video/x-msvideo"},
		{"bmp", "image/bmp"},
		{"cht", "text/cht"},
		{"cot", "application/cot"},
		{"doc", "application/msword"},
		{"gba", "application/x-gba-rom"},
		{"gif", "image/gif"},
		{"htm", "text/html"},
		{"html", "text/html"},
		{"jpeg", "image/jpeg"},
		{"jpg", "image/jpeg"},
		{"mp3", "audio/x-mpeg"},
		{"mp4", "video/mp4"},
		{"ogg", "audio/ogg"},
		{"pdf", "application/pdf"},
		{"png", "image/png"},
		{"ppt", "application/vnd.ms-powerpoint"},
		{"rar", "application/rar"},
		{"rmvb", "video/vnd.rn-realvideo"},
		{"tar", "application/x-tar"},
		{"txt", "text/plain"},
		{"wav", "audio/x-wav"},
		{"wma", "audio/x-ms-wma"},
		{"wmv", "audio/x-ms-wmv"},
		{"xls", "application/vnd.ms-excel"},
		{"xml", "text/plain"},
		{"zip", "application/zip"},
		{"",  "*/*"}
	};

	public static FileFilter VISIBLE = new FileFilter() {
		public boolean accept(File f) {
			return !f.isHidden();
		}
	};
}
