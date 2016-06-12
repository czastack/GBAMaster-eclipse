package cza.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;

public class SpinnerEditText extends EditText implements View.OnTouchListener {

	private Bitmap btnBp;
	private boolean btnShowing;
	private int btnLeft;
	protected PopupWindow dropDownArea;
	public ListView listView;

	public SpinnerEditText(Context c) {
		this(c, null);
	}
	
	public SpinnerEditText(Context context, AttributeSet attr) {
		super(context, attr);
		listView = new ListView(context);
		PopupWindow box = new PopupWindow(context);
		box.setFocusable(true);
		box.setOutsideTouchable(true);
		box.setTouchable(true);
		box.setHeight(-2);
		box.setContentView(listView);
		dropDownArea = box;
		
		btnBp = BitmapFactory.decodeResource(getResources(),
			android.R.drawable.ic_menu_more);
		setOnTouchListener(this);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = getWidth();
		btnLeft = width - btnBp.getWidth();
		if (btnShowing){
			canvas.drawBitmap(btnBp, btnLeft, (getHeight() - btnBp.getWidth()) / 2, null);
		}
		dropDownArea.setWidth(width);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		if (action == MotionEvent.ACTION_DOWN && event.getX() > btnLeft){
			showPopUp();
			return true;
		}
		return false;
	}

	public void showMoreBtn(boolean show){
		btnShowing = show;
		invalidate();
	}

	public void showPopUp(){
		dropDownArea.showAsDropDown(this);
		clearFocus();
	}
	
	public void closePopUp(){
		dropDownArea.dismiss();
	}
}
