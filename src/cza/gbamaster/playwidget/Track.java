package cza.gbamaster.playwidget;

import java.util.Iterator;
import java.util.LinkedHashMap;

import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import cza.app.EditDialog;
import cza.gbamaster.PlayActivity;
import cza.gbamaster.R;
import cza.util.ViewUtils;

/**
 * 追踪
 */
public abstract class Track extends PlayWidget  implements
		View.OnClickListener,
		TextView.OnEditorActionListener {

	protected long mAddr = -1;
	protected long mValue = -1;
	protected String mCode;
	protected EditText mCodeInput, mNameInput;
	protected View mExtBar;
	protected LinkedHashMap<String, String> mNotes;

	public Track(PlayActivity owner, int resId){
		super(owner, resId);
		View view = mLayout;
		mCodeInput = (EditText) view.findViewById(R.id.codeView);
		mNameInput = (EditText) view.findViewById(R.id.nameView);
		ViewUtils.setOnDown(mCodeInput, this);
		ViewUtils.setOnDown(mNameInput, this);
		mExtBar = view.findViewById(R.id.extBar);

		ViewUtils.registerClick(view, this,
			R.id.btn_addr_down, R.id.btn_addr_up, R.id.btn_code_down, R.id.btn_code_up,
			R.id.btn_clear, R.id.btn_close, R.id.btn_output);
		view.findViewById(R.id.btn_reposition).setOnTouchListener(this);
		mNotes = new LinkedHashMap<String, String>();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_close)
			destroy();
		if (id == R.id.btn_clear)
			removeCode();
		else if (id == R.id.btn_output)
			outputNote();
		else 
			onOffset(id);
	}
	
	protected abstract void removeCode();

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
		String text = textView.getText().toString().trim();
		if (textView == mCodeInput)
			onCodeEnter(text);
		else 
			mNotes.put(mCode, text);
		return true;
	}

	/**
	 * 更新备注
	 */
	protected void update(){
		mNameInput.setText(mNotes.get(mCode));
	}
	
	/**
	 * 输入代码处理
	 */
	protected abstract void onCodeEnter(String text);
	
	/**
	 * 点击偏移按钮时
	 */
	protected abstract void onOffset(int id);
	
	/**
	 * 输出
	 */
	private void outputNote(){
		StringBuilder sb = new StringBuilder(); 
		Iterator<LinkedHashMap.Entry<String, String>> itr = mNotes.entrySet().iterator();
		while(itr.hasNext()) {
			LinkedHashMap.Entry<String, String> entry = itr.next();
			String code = entry.getKey();
			String name = entry.getValue();
			sb.append(code).append('\t').append(name).append('\n');
		}
		EditDialog outputDialog = new EditDialog(mOwner, EditDialog.MODE_SHOW);
		outputDialog.setCopy();
		outputDialog.setTitle("追踪记录");
		outputDialog.setMessage(sb.toString());
		outputDialog.show();
	}
}