package cza.gbamaster.cot;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import cza.gbamaster.R;
import cza.hack.Cheats;
import cza.util.Pull;
import cza.util.XmlWriter;
import cza.widget.Inputs;
import cza.widget.OnceLayoutListener;

public class CheatInputs extends Inputs implements CheatView,
		OnceLayoutListener {
	
	private String mIntro;

	public CheatInputs(Context c, Pull pull) {
		super(c, pull.getValue(ATTR_NAME));
		mIntro = pull.getValue(ATTR_INTRO);
		mDialog.addHeader(R.layout.cheat_widget_auto_max);
		mDialog.findView(R.id.btn_autoMax).setOnClickListener(this);
		//绑定
		setId(getTitle().hashCode());
	}

	public CheatInput appendInput(Pull pull){
		CheatInput input = new CheatInput(getContext(), pull, false);
		append(input);
		return input;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_autoMax)
			toBeMax();
		else 
			super.onClick(v);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		children.get(children.size() - 1).setOnceLayoutListener(this);
	}

	@Override
	public void onceLayout(View view) {
		alignLabel();
	}
	
	@Override
	public String getIntro() {
		return mIntro;
	}
	
	public void toBeMax(){
		for (int i = 0, length = children.size(); i < length; i++) {
			CheatInput input = (CheatInput)children.get(i);
			input.toBeMax();
		}
	}

	@Override
	public boolean isAvailable() {
		return !avails.isEmpty();
	}

	@Override
	public String getTitle() {
		return mTitle;
	}

	@Override
	public void output(CheatViewGroup parent, Cheats cheats) {
		CheatViewGroup.putTitle(parent, this, cheats);
		for (int i = 0, length = children.size(); i < length; i++) {
			CheatInput input = (CheatInput)children.get(i);
			String code = input.output();
			if (code != null) 
				coder.formatAll(code, cheats);
		}
	}

	@Override
	public void reset() {
		clearValue();
		onSubmit();
	}

	@Override
	public void loadForm(Pull pull) {
		ArrayList<String> tempValues = new ArrayList<String>();
		//读取选中列表
		try {
			final int depth = pull.parser.getDepth();
			int type;
			while (((type = pull.parser.next()) != 3 || pull.parser.getDepth() > depth) && type != 1) {
				if (type != 2) continue;
				if (TAG_ITEM.equals(pull.parser.getName())) 
					tempValues.add(pull.getText());
			}
		} catch (Exception e) {}
		String[] tempList = new String[tempValues.size()];
		tempList = tempValues.toArray(tempList);
		setValues(tempList);
		onSubmit();
	}

	@Override
	public void saveForm(XmlWriter writer) {
		writer.startTag(TAG_DATA);
		writer.attribute(ATTR_NAME, getTitle());
		for (String value : mValues) {
			writer.startTag(TAG_ITEM);
			writer.text(value);
			writer.endTag(TAG_ITEM);
		}
		writer.endTag(TAG_DATA);
	}
}
