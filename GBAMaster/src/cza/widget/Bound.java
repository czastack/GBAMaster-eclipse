package cza.widget;

import android.view.View;

public class Bound {
	public int left, top, right, bottom, width, height;
	
	public Bound(){}
	
	public Bound(View view){
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		width = view.getWidth();
		height = view.getHeight();
		set(location[0], location[1]);
	}
	
	public void set(int x, int y){
		left = x;
		top = y;
		right = x + width;
		bottom = y + height;
	}
	
	public boolean contains(Bound bound){
		return left <= bound.left && top <= bound.top 
			&& right >= bound.right && bottom >= bound.bottom;
	}
	
	public int centerX(){
		return (left + right) / 2;
	}

	public int centerY(){
		return (top + bottom) / 2;
	}
	
	public void ensureInner(Bound bound){
		if (left <= bound.left)
			left = bound.left;
		if (top <= bound.top)
			top = bound.top;
		if (right >= bound.right){
			int differ = right - bound.right;
			left -= differ;
			right -= differ;
		}
		if (bottom >= bound.bottom){
			int differ = bottom - bound.bottom;
			top -= differ;
			bottom -= differ;
		}
	}
}
