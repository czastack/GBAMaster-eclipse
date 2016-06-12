package cza.gbamaster;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import cza.app.MenuAdapter;
import cza.app.Shortcut;

public class LauncherActivity extends BaseActivity implements 
		AdapterView.OnItemClickListener {

	private ListView listView;
	private MenuAdapter mAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		listView = (ListView) findViewById(R.id.list);
		listView.setOnItemClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.launcher, menu);
		mAdapter = new MenuAdapter(this, menu);
		listView.setAdapter(mAdapter);
		//真正的菜单
		inflater.inflate(R.menu.create_shortcut, menu);
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> adpt, View item, int position, long id) {
		Class<? extends Activity> klass = null;
		switch ((int)id) {
			case R.id.menu_mainActivity:
				klass = MainActivity.class;
				break;
			case R.id.menu_tools:
				klass = ToolsActivity.class;
				break;
			case R.id.menu_calculator:
				klass = CalculatorActivity.class;
				break;
			case R.id.menu_filebrowser:
				klass = FileActivity.class;
				break;
			case R.id.menu_package:
				klass = PackCotActivity.class;
				break;
			case R.id.menu_gbaCompress:
				klass = GBACompressActivity.class;
				break;
			case R.id.menu_codeBuilder:
				klass = CoderActivity.class;
				break;
			case R.id.menu_emulatorActivity:
				klass = EmulatorActivity.class;
				break;
			case R.id.menu_playActivity:
				klass = PlayActivity.class;
				break;
			case R.id.menu_cheatActivity:
				klass = CheatActivity.class;
				break;
			case R.id.menu_setting:
				klass = MainPreferenceActivity.class;
				break;
			case R.id.menu_createShortcut:
				createShortCut();
				return;
		}
		if (klass != null)
			bringToFront(klass);
	}

	/**
	 * 创建快捷方式
	 */
	public void createShortCut(){
		Shortcut shortcut = new Shortcut()
			.setTitle(getTitle())
			.setIcon(this, android.R.drawable.ic_menu_preferences)
			.setIntent(new Intent(this, getClass()));
		sendBroadcast(shortcut);
	}
	
}
