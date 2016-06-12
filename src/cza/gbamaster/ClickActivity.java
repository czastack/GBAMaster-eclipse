package cza.gbamaster;

import android.view.View;
import cza.util.ViewUtils;

public abstract class ClickActivity extends BaseActivity implements View.OnClickListener {
	protected void registerClick(int...ids){
		ViewUtils.registerClick(mRootLayout, this, ids);
	}

	protected void registerClick(View parent, int...ids){
		ViewUtils.registerClick(parent, this, ids);
	}
}
