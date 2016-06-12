package cza.gbamaster;

import java.io.File;
import java.io.FileFilter;

import android.os.Bundle;
import android.view.View;
import cza.file.ZOS;
import cza.widget.BtnBar;

public class PackCotActivity extends BaseActivity implements View.OnClickListener, FileFilter {
	private File[] files;
	private String[] names;
	private File dir;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		BtnBar root = new BtnBar(this, 1, false);
		dir = ((MyApplication)getApplication()).cotDir;
		files = dir.listFiles(this);
		int len = files.length;
		names = new String[len];
		for (int i = 0; i < len; i++)
			names[i] = files[i].getName();
		root.addButton(this, names);
		setContentView(root);
	}
	
	@Override
	public void onClick(View btn) {
		compress((Integer)btn.getTag());
	}
	
	@Override
	public boolean accept(File f) {
		return f.isDirectory();
	}
	
	private void compress(int i){
		File cot = new File(dir, names[i] + ".cot");
		File cotDir = files[i];
		try {
			ZOS zos = new ZOS(cot);
			for (String entry : cotDir.list()){
				zos.put(entry, new File(cotDir, entry));
			}
			zos.close();
		} catch (Exception e) {
			toast("打包失败：" + e.toString());
		}
		toast("打包成功："+ cot.getPath());
	}
}
