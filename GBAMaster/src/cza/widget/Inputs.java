package cza.widget;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import cza.app.Dialog;
import cza.gbamaster.R;

public class Inputs extends TextPrefLayout implements
		Dialog.OnClickListener {
	
	public ArrayList<Input> children = new ArrayList<Input>();
	public ArrayList<Integer> avails = new ArrayList<Integer>();
	public String[] mValues;

	public Inputs(Context context, String title){
		super(context, title);
		Dialog dialog = new Dialog(context);
		dialog.setOnClickListener(this);
		dialog.setConfirm();
		dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "清空");
		dialog.setTitle(title);
		dialog.setView(R.layout.scroll_layout);
		dialog.setContainer(R.id.layout);
		mDialog = dialog;
	}
	
	public void append(Input input){
		children.add(input);
	}
	
	public void alignLabel(){
		int size = children.size();
		int maxWidth = 0;
		int i;
		int width;
		for (i = 0; i < size; i++){
			width = children.get(i).textView.getWidth();
			if (width > maxWidth)
				maxWidth = width;
		}
		for (i = 0; i < size; i++){
			children.get(i).textView.setWidth(maxWidth);
		}
	}

	public void resetValues(){
		mValues = new String[children.size()];
	}

	public void setValues(CharSequence[] list){
		for (int i = 0; i < children.size(); i++){
			children.get(i).editView.setText(list[i]);
		}
	}
	
	@Override
	public boolean onClick(Dialog dialog, int which) {
		if (which == Dialog.BUTTON_NEGATIVE) {
			setValues(mValues);
			return true;
		} else if (which == Dialog.BUTTON_POSITIVE)
			onSubmit();
		else 
			clearValue();
		return false;
	}
	
	public void onSubmit(){
		avails.clear();
		StringBuilder sb = new StringBuilder();
		for (int i = 0, length = children.size(); i < length; i++){
			Input input = children.get(i);
			String val = input.getValue();
			mValues[i] = val;
			if (!val.isEmpty()){
				avails.add(i);
				sb.append(input.getTitle() + " " + val + "、");
			}
		}
		if (sb.length() > 0){
			sb.deleteCharAt(sb.length() - 1);
		}
		mDialog.dismiss();
		setHint(sb);
	}
	
	public void clearValue(){
		for (Input input : children)
			input.editView.getText().clear();
	}
}
