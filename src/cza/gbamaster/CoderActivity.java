package cza.gbamaster;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import cza.hack.CheatCoder;
import cza.hack.Code;
import cza.hack.HexFilter;
import cza.util.ArrayUtils;
import cza.util.ViewUtils;
import cza.widget.StringArrayAdapter;

public class CoderActivity extends ClickActivity implements 
		View.OnClickListener,
		AdapterView.OnItemSelectedListener,
		RadioGroup.OnCheckedChangeListener {
	
	private Spinner mCodeTypeView;
	private Spinner mCodeFunctionView;
	private EditText mAddrView;
	private EditText mValueView;
	private EditText mOutputView;
	private RadioGroup mValueTypeView;
	private RadioGroup mModeTypeView;
	private EditText mDataSizeView;
	private EditText mValueIncView;
	private EditText mAddrIncView;
	private Editable mOutputContent;
	private StringArrayAdapter[] mAdapters;
	private byte mCodeType;
	private byte mCodeFunc;
	private int mMode;
	private static final InputFilter[] 
	FILTER_HEX_32BIT = new InputFilter[]{new HexFilter(8)},
	FILTER_HEX = new InputFilter[]{new HexFilter(-1)};
	private static final int
	MODE_ORDINARY = 0,
	MODE_LONGVALUE = 1,
	MODE_BATCH = 2,
	SLIDE_HIDE = 0,
	SLIDE_LENGTH = 1,
	SLIDE_ALL = 2,
	SLIDE_BATCH = 3;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coder);
		mCodeTypeView = (Spinner)findView(R.id.codeTypeView);
		mCodeFunctionView = (Spinner)findView(R.id.codeFunctionView);
		mAddrView = (EditText)findView(R.id.addressView);
		mValueView = (EditText)findView(R.id.valueView);
		mOutputView = (EditText)findView(R.id.oet);
		mValueTypeView = (RadioGroup)findView(R.id.valueTypeView);
		mModeTypeView = (RadioGroup)findView(R.id.modeView);
		initSlideBar();
		registerClick(R.id.btn_create, R.id.btn_decode);
		mOutputContent = mOutputView.getText();
		mAdapters = new StringArrayAdapter[3];
		mAdapters[0] = new StringArrayAdapter(this, R.array.type_cb, StringArrayAdapter.TYPE_SPINNER);
		mAdapters[1] = new StringArrayAdapter(this, R.array.type_v1, StringArrayAdapter.TYPE_SPINNER);
		mAdapters[2] = new StringArrayAdapter(this, R.array.type_v3, StringArrayAdapter.TYPE_SPINNER);
		mCodeTypeView.setOnItemSelectedListener(this);
		mCodeFunctionView.setOnItemSelectedListener(this);
		mModeTypeView.setOnCheckedChangeListener(this);
		initFilter();
	}
	
	private void initSlideBar(){
		View slideBar = findView(R.id.slideBar);
		mDataSizeView = (EditText)slideBar.findViewById(R.id.dataSizeView);
		mValueIncView = (EditText)slideBar.findViewById(R.id.ValueIncrementView);
		mAddrIncView = (EditText)slideBar.findViewById(R.id.AddrIncrementView);
	}

	private void initFilter(){
		InputFilter[] filters = FILTER_HEX_32BIT;
		mAddrView.setFilters(filters);
		mValueView.setFilters(filters);
		mDataSizeView.setFilters(filters);
		mValueIncView.setFilters(filters);
		mAddrIncView.setFilters(filters);
	}
	
	/**
	 * 更新参数栏的状态
	 */
	private void showSlideBar(){
		int type = getSlideType();
		ViewUtils.hide(mDataSizeView, type < SLIDE_LENGTH);
		ViewUtils.hide(mValueIncView, type < SLIDE_ALL);
		ViewUtils.hide(mAddrIncView, type < SLIDE_ALL);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_create:
				create();
				break;
			case R.id.btn_decode:
				decode();
				break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> view, View item, int position, long id) {
		if (view == mCodeTypeView) {
			mCodeType = (byte)position;
			mCodeFunctionView.setAdapter(mAdapters[position]);
		} else {
			mCodeFunc = getCodeFunction(position);
			showSlideBar();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> view) {}
	
	private byte getCodeFunction(int index){
		return CheatCoder.TYPE_LIST[mCodeType][index];
	}

	/**
	 * 切换模式
	 */
	@Override
	public void onCheckedChanged(RadioGroup v, int id) {
		Editable valueContent = mValueView.getText();
		boolean longValue = false;
		int mValueLength = valueContent.length();
		switch (id){
			case R.id.option_ordinary:
				mMode = MODE_ORDINARY;
				break;
			case R.id.option_longValue:
				mMode = MODE_LONGVALUE;
				longValue = true;
				break;
			case R.id.option_batch:
				mMode = MODE_BATCH;
				break;
		}
		showSlideBar();
		if (longValue){
			mValueView.setFilters(FILTER_HEX);
		} else {
			if (mValueLength > 8)
				valueContent.delete(8, mValueLength);
			mValueView.setFilters(FILTER_HEX_32BIT);
		}
	}
	
	/**
	 * 获取参数栏应有的状态
	 */
	private int getSlideType(){
		switch (mCodeFunc){
			case CheatCoder.GSA_8_BIT_FILL:
			case CheatCoder.GSA_16_BIT_FILL:
				return SLIDE_LENGTH;
			case CheatCoder.CBA_SLIDE_CODE:
			case CheatCoder.GSA_8_BIT_SLIDE:
			case CheatCoder.GSA_16_BIT_SLIDE:
			case CheatCoder.GSA_32_BIT_SLIDE:
				return SLIDE_ALL;
		}
		if (mMode == MODE_BATCH)
			return SLIDE_BATCH;
		return SLIDE_HIDE;
	}
	
	/**
	 * 生成作弊码
	 */
	private void create(){
		Code code = new Code();
		code.type = mCodeType;
		code.func = mCodeFunc;
		code.addr = CheatCoder.hexToDec(mAddrView.getText().toString());
		byte type = code.func;
		boolean is_l_h = mValueTypeView.getCheckedRadioButtonId() == R.id.option_value_l_h;
		//获取额外参数
		int slideType = getSlideType();
		if (slideType > SLIDE_HIDE) {
			code.dataSize = CheatCoder.parseHex(mDataSizeView.getText(), 1);
			if (slideType > SLIDE_LENGTH){
				code.valueInc = CheatCoder.parseHex(mValueIncView.getText(), 0);
				code.addrInc = CheatCoder.parseHex(mAddrIncView.getText(), 0);
			}
		}
		mOutputContent.clear();
		if (mMode == MODE_LONGVALUE) {
			//长数值
			boolean ableLongValue = CheatCoder.ableLongValue(type);
			final CharSequence valueText = mValueView.getText();
			int valueSize = CheatCoder.getTypeSize(type);
			int valueLength = valueSize << 1;
			int offset, step;
			if (is_l_h){
				offset = 0;
				step = valueLength;
			} else {
				offset = valueText.length() - valueLength;
				step = -valueLength;
			}
			long addr = code.addr;
			long value;
			code.startBatchMode();
			while ((value = CheatCoder.readBytes(valueText, offset, valueSize)) != -1){
				code.value = parseValue(value, is_l_h);
				CheatCoder.encode(code);
				if (!ableLongValue)
					break;
				offset += step;
				code.addr = addr += valueSize;
			}
			code.endBatchMode();
		} else {
			code.value = parseValue(CheatCoder.hexToDec(mValueView.getText()), is_l_h);
			if (slideType == SLIDE_BATCH ) {
				//批量模式
				long addr = code.addr;
				long value = code.value;
				code.startBatchMode();
				while (code.dataSize > 0){
					CheatCoder.encode(code);
					code.addr = addr += code.addrInc;
					code.value = value += code.valueInc;
					code.dataSize--;
				}
				code.endBatchMode();
			} else {
				//普通模式
				CheatCoder.encode(code);
			}
		}
		println(code);
	}
	
	/**
	 * 解析代码
	 */
	private void decode(){
		ArrayList<Code> codes = new ArrayList<Code>();
		Code mCode = null;
		Editable content = mOutputContent;
		int start = 0;
		int end;
		int i;
		int length;
		String line;
		content.append('\n');
		length = content.length();
		for (i = 0; i < length; i++){
			char ch = content.charAt(i);
			if (ch == '\n') {
				end = i;
				while(start < end && content.charAt(start) == ' ')
					start++;
				while(end > start && content.charAt(end - 1) == ' ')
					end--;
				if (start != end){
					line = content.subSequence(start, end).toString();
					start = i + 1;
					if (CheatCoder.CBGS.matcher(line).matches()){
						if (mCode == null || !mCode.waitForSecond) {
							mCode = new Code();
							mCode.setText(line);
							codes.add(mCode);
						} else 
							mCode.addLine(line);
						CheatCoder.decode(line, mCode);
					}
				}
				start = i + 1;
			} else if (ch == '#')
				break;
		}
		for (Code code : codes)
			println(toString(code));
	}
	
	/**
	 * 数值转成10进制
	 * @param is_l_h 是否是从低位到高位
	 */
	private long parseValue(long value, boolean is_l_h){
		if (is_l_h) {
			//先倒置成高位到低位
			long temp = 0;
			while (value > 0){
				temp <<= 8;
				temp |= value & 0xFF;
				value >>= 8;
			}
			value = temp;
		}
		return value;
	}
	
	private String toString(Code code){
		StringBuilder sb = new StringBuilder();
		int pos;
		int funcIndex = ArrayUtils.indexOf(CheatCoder.TYPE_LIST[code.type], code.func);
		sb.append('\n').append('#').append(code.mText)
			.append('\n').append("raw : ");
		pos = sb.length();
		sb.append(CheatCoder.toHEX(code.addr));
		CheatCoder.ao(sb, pos, 8);
		pos += 9;
		sb.append(':').append(CheatCoder.toHEX(code.value));
		CheatCoder.ao(sb, pos, 8);
		sb.append('\n').append("type: ")
			.append(mCodeTypeView.getAdapter().getItem(code.type));
		if (funcIndex != -1)
			sb.append('\n').append("func: ")
			.append(mAdapters[code.type].getItem(funcIndex));
		if (code.dataSize != 0)
			sb.append('\n').append("dataSize: ").append(CheatCoder.toHEX(code.dataSize));
		if (code.valueInc != 0)
			sb.append('\n').append("valueInc: ").append(CheatCoder.toHEX(code.valueInc));
		if (code.addrInc != 0)
			sb.append('\n').append("addrInc: ").append(CheatCoder.toHEX(code.addrInc));
		return sb.toString();
	}
	
	private void println(CharSequence text){
		if (text == null)
			return;
		Editable content = mOutputContent;
		int length = content.length();
		if (length != 0)
			content.append('\n');
		content.append(text);
	}

	private void println(Code code){
		println(code.mText);
	}
}
