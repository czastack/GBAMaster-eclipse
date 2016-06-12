package cza.gbamaster;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.TreeSet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import cza.app.BaseContextMenu;
import cza.app.Dialog;
import cza.app.ListContextMenu;
import cza.app.RenameDialog;
import cza.file.GBASearcher;
import cza.hack.Game;
import cza.hack.MyBoy;
import cza.util.Ary;
import cza.util.Pull;
import cza.util.ViewUtils;
import cza.util.XmlWriter;
import cza.widget.ItemMoveInsetDialog;
import cza.widget.MyAdapter;

public class MainActivity extends BaseActivity implements 
		AdapterView.OnItemClickListener,
		MyAdapter.Helper,
		BaseContextMenu.Callback,
		RenameDialog.OnSummitListener,
		ItemMoveInsetDialog.Callback, 
		Runnable {

	private MyApplication mApp;
	private MyAdapter mAdapter;
	private ListView mListView;
	private File mDataFile;
	private Ary<Game> mGames;
	private Game mGame;
	private boolean dataChanged;
	private int mIndex = -1;
	private ListContextMenu mListMenu;
	private final static byte REQUEST_ADDFILE = 1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (!ensureFile(MyApplication.SD))
			return;
		mApp = (MyApplication) getApplication();
		mDataFile = mApp.gamesData;
		setContentView(R.layout.activity_main);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setOnItemClickListener(this);
		mAdapter = new MyAdapter();
		mAdapter.setHelper(this);
		mListView.setAdapter(mAdapter);
		mListMenu = new ListContextMenu(mListView);
		mListMenu.setCallback(this);
		mListMenu.setList(R.array.menu_game);
		mGames = new Ary<Game>();
		getData();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.menu_autoSearch){
			showSearchDialog();
			return true;
		} else if (id == R.id.menu_manualAdd){
			addFile();
			return true;
		}
		Intent intent = null;
		switch (id) {
			case R.id.menu_setting:
				intent = new Intent(this, MainPreferenceActivity.class);
				break;
			case R.id.menu_close:
				finish();
				break;
		}
		if (intent != null){
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> adpt, View item, int position, long id) {
		mApp.mGame = mGames.get(mIndex = position);
		Intent intent = new Intent(this, EmulatorActivity.class);
		startActivity(intent);
		onDataChange(false);
	}

	@Override
	public boolean onSummit(RenameDialog dialog, String name) {
		if(name.isEmpty())
			return dialog.error(RenameDialog.ERROR_EMPTY);
		if (gameRenameDialog == dialog)
			renameGame(name);
		return true;
	}

	@Override
	public boolean onShowMenu(BaseContextMenu dialog) {
		mGame = mGames.get(mListMenu.mIndex);
		dialog.setTitle(mGame.name);
		return true;
	}

	/**
	 * 游戏菜单
	 */
	@Override
	public void onItemClick(BaseContextMenu dialog, int position) {
		switch (position) {
			case 0:
				showGameRenameDialog();
				break;
			case 1:
				removeGame();
				break;
			case 2:
				showMoveDialog();
		}
	}

	/**
	 * 相关文件重命名
	 */
	private RenameDialog gameRenameDialog;

	private void showGameRenameDialog() {
		RenameDialog dialog = gameRenameDialog;
		if (dialog == null){
			gameRenameDialog = dialog = new RenameDialog(this);
			dialog.setOnSummitListener(this);
		}
		String name = mGame.name;
		dialog.pre(name);
		dialog.show();
	}

	private void renameGame(String name){
		File[] files = new MyBoy(mGame).getRelativeFiles();
		int start = mGame.name.length();
		for (File from : files){
			File to = new File(from.getParent(), name + from.getName().substring(start));
			from.renameTo(to);
		}
		mGame.changeName(name);
		onDataChange(true);
	}
	
	/**
	 * 从列表中移除
	 */
	private void removeGame(){
		mGames.remove(mListMenu.mIndex);
		onDataChange(true);
	}
	
	/**
	 * 更改顺序
	 */
	private ItemMoveInsetDialog mMoveDialog;

	private void showMoveDialog() {
		if (mMoveDialog == null) {
			mMoveDialog = new ItemMoveInsetDialog(this, this);
			ViewUtils.insertAfter(mListView, mMoveDialog.getView());
		}
		mIndex = mListMenu.mIndex;
		onDataChange(false);
		mMoveDialog.setIndex(mIndex);
		mMoveDialog.show();
	}

	@Override
	public boolean tryIndex(int from, int to) {
		if (mGames.moveTo(from, to)) {
			mIndex = to;
			onDataChange(true);
			if (to < mListView.getFirstVisiblePosition() ||
				to > mListView.getLastVisiblePosition()){
				mListView.setSelection(to);
			}
			return true;
		}
		return false;
	}

	@Override
	public int getMax() {
		return mGames.size() - 1;
	}
	
	
	/*
	 * 搜索游戏
	 */
	private Dialog mSearchDialog;
	private CheckBox mKeepOrderView;
	private TextView mSearchLevelView;
	private TextView mSearchCountView;
	private int mSearchState;
	
	private void showSearchDialog(){
		Dialog dialog = mSearchDialog;
		if (dialog == null) {
			mSearchDialog = dialog = new Dialog(this);
			dialog.setConfirm();
			dialog.setView(R.layout.activity_main_search_dialog);
			dialog.setOnClickListener(this);
			mKeepOrderView = (CheckBox)dialog.findView(R.id.keepOrderView);
			mSearchLevelView = (TextView)dialog.findView(R.id.searchLevelView);
			mSearchCountView = (TextView)dialog.findView(R.id.searchCountView);
			ViewUtils.clearAutoFocus(mSearchLevelView);
		}
		dialog.setTitle(R.string.searchGame);
		mSearchCountView.setText(null);
		mSearchState = STATE_NORMAL;
		dialog.show();
	}

	private int findGames(){
		boolean isEmpty;
		boolean keepOrder = mKeepOrderView.isChecked();
		if (!keepOrder)
			mGames.clear();
		isEmpty = mGames.isEmpty();
		GBASearcher searcher = new GBASearcher();
		searcher.setSearchLv(Integer.parseInt(mSearchLevelView.getText().toString()));
		searcher.find(MyApplication.SD);
		Ary<Game> games = mGames;
		if (isEmpty){
			for (File file : searcher.list)
				games.add(new Game(file));
		} else {
			Ary<Game> origin, added;
			TreeSet<Integer> originKeys = new TreeSet<Integer>();
			origin = new Ary<Game>();
			added = new Ary<Game>();
			for (File file : searcher.list){
				int key = getGameIndex(file.getName());
				if (key != -1)
					originKeys.add(key);
				else 
					added.add(new Game(file));
			}
			for (int key : originKeys)
				origin.add(games.get(key));
			games.clear();
			games.addAll(origin);
			games.addAll(added);
		}
		return games.size();
	}

	/**
	 * 游戏在列表中的序号
	 * @param games
	 * @param filename
	 * @return
	 */
	private int getGameIndex(String filename){
		Ary<Game> games = mGames;
		int length = games.size();
		for (int i = 0; i < length; i++){
			if (games.get(i).fullName.equals(filename))
				return i;
		}
		return -1;
	}

	@Override
	public void run() {
		mIndex = -1;
		handler.obtainMessage(MESSAGE_START).sendToTarget();
		handler.obtainMessage(MESSAGE_FINISH, findGames(), 0).sendToTarget();
	}
	
	private static final int 
	MESSAGE_START = 0,
	MESSAGE_FINISH = 1,
	STATE_NORMAL = 0,
	STATE_SEARCHING = 1,
	STATE_FINISHED = 2;

	/**
	 * 多线程搜索处理
	 * @author cza
	 *
	 */
	static class SearchHandler extends Handler {
		private WeakReference<MainActivity> mRef;
		
		SearchHandler(MainActivity activity){
			mRef = new WeakReference<MainActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			MainActivity theActivity;
			switch (msg.what){
				case MESSAGE_START:
					theActivity = mRef.get();
					theActivity.mSearchDialog.setTitle(R.string.searching);
					theActivity.mSearchLevelView.clearFocus();
					theActivity.mSearchState = STATE_SEARCHING;
					break;
				case MESSAGE_FINISH:
					theActivity = mRef.get();
					theActivity.mSearchDialog.setTitle(R.string.searchFinish);
					theActivity.mSearchCountView.setText(theActivity.getResources().getString(R.string.searchCount, msg.arg1));
					theActivity.mSearchState = STATE_FINISHED;
					theActivity.onDataChange(true);
					break;
			}
		}
	};
	
	private SearchHandler handler = new SearchHandler(this);


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;
		switch (requestCode) {
			case REQUEST_ADDFILE:
				addFile((Object[])data.getSerializableExtra("list"));
				break;
		}
	}
	
	/**
	 * 手动添加文件
	 */
	private void addFile(){
		Intent intent = new Intent(this, FileActivity.class)
			.putExtra(INTENT_MODE, FileActivity.MODE_PICKFILES)
			.putExtra(INTENT_TITLE, getString(R.string.chooseTypeFile, "gba zip"))
			.putExtra(INTENT_TYPE, "gba zip");
		startActivityForResult(intent, REQUEST_ADDFILE);
	}
	
	/**
	 * 添加文件进列表
	 */
	private void addFile(Object[] files){
		int newCount = 0;
		for (int i = 0; i < files.length; i++){
			File file = (File)files[i];
			if (getGameIndex(file.getName()) == -1 && GBASearcher.isGBA(file)){
				mGames.add(new Game(file));
				newCount++;
			}
		}
		if(newCount > 0)
			onDataChange(true);
		toast(getResources().getString(R.string.searchCount, newCount));
	}
	

	@Override
	public boolean onClick(Dialog dialog, int which) {
		if (which == Dialog.BUTTON_POSITIVE && dialog == mSearchDialog){
			switch (mSearchState){
				case STATE_FINISHED:
					return true;
				case STATE_NORMAL:
					new Thread(this).start();
					return false;
				case STATE_SEARCHING:
					return false;
			}
		}
		return super.onClick(dialog, which);
	}

	@Override
	protected void onDestroy() {
		if (dataChanged){
			writeData();
		}
		super.onDestroy();
	}
	
	private void getData(){
		if (mDataFile.exists() && readData())
			onDataChange(false);
		else 
			showSearchDialog();
	}
	
	/**
	 * 读取xml数据
	 * @return
	 */
	private boolean readData(){
		try {
			mGames = new Ary<Game>();
			Pull pull = new Pull();
			pull.start(mDataFile);
			int type;
			while ((type = pull.parser.next()) != 1) {
				if (type != 2) continue;
				if("game".equals(pull.parser.getName())){
					String path = pull.getValue("path");
					Game game = new Game(path);
					game.name = pull.getValue("name");
					game.type = pull.getValue("type");
					game.size = pull.getValue("size");
					game.path = path;
					mGames.add(game);
				}
			}
		} catch (Exception e){
			return false;
		}
		return true;
    }

	private void writeData(){
		XmlWriter writer = new XmlWriter();
		writer.start("games");
		for (Game game : mGames) {
			writer.startTag("game");
			writer.attribute("name", game.name);
			writer.attribute("type", game.type);
			writer.attribute("size", game.size);
			writer.attribute("path", game.path);
			writer.endTag("game");
		}
		writer.end();
		writer.write(mDataFile);
	}

	private void onDataChange(boolean fileChanged){
		if(fileChanged)
			dataChanged = true;
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mGames != null ? mGames.size() : 0;
	}

	@Override
	public View getView(int position, View item) {
		Holder holder;
		if (item == null) {
			holder = new Holder();
			item = inflateView(R.layout.game_item);
			holder.findView(item);
			item.setTag(holder);
		} else {
			holder = (Holder) item.getTag();
		}
		holder.set(mGames.get(position), position == mIndex);
		return item;
	}
	
	private class Holder {
		public TextView name, type, size;

		private void findView(View item){
			name = (TextView) item.findViewById(R.id.gameName);
			type = (TextView) item.findViewById(R.id.gameType);
			size = (TextView) item.findViewById(R.id.gameSize);
			ViewUtils.setColorList(name);
		}

		private void set(Game g, boolean checked) {
			name.setText(g.name);
			type.setText(g.type);
			size.setText(g.size);
			name.setSelected(checked);
		}
	}
}

