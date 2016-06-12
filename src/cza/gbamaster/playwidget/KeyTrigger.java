package cza.gbamaster.playwidget;

import java.util.ArrayList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import cza.app.BaseContextMenu;
import cza.app.ContextMenu;
import cza.app.Dialog;
import cza.app.ListDialog;
import cza.app.ListViewController;
import cza.gbamaster.PlayActivity;
import cza.gbamaster.R;
import cza.preference.EmulatorPreferenceFragment;
import cza.widget.MyAdapter;

public class KeyTrigger extends PlayWidget implements 
		View.OnClickListener, 
		Dialog.OnClickListener,
		ListDialog.OnSubmitListener,
		ContextMenu.Callback,
		MyAdapter.Helper,
		Runnable {

	private Dialog mConfigDialog;
	private TextView mTextView;
	private TextView mDelayView;
	private ListView mEventListView;
	private ListViewController mListCotroller;
	private ListDialog mAddKeyDialog;
	private ArrayList<Event> mEventList;
	
	public KeyTrigger(PlayActivity owner) {
		super(owner, new Button(owner));
		setSize(ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mLayout.setOnClickListener(this);
		mEventList = new ArrayList<Event>();
		showConfigDialog();
		ContextMenu contextMenu = new ContextMenu(mLayout);
		contextMenu.setList(R.array.menu_key_trigger);
		contextMenu.setCallback(this);
	}

	@Override
	public void onClick(View v) {
		if (v == mLayout){
			new Thread(this).start();
		} else {
			int id = v.getId();
			switch(id){
			case R.id.btn_deleteEvent:
				break;
			case R.id.btn_addDelay:
				onAddDelay();
				break;
			case R.id.btn_addKey:
				onAddKey();
				break;
			}
		}
	}

	@Override
	public boolean onShowMenu(BaseContextMenu dialog) {
		return true;
	}

	/**
	 * 上下文菜单
	 */
	@Override
	public void onItemClick(BaseContextMenu dialog, int position) {
		switch (position){
		case 0:
			showConfigDialog();
			break;
		case 1:
			mLayout.setOnTouchListener(this);
			break;
		case 2:
			destroy();
			break;
		}
	}
	
	/**
	 * 配置对话框
	 */
	private void showConfigDialog(){
		Dialog dialog = mConfigDialog;
		if (dialog == null){
			mConfigDialog = dialog = new Dialog(mLayout.getContext());
			dialog.setTitle(R.string.keyTrigger);
			dialog.setConfirm();
			dialog.setView(R.layout.dialog_keytrigger_config);
			mTextView = (TextView)dialog.findView(R.id.text);
			mDelayView = (TextView)dialog.findView(R.id.delay);
			dialog.findView(R.id.btn_deleteEvent).setOnClickListener(this);
			dialog.findView(R.id.btn_addDelay).setOnClickListener(this);
			dialog.findView(R.id.btn_addKey).setOnClickListener(this);
			mEventListView = (ListView)dialog.findView(R.id.list);
			mListCotroller = new ListViewController(mEventListView);
			mListCotroller.setItems(this, null, ListViewController.MODE_SINGLE);
			dialog.setOnClickListener(this);
		}
		dialog.show();
	}

	/**
	 * 完成配置
	 */
	@Override
	public boolean onClick(Dialog dialog, int which) {
		if (dialog == mConfigDialog && which == Dialog.BUTTON_POSITIVE){
			((Button)mLayout).setText(mTextView.getText());
		}
		return true;
	}
	
	/**
	 * 添加按键事件
	 */
	private void onAddKey(){
		ListDialog dialog = mAddKeyDialog;
		if (dialog == null){
			mAddKeyDialog = dialog = new ListDialog(mLayout.getContext());
			dialog.setTitle(R.string.addKey);
			dialog.setItems(EmulatorPreferenceFragment.GAME_KEY_NAME, null, null);
			dialog.setOnSubmitListener(this);
		}
		dialog.checkAll(false);
		dialog.show();
	}

	/**
	 * 完成添加按键
	 */
	@Override
	public void onSubmit(ListViewController dialog, int[] checkedIndexs) {
		int keyCode = 0;
		String keyNames;
		if (checkedIndexs.length != 0){
			StringBuilder sb = new StringBuilder();
			for (int i : checkedIndexs){
				keyCode |= EmulatorPreferenceFragment.GAME_KEY_CODE[i];
				sb.append(EmulatorPreferenceFragment.GAME_KEY_NAME[i]).append('、');
			}
			sb.deleteCharAt(sb.length() - 1);
			keyNames = sb.toString();
		} else {
			keyNames = "空";
		}
		Event event = new Event();
		event.type = Event.TYPE_KEY;
		event.data = keyCode;
		event.text = keyNames;
		mEventList.add(event);
		mListCotroller.refresh();
	}
	
	/**
	 * 添加延迟
	 */
	private void onAddDelay(){
		if (mDelayView.getText().length() == 0)
			return;
		Event event = new Event();
		event.type = Event.TYPE_DELAY;
		event.data = Integer.parseInt(mDelayView.getText().toString());
		event.text = mLayout.getContext().getString(R.string.delayText, event.data);
		mEventList.add(event);
		mListCotroller.refresh();
	}

	/**
	 * 执行事件
	 */
	@Override
	public void run() {
		for (Event event : mEventList){
			if (event.type == Event.TYPE_KEY) {
				Log.d("测试", event.data + "");
				mOwner.getEmulator().setKeyStates(event.data);
			}
			else if(event.type == Event.TYPE_DELAY)
				try {
					Thread.sleep(event.data);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}

	@Override
	public int getCount() {
		return mEventList.size();
	}

	@Override
	public View getView(int position, View item) {
		if (item == null) 
			item = LayoutInflater.from(mLayout.getContext()).inflate(android.R.layout.simple_list_item_1, null);
		((TextView) item).setText(mEventList.get(position).text);
		return item;
	}
	

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getActionMasked();
		if (action == MotionEvent.ACTION_MOVE)
			setPosition((int)event.getRawX(), (int)event.getRawY());
		else if(action == MotionEvent.ACTION_UP)
			mLayout.setOnTouchListener(null);
		return true;
	}
	
	private class Event {
		public final static int TYPE_KEY = 0;
		public final static int TYPE_DELAY = 1;
		public int type;
		public int data;
		public String text;
	}
	
}
