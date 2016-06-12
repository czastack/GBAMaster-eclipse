package cza.gbamaster.playwidget;

import android.view.KeyEvent;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import cza.gbamaster.PlayActivity;
import cza.gbamaster.R;
import cza.hack.Coder;
import cza.util.ViewUtils;

public class MemoryTrack extends Track implements 
		RadioGroup.OnCheckedChangeListener,
		CompoundButton.OnCheckedChangeListener {
	
	private int mSize = 1;
	private boolean mIsWriteMode;
	private boolean mAddrChanged;
	private boolean mAutoRestore = true;
	private long mOriginValue = -1;
	private StringBuilder mCodeBuffer = new StringBuilder();
	private EditText mValueInput;
	
	public MemoryTrack(PlayActivity owner){
		super(owner, R.layout.track_memory);
		RadioGroup radios = (RadioGroup)mLayout.findViewById(R.id.sizeView);
		radios.setOnCheckedChangeListener(this);
		RadioGroup modes = (RadioGroup)mLayout.findViewById(R.id.modeView);
		modes.setOnCheckedChangeListener(this);
		CompoundButton autoRestoreView = (CompoundButton)mLayout.findViewById(R.id.option_autoRestore);
		autoRestoreView.setOnCheckedChangeListener(this);
		mValueInput = (EditText) mLayout.findViewById(R.id.valueView);
		ViewUtils.setOnDown(mValueInput, this);
	}

	@Override
	protected void removeCode() {
		if (mOriginValue != -1){
			mOwner.getEmulator().writeBytes(mAddr, mOriginValue, mSize);
			mOriginValue = -1;
		}
	}

	@Override
	protected void onCodeEnter(String text) {
		setAddr((int)Coder.fromLongHex(text));
	}

	/**
	 * 设置地址
	 */
	private void setAddr(long addr) {
		if (mAutoRestore)
			removeCode();
		mAddr = addr;
		onAddrChange();
		mCodeInput.setText(Coder.toWordString(mAddr));
	}
	
	/**
	 * 输入地址
	 */
	private void onAddrChange() {
		mAddrChanged = true;
		if (mIsWriteMode){
			setValue(mValue);
		} else {
			mValue = (int)mOwner.getEmulator().readBytes(mAddr, mSize);
			onValueChange();
		}
	}

	/**
	 * 设置值
	 */
	private void setValue(long value) {
		if (mAddrChanged){
			mOriginValue = mValue;
			mAddrChanged = false;
		}
		mValue = value;
		mOwner.getEmulator().writeBytes(mAddr, value, mSize);
		onValueChange();
	}
	
	/**
	 * 更新数值显示
	 */
	private void onValueChange(){
		mValueInput.setText(Coder.toHexString(mValue & Coder.FLAG_32BIT, mSize));
		update();
	}
	
	@Override
	protected void onOffset(int id) {
		if (mAddr == -1)
			mOwner.toast(R.string.input_addr);
		else if(mValue == -1 && mIsWriteMode)
			mOwner.toast(R.string.input_value);
		else {
			if (id == R.id.btn_addr_down || id == R.id.btn_addr_up){
				if (id == R.id.btn_addr_down) {
					if (mAddr != 0)
						setAddr(mAddr - mSize);
				} else if (id == R.id.btn_addr_up) {
					if (mAddr != 0xFFFFFFFF)
						setAddr(mAddr + mSize);
				}
			} else {
				// 数值改变
				if (id == R.id.btn_code_down) {
					if (mValue != 0)
						setValue(mValue - 1);
				} else {
					if (mValue != 0xFFFF)
						setValue(mValue + 1);
				}
			}
		}
	}

	/**
	 * 切换数据长度
	 * @param group
	 * @param checkedId
	 */
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.u8:
			mSize = 1;
			break;
		case R.id.u16:
			mSize = 2;
			break;
		case R.id.u32:
			mSize = 4;
			break;
		case R.id.option_read:
			mIsWriteMode = false;
			break;
		case R.id.option_write:
			mIsWriteMode = true;
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(buttonView.getId() == R.id.option_autoRestore)
			mAutoRestore = isChecked;
	}
	

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
		if (textView == mValueInput){
			if (textView.getText().length() == 0)
				mOwner.toast(R.string.input_value);
			else 
				setValue(Coder.readLowBytes(textView.getText(), mSize));
			return true;
		}
		return super.onEditorAction(textView, actionId, event);
	}

	@Override
	protected void update(){
		StringBuilder sb = mCodeBuffer;
		sb.setLength(0);
		sb.append(Coder.toHEX(mAddr));
		Coder.ao(sb, 0, 8);
		sb.append(':').append(Coder.toHEX(mValue));
		Coder.ao(sb, 10, mSize << 2);
		mCode = sb.toString();
		super.update();
	}
}
