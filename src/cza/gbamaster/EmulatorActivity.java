package cza.gbamaster;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import cza.app.BaseContextMenu;
import cza.app.Dialog;
import cza.app.FileCoverConfirm;
import cza.app.FileDeleteConfirm;
import cza.app.Shortcut;
import cza.file.FileUtils;
import cza.hack.Cheats;
import cza.hack.ChtInfo;
import cza.hack.ChtInfos;
import cza.hack.Emu;
import cza.hack.Game;
import cza.hack.GameBoid;
import cza.hack.MyBoy;
import cza.util.ViewUtils;
import cza.widget.MyAdapter;

public class EmulatorActivity extends ClickActivity implements 
		RadioGroup.OnCheckedChangeListener,
		BaseContextMenu.Callback,
		FileDeleteConfirm.Callback {

	private static final int REQUEST_SETSAV = 0;
	private static final int REQUEST_SETCHT = 1;
	
	private Game mGame;
	private boolean isMyBoy, isCht;
	private Emu gameboid, myboy, mEmulator;
	//控件
	private TextView nameView, sizeView, pathView;
	private RadioGroup mEmulatorSwitch, mCheatTypeView;
	private ImageView imgView;
	private TextView savTimeView;
	private View mChtBar, mCotBar;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Game game = ((MyApplication)getApplication()).mGame;
		mGame = game;
		if (game == null && (game = getGame(getIntent())) == null){
			toast(R.string.notRunning);
			finish();
			return;
		}
		
		SharedPreferences pref = getSharedPreferences("game", MODE_PRIVATE);
		isMyBoy = pref.getBoolean(MyBoy.KEY, false);
		isCht = pref.getBoolean("isCht", true);
		gameboid = new GameBoid();
		myboy = new MyBoy();

		//初始化控件
		setContentView(R.layout.activity_emulator);
		nameView = (TextView) findViewById(R.id.gameName);
		sizeView = (TextView) findViewById(R.id.gameSize);
		pathView = (TextView) findViewById(R.id.gamePath);
		mEmulatorSwitch = (RadioGroup) findViewById(R.id.emulators);
		mEmulatorSwitch.setOnCheckedChangeListener(this);
		imgView = (ImageView) findViewById(R.id.ss0Img);
		mCheatTypeView = (RadioGroup) findViewById(R.id.cheatTypeView);
		mCheatTypeView.setOnCheckedChangeListener(this);
		savTimeView = (TextView) findViewById(R.id.ss0Time);
		mChtBar = findView(R.id.chtBar);
		mCotBar = findView(R.id.cotBar);
		registerClick(R.id.btn_createCht, R.id.btn_cht_edit, R.id.btn_cht_option,
				R.id.btn_preview, R.id.btn_browser);
		
		//载入数据
		loadGame(game);
		refreshChtState();
		ViewUtils.chkRadioAt(mCheatTypeView, isCht ? 0 : 1);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		loadGame(getGame(intent));
	}
	
	private Game getGame(Intent intent){
		String path = intent.getStringExtra(INTENT_PATH);
		if (path == null){
			Uri data = intent.getData();
			if (data == null) 
				return null;
			else 
				path = data.getPath();
		}
		File gameFile = new File(path);
		if (ensureFile(gameFile))
			return new Game(gameFile);
		return null;
	}
	
	/**
	 * 载入游戏
	 * @param game
	 */
	private void loadGame(Game game){
		if (game == null)
			return;
		mGame = game;
		nameView.setText(game.name);
		sizeView.setText(game.size);
		pathView.setText(game.path);
		gameboid.load(game);
		myboy.load(game);
		ViewUtils.chkRadioAt(mEmulatorSwitch, isMyBoy ? 1 : 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.emulator_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_shortcut:
				createShortCut();
				return true;
			case R.id.menu_runGame:
				play();
				break;
			case R.id.menu_testGame:
				playWithDef();
				break;
			case R.id.menu_setSav:
				pickSav();
				break;
			case R.id.menu_setST:
				showSTEditDialog();
				break;
			case R.id.menu_importCht:
				showImportChtsDialog();
				break;
			case R.id.menu_openDir:
				openGameDir();
				break;
			case R.id.menu_edit:
				goEdit();
				break;
			case R.id.menu_romInfo:
				showRomInfo();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onRestart() {
		//刷新数据
		refreshData();
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		SharedPreferences pref = getSharedPreferences("game", MODE_PRIVATE);
		if (pref != null){
			SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean(MyBoy.KEY, isMyBoy);
			editor.putBoolean("isCht", isCht);
			editor.commit();
		}
		if (mCotSelector != null && mCotSelector.changed)
			mCotSelector.writePref();
		//防止重复
		((MyApplication)getApplication()).mGame = null;
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) return;
		String path = intent.getStringExtra(INTENT_PATH);
		switch (requestCode) {
			case REQUEST_SETSAV:
				setSav(path);
				break;
			case REQUEST_SETCHT:
				setCht(path);
				break;
		}
	}
	
	/**
	 * 按钮点击事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_createCht:
				createCht();
				break;
			case R.id.btn_cht_edit:
				goCheatEdit();
				break;
			case R.id.btn_cht_option:
				showChtMenu();
				break;
			case R.id.btn_preview:
				previewCot();
				break;
			case R.id.btn_browser:
				mCotSelector.show();
				break;
		}
	}

	/**
	 * 切换Radio监听
	 */
	@Override
	public void onCheckedChanged(RadioGroup v, int id) {
		if (mEmulatorSwitch == v) {
			// 切换模拟器监听
			if (R.id.gameboid == id){
				isMyBoy = false;
				mEmulator = gameboid;
			} else {
				isMyBoy = true;
				mEmulator = myboy;
			}
			mCht = mEmulator.getChtFile();
			changeData();
			refreshChtState();
		} else {
			// 切换底部工具栏
			isCht = R.id.chtRadio == id;
			ViewUtils.hide(mChtBar, !isCht);
			ViewUtils.hide(mCotBar, isCht);
			if (!isCht)
				initCotSelector();
		}
	}

	/**
	 * 模拟器是否安装
	 */
	private boolean isInstalled(Emu emulator) {
		try {
			return null != getPackageManager().getPackageInfo(emulator.packageName, 0);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 用所选模拟器运行游戏
	 */
	private void play(){
		if (isInstalled(mEmulator)){
			startActivity(mEmulator.getOpenRomIntent());
		} else {
			toast(getString(R.string.uninstalled, mEmulator.NAME));
		}
	}

	/**
	 * 调试游戏
	 */
	private void playWithDef(){
		if (isInstalled(gameboid)) {
			boolean withCht = isCht || mCotSelector == null || mCotSelector.mCotName == null;
			Intent intent = getToFrontIntent(PlayActivity.class)
				.setData(Uri.fromFile(mEmulator.gameFile));
			if (!withCht) 
				intent.putExtra("cotPath", mCotSelector.getCotFile().getPath());
			startActivity(intent);
		} else 
			toast(getString(R.string.uninstalled, gameboid.NAME));
	}

	/**
	 * 刷新数据(包括快捷存档信息)
	 */
	private void refreshData(){
		if (mEmulator.st0 != null)
			mEmulator.st0.recycle();
		mEmulator.st0 = null;
		changeData();
	}

	/**
	 * 刷新数据
	 */
	private void changeData(){
		mEmulator.chkST0();
		imgView.setImageBitmap(mEmulator.st0.bp);
		savTimeView.setText(mEmulator.st0.timeStr);
	}

	/**
	 * 创建快捷方式
	 */
	public void createShortCut(){
		Shortcut shortcut = new Shortcut()
			.setTitle(mGame.name)
			.setIcon(mEmulator.st0.bp)
			.setIntent(new Intent(this, getClass()).putExtra(INTENT_PATH, mGame.path));
		sendBroadcast(shortcut);
	}

	@Override
	public void onDelete(FileDeleteConfirm dialog, File file) {
		refreshChtState();
	}
	
	private void refreshChtState(){
		boolean hasCht = mCht.exists();
		ViewUtils.hide(findView(R.id.btn_createCht), hasCht);
		ViewUtils.hide(findView(R.id.chtBar2), !hasCht);
	}

	/**
	 * cht菜单
	 */
	private File mCht;
	private BaseContextMenu mChtMenu;
	
	private void showChtMenu(){
		BaseContextMenu menu = mChtMenu;
		if (menu == null) {
			menu = mChtMenu = new BaseContextMenu(this);
			menu.setTitle(R.string.chtFile);
			menu.setCallback(this);
			menu.setList(R.array.menu_cht);
		}
		menu.show();
	}

	/**
	 * 创建作弊文件
	 */
	private void createCht(){
		try {
			mCht.createNewFile();
			refreshChtState();
		} catch (Exception e) {}
	}

	/**
	 * 编辑作弊码
	 */
	private void goCheatEdit(){
		startActivity(
			new Intent(this, CheatActivity.class)
			.putExtra(INTENT_PATH, mCht.getPath())
			.putExtra(MyBoy.KEY, isMyBoy));
	}

	/**
	 * 打开文件位置
	 */
	private void openDir(){
		Intent intent = new Intent(EmulatorActivity.this, FileActivity.class)
			.putExtra(INTENT_PATH, mCht.getParent())
			.putExtra(INTENT_CHECKED, mCht.getName())
			.putExtra(INTENT_AUTO_OPEN, true);
		startActivity(intent);
	}
	
	/**
	 * 删除作弊文件
	 */
	private void deleteCht(){
		FileDeleteConfirm dialog = new FileDeleteConfirm(this);
		dialog.setCallback(this);
		dialog.setFile(mCht);
		dialog.setHint();
		dialog.show();
	}
	
	//cht菜单 结束
	
	/**
	 * 选择模板
	 */
	private CotSelector mCotSelector;
	private TextView mCotNameView;

	private void initCotSelector() {
		if (mCotSelector == null) {
			mCotSelector = new CotSelector(this, mGame.name);
			mCotSelector.setCallback(this);
			mCotNameView = (TextView)findView(R.id.cotNameView);
			updateCot();
		}
	}
	
	public void updateCot(){
		mCotNameView.setText(mCotSelector.mCotName);
	}
	
	public void previewCot(){
		if (mCotSelector.isSelected()){
			Intent intent = new Intent(this, CotActivity.class);
			intent.putExtra(INTENT_PATH, mCotSelector.getCotFile().getPath());
			startActivity(intent);
		} else {
			mCotSelector.show();
		}
	}

	/**
	 * 列表对话框
	 */
	@Override
	public boolean onShowMenu(BaseContextMenu dialog) {
		return false;
	}

	@Override
	public void onItemClick(BaseContextMenu dialog, int position) {
		if (mChtMenu == dialog)
		switch (position) {
			case 0:
				goCheatEdit();
				break;
			case 1:
				openDir();
				break;
			case 2:
				deleteCht();
				break;
		} else 
			updateCot();
	}

	/**
	 * 对话框确认事件
	 */
	@Override
	public boolean onClick(Dialog dialog, int which) {
		if (which == Dialog.BUTTON_POSITIVE)
			if (mSTEditDialog == dialog) {
				refreshData();
			} else 
				return super.onClick(dialog, which);
		return true;
	}

	/**
	 * 打开文件浏览选取电池记忆文件
	 */
	private void pickSav(){
		Intent intent = new Intent(this, FileActivity.class)
			.putExtra(INTENT_MODE, FileActivity.MODE_PICKFILE)
			.putExtra(INTENT_TITLE,"选择sav文件")
			.putExtra(INTENT_TYPE, "sav");
		startActivityForResult(intent, REQUEST_SETSAV);
	}


	/**
	 * 设置电池记忆文件
	 */
	private void setSav(String path){
		File from = new File(path);
		File sav = mEmulator.withType("sav");
		if (sav.exists())
			new FileCoverConfirm(this).setFile(from, sav).show();
		else 
			FileUtils.copy(from, sav);
	}

	/**
	 * 打开文件浏览选取作弊文件
	 */
	private void pickCht(){
		Intent intent = new Intent(this, FileActivity.class)
			.putExtra(INTENT_MODE, FileActivity.MODE_PICKFILE)
			.putExtra(INTENT_TITLE,"选择cht文件")
			.putExtra(INTENT_TYPE, "cht");
		startActivityForResult(intent, REQUEST_SETCHT);
	}

	/**
	 * 设置作弊文件
	 */
	private void setCht(String path){
		File cht = new File(path);
		Emu emu = MyBoy.testCht(cht) ? myboy : gameboid;
		importChts.setCht(emu, cht);
	}

	/**
	 * 打开文件夹
	 */
	private void openGameDir(){
		Intent intent = new Intent(this, FileActivity.class)
			.putExtra(INTENT_PATH, mGame.dir)
			.putExtra(INTENT_CHECKED, mGame.fullName);
		startActivity(intent);
	}

	/**
	 * 打开文件夹并选中相关文件
	 */
	private void goEdit(){
		Intent intent = new Intent(this, FileActivity.class)
			.putExtra(INTENT_PATH, mGame.dir)
			.putExtra(INTENT_SCREEN_TEXT, mGame.name)
			.putExtra(INTENT_SCREEN_TYPE, FileActivity.SCREEN_FILE)
			.putExtra(INTENT_AUTO_MENU, true);
		startActivity(intent);
	}

	/**
	 * 即时存档排序
	 */
	private STEditDialog mSTEditDialog;
	
	private void showSTEditDialog(){
		if (mEmulator.getSTCount() == 0){
			toast(R.string.noST);
			return;
		}
		if (mSTEditDialog == null){
			mSTEditDialog = new STEditDialog(this, mEmulator);
			mSTEditDialog.setOnClickListener(this);
		}
		mSTEditDialog.show();
	}

	/**
	 * 显示rom信息
	 */
	private void showRomInfo(){
		String[] array = null;
		try {
			array = mEmulator.getRomInfo();
		} catch (Exception e) {
			return;
		}
		Dialog dialog = new Dialog(this);
		dialog.setTitle(R.string.romInfo);
		dialog.setView(R.layout.activity_emulator_rom_info);
		dialog.setButton(Dialog.FLAG_BUTTON_POSITIVE);
		TextView nameView = (TextView)dialog.findView(R.id.nameView);
		TextView codeView = (TextView)dialog.findView(R.id.codeView);
		TextView makerView = (TextView)dialog.findView(R.id.makerView);
		nameView.setText(array[0]);
		codeView.setText(array[1]);
		makerView.setText(array[2]);
		dialog.show();
	}
	
	/**
	 * 导入作弊文件
	 */
	private ImportChts importChts;
	
	private void showImportChtsDialog(){
		if (importChts == null)
			importChts = new ImportChts(this);
		importChts.show();
	}
	

	/**
	 * 内部类：导入作弊文件
	 */
	private class ImportChts extends BaseContextMenu {
		private Emu from, to;
		private ChtAdapter mAdapter;
		private ChtInfos mInfos;

		public ImportChts(Context c) {
			super(c);
			from = isMyBoy ? gameboid : myboy;
			to = mEmulator;
			setTitle(R.string.importCht);
			setConfirm();
			setButton(BUTTON_POSITIVE, R.string.chooseFile);
			TextView hint = (TextView) addHeader(R.layout.activity_emulator_dialog_importcht);
			hint.setText(c.getString(R.string.importFrom, from.NAME));
			listView.setOnItemClickListener(this);
			mInfos = from.getChtInfos();
			mAdapter = new ChtAdapter();
			mAdapter.setData(mInfos);
			listView.setAdapter(mAdapter);
		}
		
		@Override
		public void onItemClick(AdapterView<?> p1, View p2, int index, long id) {
			this.setCht(from, mInfos.getCht(mInfos.get(index).name));
			dismiss();
		}

		@Override
		public boolean triggerClick(int which) {
			if (which == BUTTON_POSITIVE)
				pickCht();
			return true;
		}
		
		private void setCht(Emu parser, File cht){
			try {
				Cheats data = parser.parseCht(cht);
				to.writeCht(data, to.getChtFile());
			} catch (Exception e) {
				toast(cht.getName() + "导入失败");
			}
		}
	}
	
	
	/**
	 * 内部类：作弊文件列表适配器
	 */
	private class ChtAdapter extends MyAdapter {
		private ChtInfos mInfos;

		public void setData(ChtInfos infos) {
			mInfos = infos;
		}

		public int getCount() {
			return mInfos != null ? mInfos.size() : 0;
		}

		public View getView(int position, View item, ViewGroup parent) {
			Holder holder;
			if (item == null) {
				holder = new Holder();
				item = inflateView(R.layout.chtsel_item);
				holder.findView(item);
				item.setTag(holder);
			} else {
				holder = (Holder) item.getTag();
			}
			holder.set(mInfos.get(position));
			return item;
		}

		private class Holder {
			private TextView name, size, time;
			public void findView(View item) {
				name = (TextView) item.findViewById(R.id.cht_name);
				size = (TextView) item.findViewById(R.id.cht_size);
				time = (TextView) item.findViewById(R.id.cht_time);
			}

			public void set(ChtInfo chtInfo) {
				name.setText(chtInfo.name);
				size.setText(chtInfo.size);
				time.setText(chtInfo.time);
			}
		}
	}
}
