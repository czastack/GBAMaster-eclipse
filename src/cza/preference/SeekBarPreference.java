package cza.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import cza.gbamaster.R;

public class SeekBarPreference
extends DialogPreference
implements SeekBar.OnSeekBarChangeListener {
    private static final String NS = "http://androidemu.com/apk/res/android";
    private int maxValue;
    private int minValue;
    private int newValue;
    private int oldValue;
    private SeekBar seekBar;
    private TextView valueView;

	public SeekBarPreference(Context c, AttributeSet attr) {
		super(c, attr);
		minValue = attr.getAttributeIntValue(NS, "minValue", 0);
		maxValue = attr.getAttributeIntValue(NS, "maxValue", 100);
		setDialogLayoutResource(R.layout.seekbar_dialog);
		setPositiveButtonText("确定");
		setNegativeButtonText("取消");
	}

	protected void onBindDialogView(View v){
		super.onBindDialogView(v);
		seekBar = ((SeekBar)v.findViewById(R.id.seekbar));
		seekBar.setMax(maxValue - minValue);
		seekBar.setProgress(newValue - minValue);
		seekBar.setOnSeekBarChangeListener(this);
		valueView = ((TextView)v.findViewById(R.id.value));
		valueView.setText(Integer.toString(newValue));
	}

	protected void onDialogClosed(boolean changed){
		super.onDialogClosed(changed);
		if (changed){
			oldValue = newValue;
			persistInt(newValue);
			newValue = oldValue;
		} else {
			newValue = oldValue;
		}
	}

    protected Object onGetDefaultValue(TypedArray typedArray, int n) {
        return typedArray.getInteger(n, 0);
    }

    public void onProgressChanged(SeekBar seekBar, int n, boolean n2) {
        newValue = (n + minValue);
        valueView.setText(Integer.toString(newValue));
    }

    protected void onSetInitialValue(boolean bl, Object i) {
        newValue = oldValue = bl ? getPersistedInt(0) : (Integer)i;
    }

	public void onStartTrackingTouch(SeekBar seekBar) {}
	public void onStopTrackingTouch(SeekBar seekBar) {}
}

