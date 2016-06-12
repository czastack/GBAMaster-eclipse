package cza.gbamaster;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import cza.hack.CBCoder;
import cza.util.ArrayUtils;
import cza.util.Pull;
import cza.util.XmlWriter;

public class ToolsActivity extends TwoEditActivity implements RadioGroup.OnCheckedChangeListener {
	private ViewGroup extBar;
	private RadioGroup typeSwitch;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tools);
		initView();
		typeSwitch = (RadioGroup) findViewById(R.id.typeSwitch);
		typeSwitch.setOnCheckedChangeListener(this);
		extBar = (ViewGroup) findViewById(R.id.extBar);
		shortcutIcon = R.drawable.tool;
		typeSwitch.check(R.id.chtConverter);
    }

	@Override
	public void onCheckedChanged(RadioGroup group, int id) {
		boolean showToUp = false;
		extBar.removeAllViews();
		if (id == R.id.chtConverter)
			extBar.addView(initChtExt());
		else if (id == R.id.regex){
			showToUp = true;
			extBar.addView(initRegexExt());
		}
		toUp.setVisibility(showToUp ? 0 : 8);
	}

	@Override
	protected void go() {
		String text = iet.getText().toString();
		if (text.isEmpty()) {
			toast(R.string.error);
			return;
		}
		String output;
		switch(typeSwitch.getCheckedRadioButtonId()){
		case R.id.chtConverter:
			output = cht(text);
			break;
		case R.id.sort:
			output = sort(text);
			break;
		case R.id.Xml:
			output = xml(text);
			break;
		default:
			output = regex(text);
			break;
		}
		oet.setText(output);
	}

	/*
	 * 排序
	 */
	private String sort(String in) {
		String[] lines = in.split("\n");
		Arrays.sort(lines, MyApplication.COMPARATOR);
		return ArrayUtils.join(lines, "\n");
	}

	/*
	 * 作弊码
	 */
	private View chtItem;
	private EditText cht_input_addr_offset, cht_input_value_offset;
	
	private View initChtExt() {
		if (chtItem == null){
			chtItem = inflateView(R.layout.input_cheat_ext);
			cht_input_addr_offset = (EditText) chtItem.findViewById(R.id.input_addrOffset);
			cht_input_value_offset = (EditText) chtItem.findViewById(R.id.input_valueOffset);
		}
		return chtItem;
	}

	private String cht(String in) {
		CBCoder coder = CBCoder.getInstance();
		coder.addrOffset = (int)CBCoder.hexToDec(cht_input_addr_offset.getText());
		coder.valueOffset = (int)CBCoder.hexToDec(cht_input_value_offset.getText());
		String output = coder.formatAll(in).toString();
		coder.reset();
		return output;
	}

	
	/*
	 * xml格式化
	 */
	private String xml(String in) {
		Pull pull = new Pull();
		XmlWriter writer = new XmlWriter();
		try {
			pull.start(new ByteArrayInputStream(in.getBytes()));
			writer.start();
			int type;
			while((type = pull.parser.next()) != 1) {
				switch (type){
					case 2:
						writer.startTag(pull.parser.getName());
						for (int i = 0; i < pull.parser.getAttributeCount(); i++){
							writer.attribute(pull.parser.getAttributeName(i), pull.parser.getAttributeValue(i));
						}
						break;
					case 3:
						writer.endTag(pull.parser.getName());
						break;
					case 4:
						String text = pull.parser.getText().trim();
						if (!text.isEmpty()){
							writer.text(text);
						}
						break;
				}
			}
		} catch (Exception e) {}
		return writer.toString();
	}


	/*
	 * 查找替换
	 */
	private View regexItem;
	private CheckBox option_splitGroup;
	private EditText regexFind, regexReplace;
	private View initRegexExt() {
		if (regexItem == null){
			regexItem = inflateView(R.layout.input_regex);
			option_splitGroup = (CheckBox) regexItem.findViewById(R.id.option_splitGroup);
			regexFind = (EditText) regexItem.findViewById(R.id.input_find);
			regexReplace = (EditText) regexItem.findViewById(R.id.input_replacement);
		}
		return regexItem;
	}
		
	private String regex(String in) {
		boolean splitGroup = option_splitGroup.isChecked();
		String find = regexFind.getText().toString();
		String replace = regexReplace.getText().toString();
		String[] finds = null;
		String[] replaces = null;
		if (splitGroup){
			finds = find.split("\n");
			replaces = replace.split("\n");
		} else {
			finds = new String[]{find};
			replaces = new String[]{replace};
		}
		try {
			for (int i = 0; i < finds.length; i++){
				Pattern regex = Pattern.compile(finds[i]);
				Matcher m = regex.matcher(in);
				String rep;
				if (i < replaces.length){
					rep = escape(replaces[i]);
				} else {
					rep = "";
				}
				in = m.replaceAll(rep);
			}
			return in;
		} catch (Exception e) {
			return getString(R.string.regexError);
		}
	}
	
	/**
	 * 转义
	 */
	public static String escape(String text){
		StringBuilder sb = new StringBuilder();
		int length = text.length();
		boolean pending = false;
		for (int i = 0; i < length; i++){
			char ch = text.charAt(i);
			if (pending) {
				ch = getEsapeChar(ch);
				pending = false;
			} else if ('\\' == ch) {
				ch = '\0';
				pending = true;
			}
			if (ch != '\0')
				sb.append(ch);
		}
		return sb.toString();
	}
	
	public static char getEsapeChar(char ch){
		switch (ch){
			case 'b':
				return '\b';
			case 't':
				return '\t';
			case 'n':
				return '\n';
			case 'r':
				return '\r';
			case 'f':
				return '\f';
		}
		return ch;
	}
}
