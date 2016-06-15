package cza.gbamaster;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import cza.preference.BasePreferenceFragment;
import cza.preference.EmulatorPreferenceFragment;
import cza.preference.MainPreferenceFragment;

public class MainPreferenceActivity extends BaseActivity {
	
	public static final int 
	LAUNCH_MAIN = 0,
	LAUNCH_EMULATOR = 1;
	public static final String INTENT_LAUNCH_ID = "launchId";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_layout);
		int launchId = getIntent().getIntExtra(INTENT_LAUNCH_ID, LAUNCH_MAIN);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		BasePreferenceFragment fragment;
		if (launchId == LAUNCH_MAIN)
			fragment = new MainPreferenceFragment();
		else 
			fragment = new EmulatorPreferenceFragment();
		fragmentTransaction.replace(android.R.id.content, fragment);
		fragmentTransaction.commit();
	}
	
	@Override
	protected void onHomeClick() {
		FragmentManager manager = getFragmentManager();
		if(manager.getBackStackEntryCount() > 0)
			manager.popBackStack();
		else
			super.onHomeClick();
	}
}
