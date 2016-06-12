package cza.widget;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import java.util.Arrays;

/**
 * ListView多选控制
 * @author cza
 *
 */
public class ListViewCheckControler implements View.OnTouchListener {
	private ListView mListView;
	/**
	 * 单点触发横坐标界限
	 * 若>0，触发位置在左
	 * 若<0，触发位置在右
	 */
	private int mSingleTriggerPos;
	private int count;
	private int[] positions = new int[2];
	private Callback mCallback;
	private byte trigger;
	private final static byte 
	TRIGGER_SINGLE = 1,
	TRIGGER_DOUBLE = 2;

	public ListViewCheckControler(ListView li, Callback callback) {
		mListView = li;
		mCallback = callback;
		serve(true);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				count = 1;
				if(mSingleTriggerPos != 0)
				{
					int eX = (int) event.getX(0);
					int eY = (int) event.getY(0);
					if( (mSingleTriggerPos > 0 && eX < mSingleTriggerPos) || 
						(mSingleTriggerPos < 0 && eX > (v.getWidth() + mSingleTriggerPos)))
					{
						int pos = mListView.pointToPosition(eX, eY);
						positions[0] = positions[1] = pos;
						trigger = TRIGGER_SINGLE;
						onItemCheck();
						return true;
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				count = 0;
				switch(trigger){
				case TRIGGER_SINGLE:
					trigger = 0;
					return true;
				case TRIGGER_DOUBLE:
					trigger = 0;
					mCallback.onItemCheck(positions[0], positions[1]);
					return false;
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
				count--;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				count++;
				if (count == 2) {
					for (int i = 0; i < 2; i++) {
						int x = (int) event.getX(i);
						int y = (int) event.getY(i);
						int p = mListView.pointToPosition(x, y);
						if (p == -1)
							return false;
						positions[i] = p;
					}
					Arrays.sort(positions);
					trigger = TRIGGER_DOUBLE;
				}
				return true;
		}
		return false;
	}

	public void serve(boolean on) {
		mListView.setOnTouchListener(on ? this : null);
	}

	public void setmSingleTriggerPos(int pos) {
		mSingleTriggerPos = pos;
	}
	
	public void onItemCheck(){
		mCallback.onItemCheck(positions[0], positions[1]);
	}

	public interface Callback {
		public void onItemCheck(int start, int end);
	}
}



