package cza.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class Pull {
	public XmlPullParser parser;
	
	public Pull(){}
	
	public Pull(InputStream is) throws Exception{
		start(is);
	}
	
	public Pull(File in) throws Exception{
		start(in);
	}

	/**
	 * 打开输入流
	 * @param is
	 * @throws Exception
	 */
	public void start(InputStream is) throws Exception {
		parser = XmlPullParserFactory.newInstance().newPullParser();
		parser.setInput(is, "UTF-8");
	}

	/**
	 * 打开文件流
	 * @param in
	 * @throws Exception
	 */
	public void start(File in) throws Exception {
		start(new FileInputStream(in));
	}

	/**
	 * 读取字符串属性
	 * @param name
	 * @return
	 */
	public String getValue(String name){
		return parser.getAttributeValue("", name);
	}

	/**
	 * 读取布尔类型
	 * @param name
	 * @return
	 */
	public boolean getBoolean(String name){
		return "true".equals(getValue(name));
	}

	/**
	 * 读取整形
	 * 默认值为0
	 * @param name
	 * @return
	 */
	public int getInt(String name){
		return getInt(name, 0);
	}

	/**
	 * 读取整形
	 * 支持16进制
	 * @param name 属性名
	 * @param def 默认值
	 * @return
	 */
	public int getInt(String name, int def){
		String value = getValue(name);
		if (value == null || value.isEmpty())
			return def;
		char ch = 0;
		if (value.length() > 2 && (value.charAt(0) == '0' && (ch = value.charAt(1)) == 'x' || ch == 'X'))
			return Integer.parseInt(value.substring(2), 16);
		else 
			return Integer.parseInt(value);
	}
	
	/**
	 * 读取标签值
	 * @return
	 * @throws Exception
	 */
	public String getText() throws Exception{
		String text = parser.nextText();
		parser.next();
		return text;
	}
}
