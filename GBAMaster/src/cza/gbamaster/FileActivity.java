package cza.gbamaster;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import cza.app.Dialog;
import cza.app.EditDialog;
import cza.app.Shortcut;
import cza.file.FileInfo;
import cza.file.FileInfos;
import cza.util.ViewUtils;

public class FileActivity extends cza.app.FileActivity {
	private BatchRename batchRename;
	private LS ls;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        displayHomeButton();
		if (MODE_BROWSER == mode) {
			Intent intent = getIntent();
			//筛选文件
			String screenText = intent.getStringExtra(INTENT_SCREEN_TEXT);
			if (screenText != null) {
				int screenType = intent.getIntExtra(INTENT_SCREEN_TYPE, SCREEN_ALL);
				onScreen(screenText, screenType);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_newFile:
				showNewFileDialog();
				return true;
			case R.id.menu_screen:
				showScreenDialog();
				return true;
			case R.id.menu_ls:
				if (ls == null)
					ls = new LS(this);
				ls.ls();
				return true;
			case R.id.menu_batchRename:
				startBatchRename();
				return true;
			case R.id.menu_undoRename:
				if (batchRename != null)
					batchRename.undo();
				return true;
			case R.id.menu_createShortcut:
				createShortCut();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onRename(FileMenu menu) {
		if (ableMulChk) 
			startBatchRename();
		else 
			super.onRename(menu);
	}

	/**
	 * 创建快捷方式
	 */
	private void createShortCut() {
		Shortcut shortcut = new Shortcut()
			.setTitle(mFolder.getName())
			.setIcon(this, MyApplication.ICON_DIR)
			.setIntent(
			new Intent(this, getClass())
			.putExtra("path", mDir)
			.putExtra("mode", MODE_BROWSER)
		);
		sendBroadcast(shortcut);
	}

	/**
	 * 对话框确认事件
	 */
	@Override
	public boolean onClick(Dialog dialog, int which) {
		if (which == Dialog.BUTTON_POSITIVE){
			if (dialog == batchRename) {
				if (ableMulChk) 
					stopMulChk();
				refreshData();
				return true;
			}
			if (screenDialog == dialog) {
				return onScreen();
			} else if (newFileDialog == dialog) {
				return onNewFile();
			}
		}
		return super.onClick(dialog, which);
	}

	/**
	 * 批量重命名
	 */
	private void startBatchRename() {
		if (batchRename == null) {
			batchRename = new BatchRename(this);
			batchRename.setOnClickListener(this);
		}
		batchRename.setInfos(getInfos());
		batchRename.show();
	}


	/**
	 * 筛选
	 */
	private Dialog screenDialog;
	private EditText input_screen;
	private RadioGroup screenRange;
	public static final int 
	SCREEN_ALL = R.id.option_all,
	SCREEN_FOLDER = R.id.option_folder,
	SCREEN_FILE = R.id.option_file;

	private void showScreenDialog() {
		Dialog dialog = screenDialog;
		if (dialog == null){
			screenDialog = dialog = new Dialog(this);
			dialog.setOnClickListener(this);
			dialog.setTitle("筛选");
			dialog.setConfirm();
			dialog.setView(R.layout.filebrowser_screen);
			screenRange = (RadioGroup) dialog.findView(R.id.typeSwitch);
			input_screen = (EditText) dialog.findView(R.id.input_regex);
		}
		dialog.show();
	}

	private boolean onScreen() {
		String regexStr = input_screen.getText().toString();
		int type = screenRange.getCheckedRadioButtonId();
		return onScreen(regexStr, type);
	}

	private boolean onScreen(String regexStr, int type) {
		if (regexStr.isEmpty()) {
			if (SCREEN_ALL == type) 
				mInfos.chkAll(true);
			else if (SCREEN_FOLDER == type)
				mInfos.checkInfos(true);
			else if (SCREEN_FILE == type) 
				mInfos.checkInfos(false);
		} else {
			Pattern regex;
			try {
				regex = Pattern.compile(regexStr);
			} catch (Exception e) {
				toast("正则错误");
				return false;
			}
			mInfos.chkAll(false);
			List<FileInfo> infos = mInfos;
			if (SCREEN_FOLDER == type)
				infos = mInfos.getInfos(true);
			else if (SCREEN_FILE == type)
				infos = mInfos.getInfos(false);
			int len = mInfos.size();
			int first = len;
			for (FileInfo info : infos) {
				Matcher m = regex.matcher(info.name);
				if (info.checked = m.find()){
					mInfos.checkedCount++;
					int index = mInfos.indexOf(info);
					if (index < first)
						first = index;
				}
			}
			if (first < len) {
				mListView.setSelection(first);
			}
		}
		startMulChk();
        chkMulChk();
        return true;
	}

	/**
	 * 新建文件
	 */
	private EditDialog newFileDialog;
	private RadioGroup newFileType;

	private void showNewFileDialog() {
		Dialog dialog = newFileDialog;
		if (dialog == null){
			dialog = newFileDialog = new EditDialog(this, EditDialog.MODE_INPUT);
			dialog.setOnClickListener(this);
			dialog.setTitle("新建");
			newFileType = (RadioGroup) dialog.addHeader(R.layout.filebrowser_newfile);
		}
		dialog.show();
	}

	private boolean onNewFile() {
		String[] items = newFileDialog.getText().split("\n");
		for (String str : items) {
			File f = new File(mFolder, str);
			try {
				switch (newFileType.getCheckedRadioButtonId()) {
					case R.id.option_file:
						f.getParentFile().mkdirs();
						f.createNewFile();
						break;
					case R.id.option_folder:
						f.mkdirs();
						break;
				}
			} catch (Exception e) {
				toast(e.toString());
			}
			refreshData();
		}
		return true;
	}

	/**
	 * 输出列表
	 */
	private class LS extends EditDialog implements 
	CompoundButton.OnCheckedChangeListener {

		private boolean pDir, withSep, withSize;
		private FileInfos myInfos;
		LS(Context c) {
			super(c, MODE_EMPTY);
			setCopy();
			setView(R.layout.filebrowser_ls);
			textarea = (EditText) findView(R.id.iet);
			ViewUtils.registerCheck(getMainView(), this, 
				R.id.option_path, 
				R.id.option_separator, 
				R.id.option_size);
		}

		@Override
		public void onCheckedChanged(CompoundButton v, boolean checked) {
			switch (v.getId()) {
				case R.id.option_path:
					pDir = checked;
					break;
				case R.id.option_separator:
					withSep = checked;
					break;
				case R.id.option_size:
					withSize = checked;
					break;
			}
			setMessage(getString(myInfos));
		}

		private String getString(FileInfos fs) {
			StringBuilder sb = new StringBuilder();
			String p = "\n", t = "\t",
				sep = withSep ? "/" : "";
			for (FileInfo f : fs) {
				sb.append(p  + f.name);
				if (f.isDir) {
					sb.append(sep);
				} else if (withSize) {
					sb.append(t + f.size);
				}
			}
			if (pDir) {
				sb.insert(0, mDir);
			} else {
				sb.deleteCharAt(0);
			}
			return sb.toString();
		}

		private void ls() {
			if (mInfos.isEmpty()) {
				toast("这是一个标准的空文件夹");
				return;
			}
			myInfos = getInfos();
			String text = getString(myInfos);
			setTitle(mDir);
			setMessage(text);
			show();
		}
	}
}
