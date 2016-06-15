package cza.gbamaster.cot;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import cza.app.Dialog;
import cza.gbamaster.R;
import cza.hack.Cheats;
import cza.util.Pull;
import cza.util.XmlWriter;
import cza.widget.Input;
import cza.widget.TextPrefLayout;

public class CheatViewGroup implements CheatView {

	public int increment;
	public boolean showInDialog;
	private boolean useParentFormat;
	private String mTitle;
	private String mIntro;
	private String mChildTitleFormat;
	private View mIncrementView;
	private TextPrefLayout entrance;
	private ArrayList<CheatView> mViews;
	
	public CheatViewGroup(Context context, Pull pull, CotResource cotRes){
		mViews = new ArrayList<CheatView>();
		if (pull == null)
			return;
		mTitle = pull.getValue(ATTR_NAME);
		mIntro = pull.getValue(ATTR_INTRO);
		String hint = pull.getValue(ATTR_HINT);
		mChildTitleFormat = pull.getValue(ATTR_TITLE_FORMAT);
		useParentFormat = pull.getBoolean(ATTR_USE_PARENT_FORMAT);
		showInDialog = pull.getBoolean(ATTR_SHOW_IN_DIALOG);
		if (showInDialog){
			entrance = new TextPrefLayout(context, mTitle);
			entrance.setHint(hint);
			Dialog dialog = new Dialog(context);
			dialog.setTitle(mTitle);
			dialog.setButton(Dialog.BUTTON_NEGATIVE, "完成");
			dialog.setView(R.layout.scroll_layout);
			dialog.setContainer(R.id.layout);
			entrance.mDialog = dialog;
		}
		increment = pull.getInt(ATTR_INCREMENT);
		if (increment > 0) {
			String entryName = pull.getValue(ATTR_ENTRY);
			String incrementHint = pull.getValue(ATTR_INCREMENT_HINT);
			if (entryName == null){
				Input input = new Input(context, incrementHint);
				TextPrefLayout.vertical(input);
				input.editView.setInputType(2);
				input.editView.setText("0");
				mIncrementView = input;
			} else {
				SimpleSelect select = new SimpleSelect(context, incrementHint);
				select.readData(cotRes, entryName);
				mIncrementView = select;
			}
		}
	}
	
	public void add(CheatView view){
		mViews.add(view);
	}

	public View getLayout(){
		return entrance.mDialog.mContainer;
	}
	
	public View getView(){
		//如果在对话框中显示
		//就先把偏移输入框放入对话框
		if (showInDialog){
			if (mIncrementView != null) {
				ViewGroup group = (ViewGroup)getLayout();
				group.addView(mIncrementView, 0);
			}
			return entrance;
		} else 
			return mIncrementView;
	}

	/**
	 * 当前偏移序号
	 * @return
	 */
	private int getOffset() {
		if (mIncrementView instanceof Input){
			String numStr;
			if (!(numStr = ((Input)mIncrementView).getValue()).isEmpty()) 
				return Integer.parseInt(numStr);
		} else if (mIncrementView instanceof SimpleSelect)
			return ((SimpleSelect)mIncrementView).getValue();
		return 0;
	}

	/**
	 * 当前偏移标识符
	 * @return
	 */
	private Object getOffsetTag() {
		if (mIncrementView instanceof Input)
			return getOffset();
		else if (mIncrementView instanceof SimpleSelect)
			return ((SimpleSelect)mIncrementView).mSelect.getSelectedItem();
		return null;
	}
	
	private int getIncrement() {
		return increment * getOffset();
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public String getTitle() {
		return mTitle;
	}
	
	@Override
	public String getIntro() {
		return mIntro;
	}

	public String processTitle(String title){
		if (mChildTitleFormat == null)
			return title;
		else 
			return String.format(mChildTitleFormat, getTitle(), getOffsetTag(), title);
	}
	
	@Override
	public void output(CheatViewGroup parent, Cheats cheats) {
		int lastOffset = coder.addrOffset;
		coder.addrOffset = getIncrement();
		for (CheatView view : mViews) {
			if (view.isAvailable()) {
				view.output(this, cheats);
				if(useParentFormat)
					cheats.updateTitle(parent.processTitle(cheats.mCheat.name));
			}
		}
		coder.addrOffset = lastOffset;
	}

	@Override
	public void reset() {
		for (CheatView view : mViews) 
			if(view.isAvailable())
				view.reset();
	}


	@Override
	public void loadForm(Pull pull) {
		for (CheatView view : mViews) 
			view.loadForm(pull);
	}

	@Override
	public void saveForm(XmlWriter writer) {
		for (CheatView view : mViews) 
			if(view.isAvailable())
				view.saveForm(writer);
	}
	
	static void putTitle(CheatViewGroup parent, CheatView child, Cheats cheats){
		String title = child.getTitle();
		if (parent != null) {
			title = parent.processTitle(title);
		}
		cheats.putCheat(title);
	}
}
