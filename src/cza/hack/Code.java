package cza.hack;

public class Code {
	public byte type, func;
	public long addr, value;
	public int dataSize, valueInc, addrInc;
	public boolean waitForSecond;
	private boolean mIsBatchMode;
	public StringBuilder mText = new StringBuilder();

	public void clear(){
		mText.delete(0, mText.length());
	}

	public void setText(String text){
		if (mIsBatchMode && mText.length() > 0) {
			addLine(text);
		} else {
			clear();
			mText.append(text);
		}
	}

	public void addLine(String line){
		mText.append('\n').append(line);
	}

	public String getText(){
		return mText.toString();
	}

	public void toBeGS(boolean v3){
		setText(CheatCoder.rawToGS(addr, value, v3));
	}

	public void toBeCB(){
		setText(CheatCoder.rawToCB(addr, value));
	}

	public void startBatchMode(){
		clear();
		mIsBatchMode = true;
	}

	public void endBatchMode(){
		mIsBatchMode = false;
	}
}
