package cza.preference;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import cza.gbamaster.R;

public class MainPreferenceFragment extends BasePreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_main);
	}

	public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference pref) {
		String key = pref.getKey();
		if ("emu".equals(key)) {
			onStartEmulatorSettings();
			return true;
		}
		return super.onPreferenceTreeClick(screen, pref);
	}

	/**
	 * 内置模拟器设置
	 */
	private void onStartEmulatorSettings() {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		EmulatorPreferenceFragment fragment = new EmulatorPreferenceFragment();
		fragmentTransaction.replace(android.R.id.content, fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
}
