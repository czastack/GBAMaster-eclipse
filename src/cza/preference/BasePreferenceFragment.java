package cza.preference;

import android.app.Dialog;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class BasePreferenceFragment extends PreferenceFragment implements View.OnClickListener {

	/**
	 * 显示返回健
	 * @param preferenceScreen
	 */
	protected void dispalyHomeButton(PreferenceScreen preferenceScreen) {
		Dialog dialog = preferenceScreen.getDialog();
		if (dialog != null){
			dialog.getActionBar().setDisplayHomeAsUpEnabled(true);
			View homeBtn = dialog.findViewById(android.R.id.home);
			if (homeBtn != null) {
				View view = (View) homeBtn.getParent();
				if (view instanceof FrameLayout) {
					if (view.getParent() instanceof LinearLayout) {
						view = (View) view.getParent();
					}
				} else {
					view = homeBtn;
				}
				view.setTag(dialog);
				view.setOnClickListener(this);
			}
		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if (preference instanceof PreferenceScreen)
			dispalyHomeButton((PreferenceScreen) preference);
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	@Override
	public void onClick(View view) {
		Object tag = view.getTag();
		if (tag instanceof Dialog)
			((Dialog)tag).dismiss();
	}
}
