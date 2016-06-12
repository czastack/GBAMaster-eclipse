package cza.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class Clipboard {

	public static ClipboardManager getManager(Context context){
		return (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
	}

	public static void copy(Context context, CharSequence text) {
		ClipboardManager manager = getManager(context);
		ClipData cd = ClipData.newPlainText("label", text);
		manager.setPrimaryClip(cd);
	}

	public static String getText(Context context) {
		ClipboardManager manager = getManager(context);
		if (manager.hasPrimaryClip()) {
			ClipData cd = manager.getPrimaryClip();
			ClipData.Item item = cd.getItemAt(0);
			return item.getText().toString();
		}
		return null;
	}
}
