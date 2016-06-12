package cza.gbamaster.cot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import cza.gbamaster.R;
import cza.hack.Cheat;
import cza.hack.Cheats;
import cza.util.Pull;
import cza.util.XmlWriter;
import cza.widget.TextPrefLayout;

public class CheatRadioGroup extends TextPrefLayout implements CheatView {
	private Cheat[] mCheats;
	private LayoutInflater mInflater;
	private RadioGroup mGroup;
	private String mIntro;

	public CheatRadioGroup(Context context, Pull pull) throws Exception {
		super(context, pull.getValue(ATTR_NAME));
		setHint(pull.getValue(ATTR_HINT));
		mIntro = pull.getValue(ATTR_INTRO);
		mInflater = LayoutInflater.from(context);
		mGroup = (RadioGroup)mInflater.inflate(R.layout.cheatview_radio_group, null);
		addView(mGroup);
		addRadio(context.getString(R.string.close));
		final int count = pull.getInt(ATTR_COUNT);
		final int depth = pull.parser.getDepth();
		int index = 0;
		int type;
		Cheat[] cheats = mCheats = new Cheat[count];
		String tagName;
		Cheat cheat = null;
		while (((type = pull.parser.next()) != 3 || pull.parser.getDepth() > depth) && type != 1) {
			if (type != 2) continue;
			tagName = pull.parser.getName();
			if (WIDGET_CHEAT.equals(tagName)) {
				cheats[index++] = cheat = new Cheat();
				cheat.name = pull.getValue(ATTR_NAME);
				addRadio(cheat.name);
			} else if (TAG_CODE.equals(tagName)) 
				coder.formatCode(pull.getText(), cheat);
		}
		reset();
		//绑定
		setId(mTitle.hashCode());
	}
	
	@Override
	public String getIntro() {
		return mIntro;
	}
	
	private void addRadio(String title){
		mInflater.inflate(R.layout.cheatview_radio, mGroup);
		RadioButton radio = (RadioButton)mGroup.getChildAt(mGroup.getChildCount() - 1);
		radio.setText(title);
	}
	
	/**
	 * 设置/获取选中状态
	 * position 为-1 返回选中项
	 * position >0  设置选中项  返回0
	 * @param position
	 * @return
	 */
	private int etChecked(final int position){
		RadioGroup group = mGroup;
		int i = 0;
		int index = 0;
		final int count = group.getChildCount();
		View child;
		RadioButton radio;
		while (i < count){
			child = group.getChildAt(i);
			if (child instanceof RadioButton){
				radio = (RadioButton)child;
				if (position > -1) {
					//设置选项
					if (index == position) {
						radio.setChecked(true);
						break;
					}
				} else if (radio.isChecked())
					return index; //获取选项
				index++;
			}
			i++;
		}
		return 0;
	}
	
	/**
	 * 选中的cheat
	 * @return
	 */
	public Cheat getSelectedCheat(){
		int selectedIndex = etChecked(-1) - 1; // 0项为关闭
		if (selectedIndex != -1)
			return mCheats[selectedIndex];
		return null;
	}
	
	@Override
	public String getTitle() {
		Cheat cheat = getSelectedCheat();
		if (cheat != null)
			return mTitle + cheat.name;
		else 
			return mTitle;
	}

	@Override
	public boolean isAvailable() {
		return etChecked(-1) != 0;
	}

	@Override
	public void output(CheatViewGroup parent, Cheats cheats) {
		Cheat cheat = getSelectedCheat();
		CheatViewGroup.putTitle(parent, this, cheats);
		for (String code : cheat.codes)
			coder.formatCode(code, cheats.mCheat);
	}

	@Override
	public void reset() {
		etChecked(0);
	}

	@Override
	public void loadForm(Pull pull) {
		etChecked(pull.getInt(ATTR_INDEX));
	}

	@Override
	public void saveForm(XmlWriter writer) {
		writer.startTag(TAG_DATA);
		writer.attribute(ATTR_NAME, getTitle());
		writer.attribute(ATTR_INDEX, Integer.toString(etChecked(-1)));
		writer.endTag(TAG_DATA);
	}
}
