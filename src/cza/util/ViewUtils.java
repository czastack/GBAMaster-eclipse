package cza.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import cza.app.Clipboard;
import cza.gbamaster.R;

public class ViewUtils {
	
	/**
	 * 注册点击事件
	 * @param container
	 * @param l
	 */
	public static void setOnClickListenerIn(ViewGroup container, View.OnClickListener l){
		for (int i = 0, len = container.getChildCount(); i < len; i++){
			container.getChildAt(i).setOnClickListener(l);
		}
	}
	
	/**
	 * 注册点击监听
	 * @param container
	 * @param l
	 */
	public static void registerClick(View container, View.OnClickListener l, int...ids){
		for (int id: ids){
			container.findViewById(id).setOnClickListener(l);
		}
	}

	/**
	 * 注册选中切换监听
	 * @param container
	 * @param l
	 * @param ids
	 */
	public static void registerCheck(View container, CompoundButton.OnCheckedChangeListener l, int...ids){
		for (int id: ids){
			((CompoundButton)container.findViewById(id)).setOnCheckedChangeListener(l);
		}
	}
	
	/**
	 * 取消自动获取焦点
	 * @param v
	 */
	public static void clearAutoFocus(View v) {
		View parent = (View) v.getParent();
		parent.setFocusable(true);
		parent.setFocusableInTouchMode(true);
	}

	/**
	 * 设置可点击
	 * @param v
	 */
	public static void setCheckable(View v) {
		v.setClickable(true);
		v.setFocusable(true);
	}
	
	public static void chkRadioAt(RadioGroup parent, int position){
		((RadioButton)parent.getChildAt(position)).setChecked(true);
	}
	
	/**
	 * 切换显示状态
	 * @param v
	 */
	public static void display(View v){
		v.setVisibility(v.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
	}

	/**
	 * 隐藏控件
	 * @param v
	 * @param hidden
	 */
	public static void hide(View v, boolean hidden){
		v.setVisibility(hidden ? View.GONE : View.VISIBLE);
	}

	/**
	 * 输入框控制
	 * @param v
	 * @param id
	 */
	public static void edit(EditText v, int id){
		switch (id){
			case R.id.btn_clear:
				v.getText().clear();
				break;
			case R.id.btn_paste:
				v.setText(Clipboard.getText(v.getContext()));
				break;
			case R.id.btn_copy:
				Clipboard.copy(v.getContext(), v.getText());
				break;
		}
	}
	
	/**
	 * 注册回车提交监听
	 * @param v
	 * @param l
	 */
	public static void setOnDown(TextView v,  TextView.OnEditorActionListener l) {
		v.setSingleLine(true);
		v.setImeOptions(6);
		v.setOnEditorActionListener(l);
	}
	
	/**
	 * 设置颜色列表
	 * @param v
	 */
	public static void setColorList(TextView v){
		Context context = v.getContext();
		int i[] = {android.R.attr.textColorSecondary};
		TypedArray array = context.getTheme().obtainStyledAttributes(i);
		int origin = array.getColor(0, Color.BLACK);
		int highlight = context.getResources().getColor(R.color.highlight);
		int[] colors = {highlight, origin};  
        int[][] states = {{android.R.attr.state_selected}, {}};  
        v.setTextColor(new ColorStateList(states, colors));  
	}
	
	/**
	 * 替换子元素
	 * @param origin
	 * @param current
	 */
	public static void replaceView(View origin, View current){
		ViewGroup parent = (ViewGroup)origin.getParent();
		if (parent == null)
			return;
		parent.removeView(current);
		int index = parent.indexOfChild(origin);
		parent.removeView(origin);
		parent.addView(current, index, origin.getLayoutParams());
	}
	
	/**
	 * 插入元素
	 * @param target
	 * @param insert
	 */
	private static void insertView(View target, View insert, int offset){
		ViewGroup parent = (ViewGroup)target.getParent();
		if (parent == null)
			return;
		ViewGroup originalParent = (ViewGroup)target.getParent();
		if (originalParent != null)
			originalParent.removeView(insert);
		int index = parent.indexOfChild(target);
		parent.addView(insert, index + offset);
	}
	
	/**
	 * 在其后插入元素
	 * @param target
	 * @param insert
	 */
	public static void insertAfter(View target, View insert){
		insertView(target, insert, 1);
	}
	
	/**
	 * 在其前插入元素
	 * @param target
	 * @param insert
	 */
	public static void insertBefore(View target, View insert){
		insertView(target, insert, 0);
	}
}

