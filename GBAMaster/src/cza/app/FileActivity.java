package cza.app;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Stack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cza.file.FileInfo;
import cza.file.FileInfos;
import cza.file.FileTypeFilter;
import cza.file.FileUtils;
import cza.gbamaster.MyApplication;
import cza.gbamaster.R;
import cza.util.Pull;
import cza.util.ViewUtils;
import cza.util.XmlWriter;
import cza.widget.ListViewCheckControler;
import cza.widget.MyAdapter;
import cza.widget.SpinnerEditText;
import cza.widget.StringArrayAdapter;

public class FileActivity extends BaseActivity implements 
MyAdapter.Helper,
AdapterView.OnItemClickListener,
ListViewCheckControler.Callback,
View.OnClickListener {

	protected byte mode;
	protected boolean ableMulChk;
	protected FileInfos mInfos;
	protected ListView mListView;
	protected File mFolder;
	protected String mDir;
	private FileFilter mFilter = FileUtils.VISIBLE;
	private View addrBar;
	private View mulChkBar;
	private View moveBar;
	private SpinnerEditText path;
	private MyAdapter fileAdpt;
	private Stack<FileInfos> mParentDirCache = new Stack<FileInfos>();
	//模块
	private BookMarkDirs bookMarkDirs;
	private SaveAs saveAs;
	public final static byte
	MODE_PICKDIR = 0,
	MODE_PICKFILE = 1,
	MODE_PICKFILES = 2,
	MODE_SAVEAS = 3,
	MODE_BROWSER = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		mode = intent.getByteExtra(INTENT_MODE, MODE_BROWSER);
		String title = intent.getStringExtra(INTENT_TITLE);
		mDir = intent.getStringExtra(INTENT_PATH); //设置起始目录
		if (mDir == null) {
			mDir = App.SD_PATH;
		}
		if (title != null) 
			setTitle(title);

		setContentView(R.layout.filebrowser);
		addrBar = findViewById(R.id.addrBar);

		mListView = (ListView) findViewById(R.id.list);
		fileAdpt = new MyAdapter();
		fileAdpt.setHelper(this);
		mListView.setAdapter(fileAdpt);
		mListView.setOnItemClickListener(this);

		bookMarkDirs = new BookMarkDirs(this);
		//类型过滤
		String type = intent.getStringExtra(INTENT_TYPE);
		if (type != null)
			mFilter = new FileTypeFilter(type.split(" "));
		getData();

		//不同模式的操作
		if (MODE_PICKFILES == mode) {
			createMulChk();
			addFooter(R.layout.activity_file_pick_files);
		} else if (MODE_SAVEAS == mode)
			saveAs = new SaveAs();
		else if (MODE_BROWSER == mode) {
			createMulChk();
			//先选中文件
			String checkedFileName = intent.getStringExtra(INTENT_CHECKED);
			if (checkedFileName != null) {
				if (!checkedFileName.contains(":")) {
					checkFile(checkedFileName);
					if (intent.getBooleanExtra(INTENT_AUTO_OPEN, false)) 
						onFileClicked(new File(mDir, checkedFileName));
				} else {
					checkFile(checkedFileName.split(":"));
				}
			}
			FileMenu fileMenu = new FileMenu();
			//弹出文件菜单
			if (intent.getBooleanExtra(INTENT_AUTO_MENU, false) && ableMulChk)
				fileMenu.manualShow();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mode != MODE_BROWSER) return false;
		getMenuInflater().inflate(R.menu.file_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				refreshData();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View btn) {
		switch (btn.getId()) {
			case R.id.checkAll:
				mInfos.chkAll(true);
				break;
			case R.id.btn_checkInverse:
				mInfos.chkAll();
				break;
			case R.id.btn_quite:
				mInfos.chkAll(false);
				chkMulChk();
				break;
			case R.id.btn_finishPickFiles:
				finishPickFiles();
				break;
		}
		chkMulChk();
	}

	@Override
	protected void onDestroy() {
		bookMarkDirs.writeData();
		super.onDestroy();
	}

	private boolean notSDRoot() {
		return !App.isSD(mFolder);
	}

	private void upDir() {
		File up = mFolder.getParentFile();
		if (up != null) {
			mDir = up.getPath();
			int depth = mParentDirCache.size();
			if(depth != 0)
				mParentDirCache.pop();
			getData(up, depth > 1 ? mParentDirCache.peek() : null);
			mListView.setSelection(mInfos.selection);
		}
	}

	/**
	 * 路径跳转
	 * @param dir
	 * @return
	 */
	private boolean getData(String dir) {
		if (mDir == dir) 
			return true;
		mDir = dir;
		return getData();
	}

	/**
	 * 更新目录数据
	 * @return
	 */
	private boolean getData() {
		mParentDirCache.clear();
		return getData(new File(mDir), null);
	}

	/**
	 * 更新目录数据
	 * @param folder
	 * @return
	 */
	private boolean getData(File folder, FileInfos infos) {
		boolean result = false;
		if (folder.exists() && folder.canRead()) {
			if (folder.isDirectory()) {
				result = true;
				mFolder = folder;
				if (infos == null) {
					mInfos = new FileInfos(folder.listFiles(mFilter));
					mInfos.path = mDir;
				} else {
					mInfos = infos;
				}
				bookMarkDirs.chk();
				changeData();
				mListView.setSelection(0);
			} else {
				onFileClicked(folder);
			}
		} else {
			mDir = mFolder.getPath();
		}
		path.setText(mDir);
		return result;
	}

	protected void refreshData() {
		getData();
	}

	/**
	 * 点击文件
	 */
	private void onFileClicked(File file) {
		switch (mode) {
			case MODE_PICKFILE:
				break;
			case MODE_PICKFILES:
				checkFile(file.getName());
				return;
			case MODE_SAVEAS:
				saveAs.name.setText(file.getName());
				return;
			case MODE_BROWSER:
				try {
					startActivity(App.openFile(file));
				} catch (Exception e) {}
				return;
		}
		finishWithResult(file);
	}

	/**
	 * 点击文件夹
	 */
	private void onFolderClicked(File folder) {
		if (folder.canRead()) {
			mInfos.selection = mListView.getFirstVisiblePosition();
			mDir = folder.getPath();
			getData(folder, null); //进入子目录
			mParentDirCache.push(mInfos); //缓冲入栈
		}
	}

	private void changeData() {
		fileAdpt.notifyDataSetChanged();
	}

	/**
	 * 拦截返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && notSDRoot()) {
			if (ableMulChk) {
				stopMulChk();
			} else {
				upDir();
			}
            return true;
        }
		return super.onKeyDown(keyCode, event);
    }

	protected FileInfos getInfos() {
		return ableMulChk ? mInfos.getCheckedInfos() : mInfos;
	}

	/**
	 * 填充布局
	 */
	@Override
	public int getCount() {
		return mInfos != null ? mInfos.size() : 0;
	}

	@Override
	public View getView(int position, View item) {
		Holder holder;
		if (item == null) {
			holder = new Holder();
			item = inflateView(R.layout.file_item);
			holder.findView(item);
			item.setTag(holder);
		} else {
			holder = (Holder) item.getTag();
		}
		holder.set(mInfos.get(position));
		return item;
	}

	private class Holder {
		private ImageView img;
		private TextView name, size;
		private void findView(View item) {
			img = (ImageView) item.findViewById(R.id.file_icon);
			name = (TextView) item.findViewById(R.id.file_name);
			size = (TextView) item.findViewById(R.id.file_size);
			ViewUtils.setColorList(name);
		}
		private void set(FileInfo info) {
			img.setImageResource(info.icon);
			name.setText(info.name);
			size.setText(info.size);
			if (name.isSelected() != info.checked) {
				name.setSelected(info.checked);
			}
		}
	}

	/**
	 * 选中列表
	 */
	@Override
	public void onItemClick(AdapterView<?> apt, View v, int position, long id) {
		if (ableMulChk) {
			mInfos.chkAt(position);
			chkMulChk();
			return;
		}
		FileInfo info = mInfos.get(position);
		File file = new File(mDir, info.name);
		if (info.isDir) 
			onFolderClicked(file);
		else 
			onFileClicked(file);
	}

	/**
	 * 书签
	 */
	private class BookMarkDirs extends LinkedHashSet<String> implements 
	View.OnClickListener, 
	TextView.OnEditorActionListener,
	AdapterView.OnItemClickListener {

		private static final long serialVersionUID = -8737305593441979136L;
		private CheckBox chkbox;
		private File dataFile;
		private ArrayList<String> list = new ArrayList<String>();
		private StringArrayAdapter adapter;
		private boolean changed = false;

		public BookMarkDirs(Context c) {
			View addrBar = FileActivity.this.addrBar;
			chkbox = (CheckBox) addrBar.findViewById(R.id.filebrowser_bookmark);
			chkbox.setOnClickListener(this);
			path = (SpinnerEditText) addrBar.findViewById(R.id.filebrowser_path);
			path.setSingleLine(true);
			path.setOnEditorActionListener(this);
			addrBar.findViewById(R.id.filebrowser_upbtn).setOnClickListener(this);
			adapter = new StringArrayAdapter(c, list, StringArrayAdapter.TYPE_LIST);
			path.listView.setAdapter(adapter);
			path.listView.setOnItemClickListener(this);
			MyApplication app = (MyApplication)getApplication();
			dataFile = app.bookMark;
			if (!parse()) {
				add(App.SD_PATH);
				add(app.myDir.getPath());
			}
			sync();
			showBt();
		}

		@Override
		public void onClick(View v) {
			if (v == chkbox) {
				if (chkbox.isChecked()) {
					add(mDir);
				} else {
					remove(mDir);
				}
				this.changeData();
			} else {
				upDir();
			}
		}

		@Override
		public void onItemClick(AdapterView<?> apt, View v, int position, long id) {
			String dir = list.get(position);
			if (!getData(dir)) {
				remove(dir);
				this.changeData();
			}
			path.closePopUp();
		}

		@Override  
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			getData(v.getText().toString());
			return true;  
		}

		private void chk() {
			chkbox.setChecked(contains(mDir));
		}

		private void sync() {
			list.clear();
			list.addAll(this);
		}

		public void writeData() {
			if (changed) {
				write();
			}
		}

		private void showBt() {
			path.showMoreBtn(size() > 0);
		}

		private void changeData() {
			changed = true;
			showBt();
			sync();
			adapter.notifyDataSetChanged();
		}

		private boolean parse() {
			Pull pull = new Pull();
			try {
				pull.start(dataFile);
				int type;
				while ((type = pull.parser.next()) != 1) {
					if (type != 2) continue;
					if ("dir".equals(pull.parser.getName())) {  
						add(pull.getValue("path"));
					}
				}
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		private void write() {
			XmlWriter writer = new XmlWriter();
			writer.start("dirs");
			for (String dir : list) {
				writer.startTag("dir");
				writer.attribute("path", dir);
				writer.endTag("dir");
			}
			writer.end();
			writer.write(dataFile);
		}
	}

	/**
	 * 添加顶部控件栏
	 */
	protected void addHeader(int resId) {

	}


	/**
	 * 添加底部控件栏
	 */
	private HashSet<Integer> footerSet;
	private ViewGroup footerBar;

	protected void addFooter(int resId) {
		if (footerBar == null) {
			footerBar = (ViewGroup)findView(R.id.footerBar);
			footerSet = new HashSet<Integer>();
		}
		//不能重复添加
		if (!footerSet.contains(resId))
			footerBar.addView(inflateView(resId));
		switch (resId) {
			case R.layout.activity_file_pick_files:
				if (btn_finishPickFiles == null) {
					btn_finishPickFiles = (TextView)footerBar.findViewById(R.id.btn_finishPickFiles);
					btn_finishPickFiles.setOnClickListener(this);
				}
				break;
		}
	}

	/**
	 * 多选
	 */
	private TextView mulChkState;

	private void createMulChk() {
		new ListViewCheckControler(mListView, this).setmSingleTriggerPos(App.dip2px(40));
		mulChkBar = findView(R.id.mulChkBar);
		mulChkState = (TextView) mulChkBar.findViewById(R.id.mulChkState);
		ViewUtils.registerClick(mulChkBar, this, R.id.checkAll, R.id.btn_checkInverse, R.id.btn_quite);
		stopMulChk();
	}

	protected void startMulChk() {
		ableMulChk = true;
		mulChkBar.setVisibility(0);
		addrBar.setVisibility(4);
	}

	protected void stopMulChk() {
		mInfos.chkAll(false);
		ableMulChk = false;
		mulChkBar.setVisibility(4);
		addrBar.setVisibility(0);
		changeData();
	}

	protected void chkMulChk() {
		if (mInfos.checkedCount == 0) 
			stopMulChk();
		else 
			mulChkState.setText("选中" + mInfos.getState());
		changeData();
	}

	@Override
	public void onItemCheck(int start, int end) {
		boolean trigger = false;
		if (mode == MODE_BROWSER){
			mInfos.mulChk(start, end);
			trigger = true;
		} else if (mode == MODE_PICKFILES){
			FileInfos infos = mInfos;
			for (int i = start; i <= end; i++){
				if(!infos.get(i).isDir){
					infos.chkAt(i);
					trigger = true;
				}
			}
		}
		if(trigger){
			startMulChk();
			chkMulChk();
		}
	}

	private void checkFile(String...files) {
		startMulChk();
		int len = mInfos.size();
		int first = len;
		for (String name : files) {
			int i = mInfos.indexOf(name);
			if (i != -1) {
				mInfos.chkAt(i);
				if (i < first) {
					first = i;
				}
			}
		}
		if (first < len) {
			mListView.setSelection(first);
		}
		chkMulChk();
	}
	
	
	protected void onRename(FileMenu menu) {
		menu.showRename();
	}


	/**
	 * 文件菜单
	 */
	protected class FileMenu extends ListContextMenu implements 
			FileMoveDialog.Callback,
			RenameDialog.OnSummitListener,
			BaseContextMenu.Callback,
			Dialog.OnClickListener {	

		private FileInfo myInfo;
		private FileInfos myInfos;
		private DeleteConfirm delete;
		private FileMoveDialog move;
		private RenameDialog rename;

		public FileMenu() {
			super(mListView);
			setList(R.array.menu_file);
			setCallback(this);
		}

		@Override
		public void onItemClick(BaseContextMenu d, int index) {
			switch (index) {
				case 0:
					onRename(this);
					break;
				case 1:
					startMoveDialog(true);
					break;
				case 2:
					startMoveDialog(false);
					break;
				case 3:
					copyDir();
					break;
				case 4:
					share();
					break;
				case 5:
					showDelete();
					break;
			}
		}

		@Override
		public boolean onShowMenu(BaseContextMenu d) {
			myInfo = mInfos.get(mIndex);
			if (ableMulChk) {
				if (!myInfo.checked) 
					return false;
				setTitle("所选文件");
				myInfos = mInfos.getCheckedInfos();
			} else {
				setTitle(myInfo.name);
				myInfos = new FileInfos(mDir);
				myInfos.add(myInfo);
				myInfos.dirCount = myInfo.isDir ? 1 : 0;
			}
			return true;
		}
		
		/**
		 * 手动显示
		 */
		public void manualShow(){
			setTitle("所选文件");
			myInfos = mInfos.getCheckedInfos();
			show();
		}

		/**
		 * 重命名
		 */
		private void showRename() {
			if (rename == null) {
				rename = new RenameDialog(getContext());
				rename.setOnSummitListener(this);
			}
			String name = myInfo.name,
				type = myInfo.type;
			rename.pre(name);
			if (!type.isEmpty()) {
				rename.textarea.setSelection(0, name.length() - type.length() - 1);
			}
			rename.show();
		}

		@Override
		public boolean onSummit(RenameDialog d, String name) {
			if (name.isEmpty()) {
				return d.error(RenameDialog.ERROR_EMPTY);
			}
			File file = new File(mFolder, myInfo.name);
			File newFile = new File(mFolder, name);
			if (newFile.exists()) {
				return d.error(RenameDialog.ERROR_EXIST);
			} else {
				file.renameTo(newFile);
				toast(myInfo.name + " 重命名为 " + name);
				myInfo.name = name;
				changeData();
				return true;
			}
		}

		/**
		 * 删除文件
		 */
		@Override
		public boolean onClick(Dialog dialog, int which) {
			if (which == Dialog.BUTTON_POSITIVE && dialog == delete) {
				if (ableMulChk) {
					mInfos.remove(true);
					stopMulChk();
				} else {
					mInfos.remove(mIndex);
				}
				changeData();
			}
			return true;
		}

		/**
		 * 复制移动
		 * @param isMove
		 */
		private void startMoveDialog(boolean isMove) {
			if (move == null) {
				moveBar = FileActivity.this.findViewById(R.id.moveBar);
				move = new FileMoveDialog(getContext(), moveBar);
			}
			move.startService(this, isMove, myInfos);
		}

		@Override
		public void onStartService() {
			if (ableMulChk) {
				stopMulChk();
			}
		}

		@Override
		public void onAction() {
			move.test(mDir);
		}

		@Override
		public void onFinishMove() {
			mParentDirCache.remove(myInfos.path);
			refreshData();
		}

		/**
		 * 复制路径
		 */
		private void copyDir() {
			int len = myInfos.size();
			StringBuilder sb = new StringBuilder();
			sb.append(myInfos.path).append(File.separatorChar);
			if (myInfos.size() == 1) {
				sb.append(myInfo.name);
			} else {
				for (int i = 0; i < len; i++) {
					sb.append('\n');
					sb.append(myInfos.get(i).name);
				}
			}
			Clipboard.copy(getContext(), sb);
			sb.insert(0, '\n');
			sb.insert(0, getString(R.string.copySucceed));
			toast(sb);
		}


		/**
		 * 删除
		 */
		private void showDelete() {
			if (delete == null) {
				delete = new DeleteConfirm(getContext());
				delete.setOnClickListener(this);
			}
			delete.setInfos(myInfos);
			delete.show();
		}

		/**
		 * 分享
		 */
		private void share() {
			App.shareFiles(FileActivity.this, myInfos.toArray());
		}
	}
	 

	/**
	 * 另存为
	 */
	private class SaveAs implements 
	View.OnClickListener, 
	TextView.OnEditorActionListener {

		private View bar;
		private EditText name;

		SaveAs() {
			bar = findViewById(R.id.saveAsBar);
			bar.setVisibility(View.VISIBLE);
			name = (EditText) bar.findViewById(R.id.saveAsName);
			bar.findViewById(R.id.saveAsOk).setOnClickListener(this);
			name.setText(getIntent().getStringExtra("saveAsName"));
			ViewUtils.setOnDown(name, this);
			name.requestFocus();
		}

		@Override
		public void onClick(View v) {
			finish(name.getText().toString());
		}

		@Override  
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {  
			finish(v.getText().toString());
			return true;  
		}

		private void finish(String text) {
			File file = new File(mFolder, text);
			finishWithResult(file);
		}
	}


	/**
	 * MODE_PICKFILES
	 */
	TextView btn_finishPickFiles;

	private void finishPickFiles() {
		if (mInfos.checkedCount == 0){
			toast(R.string.nofile);
			return;
		}
		Intent intent = new Intent();
		intent.putExtra("list", mInfos.getCheckedInfos().toArray());
		setResult(RESULT_OK, intent);
		finish();
	}


	public void finishWithResult(File file) {
		finishWithResult(file.getPath());
	}

	public void finishWithResult(String path) {
		Intent intent = new Intent();
		intent.putExtra("path", path);
		setResult(RESULT_OK, intent);
		finish();
	}
}
