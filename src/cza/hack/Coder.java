package cza.hack;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cza.util.Calculator;

public class Coder {
	private static StringBuilder mBuilder = new StringBuilder();
	public static final Calculator calculator = Calculator.getInstance();
	public static final int
	FLAG_8BIT  = 0x000000FF,
	FLAG_16BIT = 0x0000FFFF;
	public static final long
	FLAG_32BIT = 0xFFFFFFFFL;

	public static final String 
	GS = "(?i)\\b[0-9a-f]{8} ?[0-9a-f]{8}\\b", //Gameshark
	CB = "(?i)[0-9a-f]{8} [0-9a-f]{4}",
	EC = "(?i)[0-9a-f]{3,8},[,0-9a-f]+",
	RAW = "(?i)0[23][0-9a-f]{6}[ :][0-9a-f]{1,8}";
	public static Pattern 
	CODE = Pattern.compile(GS + "|[0-9a-f]{3,8}[ :,][,0-9a-f]+"),
	CBGS = Pattern.compile(CB + '|' + GS);

	/**
	 * 验证16进制字符
	 */
	public static boolean isHex(char ch) {
		return '0' <= ch && ch <= '9'
			|| 'A' <= ch && ch <= 'Z'
			|| 'a' <= ch && ch <= 'z';
	}

	/**
	 * 十六进制　转　十进制
	 */
	public static long hexToDec(CharSequence text) {
		if (text == null || text.length() == 0)
			return 0;
		long dec = 0;
		boolean negative = false;
		int i = 0;
		int length = text.length();
		char hex;
		if (text.charAt(i) == '-'){
			i++;
			negative = true;
		}
		//忽略前面的0
		while (i < length && text.charAt(i) == '0')
			i++;
		while (i < length) {
			hex = text.charAt(i++);
			if (hex >= '0' && hex <= '9')
				dec += hex & 15;
			else if (hex >= 'A' && hex <= 'F')
				dec += hex - 55;
			else if (hex >= 'a' && hex <= 'f')
				dec += hex - 87;
			else 
				continue;
			dec <<= 4;
		}
		dec >>>= 4;
		if (negative)
			dec = -dec;
		return dec;
	}

	/**
	 * 转置16位
	 * @param value
	 * @return
	 */
	public static int reverseHalfWord(int value){
		return ((value >>> 8) & FLAG_8BIT) | ((value & FLAG_8BIT) << 8);
	}

	/**
	 * 转置32位
	 * @param value
	 * @return
	 */
	public static long reverseWord(long value){
		return reverseHalfWord((int)(value >>> 16)) | (reverseHalfWord((int)value) << 16);
	}

	/**
	 * 转置指定长度
	 * @param value
	 * @param size
	 * @return
	 */
	public static long reverse(long value, int size){
		if (size == 0)
			return 0;
		switch (size){
			default:
			case 1:
				return value;
			case 2:
				return reverseHalfWord((int)value);
			case 4:
				return reverseWord(value);
		}
	}

	/**
	 * 从指定位置开始解析指定长度16进制字符串
	 * @param text 要解析的字符串
	 * @param offset 偏移
	 * @param size 字节数
	 * @return
	 */
	public static int readBytes(CharSequence text, final int offset, final int size){
		int end;
		if (text == null || (end = text.length()) == 0)
			return 0;
		int step = size << 1;
		if (size > 0)
			end = Math.min(end, offset + step);
		if (offset <= -step || offset >= end)
			return -1;
		int dec = 0;
		int i = offset < 0 ? 0 : offset;
		char hex;
		while (i < end) {
			hex = text.charAt(i++);
			if (hex >= '0' && hex <= '9')
				dec += hex & 15;
			else if (hex >= 'A' && hex <= 'F')
				dec += hex - 55;
			else if (hex >= 'a' && hex <= 'f')
				dec += hex - 87;
			else 
				continue;
			dec <<= 4;
		}
		dec >>>= 4;
		return dec;
	}
	
	/**
	 * 从低位解析指定长度的16进制
	 * @param text
	 * @param size 字节数
	 * @return
	 */
	public static int readLowBytes(CharSequence text, final int size){
		int start = text.length() - (size << 1);
		if (start < 0)
			start = 0;
		return readBytes(text, start, size);
	}

	/**
	 * 10进制转指定长度16进制
	 * @param num
	 * @param size 字节数
	 * @return
	 */
	public static String toHexString(long num, int size){
		return ao(toHEX(num), size << 1);
	}

	/**
	 * 10进制转8位16进制
	 * @param num
	 * @return
	 */
	public static String toByteString(int num){
		return toHexString(num, 1);
	}

	/**
	 * 10进制转16位16进制
	 * @param num
	 * @return
	 */
	public static String toHalfWordString(int num){
		return toHexString(num, 2);
	}

	/**
	 * 10进制转16位16进制
	 * @param num
	 * @return
	 */
	public static String toHalfWordString(long num){
		return toHalfWordString((int)(num & FLAG_16BIT));
	}

	/**
	 * 10进制转32位16进制
	 * @param num
	 * @return
	 */
	public static String toWordString(long num){
		return toHexString(num, 4);
	}

	/**
	 * 字符串添加前缀0以达到指定长度
	 * @param in  字符串
	 * @param len 需要达到的长度
	 * @return 处理后的字符串
	 */
	public static String ao(String src, int len){
		mBuilder.replace(0, mBuilder.length(), src);
		ao(mBuilder, 0, len);
		return mBuilder.toString();
	}
	
	/**
	 * 字符串添加前缀0以达到指定长度
	 * @param sb 缓冲区
	 * @param start 目标开始
	 * @param len 目标长度
	 */
	public static void ao(StringBuilder sb, int start, int len){
		int mLen = sb.length() - start;
		if (mLen < len) {
			char[] zero = new char[len - mLen];
			Arrays.fill(zero, '0');
			sb.insert(start, zero);
		}
	}
	
	/**
	 * 转大写
	 * @param text
	 * @return
	 */
	public static String upper(String text){
		return text.toUpperCase(Locale.getDefault());
	}
	
	/**
	 * 16进制安全转10进制
	 * @param text
	 * @param def 默认值
	 * @return
	 */
	public static int parseHex(CharSequence text, int def){
		String str = text.toString();
		if (text == null || str.isEmpty())
			return def;
		return fromHex(str);
	}

	/**
	 * 16进制转10进制
	 * @param s
	 * @return
	 */
	public static int fromHex(String s){
		return Integer.valueOf(s, 16);
	}

	/**
	 * 长16进制转10进制
	 * @param s
	 * @return
	 */
	public static long fromLongHex(String s){
		return Long.valueOf(s, 16);
	}

	/**
	 * 10进制小写16进制
	 * @param n
	 * @return
	 */
	public static String toHex(int n){
		return Integer.toHexString(n);
	}

	/**
	 * 长10进制小写16进制
	 * @param n
	 * @return
	 */
	public static String toHex(long n){
		return Long.toHexString(n);
	}

	/**
	 * 10进制大写16进制
	 * @param n
	 * @return
	 */
	public static String toHEX(int n){
		return upper(toHex(n));
	}

	/**
	 * 长10进制转大写16进制
	 * @param n
	 * @return
	 */
	public static String toHEX(long n){
		return upper(toHex(n));
	}
	
	/**
	 * 替换进制
	 */
	public static String toBaseString(CharSequence text, int basein, int baseout){
		Pattern pattern = Pattern.compile("[0-9a-fA-F]+");
		Matcher matcher = pattern.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			long dec = Long.parseLong(matcher.group(), basein);
			String out = Long.toString(dec, baseout);
			matcher.appendReplacement(sb, out);
		}
		matcher.appendTail(sb);
		String result = upper(sb.toString());
		return result;
	}
}
