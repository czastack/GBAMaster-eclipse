package cza.gbamaster;

import java.io.File;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import cza.app.App;
import cza.file.FileUtils;
import cza.hack.Game;

public class MyApplication extends App {
	public final static int
	ICON_DIR = R.drawable.file_folder,
	ICON_GBA = R.drawable.file_gba,
	ICON_FILE = R.drawable.file_unknown;
	//文件夹
	public File myDir, cotDir, backupDir;
	//文件
	public File gamesData, bookMark;
	public Game mGame;

	@Override
	public void onCreate() {
		super.onCreate();
		firstInit();
	}

	protected void initFile(){
		super.initFile();
		if (SD_EXIST){
			myDir = new File(SD, "GBAMaster");
			gamesData = new File(myDir, "games.xml");
			bookMark = new File(myDir, "bookMark.xml");
			cotDir = new File(myDir, "cot");
			backupDir = new File(myDir, "backup");
			myDir.mkdir();
			cotDir.mkdir();
			backupDir.mkdir();
		}
	}
	
	//第一次启动
	private void firstInit(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.contains(KEY_NOTFIRST))
			return;
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(KEY_NOTFIRST, true);
		editor.putBoolean("enableVKeypad", true);
		editor.commit();
	}
	

	/**
	 * 图标
	 */
	
	public static int getIcon(File f){
		if (f.isDirectory()) return ICON_DIR;
		return getIcon(FileUtils.getType(f));
	}

	public static int getIcon(String type){
		if("gba".equals(type))
			return ICON_GBA;
		return ICON_FILE;
	}
}
