package cza.preference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import cza.gbamaster.R;

public class KeyPreference extends DialogPreference
		implements DialogInterface.OnKeyListener {
    private int newValue;
    private int oldValue;

    public KeyPreference(Context context) {
		this(context, null);
	}

	public KeyPreference(Context context, AttributeSet attr) {
		super(context, attr);
		setPositiveButtonText(R.string.clear);
		setDefaultValue(0);
	}

    private String getKeyName(int keyCode) {
		switch (keyCode) {
			case 29: return "A";
			case 30: return "B";
			case 31: return "C";
			case 32: return "D";
			case 33: return "E";
			case 34: return "F";
			case 35: return "G";
			case 36: return "H";
			case 37: return "I";
			case 38: return "J";
			case 39: return "K";
			case 40: return "L";
			case 41: return "M";
			case 42: return "N";
			case 43: return "O";
			case 44: return "P";
			case 45: return "Q";
			case 46: return "R";
			case 47: return "S";
			case 48: return "T";
			case 49: return "U";
			case 50: return "V";
			case 51: return "W";
			case 52: return "X";
			case 53: return "Y";
			case 54: return "Z";
			case 7: return "0";
			case 8: return "1";
			case 9: return "2";
			case 10: return "3";
			case 11: return "4";
			case 12: return "5";
			case 13: return "6";
			case 14: return "7";
			case 15: return "8";
			case 16: return "9";
			case 57: return "ALT (left)";
			case 58: return "ALT (right)";
			case 59: return "SHIFT (left)";
			case 60: return "SHIFT (right)";
			case 62: return "SPACE";
			case 67: return "DEL";
			case 66: return "ENTER";
			case 77: return "@";
			case 56: return ".";
			case 55: return ",";
			case 23: return "DPAD Center";
			case 19: return "DPAD Up";
			case 20: return "DPAD Down";
			case 21: return "DPAD Left";
			case 22: return "DPAD Right";
			case 4: return "BACK";
			case 5: return "CALL";
			case 27: return "CAMERA";
			case 80: return "FOCUS";
			case 84: return "SEARCH";
			case 24: return "Volume UP";
			case 25: return "Volume DOWN";
			case 0: return "无";
		}
		return "未知键";
	}

	private static boolean isKeyConfigurable(int keyCode) {
		switch (keyCode) {
			case 3:
			case 26:
			case 82:
				return false;
		}
		return true;
	}

    private void updateSummary() {
		setSummary(getKeyName(newValue));
	}

    public final int getKeyValue() {
        return newValue;
    }

    public void onClick(DialogInterface d, int n) {
        if (n == -1) {
            newValue = 0;
        }
		super.onClick(d, n);
    }

	protected void onDialogClosed(boolean changed) {
		if (changed) {
			oldValue = newValue;
			persistInt(newValue);
			updateSummary();
			return;
		} else {
			newValue = oldValue;
		}
	}

    protected Object onGetDefaultValue(TypedArray typedArray, int n) {
        return typedArray.getInteger(n, 0);
    }

	public boolean onKey(DialogInterface d, int n, KeyEvent event) {
		if (KeyPreference.isKeyConfigurable(n)) {
			newValue = n;
			super.onClick(d, -1);
			d.dismiss();
			return true;
		}
		return false;
	}

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setTitle(getTitle())
			.setMessage("请按下设备上的一个键...")
			.setOnKeyListener(this);
	}

	protected void onSetInitialValue(boolean bl, Object i) {
		int n = bl ? getPersistedInt(0) : (Integer)i;
		newValue = oldValue = n;
		updateSummary();
	}

	public final void setKey(int key) {
		newValue = oldValue = key;
		updateSummary();
	}

    protected void showDialog(Bundle bundle) {
        super.showDialog(bundle);
        Dialog dialog = this.getDialog();
        if (dialog != null) {
			dialog.getWindow().clearFlags(131072);
		}
    }
}

