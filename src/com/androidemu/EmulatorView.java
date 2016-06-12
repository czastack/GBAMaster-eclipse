package com.androidemu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class EmulatorView extends SurfaceView {
    public static final int 
	SCALING_2X = 1,
    SCALING_ORIGINAL = 0,
    SCALING_PROPORTIONAL = 2,
    SCALING_STRETCH = 3;
    private int scalingMode = SCALING_PROPORTIONAL;

    public EmulatorView(Context c, AttributeSet attr) {
        super(c, attr);
        SurfaceHolder holder = getHolder();
        holder.setFormat(4);
        holder.setKeepScreenOn(true);
    }
 
	private void updateSurfaceSize() {
		int w = getWidth();
		int h = getHeight();
		if (w == 0 || h == 0) 
			return;
		int mH = 0;
		int mW = 0;
		switch (scalingMode) {
			case SCALING_ORIGINAL:
				mW = w;
				mH = h;
				break;
			case SCALING_2X:
				mW = w / 2;
				mH = h / 2;
				break;
			case SCALING_STRETCH:
				if (w < h)
					break;
				mW = Emulator.VIDEO_W;
				mH = Emulator.VIDEO_H;
				break;
		}
        if ((mW < Emulator.VIDEO_W || mH < Emulator.VIDEO_H) && ((mW = (((mH = Emulator.VIDEO_H) * w) / h)) < Emulator.VIDEO_W)) {
            mW = 240;
            mH = ((mW * h) / w);
        }
        this.getHolder().setFixedSize(-4 & (mW + 3), -4 & (mH + 3));
    }

    protected void onSizeChanged(int l, int t, int r, int b) {
        this.updateSurfaceSize();
    }

    public void setScalingMode(int n) {
        if (scalingMode == n)
			return;
        scalingMode = n;
        updateSurfaceSize();
    }
}

