package cza.gbamaster.playwidget;

import android.view.View;
import android.widget.ImageView;
import cza.gbamaster.PlayActivity;
import cza.gbamaster.R;
import cza.hack.CBCoder;

public class CheatTrack extends Track {

	private boolean mIsCB;
	private char mAddrPrefix;
	private boolean mExpanded;
	
	public CheatTrack(PlayActivity owner){
		super(owner, R.layout.track_cheat);

		mLayout.findViewById(R.id.btn_display).setOnClickListener(this);
		mLayout.findViewById(R.id.btn_pause).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_display)
			mExtBar.setVisibility((mExpanded = !mExpanded) ? 0 : 8);
		else if (id == R.id.btn_pause){
			ImageView btn = (ImageView) v;
			btn.setImageResource(
				mOwner.switchState() ? 
				android.R.drawable.ic_media_play : 
				android.R.drawable.ic_media_pause);
		}
		else
			super.onClick(v);
	}
	
	@Override
	protected void onCodeEnter(String text) {
		mIsCB = text.matches(CBCoder.CB);
		if (mIsCB){
			mAddrPrefix = text.charAt(0);
			mAddr = CBCoder.fromHex(text.substring(1, 8));
			mValue = CBCoder.fromHex(text.substring(9));
			update();
		} else if (text.matches(CBCoder.GS)){
			mAddr = -1;
			removeCode();
			mCode = text;
			update();
		}
	}
	
	@Override
	protected void onOffset(int id) {
		if (mAddr != -1){
			if (id == R.id.btn_addr_down) {
				if (mAddr != 0)
					mAddr--;
			} else if (id == R.id.btn_addr_up) {
				if (mAddr != 0xFFFFFFF)
					mAddr++;
			} else if (id == R.id.btn_code_down) {
				if (mValue != 0)
					mValue--;
			} else {
				if (mValue != 0xFFFF)
					mValue++;
			}
			update();
		} else if (mCode != null)
			mOwner.toast(R.string.onlyCB);
		else 
			mOwner.toast(R.string.input_cheat);
	}

	/**
	 * 切换后删除原来的Cheat
	 */
	protected void removeCode(){
		if (mCode != null){
			mOwner.getEmulator().removeCheat(mCode);
		}
	}
	
	@Override
	protected void update(){
		if (mIsCB) {
			removeCode();
			mCode = CBCoder.formatCode(mAddrPrefix, (int)mAddr, (int)mValue, 0);
			mCodeInput.setText(mCode);
		}
		mOwner.getEmulator().addCheat(mCode);
		super.update();
	}
	
	@Override
	protected void destroy(){
		removeCode();
		super.destroy();
	}
}
