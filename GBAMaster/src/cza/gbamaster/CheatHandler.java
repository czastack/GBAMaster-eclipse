package cza.gbamaster;

import android.content.Intent;
import android.net.Uri;
import cza.app.Dialog;
import cza.app.TextDialog;
import cza.hack.Cheats;

public abstract class CheatHandler extends BaseActivity implements 
		Dialog.OnClickListener {
	public static Cheats mPlayingCheats;
	
	public boolean mIsPlaying;
	public boolean mChanged;
	public final static int MASK_OPTION_FINISH = 1;
	public final static String INTENT_OPTION = "option";
	
	protected TextDialog saveChangeDialog;
	
	protected String initPath(){
		/*
		 * 不管是否从内置模拟器调用，
		 * 都要记录保存路径
		 */
		Intent intent = getIntent();
		String path;
		Uri data = intent.getData();
		if (data != null)
			path = data.getPath();
		else 
			path = intent.getStringExtra(INTENT_PATH);
		if (path == null){
			toast(R.string.notRunning);
			exit();
			return null;
		}
		// 如果是从内置模拟器调用的，就用模拟器实例化的Cheats
		// 否则就用传入的path另外实例化一个
		mIsPlaying = intent.getBooleanExtra(INTENT_PLAYING, false);
		return path;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		int option = intent.getIntExtra(INTENT_OPTION, 0);
		if (option == MASK_OPTION_FINISH){
			mIsPlaying = false;
			finish();
		}
	}

	@Override
	public void finish() {
		if (mIsPlaying)
			backToPlay();
		else if (mChanged)
			showSaveChangeDialog();
		else 
			exit();
	}
	
	protected void exit(){
		setResult(RESULT_OK);
		super.finish();
	}


	/**
	 * 保存修改对话框
	 */
	protected void showSaveChangeDialog(){
		TextDialog dialog = saveChangeDialog;
		if (dialog == null){
			dialog = saveChangeDialog = new TextDialog(this, 0);
			dialog.setTitle("保存修改");
			dialog.setButton(Dialog.BUTTON_POSITIVE, "是");
			dialog.setButton(Dialog.BUTTON_NEGATIVE, R.string.cancel);
			dialog.setButton(Dialog.BUTTON_NEUTRAL, "否");
			dialog.setOnClickListener(this);
			dialog.setMessage("数据已修改，是否保存到文件？");
		}
		dialog.show();
	}

	@Override
	public boolean onClick(Dialog dialog, int which) {
		if (which != Dialog.BUTTON_NEGATIVE) {
			if (which == Dialog.BUTTON_POSITIVE) {
				onSaveDefault();
			}
			exit();
		}
		dialog.cancel();
		return true;
	}
	
	protected abstract void onSaveDefault();
	
	/**
	 * 返回内置模拟器
	 */
	private Intent mPlayIntent;

	protected void backToPlay(){
		if (mPlayIntent == null){
			mPlayIntent = getToFrontIntent(PlayActivity.class);
		}
        startActivity(mPlayIntent.putExtra(INTENT_CHANGED, mChanged));
	}
	

	
	/**
	 * 检测选项
	 * @param option
	 * @param mask
	 * @return
	 */
	public boolean hasOption(int option, int mask){
		return (option & mask) != 0;
	}
}
