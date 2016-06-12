package cza.gbamaster;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import cza.app.Shortcut;
import cza.util.ViewUtils;

public abstract class TwoEditActivity extends ClickActivity implements 
		View.OnClickListener,
		View.OnTouchListener {

	protected EditText iet, oet;
	protected Button toUp;

    protected void initView(){
		iet = (EditText) findView(R.id.iet);
		oet = (EditText) findView(R.id.oet);
		iet.setOnTouchListener(this);
		oet.setOnTouchListener(this);
		registerClick(R.id.btn_clear, R.id.btn_paste, R.id.btn_go, R.id.btn_copy, R.id.btn_toUp);
		toUp = (Button) findViewById(R.id.btn_toUp);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.create_shortcut, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_createShortcut:
				createShortCut();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			case R.id.btn_clear:
			case R.id.btn_paste:
				ViewUtils.edit(iet, id);
				break;
			case R.id.btn_go:
				go();
				break;
			case R.id.btn_copy:
				ViewUtils.edit(oet, id);
				break;
			case R.id.btn_toUp:
				iet.setText(oet.getText());
				break;
		}
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
			case R.id.iet:
			case R.id.oet:
				// 解决scrollView中嵌套EditText导致不能上下滑动的问题
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_DOWN:
						v.getParent().requestDisallowInterceptTouchEvent(true);
						break;
					case MotionEvent.ACTION_UP:
						v.getParent().requestDisallowInterceptTouchEvent(false);
						break;
				}
		}
		return false;
	}
	
	abstract protected void go();
	
	//快捷方式
	protected int shortcutIcon;
	protected Intent shortcutIntent;
	protected void createShortCut(){
		Shortcut shortcut = new Shortcut()
			.setTitle(getTitle())
			.setIcon(this, shortcutIcon)
			.setIntent(new Intent(this, getClass()));
		sendBroadcast(shortcut);
	}
}
