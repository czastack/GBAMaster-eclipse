package cza.app;

import android.content.Context;
import android.widget.EditText;
import cza.gbamaster.R;
import cza.util.ViewUtils;

public class EditDialog extends Dialog {

	public EditText textarea;
	public static final int 
	MODE_INPUT = 0,
	MODE_SHOW = 1,
	MODE_EMPTY = -1;

	public EditDialog(Context context, int mode){
		super(context);
		if (mode == MODE_INPUT){
			setView(R.layout.multi_text_input);
			textarea = (EditText) findView(R.id.iet);
			setConfirm();
		} else if (mode == MODE_SHOW){
			setView(textarea = new EditText(context));
		}
	}

	public void setCopy() {
		setButton(BUTTON_NEGATIVE, R.string.cancel);
		setButton(BUTTON_NEUTRAL, R.string.copy);
	}

	@Override
	public boolean triggerClick(int which) {
		if(which == BUTTON_NEUTRAL) {
			ViewUtils.edit(textarea, R.id.btn_copy);
			return true;
		}
		return super.triggerClick(which);
	}

	public void setMessage(CharSequence text){
		textarea.setText(text);
	}

	public String getText() {
		return textarea.getText().toString();
	}
}
