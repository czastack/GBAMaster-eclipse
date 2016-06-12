package cza.gbamaster.cot;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class CheatEditText extends EditText implements 
		View.OnTouchListener  {
	private static Bitmap btnClearBp, btnMaxBp;
	private int btnClearLeft, btnMaxLeft;
	public String maxValue;
	

	public CheatEditText(Context c) {
		this(c, null);
	}

	public CheatEditText(Context c, AttributeSet attr) {
		super(c, attr);
		if (btnClearBp == null){
			//初始化按钮资源
			Resources res = getResources();
			btnClearBp = BitmapFactory.decodeResource(res,
				android.R.drawable.ic_menu_close_clear_cancel);
			btnMaxBp = BitmapFactory.decodeResource(res,
				android.R.drawable.ic_menu_add);
		}
		setInputType(2);
		InputFilter[] filters = {new InputFilter.LengthFilter(18)};
		setFilters(filters);
		setOnTouchListener(this);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = getWidth();
		int height = getHeight();
		btnMaxLeft = btnClearLeft = width;
		if (!isEmpty()){
			btnClearLeft -= btnClearBp.getWidth();
			canvas.drawBitmap(btnClearBp, btnClearLeft, (height - btnClearBp.getHeight()) / 2, null);
		}
		if (canBeMax()){
			btnMaxLeft = btnClearLeft - btnMaxBp.getWidth();
			canvas.drawBitmap(btnMaxBp, btnMaxLeft, (height - btnMaxBp.getWidth()) / 2, null);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		if (action == MotionEvent.ACTION_DOWN){
			float eX = event.getX();
			if (eX > btnClearLeft){
				clear();
				return true;
			} else if (eX > btnMaxLeft){
				toBeMax();
				return true;
			}
		}
		return false;
	}
	
	public boolean isEmpty(){
		return getText().length() == 0;
	}

	public boolean canBeMax(){
		return maxValue != null && !maxValue.contentEquals(getText());
	}
	
	public void clear(){
		getText().clear();
		clearFocus();
	}
	
	public void toBeMax(){
		setText(maxValue);
	}
}
