package com.androidemu;

import java.nio.Buffer;
import java.util.ArrayList;

import android.content.Context;
import android.os.Build;
import android.view.SurfaceHolder;

public class Emulator {
	public static final int 
	GAMEPAD_UP		= 0x0040,
	GAMEPAD_DOWN	= 0x0080,
	GAMEPAD_LEFT	= 0x0020,
	GAMEPAD_RIGHT	= 0x0010,
	GAMEPAD_A		= 0x0001,
	GAMEPAD_B		= 0x0002,
	GAMEPAD_SELECT	= 0x0004,
	GAMEPAD_START	= 0x0008,
	GAMEPAD_TL		= 0x0200,
	GAMEPAD_TR		= 0x0100,
	GAMEPAD_A_TURBO	= (GAMEPAD_A << 16),
	GAMEPAD_B_TURBO	= (GAMEPAD_B << 16),
	GAMEPAD_LEFT_RIGHT = GAMEPAD_LEFT | GAMEPAD_RIGHT,
	GAMEPAD_UP_DOWN = GAMEPAD_UP | GAMEPAD_DOWN,
	GAMEPAD_DIRECTION = GAMEPAD_UP_DOWN | GAMEPAD_LEFT_RIGHT;
	
	public static final int 
		VIDEO_H = 160,
		VIDEO_W = 240;
    private static Emulator emulator;
    private ArrayList<String> cheats;
    private final static EmuMedia emuMedia = null; // 让编译器引入EmuMedia

    private Emulator(String libDir, String dataDir) {
        int version = Build.VERSION.SDK_INT;
        if (version < 5 && Build.MODEL.contains("Archos")) 
            version = 5;
        initialize(libDir, dataDir, version);
    }

    public static Emulator createInstance(Context context, String dataDir) {
		if (emulator == null) {
			String libDir = "/data/data/com.androidemu.gba/lib/";
			System.loadLibrary("master");
			emulator = new Emulator(libDir, dataDir);
		}
		return emulator;
	}

    public static Emulator getInstance() {
        return emulator;
    }

    public boolean loadROM(String path) {
        if (!nativeLoadROM(path)) 
            return false;
        cheats = new ArrayList<String>();
        return true;
    }

    public void unloadROM() {
        nativeUnloadROM();
        cheats = null;
    }

	public boolean addCheat(String code){
		cheats.add(code);
		return nativeAddCheat(code);
	}

	public void removeCheat(String code){
		cheats.remove(code);
		nativeRemoveCheat(code);
	}

	public void destroyCheat(){
		for (String code : cheats){
			nativeRemoveCheat(code);
		}
		cheats.clear();
	}


	private native boolean initialize(String libDir, String dataDir, int version);
	private native boolean nativeLoadROM(String path);
	private native void nativeUnloadROM();
	public native void getScreenshot(Buffer bf);
	public native boolean loadBIOS(String path);
	public native boolean loadState(String path);
	public native void pause();
	public native void power();
	public native void reset();
	public native void resume();
	public native boolean saveState(String path);
	public native void setKeyStates(int key);
	public native void setOption(String key, String value);
	public native void setSurface(SurfaceHolder surface);
	public native void setSurfaceRegion(int width, int height, int paddingX, int paddingY, int realWidth, int realHeight);
	
	public native boolean nativeAddCheat(String code);
    public native void nativeRemoveCheat(String code);
	
	public native long readBytes(long addr, int size);
	public native void writeBytes(long addr, long value, int size);

	public void setOption(String key, int value) {
		setOption(key, Integer.toString(value));
	}

	public void setOption(String key, boolean value) {
		setOption(key, Boolean.toString(value));
	}
}

