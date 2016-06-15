package cza.gbamaster.cot;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.util.SparseArray;
import cza.hack.CheatCoder;
import cza.util.Pull;

public class CotResource {
	public final static String
	RES_LAYOUT = "layout.xml",
	RES_STRING = "string.xml",
	RES_LOGO = "logo.png";
	
	private String mPath;
	private ZipFile mCot;
	private Map<String, String> mStrings;
	private Map<String, SelectEntry> mSelectEntries;
	
	public CotResource(String path) throws Exception{
		mPath = path;
		mStrings = new HashMap<String, String>();
		mSelectEntries = new HashMap<String, SelectEntry>();
		loadStrings();
	}

	/**
	 * 加载字符串
	 * @throws Exception 
	 */
	private void loadStrings() {
		try {
			Pull pull = getParser(RES_STRING);
			if (pull == null)
				return;
			int type;
			while ((type = pull.parser.next()) != 1) {
				if (type != 2) continue;
				if ("string".equals(pull.parser.getName())) {  
					mStrings.put(pull.getValue("name"), pull.getText());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取输入流
	 * @param entryName
	 * @return
	 * @throws Exception
	 */
	public InputStream open(String entryName) throws Exception{
		if (mCot == null)
			mCot = new ZipFile(mPath);
		ZipEntry entry = mCot.getEntry(entryName);
		if (entry != null)
			return mCot.getInputStream(entry);
		return null;
	}
	
	/**
	 * 打开Xml解析器
	 * @param entryName
	 * @return
	 * @throws Exception
	 */
	public Pull getParser(String entryName) throws Exception{
		InputStream is = open(entryName);
		return is == null ? null : new Pull(is);
	}
	
	
	/**
	 * 打开主布局解析器
	 * @param entryName
	 * @return
	 * @throws Exception
	 */
	public Pull getLayoutParser() throws Exception{
		Pull pull = new CotXmlParser(this);
		pull.start(open(RES_LAYOUT));
		return pull;
	}
	
	/**
	 * 获取字符串
	 * @param tag
	 * @return
	 */
	public String getString(String tag){
		if (tag != null && mStrings != null && tag.startsWith("@string/"))
			return mStrings.get(tag.substring(8));
		return tag;
	}
	
	/**
	 * 获取数据项
	 * @param entryName
	 * @return
	 */
	public SelectEntry getSelectData(String entryName){
		if (mSelectEntries.containsKey(entryName))
			return mSelectEntries.get(entryName);
		try {
			SelectEntry entry = new SelectEntry();
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(open(entryName), "UTF-8"));
			String str;
			entry.datas = new SelectData[Integer.parseInt(reader.readLine()) + 1];
			int index = 1;
			SparseArray<String> tags = new SparseArray<String>();
			while ((str = reader.readLine()) != null) {
				if (str.isEmpty())
					continue;
				if (str.charAt(0) == '■') {
					tags.append(index, str.substring(1));
					continue;
				}
				SelectData data = new SelectData();
				String[] arr = str.split("\t\t");
				data.value = CheatCoder.hexToDec(arr[0]);
				data.name = arr[1];
				if (arr.length > 2) 
					data.hint = arr[2];
				entry.datas[index++] = data;
			}
			// 快捷定位
			int len = tags.size();
			if (len > 0) {
				entry.tagIndexs = new int[len];
				entry.tagTexts = new String[len];
				for (int i = 0; i < len; i++) {
					entry.tagIndexs[i] = tags.keyAt(i);
					entry.tagTexts[i] = tags.valueAt(i);
				}
			}
			reader.close();
			close();
			mSelectEntries.put(entryName, entry);
			return entry;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 释放字符串
	 */
	public void freeStrings(){
		mStrings = null;
	}
	
	/**
	 * 释放资源
	 * @throws Exception
	 */
	public void close() throws Exception{
		if(mCot != null){
			mCot.close();
			mCot = null;
		}
	}
}
