package cza.gbamaster;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.androidemu.Emulator;
import com.androidemu.EmulatorView;

import cza.app.BaseContextMenu;
import cza.app.Dialog;
import cza.app.TextDialog;
import cza.file.FileUtils;
import cza.gbamaster.playwidget.CheatTrack;
import cza.gbamaster.playwidget.KeyTrigger;
import cza.gbamaster.playwidget.MemoryTrack;
import cza.gbamaster.playwidget.VirtualKeypad;
import cza.hack.Cheat;
import cza.hack.Cheats;
import cza.hack.GameBoid;
import cza.preference.EmulatorPreferenceFragment;

public class PlayActivity extends BaseActivity implements 
		SurfaceHolder.Callback,
		GameKeyListener, 
		SharedPreferences.OnSharedPreferenceChangeListener,
		BaseContextMenu.Callback {

	private Cheats mCheats;
	private Emulator mEmulator;
    private EmulatorView emulatorView;
    private float mFastForwardSpeed;
    private boolean mInFastForward;
    private Keyboard mKeyboard;
    private int mFastForwardKey;
    private int mQuickLoadKey;
    private int mQuickSaveKey;
    private int mScreenshotKey;
    private VirtualKeypad mVkeypad;
	private GameBoid gameboid;
	private String ss0, mRomPath;
	private String mTempRomPath;
	private String mCotPath;
	private boolean mIsWithCht;
	private STDialog mStateDialog;
	private final SimpleDateFormat timer = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        File dataDir = getDir("data", 0);
		copyAsset(dataDir, "game_config.txt");
		copyAsset(dataDir, "bios");
        
		mEmulator = Emulator.createInstance(this, dataDir.getAbsolutePath());
        if (!mEmulator.loadBIOS(new File(dataDir, "bios").getAbsolutePath())) {
			toast(R.string.biosError);
			finish();
			return;
		}

        setVolumeControlStream(3);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);
		//初始化控件
		setContentView(R.layout.activity_play);
		emulatorView = (EmulatorView) findView(R.id.emulatorView);
        emulatorView.getHolder().addCallback(this);
        emulatorView.requestFocus();
        //键盘监听
		mKeyboard = new Keyboard(emulatorView, this);
		//模拟器默认设置
		mEmulator.setOption("frameSkipMode", "auto");
		mEmulator.setOption("maxFrameSkips", 2);
		mEmulator.setOption("refreshRate", "default");
		mEmulator.setOption("saveType", "auto");
		mEmulator.setOption("enableSRAM", true);

		//触发设置
		String[] keys = {"fullScreenMode", "fastForwardSpeed", "soundEnabled", "soundVolume", "enableVKeypad", "scalingMode", "orientation"};
        for (int i = 0; i < keys.length; i++){
			onSharedPreferenceChanged(pref, keys[i]);
		}
        loadKeyBindings(pref);
		
		Intent intent = getIntent();
		mIsWithCht = (mCotPath = intent.getStringExtra("cotPath")) == null;
		gameboid = new GameBoid();
		getActionBar().setDisplayShowTitleEnabled(false);
		onNewIntent(intent);
	}
    
    @Override
    public void finish() {
        unload();
    	CheatHandler.mPlayingCheats = null;
    	super.finish();
    }

	/**
	 * 从作弊界面返回或者打开了新的游戏
	 */
    protected void onNewIntent(Intent intent) {
		if(intent.hasExtra(INTENT_CHANGED)){
			//更新作弊
			if (intent.getBooleanExtra(INTENT_CHANGED, false))
				sync();
		} else {
			//新游戏
			Uri data = intent.getData();
			if (data != null){
				mTempRomPath = data.getPath();
				if (gameboid.unloaded())
					//第一次加载
					reloadRom(true);
				else 
					//是切换游戏
					showNewRomDialog();
			} else if (mRomPath == null){
				//没有加载游戏
				toast(R.string.notRunning);
				finish();
			}
		}
    }

	/**
	 * 新游戏
	 */
	private void reloadRom(boolean isFirst){
		if (!isFirst) 
			unload();
		mRomPath = mTempRomPath;
		gameboid.load(mRomPath);
		ss0 = gameboid.getSTFile(0).getPath();
		if (!loadROM())
			return;
		if (mIsWithCht)
			mCheats = gameboid.parseCht(gameboid.getChtFile());
		else
			// 模板模式不能预加载cht
			mCheats = new Cheats();
		CheatHandler.mPlayingCheats = mCheats;
		sync();
	}

    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.play_activity, menu);
		setIconEnable(menu, true);
		return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean hideQuickSL = !mVkeypad.isAbleQuickSL();
		menu.getItem(0).setVisible(hideQuickSL);
		menu.getItem(1).setVisible(hideQuickSL);
		return super.onPrepareOptionsMenu(menu);
	}
 
	public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_quickLoad:
				quickLoad();
				return true;
			case R.id.menu_quickSave:
				quickSave();
				return true;
            case R.id.menu_load:
            	showStateDialog(false);
				return true;
			case R.id.menu_save:
				showStateDialog(true);
				return true;
			case R.id.menu_setting:
				startActivity(new Intent(this, MainPreferenceActivity.class)
					.putExtra(MainPreferenceActivity.INTENT_LAUNCH_ID, 
							MainPreferenceActivity.LAUNCH_EMULATOR));
				return true;
			case R.id.menu_speed:
				onFastForward();
				menuItem.setTitle(mInFastForward ? R.string.normal : R.string.fast);
				return true;
			case R.id.menu_cheat:
				editCheat();
				return true;
			case R.id.menu_reset:
				mEmulator.reset();
				return true;
			case R.id.menu_screenshot:
				onScreenshot();
				return true;
			case R.id.menu_cheatTest:
				new CheatTrack(this);
				break;
			case R.id.menu_memoryTest:
				new MemoryTrack(this);
				break;
			case R.id.menu_keyTrigger:
				new KeyTrigger(this);
				break;
			case R.id.menu_close:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(menuItem);
    }

	/**
	 * 列表对话框点击事件
	 */
	@Override
	public void onItemClick(BaseContextMenu dialog, int position){
		if (quiteDialog == dialog) {
			//退出游戏
			if (position != 0){
				if (position == 1) 
					quickSave();
				finish();
			}
		} else if (mStateDialog == dialog) {
			//保存/载入存档
			String path = mStateDialog.list.get(position).path;
			if (mStateDialog.saveMode)
				saveState(path);
			else 
				mEmulator.loadState(path);
		}
	}

	/**
	 * 获取主视图
	 * @return
	 */
	public ViewGroup getContentView(){
		return (ViewGroup)mRootLayout;
	}

	/**
	 * 获取模拟器实例
	 * @return
	 */
	public Emulator getEmulator(){
		return mEmulator;
	}

	/**
	 * 退出对话框
	 */
	@Override
	public boolean onShowMenu(BaseContextMenu menu){
		return true;
	}

	/**
	 * 结束
	 */
    private void unload() {
		if (mEmulator != null) {
			mEmulator.unloadROM();
		} if (mCheatIntent != null){
			mCheatIntent.putExtra(CheatHandler.INTENT_OPTION, CheatHandler.MASK_OPTION_FINISH);
			startActivity(mCheatIntent);
			mCheatIntent = null;
		}
    }
	
	/**
	 * 解压bios和game_config
	 */
	private void copyAsset(File dir, String filename){
		Resources res = getResources();
		File file = new File(dir, filename);
		if (!file.exists()) {
			try {
				InputStream is = res.getAssets().open(filename);
				FileUtils.write(is, file);
				is.close();
			} catch (Exception e) {}
		}
	}

    private static int getScalingMode(String type) {
		if("original".equals(type))
			return 0;
		else if("2x".equals(type))
			return 1;
		else if("proportional".equals(type))
			return 2;
		else 
			return 3;
    }

    private static int getScreenOrientation(String type) {
    	if("landscape".equals(type))
			return 0;
    	else if("portrait".equals(type))
			return 1;
    	else 
    		return -1;
    }

	/**
	 * 从模拟器取得图片
	 */
    private Bitmap getScreenshot() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(76800);
        mEmulator.getScreenshot(byteBuffer);
        Bitmap bitmap = Bitmap.createBitmap(240, 160, Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(byteBuffer);
        return bitmap;
    }

	/**
	 * 加载按键映射
	 */
    private void loadKeyBindings(SharedPreferences pref) {
        mKeyboard.clearKeyMap();
        int[] codes = EmulatorPreferenceFragment.GAME_KEY_CODE;
        String[] keys = EmulatorPreferenceFragment.GAME_KEY_PREF;
        for(int i = 0; i < keys.length; i++){
			mKeyboard.mapKey(codes[i], pref.getInt(keys[i], 0));
		}
		mQuickLoadKey = pref.getInt("quickLoad", 0);
		mQuickSaveKey = pref.getInt("quickSave", 0);
		mFastForwardKey = pref.getInt("fastForward", 0);
		mScreenshotKey = pref.getInt("screenshot", 0);
    }

    private boolean loadROM() {
        String path = mRomPath;
        if (!path.endsWith(".zip") && !path.endsWith(".gba")) {
            toast("不支持此ROM的文件类型");
            finish();
            return false;
        }
        if (!mEmulator.loadROM(path)) {
            toast("游戏加载失败");
            finish();
            return false;
        }
        mInFastForward = false;
        quickLoad();
        return true;
    }

	/**
	 * 变速
	 */
    private void onFastForward() {
        setGameSpeed((mInFastForward = !mInFastForward) ? mFastForwardSpeed : 1);
    }

	/**
	 * 保存截图
	 */
	private void onScreenshot() {
        File dir = new File(((MyApplication)getApplication()).myDir, "screenshot");
        if (!dir.exists() && !dir.mkdir()) {
			toast(dir.getPath() + "不可写");
            return;
        }
		File to = new File(dir, gameboid.mGame.name + "_" + timer.format(System.currentTimeMillis()) + ".png");
        Bitmap bp = getScreenshot();
        try {
			bp.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(to));
			final int ID = 0;
			final String hint = "截图成功";
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(ID);
			@SuppressWarnings("deprecation")
			Notification notice = new Notification.Builder(this)
				.setSmallIcon(R.drawable.app_icon)
				.setLargeIcon(bp)
				.setTicker(hint)
				.setContentTitle(hint)
				.setContentText(to.getPath())
				.setContentIntent(PendingIntent.getActivity(this, 0, MyApplication.openFile(to), 
						PendingIntent.FLAG_UPDATE_CURRENT))
				.setAutoCancel(true)
				.getNotification();	
			
			manager.notify(ID, notice);
		} catch (Exception e) {}
		bp.recycle();
    }

    public void quickLoad() {
        mEmulator.loadState(ss0);
    }

    public void quickSave() {
        saveState(ss0);
    }

    private void saveState(String path) {
        mEmulator.pause();
		ZipOutputStream zos;
        try {
			zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
			zos.putNextEntry(new ZipEntry(GameBoid.IMG));
			Bitmap bp = getScreenshot();
			bp.compress(Bitmap.CompressFormat.PNG, 100, zos);
			zos.closeEntry();
			zos.close();
			bp.recycle();
		} catch (Exception e) {}
        mEmulator.saveState(path);
        mEmulator.resume();
        return;
    }

    private void setGameSpeed(float f) {
        mEmulator.pause();
        mEmulator.setOption("gameSpeed", Float.toString(f));
        mEmulator.resume();
    }

    public void onGameKeyChanged() {
		int key = this.mKeyboard.getKeyStates();
		if (mVkeypad != null) 
			key |= mVkeypad.getKeyStates();
		//化解方向键冲突
		if ((key & Emulator.GAMEPAD_LEFT_RIGHT) == Emulator.GAMEPAD_LEFT_RIGHT)
			key &= ~Emulator.GAMEPAD_LEFT_RIGHT;
		if ((key & Emulator.GAMEPAD_UP_DOWN) == Emulator.GAMEPAD_UP_DOWN)
			key &= ~Emulator.GAMEPAD_UP_DOWN;
		mEmulator.setKeyStates(key);

	}
	/**
	 * 切换操作栏
	 */
	private boolean isBarShowing = true;
	
	public void onOuter(){
		hideSystemBar(isBarShowing);
		if (isBarShowing){
			getActionBar().hide();
		} else {
			getActionBar().show();
		}
		isBarShowing = !isBarShowing;
		
	}

	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
		boolean hit = true;
		if (keyCode == mQuickLoadKey) {
			quickLoad();
		} else if (keyCode == mQuickSaveKey) {
			quickSave();
		} else if (keyCode == mFastForwardKey) {
			onFastForward();
		} else if (keyCode == mScreenshotKey) {
			onScreenshot();
		} else if ((keyCode == 27) || (keyCode == 84)) {
		} else if (keyCode == 4){
			mEmulator.pause();
			showQuiteDialog();
		} else {
			hit = super.onKeyDown(keyCode, keyEvent);
		}
		return hit;
	}

    protected void onPause() {
        super.onPause();
        mEmulator.pause();
    }

	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		if (key.startsWith("gamepad")) {
			loadKeyBindings(pref);
			return;
		}
		if("fullScreenMode".equals(key)){
			WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
			layoutParams.flags = pref.getBoolean("fullScreenMode", true) ? (1024 | layoutParams.flags) : (- 1025 & layoutParams.flags);
			getWindow().setAttributes(layoutParams);
		}
		else if("fastForwardSpeed".equals(key)){
			String val = pref.getString(key, "2x");
			mFastForwardSpeed = Float.parseFloat(val.substring(0, (val.length() - 1)));
			if (mInFastForward) 
				setGameSpeed(mFastForwardSpeed);
		}
		else if("soundEnabled".equals(key))
			mEmulator.setOption(key, pref.getBoolean(key, true));
		else if("soundVolume".equals(key))
			mEmulator.setOption(key, pref.getInt(key, 100));
		else if ("enableVKeypad".equals(key))
			if (pref.getBoolean(key, true)) {
				if (mVkeypad == null)
					mVkeypad = new VirtualKeypad(this, this);
				((ViewGroup)mRootLayout).addView(mVkeypad, -1, -1);
			} else if (mVkeypad != null)
				((ViewGroup)mRootLayout).removeView(mVkeypad);
			else if ("scalingMode".equals(key))
				emulatorView.setScalingMode(getScalingMode(pref.getString(key,
						"proportional")));
			else if ("orientation".equals(key))
				setRequestedOrientation(getScreenOrientation(pref.getString(
						key, "unspecified")));
	}

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			mKeyboard.reset();
            mEmulator.setKeyStates(0);
            mEmulator.resume();
        } else {
			mEmulator.pause();
		}
    }

    public void surfaceChanged(SurfaceHolder holder, int x, int width, int height) {
		int realWidth = Emulator.VIDEO_W;
		int realHeight = Emulator.VIDEO_H;
        if (mVkeypad != null) {
			Rect rect = new Rect();
			rect.top = getActionBar().getHeight();
			rect.right = mRootLayout.getWidth();
			rect.bottom = mRootLayout.getHeight();
			mVkeypad.resize(rect, PreferenceManager.getDefaultSharedPreferences(this));
		}
        int paddingX = (width - realWidth) / 2;
        int paddingY = (height - realHeight) / 2;
        mEmulator.setSurfaceRegion(width, height, paddingX, paddingY, realWidth, realHeight);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mEmulator.setSurface(holder);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mEmulator.setSurface(null);
    }
	
	private boolean paused;
	
	/**
	 * 切换状态
	 */
	public boolean switchState(){
		paused = !paused;
		if (paused)
			mEmulator.pause();
		else 
			mEmulator.resume();
		return paused;
	}

	/**
	 * 退出游戏对话框
	 */
	private BaseContextMenu quiteDialog;

	private void showQuiteDialog(){
		BaseContextMenu dialog = quiteDialog;
		if (dialog == null){
			quiteDialog = dialog = new BaseContextMenu(this);
			dialog.setTitle("结束游戏");
			dialog.setList(R.array.menu_quiteGame);
			dialog.setCallback(this);
		}
		dialog.show();
	}
	
	
	/**
	 * 切换游戏对话框
	 */
	private TextDialog newRomDialog;
	
	private void showNewRomDialog(){
		TextDialog dialog = newRomDialog;
		if (dialog == null){
			newRomDialog = dialog = new TextDialog(this, 0);
			dialog.setTitle(R.string.newGame);
			dialog.setMessage(getString(R.string.newGameConfirm));
			dialog.setConfirm();
			dialog.setOnClickListener(this);
			dialog.setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.saveFirst));
		}
		dialog.show();
	}
	
	/**
	 * 打开即时存档对话框
	 */
	private void showStateDialog(boolean isSave){
		if (!isSave && gameboid.getSTCount() == 0){
			toast(R.string.noST);
			return;
		}
		if (mStateDialog == null){
			mStateDialog = new STDialog(this, gameboid);
			mStateDialog.setCallback(this);
		}
		mStateDialog.setSaveMode(isSave);
		mStateDialog.show();
	}


	/**
	 * 对话框点击事件
	 */
	@Override
	public boolean onClick(Dialog dialog, int which) {
		if (dialog == newRomDialog){
			//切换游戏
			if (which != Dialog.BUTTON_NEGATIVE){
				//先保存
				if (which == Dialog.BUTTON_NEUTRAL)
					quickSave();
				reloadRom(false);
			}
			newRomDialog.dismiss();
		}
		return true;
	}
	
	/*
	 * 作弊
	 */
	private Intent mCheatIntent;

	/**
	 * 转到作弊页面
	 */
	private void editCheat(){
		if (mCheatIntent == null){
			if (mIsWithCht)
				mCheatIntent = getToFrontIntent(CheatActivity.class)
				.putExtra(INTENT_IS_MYBOY, false)
				.putExtra(INTENT_PLAYING, true)
				.putExtra(INTENT_PATH, gameboid.getChtFile().getPath());
			else 
				mCheatIntent = getToFrontIntent(CotActivity.class)
					.putExtra(INTENT_PLAYING, true)
					.putExtra(INTENT_PATH, mCotPath);
		}
		startActivity(mCheatIntent);
	}

	/**
	 * 同步作弊码
	 */
	private void sync(){
		Emulator emu = mEmulator;
		emu.destroyCheat();
		for (Cheat cheat : mCheats){
			if (cheat.checked){
				for (String code : cheat.codes){
					emu.addCheat(code);
				}
			}
		}
	}
}
