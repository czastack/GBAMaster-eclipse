package cza.hack;

import android.graphics.Bitmap;

import java.io.File;

import cza.file.FileUtils;

public class GameST {
	public int index, mIndex;
	public long time;
	public Bitmap bp;
	public String timeStr;
	public String path;
	public void setTime(File ss){
		time = ss.lastModified();
		timeStr = FileUtils.timer.format(time);
	}
	
	public void recycle(){
		if (bp != null)
			bp.recycle();
	}
}
