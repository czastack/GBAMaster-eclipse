package cza.hack;

import android.text.InputFilter;
import android.text.Spanned;

public class HexFilter implements InputFilter {

	public int mMaxLength;

	public HexFilter(int maxLength){
		mMaxLength = maxLength;
	}

	@Override
	public CharSequence filter(CharSequence text, int start, int end, Spanned dst, int dstart, int dend) {
		int textLength = text.length();
		if (textLength == 0)
			return text;
		int bufferLength;
		if (mMaxLength > -1) {
			bufferLength = mMaxLength - dst.length() + dend - dstart;
			if (bufferLength < 1)
				return "";
		} else {
			bufferLength = textLength;
		}
		char[] buffer = new char[bufferLength];
		int p;
		int i = 0;
		for (p = 0; p < textLength; p++){
			char ch = text.charAt(p);
			if (Coder.isHex(ch))
				buffer[i++] = ch;
			if (i >= bufferLength)
				break;
		}
		return String.valueOf(buffer, 0, i);
	}
}

