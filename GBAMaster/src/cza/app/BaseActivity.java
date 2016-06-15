package cza.app;

import java.io.File;
import java.lang.reflect.Method;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import cza.gbamaster.R;

public class BaseActivity extends Activity implements Dialog.OnClickListener {
	protected LayoutInflater mInflater;
	protected View mRootLayout;
	
	public static final String 
	INTENT_AUTO_OPEN = "autoOpen",
	INTENT_AUTO_MENU = "autoMenu",
	INTENT_CHANGED = "changed",
	INTENT_CHECKED = "checked",
	INTENT_PATH = "path",
	INTENT_SCREEN_TEXT = "screenText",
	INTENT_SCREEN_TYPE = "screenType",
	INTENT_TYPE = "type",
	INTENT_TITLE = "title",
	INTENT_MODE = "mode",
	INTENT_IS_MYBOY = "isMyBoy",
	INTENT_PLAYING = "playing";


	/**
	 * 显示ActionBar的返回图标
	 * 需在setContentView之前调用
	 */
	protected void displayHomeButton() {
		ActionBar actionbar = getActionBar();
		if (actionbar != null)
			actionbar.setDisplayHomeAsUpEnabled(true);
	}
	
	/**
	 * 全屏显示
	 * 需在setContentView之前调用
	 */
	protected void requestFullscree() {
		//设置全屏及隐藏标题栏
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		
	}
	
	@Override
	public void setContentView(int resId) {
		mInflater = LayoutInflater.from(this);
		setContentView(mRootLayout = inflateView(resId));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onHomeClick();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onHomeClick(){
		finish();
	}

	public View inflateView(int layoutResID){
		return mInflater.inflate(layoutResID, null);
	}
	
	public View findView(int id){
		return mRootLayout.findViewById(id);
	}
	
	/**
	 * 平板隐藏系统栏
	 */
	protected void hideSystemBar(boolean hidden){
		getWindow().getDecorView().setSystemUiVisibility(hidden ? 
				View.SYSTEM_UI_FLAG_LOW_PROFILE : 
				View.SYSTEM_UI_FLAG_VISIBLE);
	}
	
	/**
	 * 隐藏状态栏
	 */
	protected void hideStatusBar(){
		getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
			WindowManager.LayoutParams. FLAG_FULLSCREEN);
	}
	
	/**
	 * 获取屏幕尺寸
	 */
	protected Point getSize(){
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size;
	}
	
	/**
	 * 获取状态栏高度
	 * @return
	 */
	protected int getStatusBarHeight(){
		Rect frame = new Rect();  
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  
		return frame.top;
	}
	
	/**
	 * 获取标题栏高度
	 * @return
	 */
	protected int getTitleBarHeight(){
		int contentTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();  
		//statusBarHeight是上面所求的状态栏的高度  
		return contentTop - getStatusBarHeight();
	} 
	
	/**
	 * 菜单项显示图标
	 */
	protected void setIconEnable(Menu menu, boolean enable) {
		try {
			Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
			Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
			m.setAccessible(true);
			m.invoke(menu, enable);
		} catch (Exception e) {}
	}
	
	public void toast(CharSequence text){
		App.toast(this, text);
	}

	public void toast(int resId){
		App.toast(this, resId);
	}
	
	protected void alert(String title, CharSequence text){
		EditDialog dialog = new EditDialog(this, EditDialog.MODE_SHOW);
		dialog.setCopy();
		dialog.setTitle(title);
		dialog.setMessage(text);
		dialog.show();
	}
	
	protected void alert(Exception e){
		StringBuilder sb = new StringBuilder();
		sb.append(e.toString()).append('\n');
		StackTraceElement[] list = e.getStackTrace();
		for (int i = 0; i < list.length; i++){
			sb.append(i)
				.append('.')
				.append(list[i])
				.append('\n');
		}
		alert("错误", sb);
	}
	
	/**
	 * 打开之前的Activity
	 */
	protected void bringToFront(Class<? extends Activity> klass){
		startActivity(getToFrontIntent(klass));
	}
	
	public Intent getToFrontIntent(Class<? extends Activity> klass){
		return new Intent(this, klass)
			.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	}
	
	/**
	 * 检查文件
	 */
	private TextDialog fileNotFoundDialog;
	
	public boolean ensureFile(File file){
		if (file.exists())
			return true;
		TextDialog dialog = fileNotFoundDialog;
		if (dialog == null){
			fileNotFoundDialog = dialog = new TextDialog(this, 0);
			dialog.setTitle(R.string.fileNotFound);
			dialog.setButton(Dialog.BUTTON_POSITIVE, R.string.quite);
			dialog.setOnClickListener(this);
		}
		dialog.setMessage(getString(R.string.fileNotFoundBelow, file.getPath()));
		dialog.show();
		return false;
	}

	@Override
	public boolean onClick(Dialog dialog, int which) {
		if (fileNotFoundDialog == dialog) 
			finish();
		return true;
	}
}
