package cza.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import cza.app.App;
import cza.gbamaster.R;

public class DragGridView extends GridView {

	private int dragPosition;//开始拖拽的位置
	private int offsetX;
	private int offsetY;
	private int mDeadX;
	private boolean isDraging;
	private Bound mBound;
	private Bound mPreBound;
	private ImageView preview; //拖动item的preview
	private Callback mCallback;

	private WindowManager windowManager;
	private WindowManager.LayoutParams windowParams;

	public DragGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		preview = (ImageView) LayoutInflater.from(context).inflate(R.layout.draggridview_preview, null);
	}

	/**
	 * 设置回调
	 * @param callback
	 */
	public void setCallback(Callback callback){
		mCallback = callback;
	}

	/**
	 * 开始拖拽
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onStartDrag(event);
				break;
			case MotionEvent.ACTION_MOVE:
				if (isDraging) {
					onDrag(event);
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (isDraging) 
					onDrop(mPreBound.centerX(), mPreBound.centerY());
				break;
		}
		return super.onTouchEvent(event);
	}
	
	/**
	 * 开始拖拽
	 * @param event
	 */
	private void onStartDrag(MotionEvent event) {
		isDraging = false;
		//点击事件 触发
		int x = (int)event.getX();
		int y = (int)event.getY();
		//根据点击位置 获取listview中的某个 position
		dragPosition = pointToPosition(x, y);
		if (dragPosition != AdapterView.INVALID_POSITION && mCallback.isAvailablePosition(dragPosition)) {
			if (windowParams == null) {
				//初始化参数
				mBound = new Bound(this); //限制范围
				mDeadX = mBound.width - App.dip2px(40);
				windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
				mPreBound = new Bound();
				windowParams = new WindowManager.LayoutParams();
				windowParams.gravity = Gravity.TOP | Gravity.LEFT;//这个必须加
				//设置宽和高
				windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
				windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
				//设置屏幕透明
				windowParams.format = PixelFormat.TRANSLUCENT;
			}
			if (x > mDeadX) 
				return;
			//获得 listview 中 position 对应的view
			View itemView = getChildAt(dragPosition - getFirstVisiblePosition());
			//触摸点相对于item左上角的坐标
			offsetX = x - itemView.getLeft();
			offsetY = y - itemView.getTop();
			//每次都销毁一次cache，重新生成一个bitmap
			itemView.destroyDrawingCache();
			itemView.setDrawingCacheEnabled(true);
			Bitmap bm = Bitmap.createBitmap(itemView.getDrawingCache());
			//添加item缩略图到windowmanager中
			preview.setImageBitmap(bm);
			mPreBound.width = bm.getWidth();
			mPreBound.height = bm.getHeight();
			windowManager.addView(preview, windowParams);
			onDrag(event);
			isDraging = true;
		}
	}

	/**
	 * 拖拽
	 * @param event
	 */
	private void onDrag(MotionEvent event) {
		mPreBound.set((int)event.getRawX() - offsetX, (int)event.getRawY() - offsetY);
		mPreBound.ensureInner(mBound);
		windowParams.x = mPreBound.left;
		windowParams.y = mPreBound.top;
		windowManager.updateViewLayout(preview, windowParams);
	}

	/**
	 * 释放
	 * @param x
	 * @param y
	 */
	private void onDrop(int x, int y) {
		x -= mBound.left;
		y -= mBound.top;
		int position = pointToPosition(x, y);
		if (position != AdapterView.INVALID_POSITION
			&& position != dragPosition
			&& mCallback != null) {
			mCallback.onFinishDrag(dragPosition, position);
		}
		//在结束拖拽以后 remove window
		windowManager.removeView(preview);
	}


	public interface Callback {
		/**
		 * 位置可拖拽
		 * @param index
		 * @return
		 */
		public boolean isAvailablePosition(int index);
		
		/**
		 * 拖拽结束回调
		 * @param before
		 * @param after
		 */
		public void onFinishDrag(int before, int after);
	}
}
