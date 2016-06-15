package cza.app;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import cza.file.FileInfo;
import cza.file.FileInfos;
import cza.file.FileUtils;
import cza.gbamaster.R;

public class FileMoveDialog extends ListDialog implements View.OnClickListener {
	String mPath;
	View bar;
	private LinkedList<String> list;
	private FileInfos mInfos;
	private TextView state;
	private boolean isMove;
	private Callback mCallback;

	FileMoveDialog(Context c, View bar) {
		super(c);
		setTitle("存在同名文件（夹），继续？");
		list = new LinkedList<String>();
		setList(list);
		setItems(null, null);
		setConfirm();
		this.bar = bar;
		state = (TextView) bar.findViewById(R.id.moveState);
		bar.findViewById(R.id.ok).setOnClickListener(this);
		bar.findViewById(R.id.cancel).setOnClickListener(this);
	}

	public void startService(Callback callback, boolean isMove, FileInfos infos) {
		mCallback = callback;
		this.isMove = isMove;
		mInfos = infos;
		String msg = isMove ? "移动" : "复制";
		state.setText(msg + " " + mInfos.size() + "文件");
		bar.setVisibility(0);
		if (callback != null) {
			callback.onStartService();
		}
	}

	public void stopService() {
		bar.setVisibility(8);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ok) {
			if (mCallback != null) {
				mCallback.onAction();
			}
		} else {
			stopService();
		}
	}

	void test(String path) {
		list.clear();
		if (path.equals(mInfos.path)) {
			App.toast(getContext(), "目录相同");
			return;
		}
		File targetDir = new File(path);
		if (!targetDir.exists()) return;
		for (FileInfo info : mInfos.getInfos(true)) {
			if (path.startsWith(mInfos.path + "/" + info.name)) {
				App.toast(getContext(), "目标目录不能是子目录");
				return;
			}
		}
		stopService();
		mPath = path;
		Iterator<FileInfo> itr = mInfos.iterator();
		while (itr.hasNext()) {
			FileInfo info = itr.next();
			File file = new File(targetDir, info.name);
			if (file.exists()) {
				StringBuilder sb = new StringBuilder(info.name);
				if (info.isDir) {
					if (file.canRead()) {
						sb.append("\t" + file.list().length + "项目");
					}
				} else {
					sb.append("\t" + info.size);
				}
				if (!file.canWrite()) {
					sb.append("\t拒绝");
					info.checked = false;
					itr.remove();
				}
				list.add(sb.toString());
			}
		}
		if (list.size() > 0) {
			show();
		} else {
			action();
		}
	}

	void action() {
		for (FileInfo info : mInfos) {
			File from = new File(mInfos.path, info.name);
			File to = new File(mPath, info.name);
			if (isMove) {
				move(from, to);
			} else {
				copy(from, to);
			}
		}
		mCallback.onFinishMove();
	}

	@Override
	public boolean triggerClick(int which) {
		if(which == BUTTON_POSITIVE) {
			action();
		}
		return super.triggerClick(which);
	}

	private void copy(File f, File t) {
		if (!f.exists()) return;
		if (f.isDirectory()) {
			if (!f.canRead()) return;
			if (!t.exists()) {
				t.mkdir();
			} else if (t.isFile()) {
				return;
			}
			for (File ff : f.listFiles()) {
				File ft = new File(t, ff.getName());
				copy(ff, ft);
			}
		} else if (!t.isDirectory()) {
			FileUtils.copy(f, t);
		}
	}

	private void move(File f, File t) {
		if (!f.exists()) return;
		if (f.isDirectory()) {
			if (!f.canRead()) return;
			if (!t.exists()) {
				f.renameTo(t);
			} else if (t.isDirectory()) {
				for (File ff : f.listFiles()) {
					File ft = new File(t, ff.getName());
					move(ff, ft);
				}
				f.delete();
			}
		} else if (!t.isDirectory()) {
			f.renameTo(t);
		}
	}

	public interface Callback {
		public void onAction();
		public void onStartService();
		public void onFinishMove();
	}
}
