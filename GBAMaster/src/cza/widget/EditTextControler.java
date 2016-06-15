package cza.widget;

import android.text.Editable;
import android.widget.EditText;

public class EditTextControler {
	private EditText mEditText;
	
	public EditTextControler(EditText editText){
		mEditText = editText;
	}
	
	/**
	 * 获取编辑对象
	 * @return
	 */
	public Editable getContent(){
		return mEditText.getText();
	}

	/**
	 * 输入文字
	 * @param text
	 */
	public void input(CharSequence text){
		int start = getPosition();
		int end = mEditText.getSelectionEnd();
		if (text == null){
			//退格
			if (start == end){
				if (start == 0)
					return;
				else 
					start--;
			}
			getContent().delete(start, end);
		} else {
			getContent().replace(start, end, text);
		}
	}
	
	/**
	 * 获取光标位置
	 * @return
	 */
	public int getPosition(){
		return mEditText.getSelectionStart();
	}
	
	/**
	 * 获取文本长度
	 * @return
	 */
	public int length(){
		return getContent().length();
	}

	/**
	 * 获取指定光标所在行起始位置
	 * @param pos
	 * @return
	 */
	public int getLineStart(int pos){
		Editable edit = getContent();
		int start = pos;
		while (start > 0 && edit.charAt(start - 1) != '\n')
			start--;
		return start;
	}

	/**
	 * 获取指定光标所在行结束位置
	 * @param pos
	 * @return
	 */
	public int getLineEnd(int pos){
		Editable edit = getContent();
		int length = edit.length();
		int end = pos;
		while (end < length && edit.charAt(end) != '\n')
			end++;
		return end;
	}

	/**
	 * 获取当前行起始位置
	 * @return
	 */
	public int getCurrentLineStart(){
		return getLineStart(getPosition());
	}

	/**
	 * 获取最后一行的位置
	 * @return
	 */
	public int getLastLineStart(){
		return getLineStart(getContent().length());
	}

	/**
	 * 获取指定行文字
	 * @param start 光标位置
	 * @return
	 */
	public String getLine(int pos){
		Editable content = getContent();
		int start = getLineStart(pos);
		int end = getLineEnd(pos);
		return content.subSequence(start, end).toString();
	}
	
	/**
	 * 获取当前行文字
	 * @return
	 */
	public String getCurentLine(){
		return getLine(getPosition());
	}
}
