package cza.gbamaster;

import android.view.KeyEvent;
import android.view.View;

public class Keyboard implements View.OnKeyListener {
	private GameKeyListener gameKeyListener;
	private int keyStates;
	private int[] keysMap = new int[128];

	public Keyboard(View view, GameKeyListener l) {
		gameKeyListener = l;
		view.setOnKeyListener(this);
	}

	public void clearKeyMap() {
		for (int i = 0; i < keysMap.length; i++){
			keysMap[i] = 0;
		}
	}

	public final int getKeyStates() {
		return keyStates;
	}

	public void mapKey(int n, int index) {
		if ((index >= 0) && (index < keysMap.length)) {
			keysMap[index] = n | keysMap[index];
		}
	}

	public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
		if (keyCode >= keysMap.length) {
			return false;
		}
		int gameKey = keysMap[keyCode];
		if (gameKey == 0) return false;
		if (keyEvent.getRepeatCount() != 0) return true;
		keyStates = (keyEvent.getAction() == 0) ? (gameKey | keyStates) : (keyStates & (gameKey ^ -1));
		gameKeyListener.onGameKeyChanged();
		return true;
	}

	public void reset() {
		keyStates = 0;
	}
}

