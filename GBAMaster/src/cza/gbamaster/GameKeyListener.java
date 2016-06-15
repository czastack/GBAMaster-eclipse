package cza.gbamaster;

public interface GameKeyListener {
	int KEY_OUTER = -1;
	int KEY_LOAD = -2;
	int KEY_SAVE = -3;
    public void onGameKeyChanged();
	public void onOuter();
	public void quickLoad();
	public void quickSave();
}

