package cza.gbamaster.cot;

import cza.hack.CBCoder;
import cza.hack.Cheats;
import cza.util.Pull;
import cza.util.XmlWriter;

public interface CheatView {
	CBCoder coder = CBCoder.getInstance();
	String ATTR_CODE_TYPE = "codeType";
	String ATTR_CODE_FUNC = "codeFunc";
	String ATTR_COUNT = "count";
	String ATTR_NAME = "name";
	String ATTR_INTRO = "intro";
	String ATTR_ADDR = "addr";
	String ATTR_HINT = "hint";
	String ATTR_HORIZONTAL = "horizontal";
	String ATTR_INDEX = "index";
	String ATTR_MULTIPLE = "multiple";
	String ATTR_HAS_HINT = "itemHasHint";
	String ATTR_ENTRY = "entry";
	String ATTR_INCREMENT = "increment";
	String ATTR_INCREMENT_HINT = "incrementHint";
	String ATTR_MAXVALUE = "maxValue";
	String ATTR_TITLE_FORMAT = "titleFormat";
	String ATTR_USE_PARENT_FORMAT = "useParentFormat";
	String ATTR_SHOW_IN_DIALOG = "showInDialog";
	String ATTR_VALUE = "value";
	
	String WIDGET_SELECT = "select";
	String WIDGET_INPUTS = "inputs";
	String WIDGET_INPUT = "input";
	String WIDGET_CHEAT = "cheat";
	String WIDGET_RADIOS = "radios";
	String WIDGET_TITLE = "title";
	String WIDGET_GROUP = "group";
	String WIDGET_HORIZONTAL = "horizontal";
	String WIDGET_VERTICAL = "vertical";
	
	String TAG_ROOT = "cot";
	String TAG_DATA = "data";
	String TAG_ITEM = "item";
	String TAG_CODE = "code";
	
	public String getTitle();
	
	public String getIntro();
	
	public boolean isAvailable();
	
	public void output(CheatViewGroup parent, Cheats cheats);
	
	public void reset();
	
	public void loadForm(Pull pull);
	
	public void saveForm(XmlWriter writer);
}
