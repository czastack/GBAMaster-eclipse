package cza.app;

import java.io.File;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;
import cza.file.FileUtils;
import cza.gbamaster.R;

public class App extends Application {
	public static File SD;
	public static String SD_PATH;
	public static boolean SD_EXIST;

	public static float SCALE_DIP;
	public static final Collator COMPARATOR = Collator.getInstance(java.util.Locale.CHINA);
	//设置
	public static final String KEY_NOTFIRST = "notFirst";

	@Override
	public void onCreate() {
		super.onCreate();
		SCALE_DIP = this.getResources().getDisplayMetrics().density;
		initFile();
	}

	protected void initFile(){
		SD_EXIST = Environment.getExternalStorageState()
			.equals(android.os.Environment.MEDIA_MOUNTED);
		if (SD_EXIST){
			SD = Environment.getExternalStorageDirectory();
			SD_PATH = SD.getPath();
		}
	}

	public static boolean isSD(File folder) {
		return folder.getPath().equals(SD_PATH);
	}

	public static void toast(Context context, CharSequence text){
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static void toast(Context context, int resId){
		toast(context, context.getString(resId));
	}

	/**
	 * 用程序打开文件
	 */
	public static Intent openFile(File file) throws Exception{
		if (!file.exists() || !file.canRead()){
			throw new Exception();
		}
		String type = FileUtils.getType(file);
		return new Intent()
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.setAction(Intent.ACTION_VIEW)
			.setDataAndType(Uri.fromFile(file), FileUtils.getMIMEType(type));
	}

	/**
	 * 分享文件
	 */
	public static void shareFiles(Context context, File[] files){
		int length = files.length;
		boolean multiple = false;
		if (length == 0)
			return;
		else if (length > 1)
			multiple = true;
		Intent intent = new Intent();
		if (multiple){
			ArrayList<Uri> uris = new ArrayList<Uri>(length);
			for (File file : files) 
				uris.add(Uri.fromFile(file));
			intent.setAction(android.content.Intent.ACTION_SEND_MULTIPLE)
				.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
				.setType("*/*");
		} else {
			intent.setAction(android.content.Intent.ACTION_SEND)
				.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(files[0]))
				.setType(FileUtils.getMIMEType(files[0]));
		}
		context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
	}

	/**
	 * 打开图片资源
	 */
	public static Bitmap readBitmap(Context context, int resId){
		BitmapFactory.Options opt = new BitmapFactory.Options();  
		opt.inPreferredConfig = Bitmap.Config.RGB_565;   
		opt.inPurgeable = true;  
		opt.inInputShareable = true;  
		//获取资源图片  
		InputStream is = context.getResources().openRawResource(resId);  
		return BitmapFactory.decodeStream(is, null, opt);
	}

	public static int dip2px(float dip){
		return (int)(dip * SCALE_DIP + 0.5f);
	}
	
	/**
	 * 获取主题的背景颜色
	 * @return
	 */
	public static int getThemeBackground(Context context){
		TypedArray array = context.getTheme().obtainStyledAttributes(new int[] {  
			    android.R.attr.colorBackground, 
			    //android.R.attr.textColorPrimary, 
			});
		//int textColor = array.getColor(1, 0xFF00FF);
		array.recycle();
		return array.getColor(0, 0);
	}
}

