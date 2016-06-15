package cza.gbamaster;

import java.util.Locale;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import cza.hack.Coder;
import cza.util.Calculator;
import cza.widget.EditTextControler;
import cza.widget.LP;

public class CalculatorActivity extends BaseActivity implements 
View.OnClickListener, 
RadioGroup.OnCheckedChangeListener,
InputFilter {
	private EditText inputbox;
	private RadioGroup baseSwitch;
	private ViewGroup buttonGroup;
	private EditTextControler mControler;
	private Calculator calculator;
	private int mBaseout;
	private final char[] BUTTON = {
			'A', 'B', 'C', '←', 
			'D', 'E', 'F', '+', 
			'7', '8', '9', '-', 
			'4', '5', '6', '×', 
			'1', '2', '3', '÷', 
			'(', '0', ')', '='};

	@Override
    public void onCreate(Bundle savedStates) {
        super.onCreate(savedStates);
		setContentView(R.layout.activity_calculator);
		inputbox = (EditText)findView(R.id.iet);
		inputbox.setTextIsSelectable(true);
		inputbox.setFilters(new InputFilter[]{this});
		mControler = new EditTextControler(inputbox);
		baseSwitch = (RadioGroup)findView(R.id.baseSwitch);
		baseSwitch.setOnCheckedChangeListener(this);
		buttonGroup = (ViewGroup)findView(R.id.buttonGroup);
		initButton();
		baseSwitch.check(R.id.option_hex);
		calculator = new Calculator();
	}

	/**
	 * 初始化控制按钮
	 */
	private void initButton(){
		int columnCount = 4;
		int rowCount = BUTTON.length / columnCount;
		int i = 0;
		for (int row = 0; row < rowCount; row++){
			ViewGroup group = (ViewGroup)inflateView(R.layout.widget_bar);
			for (int col = 0; col < columnCount; col++){
				Button btn = (Button)inflateView(R.layout.activity_calculator_button);
				char id = BUTTON[i++];
				btn.setTag(id);
				btn.setText(String.valueOf(id));
				btn.setOnClickListener(this);
				group.addView(btn, LP.HLine);
			}
			buttonGroup.addView(group);
		}
	}

	@Override
	public void onClick(View v) {
		char ch = (Character)v.getTag();
		if ('←' == ch)
			mControler.input(null);
		else if ('=' == ch)
			workout();
		else 
			mControler.input(String.valueOf(ch));
	}

	/**
	 * 切换radio更换进制
	 */
	@Override
	public void onCheckedChanged(RadioGroup v, int id) {
		boolean isHex = R.id.option_hex == id;
		if (isHex)
			changeBase(10, 16);
		else 
			changeBase(16, 10);
		for (char i = 'A'; i <= 'F'; i++)
			buttonGroup.findViewWithTag(i).setEnabled(isHex);
	}


	/**
	 * 读取指定光标位置所在行的算式
	 * @param pos
	 * @return
	 * @throws Exception
	 */
	private String getExpr(int pos) throws Exception{
		String text = mControler.getLine(pos);
		int length = text.length();
		if (length == 0)
			throw new Exception("空算式");
		pos = 0;
		//去掉等号
		while (pos < length && text.charAt(pos) == '=')
			pos++;
		if (pos == length)
			throw new Exception("空算式");
		return text.substring(pos);
	}

	/**
	 * 切换进制
	 * @param basein
	 * @param baseout
	 */
	private void changeBase(int basein, int baseout){
		Editable content = mControler.getContent();
		mBaseout = baseout;
		String text = Coder.toBaseString(content, basein, baseout);
		content.replace(0, content.length(), text);
	}

	/**
	 * 计算结果
	 */
	private void workout(){
		try {
			int pos = mControler.getPosition();
			Editable content = mControler.getContent();
			String text = getExpr(pos);
			String result = workout(text);
			// 修改工作行
			int replaceStart = mControler.getLineEnd(pos) + 1;
			int replaceEnd = content.length();
			if(replaceStart <= replaceEnd){
				// 当前表达式下方有内容
				replaceEnd = mControler.getLineEnd(replaceStart);
				content.replace(replaceStart, replaceEnd, '=' + result);
			} else {
				// 当前表达式下方没有内容
				content
					.append('\n')
					.append('=')
					.append(result)
					.append('\n');
				inputbox.setSelection(content.length());
			}
		} catch (Exception e) {}
	}

	/**
	 * 计算结果
	 * @param text
	 * @return
	 */
	private String workout(String text) {
		text = text.replace('×', '*').replace('÷', '/');
		text = Coder.toBaseString(text, mBaseout, 10);
		String result = Long.toString(calculator.compute(text), mBaseout).toUpperCase(Locale.getDefault());
		return result;
	}

	/**
	 * 过滤字符
	 */
	@Override
	public CharSequence filter(CharSequence text, int start, int end, Spanned dst, int dstart, int dend) {
		int textLength = text.length();
		if (textLength == 0)
			return text;
		int bufferLength;
		bufferLength = textLength;
		char[] buffer = new char[bufferLength];
		int p;
		int i = 0;
		boolean isHex = mBaseout == 16;
		for (p = 0; p < textLength; p++){
			char ch = text.charAt(p);
			boolean available = 
				ch == '+' || ch == '-' || ch == '*' || ch == '/' || 
				ch == '×' || ch == '÷' || ch == '=' || ch == '\n' || 
				'0' <= ch && ch <= '9' ||
				(isHex && ('A' <= ch && ch <= 'F' || 'a' <= ch && ch <= 'f'));
			if (available)
				buffer[i++] = ch;
			if (i >= bufferLength)
				break;
		}
		return String.valueOf(buffer, 0, i);
	}
}

