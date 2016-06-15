package cza.gbamaster.cot;
import cza.hack.Cheat;
import cza.hack.Cheats;
import cza.util.Pull;
import cza.util.XmlWriter;
import cza.widget.CheckLayout;
import android.content.Context;

public class CheatItem extends CheckLayout implements CheatView {
	public Cheat mCheat;
	private String mIntro;

	public CheatItem(Context c, Pull pull) throws Exception {
		this(c, pull.getValue(ATTR_NAME), pull.getValue(ATTR_HINT));
		final int depth = pull.parser.getDepth();
		int type;
		while (((type = pull.parser.next()) != 3 || pull.parser.getDepth() > depth) && type != 1) {
			if (type != 2) continue;
			if (TAG_CODE.equals(pull.parser.getName())) 
				coder.formatCode(pull.getText(), mCheat);
		}
		mIntro = pull.getValue(ATTR_INTRO);
		//绑定
		setId(getTitle().hashCode());
	}
	
	private CheatItem(Context c, String title, String hint) {
		super(c, title, hint);
		mCheat = new Cheat();
		mCheat.name = title;
	}

	@Override
	public boolean isAvailable() {
		return mChecked;
	}

	@Override
	public String getTitle() {
		return mCheat.name;
	}
	
	@Override
	public String getIntro() {
		return mIntro;
	}

	@Override
	public void output(CheatViewGroup parent, Cheats cheats) {
		CheatViewGroup.putTitle(parent, this, cheats);
		for (String code : mCheat.codes)
			coder.formatCode(code, cheats.mCheat);
	}

	@Override
	public void reset() {
		if(mChecked)
			trigger();
	}

	@Override
	public void loadForm(Pull pull) {
		trigger();
	}

	@Override
	public void saveForm(XmlWriter writer) {
		writer.startTag(TAG_DATA);
		writer.attribute(ATTR_NAME, getTitle());
		writer.endTag(TAG_DATA);
	}
}
