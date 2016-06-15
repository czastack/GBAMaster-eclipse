package cza.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

public class Shortcut extends Intent {
	public Shortcut(){
		super("com.android.launcher.action.INSTALL_SHORTCUT");
		putExtra("duplicate", false);                            
	}

	public Shortcut setTitle(CharSequence name){
		putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
		return this;
	}

	public Shortcut setIcon(Context c, int resId){
		putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
			Intent.ShortcutIconResource.fromContext(c, resId));
		return this;
	}

	public Shortcut setIcon(Bitmap bp){
		putExtra(Intent.EXTRA_SHORTCUT_ICON, bp);
		return this;
	}

	public Shortcut setIntent(Intent intent){
		putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		return this;
	}
}
