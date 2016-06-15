package cza.gbamaster.cot;

import java.util.ArrayList;
import java.util.regex.Pattern;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import cza.app.App;
import cza.gbamaster.R;
import cza.hack.CheatCoder;
import cza.hack.Cheats;
import cza.hack.Code;
import cza.util.Pull;
import cza.util.XmlWriter;
import cza.widget.MultipleSelect;
import cza.widget.Select;
import cza.widget.SelectCallback;

public class CheatSelect extends SelectCallback implements 
		CheatView, 
		View.OnClickListener,
		SearchView.OnQueryTextListener,
		SearchView.OnCloseListener {

	private static final int MIN_SEARCH_LENGTH = 10;
	public int mIncrement;
	private boolean mLoaded, mHasHint;
	private Code mCode;
	private CotResource mCotRes;
	private String mEntryName;
	private String mIntro;
	private Select mSelect;
	private LayoutInflater mInflater;
	private ViewGroup mHeader;
	private SearchView mSearchView;
	private int mFilterCount;
	private int[] mTagIndexs, mCheckedIndexs, mFilterIndexs;
	private SelectData[] mDatas;

	public CheatSelect(Context context, Pull pull, CotResource cotRes) {
		mInflater = LayoutInflater.from(context);
		mCode = new Code();
		mCotRes = cotRes;
		mCode.addr = CheatCoder.hexToDec(pull.getValue(ATTR_ADDR));
		mCode.type = (byte)pull.getInt(ATTR_CODE_TYPE, CheatCoder.TYPE_CB);
		mCode.func = (byte)pull.getInt(ATTR_CODE_FUNC, CheatCoder.INT_16_BIT_WRITE);
		mEntryName = pull.getValue(ATTR_ENTRY);
		mIntro = pull.getValue(ATTR_INTRO);
		mHasHint = pull.getBoolean(ATTR_HAS_HINT);
		initHeader(pull.getValue(ATTR_HINT));
		createSelect(context, pull.getValue(ATTR_NAME), pull.getInt(ATTR_MULTIPLE));
		if (pull.getBoolean(ATTR_HORIZONTAL)) 
			mSelect.setOrientation(Select.HORIZONTAL);
		if (multiple) {
			mIncrement = pull.getInt(ATTR_INCREMENT);
			((MultipleSelect) mSelect)
				.setSortRow(pull.getInt("sortRow"));
		}
	}

	/**
	 * 创建选择控件
	 * @param context
	 * @param title
	 * @param selectLimit
	 * @return
	 */
	public Select createSelect(Context context, String title, int selectLimit) {
		Select select;
		multiple = selectLimit > 0;
		if (multiple) {
			select = new MultipleSelect(context, title, this);
			((MultipleSelect)select).setSelectLimit(selectLimit);
		} else {
			select = new Select(context, title, this);
		}
		select.mListDialog.setFullScreen(true, true, multiple);
		//绑定
		select.setId(title.hashCode());
		select.setTag(this);
		mSelect = select;
		return select;
	}
	
	public View getView(){
		return mSelect;
	}

	@Override
	public String getTitle() {
		return mSelect.mTitle;
	}
	
	@Override
	public String getIntro() {
		return mIntro;
	}

	/**
	 * xml读入数据
	 */
	private void readData() {
		SelectEntry entry = mCotRes.getSelectData(mEntryName);
		mDatas = entry.datas;
		if (entry.tagIndexs != null) {
			mTagIndexs = entry.tagIndexs;
			mSelect.btnBar.mId = 0;
			mSelect.btnBar.addButton(this, entry.tagTexts);
		}
		mFilterCount = mDatas.length;
		mFilterIndexs = new int[mFilterCount];
		initSearch();
	}

	/**
	 *开头
	 */
	private void initHeader(String hint) {
		mHeader = (ViewGroup)inflateView(R.layout.cheat_select_header);
		TextView hintView = (TextView)mHeader.findViewById(R.id.hintView);
		if (hint != null && !hint.isEmpty())
			hintView.setText(hint);
		else 
			mHeader.removeView(hintView);
	}

	/**
	 * 点击按钮栏按钮定位
	 */
	@Override
	public void onClick(View v) {
		mSelect.mListDialog.mListView.setSelection(mTagIndexs[(Integer)v.getTag()]);
	}
	
	/**
	 * 搜索
	 */
	public void initSearch() {
		mSearchView = (SearchView)mSelect.mListDialog.findView(R.id.searchView);
		if (mDatas.length > MIN_SEARCH_LENGTH) {
			mSearchView.setOnQueryTextListener(this);
			mSearchView.setOnCloseListener(this);
		} else {
			mSearchView.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 是否正在过滤
	 * @return
	 */
	public boolean isFiltering(){
		return mFilterCount != mDatas.length;
	}
	
	@Override
	public boolean onClose(){
		mFilterCount = mDatas.length;
		mSelect.mListDialog.refresh();
		return false;
	}
	
	@Override
	public boolean onQueryTextSubmit(String text) {
		int count = 1;
		Pattern regex = Pattern.compile(text);
		SelectData[] datas = mDatas;
		int[] filterIndexs = mFilterIndexs;
		int index = 1;
		for (int i = index; i < datas.length; i++){
			SelectData data = datas[i];
			if (regex.matcher(data.name).find() || (mHasHint && data.hint != null && regex.matcher(data.hint).find())){
				filterIndexs[index++] = i;
				count++;
			}
		}
		mFilterCount = count;
		mSelect.mListDialog.refresh();
		return true;
	}

	@Override
	public boolean onQueryTextChange(String text) {
		return false;
	}
	
	/**
	 * 真正的序号
	 */
	public int getRealPosition(int position){
		return isFiltering() ? mFilterIndexs[position] : position;
	}

	@Override
	public boolean unCheckable(int position) {
		return position == 0;
	}

	/**
	 * 单选选中某项
	 */
	@Override
	public String getTitle(int position) {
		return position == 0 ? mHeader.getResources().getString(R.string.close) : mDatas[position].name; 
	}

	/**
	 * 多选检查边界
	 */
	@Override
	public boolean isMultiCheckable(int position) {
		MultipleSelect select = (MultipleSelect)mSelect;
		if (position == 0) {
			//关闭
			select.clearSelected();
			select.select(0);
			select.mListDialog.submit(); //更新序号列表
			return false;
		} else if (!select.mListDialog.isChecked(position) && 
			select.mListDialog.getCheckCount() >= select.mSelectLimit) {
			App.toast(mSelect.getContext(), "最多只能选" + select.mSelectLimit + "个");
			return false;
		}
		return true;
	}

	@Override
	public void onShow() {
		if (!mLoaded) {
			//初始化
			readData();
			mSelect.mListDialog.ensureCapacity(getCount());
			mLoaded = true;
		}
	}

	/**
	 * 单选中某项或多选按下确定
	 */
	@Override
	public void onSubmit(boolean multiple, int[] checkedIndexs) {
		mCheckedIndexs = checkedIndexs;
		if (multiple) {
			if (checkedIndexs.length == 0) {
				//没有选中相当于关闭
				mSelect.select(0);
			} else {
				//显示选中项目的名称
				StringBuilder sb = new StringBuilder();
				for (int index : checkedIndexs) {
					if (index != -1)
						sb.append(getTitle(index)).append('、');
				}
				sb.deleteCharAt(sb.length() - 1);
				mSelect.setHint(sb);
			}
		}
	}
	
	@Override
	public boolean isAvailable(){
		return (mCheckedIndexs != null && mCheckedIndexs.length != 0)
			&& (multiple || mSelect.mIndex != 0);
	}

	/**
	 * 输出
	 */
	@Override
	public void output(CheatViewGroup parent, Cheats cheats) {
		int i;
		int[] array = mCheckedIndexs;
		Code code = mCode;
		long originAddr = code.addr;
		CheatViewGroup.putTitle(parent, this, cheats);
		for (i = 0; i < array.length; i++) {
			if (array[i] != -1) {
				code.value = mDatas[array[i]].value;
				CheatCoder.extensibleEncode(code);
				for (String line : code.getText().split("\n"))
					coder.formatCode(line, cheats.mCheat);
			}
			code.addr += mIncrement;
			
		}
		code.addr = originAddr;
	}

	@Override
	public void reset() {
		mSelect.manualSelect(0);
	}

	@Override
	public void loadForm(Pull pull) {
		onShow();
		ArrayList<Integer> tempList = new ArrayList<Integer>();
		//读取选中列表
		try {
			final int depth = pull.parser.getDepth();
			int type;
			while (((type = pull.parser.next()) != 3 || pull.parser.getDepth() > depth) && type != 1) {
				if (type != 2) continue;
				if (TAG_ITEM.equals(pull.parser.getName())) 
					tempList.add(Integer.parseInt(pull.getText()));
			}
		} catch (Exception e) {}
		if (multiple){
			int[] checkedIndexs = new int[tempList.size()];
			for (int i = 0; i < checkedIndexs.length; i++)
				checkedIndexs[i] = tempList.get(i);
			((MultipleSelect)mSelect).setCheckedList(checkedIndexs);
		} else {
			mSelect.manualSelect(tempList.get(0));
		}
		mSelect.mListDialog.submit();
	}

	@Override
	public void saveForm(XmlWriter writer) {
		writer.startTag(TAG_DATA);
		writer.attribute(ATTR_NAME, getTitle());
		for (int index : mCheckedIndexs) {
			writer.startTag(TAG_ITEM);
			writer.text(String.valueOf(index));
			writer.endTag(TAG_ITEM);
		}
		writer.endTag(TAG_DATA);
	}
	
	protected View inflateView(int layoutResID){
		return mInflater.inflate(layoutResID, null);
	}

	public int getCount() {
		return mFilterCount;
	}

	public View getView(int position, View item) {
		if (position == 0)
			return mHeader;
		Holder holder;
		if (item == null || item.getTag() == null) {
			holder = new Holder();
			item = inflateView(R.layout.listitem_text);
			holder.findView(item);
			item.setTag(holder);
		} else {
			holder = (Holder) item.getTag();
		}
		holder.set(mDatas[position]);
		return item;
	}


	private class Holder {
		private TextView name, hint;

		void findView(View item) {
			name = (TextView) item.findViewById(R.id.title);
			if (mHasHint) {
				hint = new TextView(item.getContext());
				((ViewGroup)item).addView(hint);
			}
		}

		void set(SelectData data) {
			name.setText(data.name);
			if (mHasHint) {
				hint.setText(data.hint);
			}
		}
	}
}
