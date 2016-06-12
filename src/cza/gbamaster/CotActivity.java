package cza.gbamaster;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Stack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cza.app.Dialog;
import cza.app.EditDialog;
import cza.app.ListDialog;
import cza.app.ListViewController;
import cza.app.Shortcut;
import cza.app.TextDialog;
import cza.file.FileNameFixFilter;
import cza.file.FileUtils;
import cza.gbamaster.cot.CheatInput;
import cza.gbamaster.cot.CheatInputs;
import cza.gbamaster.cot.CheatItem;
import cza.gbamaster.cot.CheatRadioGroup;
import cza.gbamaster.cot.CheatSelect;
import cza.gbamaster.cot.CheatView;
import cza.gbamaster.cot.CheatViewGroup;
import cza.gbamaster.cot.CotResource;
import cza.hack.Cheats;
import cza.util.Pull;
import cza.util.ViewUtils;
import cza.util.XmlWriter;
import cza.widget.LP;
import cza.widget.MyLayout;
import cza.widget.Select;

public class CotActivity extends CheatHandler implements 
		ListDialog.OnSubmitListener,
		ListDialog.OnClickListener,
		TextView.OnEditorActionListener,
		View.OnLongClickListener {
	private CotResource mCotRes;
	private File mCotFile;
	private FilenameFilter mFormFileFilter;
	private String mCotName;
	private CheatInputs mInputs;
	private Stack<CheatViewGroup> mStack;
	private CheatViewGroup mGroup;
	private ViewGroup mRootView;
	private Bitmap logoBitmap;
	private LinearLayout mLogoBar;
	private boolean mConnectToCheatActivity;
	public final static int MASK_OPTION_CONNECT_TO_CHAET_ACTIVITY = 2;
	public final static int MASK_OPTION_CLOSE_CONNECT = 4;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		String path = initPath();
		if (path == null)
			return;
		mChanged = mIsPlaying;
		mCotFile = new File(path);
		if (!ensureFile(mCotFile))
			return;
		hideStatusBar();
		setContentView(R.layout.scroll_layout);
		//用栈存储CheatViewGroup级别
		mStack = new Stack<CheatViewGroup>();
		mGroup = new CheatViewGroup(null, null, null);
		mStack.push(mGroup);

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean showLogo = pref.getBoolean("show_logo", true);
		mRootView = (ViewGroup) findView(R.id.layout);

		try {
			mCotRes = new CotResource(path);
			Pull pull = mCotRes.getLayoutParser();
			int type;
			while ((type = pull.parser.next()) != 2 && type != 1) {}
			if (type == 2) {
				if (showLogo) {
					String gameName = pull.getValue("name");
					String author = pull.getValue("author");
					InputStream logoStream = mCotRes.open(CotResource.RES_LOGO);
					if (logoStream != null)
						logoBitmap = BitmapFactory.decodeStream(logoStream);
					setLogoBar(logoBitmap, gameName, author);
				}
				rInflate(pull, mRootView, true);
			}
			mCotRes.freeStrings();
			mCotRes.close();
		} catch (Exception e) {
			alert(e);
		}
		//表单部分
		mCotName = FileUtils.getMainName(mCotFile);
		mFormFileFilter = new FileNameFixFilter(mCotName, ".xml");
		/*
		 * 长连接模式
		 * 模拟器 -> CheatActivity -> CotActivity
		 */
		Intent intent = getIntent();
		int option = intent.getIntExtra(INTENT_OPTION, 0);
		mConnectToCheatActivity = hasOption(option, MASK_OPTION_CONNECT_TO_CHAET_ACTIVITY);
		// 加载表单
		if (mIsPlaying)
			onLoadDefault();
	}

	@Override
	public void finish() {
		if (mConnectToCheatActivity)
			backToCheatActivity();
		else 
			super.finish();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		int option = intent.getIntExtra(INTENT_OPTION, 0);
		if (hasOption(option, MASK_OPTION_CLOSE_CONNECT)){
			// 关闭连接并退出
			mConnectToCheatActivity = false;
			exit();
		} else
			super.onNewIntent(intent);
	}
	
	/**
	 * 提交
	 */
	private void submit(){
		if (mIsPlaying) {
			backToPlay();
		} else {
			mPlayingCheats = build();
			if (mConnectToCheatActivity)
				backToCheatActivity();
			else
				exit();
		}
	}
	
	
	private Intent mCheatIntent;
	/**
	 * 返回CheatActivity
	 */
	private void backToCheatActivity(){
		if (mCheatIntent == null){
			mCheatIntent = getToFrontIntent(CheatActivity.class)
					.putExtra(INTENT_OPTION, CheatActivity.MASK_OPTION_COT_RESULT);
		}
        startActivity(mCheatIntent);
		mGroup.reset();
	}
	
	@Override
	protected void onDestroy() {
		try {
			mCotRes.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.cot_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_submit:
				submit();
				return true;
			case R.id.menu_output:
				output();
				return true;
			case R.id.menu_load:
				showLoadFormDialog();
				return true;
			case R.id.menu_save:
				showSaveFormDialog();
				return true;
			case R.id.menu_clear:
				mGroup.reset();
				break;
			case R.id.menu_shortcut:
				createShortCut();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 递归解析布局
	 * @param pull
	 * @param parent
	 * @param addHr
	 * @throws Exception
	 */
	private void rInflate(Pull pull, View parent, boolean addHr) throws Exception {
		final int depth = pull.parser.getDepth();
		boolean rParse = false;
		int type;
		while (((type = pull.parser.next()) != 3 || pull.parser.getDepth() > depth)
			   && type != 1) {
			if (type != 2)
				continue;
			View view = null;
			String tag = pull.parser.getName();
			if (CheatView.WIDGET_SELECT.equals(tag)) {
				CheatSelect select = new CheatSelect(this, pull, mCotRes);
				mGroup.add(select);
				view = select.getView();
			} else if (CheatView.WIDGET_INPUTS.equals(tag)) {
				CheatInputs inputs = mInputs = new CheatInputs(this, pull);
				rInflate(pull, inputs.mDialog.mContainer, false);
				inputs.resetValues();
				view = inputs;
				mGroup.add(inputs);
				mInputs = null;
			} else if (CheatView.WIDGET_INPUT.equals(tag)) {
				if (mInputs != null) {
					view = mInputs.appendInput(pull);
				} else {
					CheatInput input = new CheatInput(this, pull, true);
					view = input;
					mGroup.add(input);
				}
			} else if (CheatView.WIDGET_CHEAT.equals(tag)) {
				CheatItem item = new CheatItem(this, pull);
				mGroup.add(item);
				view = item;
			} else if (CheatView.WIDGET_RADIOS.equals(tag)) {
				CheatRadioGroup item = new CheatRadioGroup(this, pull);
				mGroup.add(item);
				view = item;
			} else if (CheatView.WIDGET_TITLE.equals(tag)) {
				TextView tv = new TextView(this, null,
					android.R.attr.listSeparatorTextViewStyle);
				tv.setText(pull.getValue(CheatView.ATTR_NAME));
				view = tv;
			} else if (CheatView.WIDGET_GROUP.equals(tag)) {
				CheatViewGroup group = new CheatViewGroup(this, pull, mCotRes);
				mGroup.add(group);
				mStack.push(group);
				mGroup = group;
				ViewGroup parentView = (ViewGroup)parent;
				parentView.addView(inflateView(R.layout.hr));
				parentView.addView(group.getView());
				if (group.showInDialog)
					rInflate(pull, group.getLayout(), true);
				else 
					rInflate(pull, parent, addHr);
				//退回上一级
				mStack.pop();
				mGroup = mStack.peek();
			} else if (CheatView.WIDGET_HORIZONTAL.equals(tag)) {
				rParse = true;
				view = new MyLayout(this, MyLayout.HORIZONTAL);
			} else if (CheatView.WIDGET_VERTICAL.equals(tag)) {
				rParse = true;
				view = new MyLayout(this, MyLayout.VERTICAL);
				if ("td".equals(pull.getValue("type"))) {
					view.setLayoutParams(LP.HLine);
				}
			}
			if (view != null) {
				if (rParse)
					rInflate(pull, view, false);
				ViewGroup viewgroup = (ViewGroup)parent;
				if (addHr) 
					viewgroup.addView(inflateView(R.layout.hr));
				viewgroup.addView(view);
				onAddWidget(view);
			}
		}
	}
	
	/**
	 * 添加长按监听
	 * @param view
	 */
	private void onAddWidget(View view){
		if (view instanceof CheatView)
			view.setOnLongClickListener(this);
	}
	
	/**
	 * 显示帮助详情
	 * @param v
	 * @return
	 */
	@Override
	public boolean onLongClick(View view) {
		if (view instanceof CheatView && ((CheatView)view).getIntro() != null){
			showIntro((CheatView)view);
			return true;
		}
		return false;
	}
	
	TextDialog mIntroDialog;
	
	/**
	 * 显示帮助详情
	 * @param view
	 */
	private void showIntro(CheatView view){
		TextDialog dialog = mIntroDialog;
		if (dialog == null){
			mIntroDialog = dialog = new TextDialog(this, 0);
			dialog.setButton(Dialog.BUTTON_POSITIVE, R.string.isee);
		}
		dialog.setTitle(view.getTitle());
		dialog.setMessage(view.getIntro());
		dialog.show();
	}

	private void setLogoBar(Bitmap logo, String gameName, String author) {
		LinearLayout logoBar = mLogoBar = (LinearLayout) inflateView(R.layout.activity_cot_logobar);
		ImageView img = (ImageView) logoBar.findViewById(R.id.ss0Img);
		TextView title = (TextView) logoBar.findViewById(R.id.gameName);
		TextView authorV = (TextView) logoBar.findViewById(R.id.author);
		if (logo != null) {
			img.setImageBitmap(logo);
			Point size = getSize();
			if (size.x > logo.getWidth() * 2)
				logoBar.setOrientation(0);
		} else {
			logoBar.removeView(img);
		}
		title.setText(gameName);
		if (author != null && !author.isEmpty())
			authorV.setText("模板制作：" + author);
		else 
			logoBar.removeView(authorV);
		mRootView.addView(logoBar);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (logoBitmap == null)
			return;
		if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
				|| newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
			Point size = getSize();
			mLogoBar.setOrientation(size.x > logoBitmap.getWidth() * 2 ? 0 : 1);
		}
	}
	
	/**
	 * 预览
	 */
	private void output(){
		String text = build().toString();
		if (!text.isEmpty()) {
			EditDialog outputDialog = new EditDialog(this, EditDialog.MODE_SHOW);
			outputDialog.setCopy();
			outputDialog.setTitle("导出文本");
			outputDialog.setMessage(text);
			outputDialog.show();
		} else 
			toast("空");
	}

	@Override
	protected void backToPlay() {
		build().chkAll(true);
		super.backToPlay();
	}

	private Cheats build() {
		Cheats cheats;
		if (mIsPlaying) {
			cheats = mPlayingCheats;
			cheats.clear();
		} else 
			cheats = new Cheats();
		mGroup.output(null, cheats);
		return cheats;
	}
	
	private void refreshFormFileList(){
		String[] list = mCotFile.getParentFile().list(mFormFileFilter);
		for (int i = 0; i < list.length; i++) 
			list[i] = getFormFileKey(list[i]);
		mFormNameList = list;
	}
	
	private ListDialog mSaveFormDialog;
	private ListDialog mLoadFormDialog;
	private TextView formFilenameView;
	private String[] mFormNameList;

	/**
	 * 表单文件名
	 * @param name
	 * @return
	 */
	private String getFormFileKey(String name){
		return FileUtils.getMainName(name).substring(mCotName.length() + 1);
	}
	
	/**
	 * 获取表单文件
	 * @param key
	 * @return
	 */
	private File getFormFile(CharSequence key){
		return new File(mCotFile.getParentFile(), mCotName + "_" + key + ".xml");
	}
	
	/**
	 * 默认表单文件
	 * @return
	 */
	private File getDefaultFormFile(){
		return getFormFile("默认");
	}

	@Override
	public boolean onClick(Dialog dialog, int which) {
		if (dialog == mSaveFormDialog) {
			if (which == Dialog.BUTTON_POSITIVE){
				return onSaveForm();
			} else if(which > -1){
				formFilenameView.setText(mFormNameList[which]);
				return false;
			}
		}
		return super.onClick(dialog, which);
	}

	/**
	 * 输入框提交事件
	 */
	@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (v == formFilenameView){
			if (onSaveForm())
				mSaveFormDialog.dismiss();
		}
		return true;
	}

	@Override
	public void onSubmit(ListViewController controller, int[] checkedIndexs) {
		if (controller.mDialog == mLoadFormDialog){
			loadForm(getFormFile(mFormNameList[checkedIndexs[0]]));
		}
	}

	/**
	 * 显示保存表单对话框
	 */
	private void showSaveFormDialog(){
		ListDialog dialog = mSaveFormDialog;
		if (dialog == null){
			mSaveFormDialog = dialog = new ListDialog(this);
			dialog.setTitle(R.string.saveForm);
			dialog.setConfirm();
			dialog.setOnClickListener(this);
			dialog.addHeader(R.layout.activity_cot_save_form_header);
			formFilenameView = (TextView)dialog.findView(R.id.nameView);
			ViewUtils.setOnDown(formFilenameView, this);
		}
		refreshFormFileList();
		dialog.setItems(mFormNameList, this);
		dialog.show();
	}
	
	/**
	 * 保存表单
	 * @return
	 */
	private boolean onSaveForm(){
		String name = formFilenameView.getText().toString();
		if (name.isEmpty()) {
			toast(R.string.emptyFilename);
			return false;
		} else 
			saveForm(getFormFile(name));
		return true;
	}

	/**
	 * 显示加载表单对话框
	 */
	private void showLoadFormDialog(){
		ListDialog dialog = mLoadFormDialog;
		if (dialog == null){
			mLoadFormDialog = dialog = new ListDialog(this);
			dialog.setTitle(R.string.loadForm);
			dialog.setOnSubmitListener(this);
		}
		refreshFormFileList();
		if (mFormNameList.length == 0) {
			toast(R.string.noForm);
		} else {
			dialog.setItems(mFormNameList, -1, null, false);
			dialog.show();
		}
	}

	/**
	 * 加载表单
	 */
	private void loadForm(File file){
		try {
			Pull pull = new Pull();
			pull.start(file);
			int type;
			while ((type = pull.parser.next()) != 1) {
				if (type != 2) continue;
				if (CheatView.TAG_DATA.equals(pull.parser.getName())) {
					String name = pull.getValue(CheatView.ATTR_NAME);
					CheatView cheatView = null;
					//通过名称的哈希值来查找控件
					View view = mRootView.findViewById(name.hashCode());
					if (view == null)
						break;
					if (view instanceof Select)
						cheatView = (CheatView) view.getTag();
					else 
						cheatView = (CheatView) view;
					cheatView.loadForm(pull);
				}
			}
		} catch (Exception e){}
	}

	/**
	 * 保存表单文件
	 */
	private void saveForm(File file) {
		XmlWriter writer = new XmlWriter();
		writer.start(CheatView.TAG_ROOT);
		mGroup.saveForm(writer);
		writer.end();
		writer.write(file);
		toast(getString(R.string.saveSucceed, file.getName()));
	}

	/**
	 * 保存默认表单文件
	 */
	@Override
	protected void onSaveDefault() {
		saveForm(getDefaultFormFile());
	}
	
	/**
	 * 加载默认表单文件
	 */
	private void onLoadDefault() {
		loadForm(getDefaultFormFile());
	}
	
	/**
	 * 创建快捷方式
	 */
	public void createShortCut(){
		Shortcut shortcut = new Shortcut()
			.setTitle(mCotFile.getName())
			.setIcon(logoBitmap)
			.setIntent(new Intent(this, getClass()).putExtra("path", mCotFile.getPath()));
		sendBroadcast(shortcut);
	}
}

