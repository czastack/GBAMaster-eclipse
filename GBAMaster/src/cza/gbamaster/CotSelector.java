package cza.gbamaster;

import java.io.File;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import cza.app.BaseContextMenu;
import cza.file.FileTypeFilter;

public class CotSelector extends BaseContextMenu {
	public File mCotDir;
	public String mCotName;
	public String mGameName;
	public boolean changed;
	public SharedPreferences mCotmap;
	
	public CotSelector(BaseActivity owner, String gameName){
		super(owner);
		setTitle(R.string.chooseCot);
		mCotmap = owner.getSharedPreferences(BaseActivity.PREF_COT_MAP, BaseActivity.MODE_PRIVATE);
		mGameName = gameName;
		mCotName = mCotmap.getString(gameName, null);
		mCotDir = ((MyApplication)owner.getApplication()).cotDir;
	}

	@Override
	public void show() {
		if (mCotDir.exists()) {
			String[] list = mCotDir.list(new FileTypeFilter("cot"));
			setList(list);
			super.show();
		} else 
			MyApplication.toast(getContext(), R.string.noCotDir);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View item, int index, long itemId) {
		String name = getText(index);
		if (!name.equals(mCotName)){
			mCotName = name;
			changed = true;
		}
		super.onItemClick(adapterView, item, index, itemId);
	}
	
	public boolean isSelected(){
		return mCotName != null;
	}
	
	public File getCotFile(){
		return new File(mCotDir, mCotName);
	}
	
	public void writePref(){
		SharedPreferences.Editor editor = mCotmap.edit();
		if (mCotName == null)
			editor.remove(mGameName);
		else
			editor.putString(mGameName, mCotName);
		editor.commit();
	}
	
	public void unbind(){
		mCotName = null;
	}
}
