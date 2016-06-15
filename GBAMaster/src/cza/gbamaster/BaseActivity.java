package cza.gbamaster;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends cza.app.BaseActivity {

	public static final String PREF_COT_MAP = "cotmap";
	
	protected void onCreate(Bundle savedInstanceState) {
		selectTheme();
        super.onCreate(savedInstanceState);
        displayHomeButton();
	}
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.to_launcher, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_launcher:
				bringToFront(LauncherActivity.class);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 自动选择主题
	 */
	protected void selectTheme(){
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("night_theme", false))
			setTheme(R.style.night);
	}
}
