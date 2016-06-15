package cza.gbamaster;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cza.file.FileUtils;
import cza.file.ZOS;
import cza.widget.MyAdapter;

public class GBACompressActivity extends BaseActivity implements 
		View.OnClickListener, 
		MyAdapter.Helper,
		Runnable {

	private static final int REQUEST_ADDFILE = 0;
	private CompressInfo[] mInfos;
	private String mDir;
	private MyAdapter dataAdapter;
	private TextView btn_compress, btn_browserResult;
	private View progressArea;
	private ProgressBar progressBar;
	private ListView listView;
	private TextView text_progress;
	private TextView text_state;
	

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gba_compress);
		findView(R.id.btn_addFile).setOnClickListener(this);
		btn_compress = (TextView)findView(R.id.btn_compress);
		btn_compress.setOnClickListener(this);
		btn_browserResult = (TextView)findView(R.id.btn_browserResult);
		btn_browserResult.setOnClickListener(this);
		text_state = (TextView)findView(R.id.compress_state);
		//进度条
		progressArea = findView(R.id.progressArea);
		progressBar = (ProgressBar)progressArea.findViewById(R.id.progressBar);
		text_progress = (TextView)progressArea.findViewById(R.id.text_progress);
		listView = (ListView)findView(R.id.list);
		dataAdapter = new MyAdapter();
		dataAdapter.setHelper(this);
		listView.setAdapter(dataAdapter);
		mDir = MyApplication.SD_PATH;
		reset();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_addFile:
				addFile();
				break;
			case R.id.btn_compress:
				new Thread(this).start();
				break;
			case R.id.btn_browserResult:
				browserResult();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;
		switch (requestCode) {
			case REQUEST_ADDFILE:
				addFile((Object[])data.getSerializableExtra("list"));
				break;
		}
	}

	/**
	 * 查看结果
	 */
	private void browserResult(){
		Intent intent = new Intent(this, FileActivity.class)
			.putExtra("path", mDir);
		startActivity(intent);
	}
	
	/**
	 * 选取文件
	 */
	private void addFile(){
		Intent intent = new Intent(this, FileActivity.class)
			.putExtra(INTENT_MODE, FileActivity.MODE_PICKFILES)
			.putExtra(INTENT_TITLE, getString(R.string.chooseTypeFile, "gba"))
			.putExtra(INTENT_PATH, mDir)
			.putExtra(INTENT_TYPE, "gba");
		startActivityForResult(intent, REQUEST_ADDFILE);
	}
	
	/**
	 * 添加文件进列表
	 */
	private void addFile(Object[] files){
		CompressInfo[] infos = new CompressInfo[files.length];
		for (int i = 0; i < files.length; i++)
			infos[i] = new CompressInfo((File)files[i]);
		mInfos = infos;
		mDir = infos[0].originalFile.getParent();
		changeData();
		ready();
	}


	/**
	 * 初始化
	 */
	 private void reset(){
		 btn_compress.setEnabled(false);
		 btn_browserResult.setVisibility(View.GONE);
	 }
	 
	/**
	 * 选择文件后
	 */
	private void ready(){
		btn_compress.setEnabled(true);
		btn_compress.setVisibility(View.VISIBLE);
		btn_browserResult.setVisibility(View.GONE);
		text_state.setText(R.string.ready);
	}
	
	/**
	 * 开始压缩
	 */
	private void startCompress(){
		long originalSize = 0;
		long finalSize = 0;
		File backupDir = new File(mDir, "backup");
		backupDir.mkdir();
		//开始
		handler.obtainMessage(MESSAGE_PROGRESS_START).sendToTarget();
		for (int i = 0; i < mInfos.length; i++){
			try {
				CompressInfo info = mInfos[i];
				File gba = info.originalFile;
				File zip = FileUtils.withType(gba, "zip");
				if (zip.exists()){
					info.finalSizeString = getString(R.string.fileExists);
				} else {
					long zipSize;
					ZOS zos = new ZOS(zip);
					zos.put(info.name, gba);
					zos.close();
					zipSize = zip.length();
					info.finalSizeString = FileUtils.size(zipSize);
					originalSize += info.originalSize;
					finalSize += zipSize;
					//更新进度
					handler.obtainMessage(MESSAGE_PROGRESS_NEXT, i + 1, 0).sendToTarget();
					//移动文件
					gba.renameTo(new File(backupDir, info.name));
				}
			} catch (Exception e){}
		}
		String savedSize = FileUtils.size(originalSize - finalSize);
		String state = getString(R.string.compress_complete, savedSize, backupDir.getPath());
		//结束
		handler.obtainMessage(MESSAGE_PROGRESS_STOP, state).sendToTarget();
	}

	public void run(){
		startCompress();
	}
	 
	/**
	 * 刷新列表
	 */
	private void changeData() {
		dataAdapter.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mInfos != null ? mInfos.length : 0;
	}

	@Override
	public View getView(int position, View item) {
		Holder holder;
		if (item == null) {
			holder = new Holder();
			item = inflateView(R.layout.activity_gba_compress_row);
			holder.findView(item);
			item.setTag(holder);
		} else {
			holder = (Holder) item.getTag();
		}
		holder.set(mInfos[position]);
		return item;
	}
	
	private class Holder{
		private TextView gbaName;
		private TextView gbaSize;
		private TextView zipSize;

		public void findView(View parent){
			gbaName = (TextView)parent.findViewById(R.id.gba_name);
			gbaSize = (TextView)parent.findViewById(R.id.gba_size);
			zipSize = (TextView)parent.findViewById(R.id.zip_size);
		}
		
		public void set(CompressInfo info){
			gbaName.setText(info.name);
			gbaSize.setText(info.originalSizeString);
			zipSize.setText(info.finalSizeString);
		}
	}
	
	
	private static final int 
	MESSAGE_PROGRESS_START = 0,
	MESSAGE_PROGRESS_NEXT = 1,
	MESSAGE_PROGRESS_STOP = 2;
	
	static class CompressHandler extends Handler {
		private WeakReference<GBACompressActivity> mRef;
		
		CompressHandler(GBACompressActivity activity){
			mRef = new WeakReference<GBACompressActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case MESSAGE_PROGRESS_START:
					mRef.get().progressStart();
					break;
				case MESSAGE_PROGRESS_NEXT:
					mRef.get().setProcess(msg.arg1);
					break;
				case MESSAGE_PROGRESS_STOP:
					mRef.get().progressStop(msg.obj);
					break;
				default:
					super.handleMessage(msg);
			}
		}
	}
		
	
	private CompressHandler handler = new CompressHandler(this);
	
	
	/**
	 * 压缩进度的进度框
	 */
	private void progressStart(){
		btn_compress.setEnabled(false);
		progressBar.setMax(mInfos.length);
		setProcess(0);
		progressArea.setVisibility(View.VISIBLE);
	}
	
	private void setProcess(int index){
		progressBar.setProgress(index);
		text_progress.setText(getString(R.string.progress, index, mInfos.length));
		changeData();
	}
	
	private void progressStop(Object state){
		progressArea.setVisibility(View.GONE);
		text_state.setText((String)state);
		btn_compress.setVisibility(View.GONE);
		btn_browserResult.setVisibility(View.VISIBLE);
	}
}

class CompressInfo {
	public String name, originalSizeString, finalSizeString;
	public long originalSize;
	public File originalFile;
	public File availableFile;
	
	
	public CompressInfo(File file){
		originalFile = availableFile = file;
		name = file.getName();
		originalSize = file.length();
		originalSizeString = FileUtils.size(originalSize);
	}
}
