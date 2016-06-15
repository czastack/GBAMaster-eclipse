package cza.gbamaster.cot;

import android.content.Context;
import android.view.View;
import cza.gbamaster.R;
import cza.hack.Cheats;
import cza.hack.Coder;
import cza.util.Pull;
import cza.util.ViewUtils;
import cza.util.XmlWriter;
import cza.widget.Input;
import cza.widget.PrefLayout;

public class CheatInput extends Input implements CheatView, View.OnClickListener {
	public String mAddr;
	public String maxValue;
	private String mIntro;
	private long maxNumber;

	/**
	 * vertical 是否默认垂直
	 */
	public CheatInput(Context c, Pull pull, boolean vertical) {
		super(c, pull.getValue(ATTR_NAME));
		mAddr = pull.getValue(ATTR_ADDR);
		mIntro = pull.getValue(ATTR_INTRO);
		String hint;
		String tempHint;
		hint = pull.getValue(ATTR_MAXVALUE);
		if (hint != null){
			maxValue = hint;
			maxNumber = Long.parseLong(hint);
			hint = c.getString(R.string.maxis, hint);
			initMax();
		}
		tempHint = pull.getValue(ATTR_HINT);
		if (hint != null && tempHint != null)
			hint += "，" + tempHint;
		editView.setHint(hint);
		if (!pull.getBoolean(CheatView.ATTR_HORIZONTAL) && vertical) 
			PrefLayout.vertical(this);
		setOnClickListener(this);
		//绑定
		setId(getTitle().hashCode());
	}
	
	@Override
	public String getIntro() {
		return mIntro;
	}
	
	public void toBeMax(){
		if (maxValue != null && !maxValue.isEmpty())
			editView.setText(maxValue);
	}
	
	/**
	 * 初始化自动完成控件
	 */
	private void initMax(){
		CheatEditText view = new CheatEditText(getContext());
		view.maxValue = maxValue;
		ViewUtils.replaceView(editView, view);
		editView = view;
	}

	@Override
	public void onClick(View v) {
		requesInput();
	}

	@Override
	public boolean isAvailable() {
		return !getValue().isEmpty();
	}

	@Override
	public void output(CheatViewGroup parent, Cheats cheats) {
		String code = output();
		if (code != null){
			CheatViewGroup.putTitle(parent, this, cheats);
			coder.formatAll(code, cheats);
		}
	}
	
	/**
	 * 生成代码字符串
	 */
	public String output(){
		String value = getValue();
		if (value.isEmpty()) 
			return null;
		long number = Long.parseLong(value);
		if (maxNumber != 0 && number > maxNumber) {
			//超过限制
			number = maxNumber;
			editView.setText(maxValue);
		}
		String mCode;
		if (mAddr.contains("%")) {
			mCode = String.format(mAddr, number);
		} else {
			value = Coder.toHex(number);
			StringBuilder sb = new StringBuilder();
			for (String addr : mAddr.split("\\|")) 
				sb.append('\n').append(addr).append(' ').append(value);
			mCode = sb.toString();
		}
		return mCode;
	}

	@Override
	public void reset() {
		editView.getText().clear();
	}

	@Override
	public void loadForm(Pull pull) {
		editView.setText(pull.getValue(ATTR_VALUE));
	}

	@Override
	public void saveForm(XmlWriter writer) {
		writer.startTag(TAG_DATA);
		writer.attribute(ATTR_NAME, getTitle());
		writer.attribute(ATTR_VALUE, getValue());
		writer.endTag(TAG_DATA);
	}
}
