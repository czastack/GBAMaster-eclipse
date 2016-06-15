package cza.gbamaster;

import java.io.File;
import java.util.regex.Pattern;

import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import cza.app.BaseContextMenu;
import cza.app.Dialog;
import cza.app.EditDialog;
import cza.app.ListContextMenu;
import cza.app.TextDialog;
import cza.file.FileUtils;
import cza.hack.Cheat;
import cza.hack.Cheats;
import cza.hack.Emu;
import cza.hack.GameBoid;
import cza.hack.MyBoy;
import cza.util.ViewUtils;
import cza.widget.ItemMoveInsetDialog;
import cza.widget.ListViewCheckControler;
import cza.widget.MyAdapter;

public class CheatActivity extends CheatHandler implements 
		MyAdapter.Helper,
		View.OnClickListener,
		AdapterView.OnItemClickListener,
		ListViewCheckControler.Callback, 
		BaseContextMenu.Callback,
		ActionMode.Callback,
		SearchView.OnQueryTextListener,
		SearchView.OnCloseListener,
		Runnable {
	private File mChtFile;
	private String mGameName;
	private Cheats mCheats;
	private Cheats mDisplayCheats;
	private Emu emulator, 
	gameboid = new GameBoid(), 
	myboy = new MyBoy();
	private ListView mListView;
	private MyAdapter mAdapter;
	private TextView mulChkState;
	private SearchView mSearchView;
	private boolean editing;
	private boolean isSearching;
	private boolean mConnectToCot;
	private static final int 
	REQUEST_SAVEAS = 0,
	REQUEST_NEWCHEAT = 1,
	REQUEST_OUTPUTSAVE = 2;
	
	public final static int MASK_OPTION_COT_RESULT = 2;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		String path = initPath();
		if (path == null)
			return;
		setContentView(R.layout.activity_cheat);
		mListView = (ListView) findViewById(R.id.list);
		new ListViewCheckControler(mListView, this);
		mulChkState = (TextView) findViewById(R.id.mulChkState);
		findView(R.id.checkAll).setOnClickListener(this);
		findView(R.id.checkNo).setOnClickListener(this);
		mAdapter = new MyAdapter();
		mAdapter.setHelper(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		new CheatMenu(mListView);
		//搜索栏
		mSearchView = (SearchView)findView(R.id.searchView);
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setOnCloseListener(this);
		
		Intent intent = getIntent();
		mChtFile = new File(path);
		mGameName = FileUtils.getMainName(path);
		//判断作弊码格式对应的模拟器
		boolean isMyBoy = intent.hasExtra(MyBoy.KEY) ? 
			intent.getBooleanExtra(MyBoy.KEY, false) : MyBoy.testCht(mChtFile);
		emulator = isMyBoy ? myboy : gameboid;
		if (mIsPlaying)
			mCheats = mPlayingCheats;
		else if (!open(mChtFile)){
			//如果传入的path无效，就实例化一个空的Cheats
			mCheats = new Cheats();
		}
		mDisplayCheats = mCheats;
		mDisplayCheats.count(); //统计代码数
		onDataChange();
	}
	
	@Override
	protected void exit() {
		closeCotConnect();
		super.exit();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		int option = intent.getIntExtra(INTENT_OPTION, 0);
		if (hasOption(option, MASK_OPTION_COT_RESULT)){
			// 长连接模式下返回模板结果
			onCotResultCot();
		} else
			super.onNewIntent(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.cheat_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_save:
				saveCht(mChtFile);
				return true;
			case R.id.menu_saveAs:
				saveChtAs();
				return true;
			case R.id.menu_inputcode:
				showNewCheatDialog();
				return true;
			case R.id.menu_opencot:
				showCotSelector();
				return true;
			case R.id.menu_unbindcot:
				initCotSelector().unbind();
				return true;
			case R.id.menu_cot_long_connect:
				item.setChecked(triggerCotConnect());
				return true;
			case R.id.menu_clear:
				showClearCheatDialog();
				return true;
			case R.id.menu_output:
				showOutPut();
				return true;
			case R.id.menu_edit:
				startActionMode(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 保存修改对话框
	 */
	protected void showSaveChangeDialog(){
		if (!isSearching())
			super.showSaveChangeDialog();
	}
	
	@Override
	protected void onDestroy() {
		if (mCotSelector != null)
			mCotSelector.writePref();
		super.onDestroy();
	}

	//点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.checkAll:
				onCheckAll(true);
				break;
			case R.id.checkNo:
				onCheckAll(false);
				break;
			case R.id.top:
				inputcode_index.setText("0");
				break;
			case R.id.bottom:
				inputcode_index.setText(String.valueOf(mCheats.size()));
				break;
		}
	}
	private TextDialog clearCheatDialog;

	/**
	 * 清空作弊确认
	 */
	private void showClearCheatDialog(){
		TextDialog dialog = clearCheatDialog;
		if (dialog == null){
			dialog = clearCheatDialog = new TextDialog(this, 0);
			dialog.setConfirm();
			dialog.setTitle("确认清空");
			dialog.setMessage("您确认要清空所有的作弊码么？");
			dialog.setOnClickListener(this);
		}
		dialog.show();
	}

	@Override
	protected void onSaveDefault() {
		saveCht(mChtFile);
	}
	
	/**
	 * 对话框确认事件
	 */
	@Override
	public boolean onClick(Dialog dialog, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			if (newCheatDialog == dialog) 
				onCreateNewCheat();
			else if (clearCheatDialog == dialog)
				mCheats.clear();
			else if (deleteConfirm == dialog)
				onDelete();
			else if (newCheatDialog == dialog)
				onCreateNewCheat();
			else 
				return super.onClick(dialog, which);
			changed();
			return true;
		}
		else 
			return super.onClick(dialog, which);
	}
	
	/**
	 * 多选
	 */
	private void onCheckAll(boolean checked){
		if (editing){
			mDisplayCheats.selectAll(checked);
			if (isSearching)
				mCheats.countSelected();
		} else {
			mDisplayCheats.chkAll(checked);
			if (isSearching)
				mCheats.count();
			mChanged = true;
		}
		onDataChange();
	}
	
	@Override
	public void onItemCheck(int start, int end) {
		if (editing){
			mDisplayCheats.mulSelect(start, end);
		} else {
			mDisplayCheats.mulChk(start, end);
			mChanged = true;
		}
		onDataChange();
	}

	@Override
	public void onItemClick(AdapterView<?> apt, View v, int position, long id) {
		if (isSearching)
			position = mCheats.indexOf(mDisplayCheats.get(position));
		if (editing){
			mCheats.selectAt(position);
		} else {
			mCheats.chkAt(position);
			mChanged = true;
		}
		onDataChange(position);
	}

	/**
	 * 刷新数据显示
	 */
	private void onDataChange(){
		mAdapter.notifyDataSetChanged();
		refreshState();
	}

	/**
	 * 刷新单项数据显示
	 */
	private void onDataChange(int position){
		mAdapter.updateView(position, mListView);;
		refreshState();
	}
	
	private void refreshState(){
		mulChkState.setText(editing ? 
			"选中 " + mCheats.getSelectedState() : 
			"开启 " + mCheats.getState());
	}

	private void changed(){
		onDataChange();
		mChanged = true;
	}

	@Override
	public boolean onShowMenu(BaseContextMenu d) {
		return true;
	}


	/**
	 * 搜索
	 */
	private boolean isSearching(){
		if (isSearching) {
			toast(R.string.unsupportedInSearch);
			return true;
		} else 
			return false;
	}
	
	@Override
	public boolean onClose() {
		if (isSearching){
			isSearching = false;
			mDisplayCheats = mCheats;
		}
		return false;
	}
	
	@Override
	public boolean onQueryTextSubmit(String text) {
		if (!isSearching){
			isSearching = true;
			//清除原选中
			if (editing && mCheats.selectedCount != 0)
				mCheats.selectAll(false);
		}
		Pattern regex = Pattern.compile(text);
		Cheats tempCheats = new Cheats();
		for (Cheat cheat : mCheats){
			if (regex.matcher(cheat.name).find())
				tempCheats.add(cheat);
		}
		mDisplayCheats = tempCheats;
		onDataChange();
		return true;
	}

	@Override
	public boolean onQueryTextChange(String text) {
		return false;
	}
	
	/**
	 * 批量操作
	 */
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		editing = true;
		mode.getMenuInflater().inflate(R.menu.cheat_activity_edit, menu);
		refreshState();
		return true;
	}

	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_selectOn:
				if (!isSearching()) {
					//同步
					mDisplayCheats.selectChecked(true);
					onDataChange();
				}
				break;
			case R.id.menu_checkInverse:
				mDisplayCheats.selectAll();
				if (isSearching)
					mCheats.countSelected();
				onDataChange();
				break;
			case R.id.menu_turnOnSelected:
				mCheats.checkSelected(true);
				changed();
				break;
			case R.id.menu_turnOffSelected:
				mCheats.checkSelected(false);
				changed();
				break;
			case R.id.menu_delete:
				if (hasSelected())
					showDeleteConfirm();
				break;
			case R.id.menu_output:
				if (hasSelected())
					showOutPut();
				break;
		}
		return true;
	}

	public void onDestroyActionMode(ActionMode mode) {
		onCheckAll(false);
		mListView.post(this);
	}

	@Override
	public void run() {
		editing = false;
		refreshState();
	}
	
	/**
	 * 删除作弊码确认
	 */
	private TextDialog deleteConfirm;
	
	private void showDeleteConfirm(){
		TextDialog dialog = deleteConfirm;
		if (dialog == null){
			deleteConfirm = dialog = new TextDialog(this, 0);
			dialog.setConfirm();
			dialog.setTitle("确认删除");
			dialog.setOnClickListener(this);
		}
		dialog.setMessage(getString(R.string.deleteConfirm, mCheats.selectedCount));
		dialog.show();
	}
	
	private void onDelete(){
		mCheats.removeSelected(true);
		if (isSearching)
			mDisplayCheats.removeSelected(true);
	}
	
	private boolean hasSelected(){
		boolean has = mCheats.selectedCount != 0;
		if (!has){
			toast(R.string.error);
		}
		return has;
	}
	
	/**
	 * 输入代码
	 */
	private EditDialog newCheatDialog;
	private EditText inputcode_index;
	
	private void showNewCheatDialog(){
		if (isSearching())
			return;
		EditDialog dialog = newCheatDialog;
		if (dialog == null){
			dialog = newCheatDialog = new EditDialog(this, EditDialog.MODE_INPUT);
			dialog.setTitle(R.string.newCheat);
			dialog.addHeader(R.layout.dialog_newcheat);
			dialog.setOnClickListener(this);
			inputcode_index = (EditText)dialog.findView(R.id.input_index);
			dialog.findView(R.id.top).setOnClickListener(this);
			ViewUtils.registerClick(dialog.getMainView(), this,
				R.id.top, R.id.bottom);
		}
		inputcode_index.getText().clear();
		dialog.textarea.getText().clear();
		dialog.textarea.requestFocus();
		dialog.show();
	}
	
	private void onCreateNewCheat(){
		String text = newCheatDialog.getText();
		int index = 0;
		String indexStr = inputcode_index.getText().toString();
		if (indexStr.isEmpty())
			index = -1;
		else 
			index = Integer.parseInt(indexStr);
		mCheats.addAll(index, text);
	}
	
	
	/**
	 * 模板
	 */

	private CotSelector mCotSelector;
	private Intent mCotConnectedIntent;

	/**
	 * 初始化模板选择器
	 * @return
	 */
	private CotSelector initCotSelector() {
		CotSelector selector = mCotSelector;
		if (selector == null) {
			selector = mCotSelector = new CotSelector(this, mGameName);
			selector.setCallback(this);
		}
		return selector;
	}
	
	/**
	 * 打开模板对话框
	 */
	private void showCotSelector() {
		if (isSearching())
			return;
		CotSelector selector = initCotSelector();
		if (selector.mCotName != null) {
			openCot();
			return;
		}
		selector.show();
	}
	
	/**
	 * 打开模板
	 * @see #onActivityResult
	 */
	private void openCot(){
		if (mConnectToCot){
			// 长连接模式
			if (mCotConnectedIntent == null){
				mCotConnectedIntent = getToFrontIntent(CotActivity.class)
						.putExtra(INTENT_OPTION, CotActivity.MASK_OPTION_CONNECT_TO_CHAET_ACTIVITY)
						.putExtra(INTENT_PATH, mCotSelector.getCotFile().getPath());
			}
	        startActivity(mCotConnectedIntent);
		} else {
			// 非长连接模式
			Intent intent = new Intent(this, CotActivity.class);
			intent.putExtra(INTENT_PATH, mCotSelector.getCotFile().getPath());
			startActivityForResult(intent, REQUEST_NEWCHEAT);
		}
	}
	
	/**
	 * 关闭模板长连接
	 */
	private void closeCotConnect(){
		if (mCotConnectedIntent != null){
			mCotConnectedIntent.putExtra(INTENT_OPTION, CotActivity.MASK_OPTION_CLOSE_CONNECT);
			startActivity(mCotConnectedIntent);
			mCotConnectedIntent = null;
		}
	}
	
	/**
	 * 切换目标长连接状态
	 * @return
	 */
	private boolean triggerCotConnect(){
		if (mConnectToCot)
			closeCotConnect();
		return mConnectToCot = !mConnectToCot;
	}
	
	/**
	 * 模板返回结果
	 */
	private void onCotResultCot(){
		if(mPlayingCheats != null && mPlayingCheats != mCheats) {
			// 非长连接模式
			mCheats.addAll(mPlayingCheats);
			changed();
			mPlayingCheats = mCheats;
		}
	}

	
	/**
	 * 列表对话框点击事件
	 */
	@Override
	public void onItemClick(BaseContextMenu dialog, int position) {
		if (dialog == mCotSelector)
			openCot();
		else if (dialog == mOutputCheatDialog){
			output_type = position;
			if (output_typeChooser.getCheckedRadioButtonId() == R.id.saveFile){
				Intent intent = new Intent(this, FileActivity.class)
					.putExtra(INTENT_MODE, FileActivity.MODE_SAVEAS)
					.putExtra(INTENT_TITLE,"保存文件")
					.putExtra("saveAsName", FileUtils.getMainName(mChtFile) + ".txt");
				startActivityForResult(intent, REQUEST_OUTPUTSAVE);
			} else {
				EditDialog outputDialog = new EditDialog(this, EditDialog.MODE_SHOW);
				outputDialog.setCopy();
				outputDialog.setTitle(dialog.getText(position));
				outputDialog.setMessage(buildOutput());
				outputDialog.show();
			}
		}
	}

	
	/**
	 * 输出
	 */
	private BaseContextMenu mOutputCheatDialog;
	private RadioGroup output_typeChooser;
	private int output_type;

	private void showOutPut(){
		BaseContextMenu dialog = mOutputCheatDialog;
		if (dialog == null){
			dialog = mOutputCheatDialog = new BaseContextMenu(this);
			dialog.setTitle(R.string.output);
			View view = inflateView(R.layout.dialog_output_cheat);
			((ViewGroup)dialog.findView(R.id.layout)).addView(view, 0);
			output_typeChooser = (RadioGroup) view.findViewById(R.id.typeSwitch);
			dialog.setList(R.array.outputCheatAs);
			dialog.setCallback(this);
		}
		dialog.onLongClick();
	}

	private String buildOutput(){
		Cheats selectedCheats = mCheats.filtSelected();
		if (output_type == 0){
			return selectedCheats.toString();
		} else {
			return (output_type == 1 ? gameboid : myboy)
				.writeCht(selectedCheats).toString();
		}
	}
	
	/**
	 * cht文件读写
	 */
	private boolean open(File cht){
		try {
			mCheats = emulator.parseCht(cht);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 保存作弊文件
	 * @param cht
	 */
	private void saveCht(File cht){
		emulator.writeCht(mCheats, cht);
		toast("成功保存 " + cht.getPath());
	}

	/**
	 * 作弊文件另存为
	 */
	private void saveChtAs(){
		Intent intent = new Intent(this, FileActivity.class)
			.putExtra("mode", FileActivity.MODE_SAVEAS)
			.putExtra("title","保存cht文件")
			.putExtra("path", mChtFile.getParent())
			.putExtra("saveAsName", mChtFile.getName());
		startActivityForResult(intent, REQUEST_SAVEAS);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) return;
		switch (requestCode) {
			case REQUEST_SAVEAS:
				saveCht(new File(intent.getStringExtra("path")));
				break;
			case REQUEST_NEWCHEAT:
				onCotResultCot();
				break;
			case REQUEST_OUTPUTSAVE:
				FileUtils.writeStringToFile(buildOutput(), 
					new File(intent.getStringExtra("path")));
				break;
		}
	}

	/*
	 * 填充数据
	 */
	@Override
	public int getCount() {
		return mDisplayCheats != null ? mDisplayCheats.size() : 0;
	}

	@Override
	public View getView(int position, View item) {
		Holder holder;
		if (item == null) {
			holder = new Holder();
			item = inflateView(R.layout.cheat_item);
			holder.findView(item);
			item.setTag(holder);
		} else {
			holder = (Holder) item.getTag();
		}
		holder.set(mDisplayCheats.get(position));
		return item;
	}

	private class Holder {
		private TextView name;
		private CheckBox chkbox;

		private void findView(View item){
			name = (TextView) item.findViewById(R.id.cheat_name);
			chkbox = (CheckBox) item.findViewById(R.id.cheat_chk);
			ViewUtils.setColorList(name);
		}

		void set(Cheat cheat){
			if (editing){
				name.setSelected(cheat.selected);
			}
			name.setText(cheat.name);
			chkbox.setChecked(cheat.checked);
		}
	}


	/**
	 * 作弊码长按菜单
	 */
	private class CheatMenu extends ListContextMenu implements 
			BaseContextMenu.Callback, 
			Dialog.OnClickListener, 
			ItemMoveInsetDialog.Callback {

		private Cheat mCheat;

		CheatMenu(ListView li) {
			super(li);
			setTitle(R.string.cheatCode);
			setList(R.array.menu_cheat);
			setCallback(this);
		}

		@Override
		public void onItemClick(BaseContextMenu d, int index) {
			switch (index) {
				case 0:
					showEditor();
					break;
				case 1:
					showMove();
					break;
				case 2:
					mCheats.mRemove(mCheat);
					if (isSearching)
						mDisplayCheats.remove(mIndex);
					changed();
					break;
			}
		}
		
		@Override
		public boolean onShowMenu(BaseContextMenu d){
			if (editing){
				return false;
			} else {
				mCheat = mDisplayCheats.get(mIndex);
				return true;
			}
		}

		
		/**
		 * 修改作弊码
		 */
		private EditDialog mCheatEditDialog;

		private void showEditor() {
			EditDialog dialog = mCheatEditDialog;
			if (dialog == null) {
				mCheatEditDialog = dialog = new EditDialog(getContext(), EditDialog.MODE_SHOW);
				dialog.setTitle("编辑作弊码");
				dialog.setCopy();
				dialog.setConfirm();
				dialog.setOnClickListener(this);
			}
			dialog.setMessage(mCheat.toString());
			dialog.show();
		}

		/**
		 * 替换
		 */
		private boolean onReplaceCheat(){
			String text = mCheatEditDialog.getText().trim();
			if (text.isEmpty()){
				toast(R.string.error);
				return false;
			}
			mCheats.replaceAt(mIndex, text, !isSearching);
			mCheat = null;
			changed();
			return true;
		}
		
		@Override
		public boolean onClick(Dialog dialog, int which) {
			if (which == BUTTON_POSITIVE)
				return onReplaceCheat();
			return true;
		}
		

		/**
		 * 更改顺序
		 */
		private ItemMoveInsetDialog mMoveDialog;

		private void showMove() {
			if (isSearching())
				return;
			ItemMoveInsetDialog dialog = mMoveDialog;
			if (dialog == null) {
				dialog = mMoveDialog = new ItemMoveInsetDialog(getContext(), this);
				ViewUtils.insertAfter(mListView, dialog.getView());
			}
			dialog.setIndex(mIndex);
			dialog.show();
		}

		@Override
		public boolean tryIndex(int from, int index) {
			if (mCheats.moveTo(from, index)) {
				mIndex = index;
				changed();
				if (index < mListView.getFirstVisiblePosition() ||
					index > mListView.getLastVisiblePosition()) {
					mListView.setSelection(index);
				}
				return true;
			}
			return false;
		}

		@Override
		public int getMax() {
			return mCheats.size() - 1;
		}
	}
}


