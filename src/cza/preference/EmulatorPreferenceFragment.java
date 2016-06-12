package cza.preference;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import cza.gbamaster.FileActivity;
import cza.gbamaster.MyApplication;
import cza.gbamaster.R;
import cza.util.Pull;
import cza.util.XmlWriter;

public class EmulatorPreferenceFragment extends BasePreferenceFragment {

	public static final int[] GAME_KEY_CODE = {64, 128, 32, 16, 4, 8, 1, 2, 65536, 131072, 512, 256, 96, 80, 160, 144, 3, 768, 1024 };
	public static final String[] GAME_KEY_NAME = {"上", "下", "左", "右", "选择键", "开始键", "按钮 A", "按钮 B", "按钮 A (连发)", "按钮 B (连发)", "左肩键", "右肩键", "左上", "右上", "左下", "右下", "按钮 A+B", "左肩键+右肩键", "GameShark 按钮" };
	public static final String[] GAME_KEY_PREF = {"gamepad_up", "gamepad_down", "gamepad_left", "gamepad_right", "gamepad_select", "gamepad_start", "gamepad_A", "gamepad_B", "gamepad_A_turbo", "gamepad_B_turbo", "gamepad_TL", "gamepad_TR", "gamepad_up_left", "gamepad_up_right", "gamepad_down_left", "gamepad_down_right", "gamepad_AB", "gamepad_TL_TR", "gamepad_GS" };
	private static final int REQUEST_LOADKEY = 0;
	private static final int REQUEST_SAVEKEY = 1;
	private SharedPreferences settings;
	private String keyFileName = "default.xml";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_emulator);
		onStartEmulatorSettings();
	}
	
	public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference pref) {
		String key = pref.getKey();
		if ("loadKeyProfile".equals(key)) {
			chooseFile(REQUEST_LOADKEY);
			return true;
		}
		if ("saveKeyProfile".equals(key)) {
			chooseFile(REQUEST_SAVEKEY);
			return true;
		}
		return super.onPreferenceTreeClick(screen, pref);
	}
	
	/**
	 * 生成按键映射控件
	 */
	private void onStartEmulatorSettings() {
		Activity context = getActivity();
		settings = PreferenceManager.getDefaultSharedPreferences(context);
		PreferenceGroup group = (PreferenceGroup) findPreference("gamepad");
		for (int i = 0; i < GAME_KEY_PREF.length; i++) {
			KeyPreference keyPref = new KeyPreference(context);
			keyPref.setKey(GAME_KEY_PREF[i]);
			keyPref.setTitle(GAME_KEY_NAME[i]);
			group.addPreference(keyPref);
		}
	}

	/**
	 * 选择配置文件
	 * @param requestCode
	 */
	private void chooseFile(int requestCode){
		Activity owner = getActivity();
		File dir = new File( ((MyApplication)owner.getApplication()).myDir, "keymap");
		if (!dir.exists() && !dir.mkdir())
			return;
		Intent intent = new Intent(owner, FileActivity.class)
			.putExtra("path", dir.getPath());
		if (requestCode == REQUEST_SAVEKEY){
			intent.putExtra("title","加载按键配置")
				.putExtra("mode", FileActivity.MODE_PICKFILE);
		} else {
			intent.putExtra("title","保存按键配置")
				.putExtra("mode", FileActivity.MODE_SAVEAS)
				.putExtra("saveAsName", keyFileName);
		}
		startActivityForResult(intent, requestCode);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) return;
		File file = new File(intent.getStringExtra("path"));
		switch (requestCode) {
			case REQUEST_LOADKEY:
				loadKeyProfile(file);
				keyFileName = file.getName();
				break;
			case REQUEST_SAVEKEY:
				saveKeyProfile(file);
				break;
		}
	}

	/**
	 * 加载按键配置
	 * @param file
	 */
	private void loadKeyProfile(File file){
		SharedPreferences.Editor editor = settings.edit();
		try {
			Pull pull = new Pull();
			pull.start(file);
			int type;
			while ((type = pull.parser.next()) != 1) {
				if (type != 2) continue;
				if ("key".equals(pull.parser.getName())) {
					String name = pull.getValue("name");
					int code = pull.getInt("code");
					((KeyPreference) findPreference(name)).setKey(code);
					editor.putInt(name, code);
				}
			}
			editor.apply();
		} catch (Exception e){}
	}
	
	/**
	 * 保存按键配置
	 * @param file
	 */
	private void saveKeyProfile(File file) {
		XmlWriter writer = new XmlWriter();
		writer.start("keys");
		for (String key : GAME_KEY_PREF) {
			writer.startTag("key");
			writer.attribute("name", key);
			writer.attribute("code", Integer.toString(((KeyPreference) findPreference(key)).getKeyValue()));
			writer.endTag("key");
		}
		writer.end();
		writer.write(file);
	}
}
