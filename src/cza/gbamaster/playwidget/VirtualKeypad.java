package cza.gbamaster.playwidget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import com.androidemu.Emulator;

import cza.gbamaster.GameKeyListener;
import cza.gbamaster.MyApplication;
import cza.gbamaster.R;

@SuppressLint("ViewConstructor")
public class VirtualKeypad extends View {
	
    private static float[] DPAD_DEADZONE_VALUES = {0.1f, 0.14f, 0.1667f, 0.2f, 0.25f};
    private float dpadDeadZone = DPAD_DEADZONE_VALUES[2];
    private GameKeyListener gameKeyListener;
	private Rect mBound;
	//模拟同时两个按键
    private boolean inBetweenPress; //点击两个按钮中间模拟同时按住
    private float pointSizeThreshold; //触摸点的阀值
    private int keyStates; //当前按键状态
    private int margin;
	private Control[] controls = new Control[8];
	//方向键 A/B键 A/B连发键 左肩键 右肩键 选择/开始键
    private Control DPAD, AB, AB_TURBO, TL, TR, SS, LOAD, SAVE;
	private Paint paint;
	private boolean mAbleQuickSL;

    public VirtualKeypad(Context context, GameKeyListener l) {
        super(context);
        Bitmap src = MyApplication.readBitmap(context, R.drawable.vkeypad);
		DPAD = createControl(0, src, 0, 0, 120, 120);
		AB = createControl(1, src, 144, 0, 112, 48);
		AB_TURBO = createControl(2, src, 144, 48, 112, 48);
		TL = createControl(3, src, 0, 120, 64, 40);
		TR = createControl(4, src, 64, 120, 64, 40);
		SS = createControl(5, src, 166, 96, 90, 32);
		LOAD = createControl(6, src, 32, 224, 32, 32);
		SAVE = createControl(7, src, 64, 224, 32, 32);
		src.recycle();
		paint = new Paint();
		paint.setAlpha(200);
		gameKeyListener = l;
    }
	
	public boolean isAbleQuickSL(){
		return mAbleQuickSL;
	}

	/**
	 * 构建控件
	 */
	private Control createControl(int i, Bitmap src, int l, int t, int w, int h){
		return controls[i] = new Control(Bitmap.createBitmap(src, l, t, w, h));
	}

	/**
	 * 按钮大小
	 */
    private static float getControlScale(SharedPreferences pref) {
        String size = pref.getString("vkeypadSize", null);
		if ("small".equals(size)) 
			return 1;
		else if ("large".equals(size)) 
			return 1.5f;
		else 
			return 1.2f;
    }

	/**
	 * 调整布局
	 */
    private void reposition() {
		int l = mBound.left;
		int t = mBound.top;
		int r = mBound.right;
		int b = mBound.bottom;
		if (mBound.width() > mBound.height()) {
			//横屏 左右留白
			l += margin;
			r -= margin;
		} else {
			//竖屏 上下留白
			t += margin;
			b -= margin;
		}
		int dpadTop = b - DPAD.H;
		DPAD.layout(l, dpadTop);
		int abLeft = r - AB.W;
		AB.layout(abLeft, b - AB.H);
		if (AB_TURBO.on) 
			AB_TURBO.layout(abLeft, dpadTop);
		TL.layout(l, t);
		TR.layout(r - TR.W, t);
		//选择/开始键的横坐标
		int ssLeft = (r - SS.W) / 2;
		//如果没有和方向键重叠
		//就放在下面，否则放在上面
		if (mAbleQuickSL = ssLeft > l + DPAD.W) {
			SS.layout(ssLeft, b - SS.H);
			//快速加载/保存 放在上面
			int loadLeft, saveLeft;
			loadLeft = TL.W + (r - TL.W - TR.W) * 1 / 3 - LOAD.W / 2;
			saveLeft = TL.W + (r - TL.W - TR.W) * 2 / 3 - SAVE.W / 2;
			LOAD.layout(loadLeft, t);
			SAVE.layout(saveLeft, t);
			LOAD.on = SAVE.on = true;
		} else {
			SS.layout(ssLeft, t);
			LOAD.on = SAVE.on = false;
		}
	}

	/**
	 * 入口：调整控件大小
	 */
    public final void resize(Rect rect, SharedPreferences pref) {
		mBound = rect;
		int dpadDeadZoneIndex = pref.getInt("dpadDeadZone", 2);
		dpadDeadZone = DPAD_DEADZONE_VALUES[dpadDeadZoneIndex];
		//模拟同时两个按键
		inBetweenPress = pref.getBoolean("inBetweenPress", false);
		pointSizeThreshold = 1;
		if (pref.getBoolean("pointSizePress", false)) 
			pointSizeThreshold = pref.getInt("pointSizePressThreshold", 7) / 10 - 0.01f;
		//允许AB连发键
		AB_TURBO.on = !pref.getBoolean("disableAB_TURBO", false);
		margin = (8 * pref.getInt("layoutMargin", 2));
		float ctrlScale = getControlScale(pref);
		//按键有效区外延
		int hitOut = MyApplication.dip2px(pref.getInt("hitOut", 50));
		for (Control ctrl : controls)
			ctrl.resize(ctrlScale, hitOut);
		reposition();
		invalidate(); //重绘
	}

	@Override
	protected void onDraw(Canvas canvas) {
		for (Control ctrl : controls){
			if (ctrl.on) 
				ctrl.draw(canvas, paint);
		}
	}

	/**
	 * 处理触摸事件
	 * 如果在按键区域（上下两部分），就判断是否按在键上，并发送模拟按键
	 * 否则就返回-1 弹出/隐藏ActionBar
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event){
		int key = 0;
		int action = event.getActionMasked();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:{
					int pointerIndex = event.getActionIndex();
					key = keyStates | getGameKeyAt(event, pointerIndex);
					break;
				}
			case MotionEvent.ACTION_POINTER_UP:{
					int pointerIndex = event.getActionIndex();
					key = keyStates & ~getGameKeyAt(event, pointerIndex);
					break;
				}
			case MotionEvent.ACTION_MOVE:
				//重新计算每个点的键值
				for (int i = 0, pointerCount = event.getPointerCount(); i < pointerCount; i++) 
					key |= getGameKeyAt(event, i);
				break;
			case MotionEvent.ACTION_UP: //最后一个键松开
				break;
		}
		if (keyStates != key) {
			keyStates = key;
			gameKeyListener.onGameKeyChanged();
		}
		return true;
	}

	/**
	 * 获取触摸的按钮的键值
	 * @param event 触摸事件
	 * @param pointerIndex 触摸点的序号
	 * @return 相应的键值
	 */
	private int getKeyAt(MotionEvent event, int pointerIndex) {
		int key = 0;
		float eX = event.getX(pointerIndex);
		float eY = event.getY(pointerIndex);
		if (eY > TL.getBottom() && eY < DPAD.getTop())
			return GameKeyListener.KEY_OUTER;
		Control ctrl;
		int id = 0;
		int length = controls.length;
		do {
			ctrl = controls[id];
			if (ctrl.hit(eX, eY))
				break;
			id++;
		} while (id < length);
		//都没按中
		if (id == length)
			return 0;
		//在控件内部的位置（比例）
		float xR = (eX - ctrl.X) / ctrl.W;
		float yR = (eY - ctrl.Y) / ctrl.H;
		switch (id) {
			case 0: 
				//方向键
				if (xR < (0.5 - dpadDeadZone)) 
					key = Emulator.GAMEPAD_LEFT;
				else if (xR > 0.5 + dpadDeadZone) 
					key = Emulator.GAMEPAD_RIGHT;
				if (yR < (0.5 - dpadDeadZone)) 
					key |= Emulator.GAMEPAD_UP;
				else if (yR > (0.5 + dpadDeadZone)) 
					key |= Emulator.GAMEPAD_DOWN;
				break;
			case 1:
			case 2: {
					//A/B键 / A/B连发键
					int A, B;
					if (id == 1) {
						A = Emulator.GAMEPAD_A;
						B = Emulator.GAMEPAD_B;
					} else {
						A = Emulator.GAMEPAD_A_TURBO;
						B = Emulator.GAMEPAD_B_TURBO;
					}
					//如果触摸点很大，就直接模拟同时按住
					if (event.getSize() > pointSizeThreshold) 
						return A | B;
					//否则判断横坐标比例
					if (inBetweenPress) {
						if (xR > 0.58) 
							return A;
						else if (xR < 0.42) 
							return B;
						else 
							return A | B;
					}
					return xR > 0.5 ? A: B;
				} 
			case 3: 
				//左肩键
				return Emulator.GAMEPAD_TL;
			case 4:
				//右肩键
				return Emulator.GAMEPAD_TR;
			case 5:
				//选择 开始
				return xR < 0.5 ? Emulator.GAMEPAD_SELECT: Emulator.GAMEPAD_START;
			case 6:
				//快速加载
				return GameKeyListener.KEY_LOAD;
			case 7:
				//快速保存
				return GameKeyListener.KEY_SAVE;
		}
		return key;
	}
	
	public int getGameKeyAt(MotionEvent event, int pointerIndex) {
		int key = getKeyAt(event, pointerIndex);
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		boolean isDown = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN;
		if (key == GameKeyListener.KEY_OUTER) {
			if (isDown) 
				gameKeyListener.onOuter();
		} else if (key == GameKeyListener.KEY_LOAD) {
			if (isDown)
				gameKeyListener.quickLoad();
		} else if (key == GameKeyListener.KEY_SAVE) {
			if (isDown)
				gameKeyListener.quickSave();
		} else 
			return key;
		return 0;
	}

    public final int getKeyStates() {
        return keyStates;
    }
}

/**
 * 控件类
 */
class Control {
	private Bitmap src, bp;
	public boolean on = true;
	public int H, W, X, Y;
	//left top right bottom padding
	private int l, t, r, b, p;

	public Control(Bitmap bp) {
		src = bp;
	}
	
	/**
	 * 绘制
	 */
	final void draw(Canvas canvas, Paint paint) {
		canvas.drawBitmap(bp, X, Y, paint);
	}

	/**
	 * 生成布局参数
	 */
	public final void layout(int x, int y) {
		X = x;
		Y = y;
		l = x - p;
		r = x + W + p;
		t = y - p;
		b = y + H + p;
	}

	/**
	 * 改变大小和触摸外延
	 */
	public final void resize(float scale, int pad) {
		W = MyApplication.dip2px(scale * src.getWidth());
		H = MyApplication.dip2px(scale * src.getHeight());
		p = pad;
		//释放原来的Bitmap
		if (bp != null)
			bp.recycle();
		//生成新的Bitmap
		bp = Bitmap.createScaledBitmap(src, W, H, true);
	}

	/**
	 * 判断有没有按到
	 */
	public boolean hit(float x, float y) {
		return on && x >= l && x < r && y >= t && y < b;
	}
	
	public int getTop(){
		return t;
	}

	public int getBottom(){
		return b;
	}
}
