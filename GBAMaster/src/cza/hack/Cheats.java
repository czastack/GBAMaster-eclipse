package cza.hack;

import java.util.HashMap;
import java.util.Iterator;

import cza.util.CheckableItems;

public class Cheats extends CheckableItems<Cheat> {

	private static final long serialVersionUID = 844898351218458965L;
	public Cheat mCheat;
	public int selectedCount;
	private HashMap<String, Cheat> map = new HashMap<String, Cheat>();

	public void checkCheat(){
		if (mCheat == null)
			putCheat("无名称");
	}
	
	public void updateTitle(String title){
		map.remove(mCheat.name);
		mCheat.name = title;
		map.put(title, mCheat);
	}
	
	public void putCheat(String name){
		if (name == null || name.isEmpty() || "同上".equals(name)) 
			return;
		mCheat = map.get(name);
		if (mCheat == null){
			mCheat = new Cheat();
			mCheat.name = name;
			add(mCheat);
			map.put(name, mCheat);
		}
	}

	public void setType(String type){
		checkCheat();
		mCheat.type = type;
		mCheat.isGs1 = "gs1".equals(type);
	}

	public void putCode(String code){
		checkCheat();
		CBCoder.getInstance().formatCode(code, mCheat);
	}

	public void setOn(boolean on){
		checkCheat();
		mCheat.checked = on;
	}
	
	public boolean containsBefore(Cheat cheat, int index){
		for (int i = 0; i < index; i++){
			if (get(i) == cheat)
				return true;
		}
		return false;
	}
	
	public boolean contains(Cheat cheat){
		return map.containsKey(cheat.name);
	}

	/**
	 * 插入
	 */
	public Cheats addAll(int index, String in){
		Cheats cheats = null;
		if (index < 0 || index > size())
			CBCoder.getInstance().formatAll(in, this);
		else {
			cheats = CBCoder.getInstance().formatAll(in);
			addAll(index, cheats);
		}
		mCheat = null;
		return cheats;
	}

	public void addAll(Cheats cheats){
		addAll(size(), cheats);
	}

	public void addAll(int index, Cheats cheats){
		boolean noBefore = true;
		for (Cheat cheat: cheats){
			putCheat(cheat.name);
			if (!cheat.codes.isEmpty()) {
				mCheat.clear();
				mCheat.addAll(cheat);
			}
			if (!containsBefore(mCheat, index)) {
				if (!noBefore)
					index++;
				noBefore = true;
			} else {
				if (noBefore)
					index--;
				noBefore = false;
			}
			moveTo(indexOf(mCheat), index);
			if (noBefore)
				index++;
		}
		mCheat = null;
	}

	public void toString(StringBuilder sb){
		for (Cheat cheat: this) {  
			cheat.toString(sb);
		}
		if (size() > 0){
			sb.deleteCharAt(0);
		}
	}

	//选中 开始
	public void selectAt(int i){
		if (get(i).select()){
			selectedCount++;
		} else {
			selectedCount--;
		}
	}
	
	public void mulSelect(int start, int end){
		for (int i = start; i <= end; i++){
			selectAt(i);
		}
	}

	public void selectAll(){
		mulSelect(0, size() - 1);
	}

	public void selectAll(boolean selected){
		for (Cheat cheat : this){
			cheat.selected = selected;
		}
		selectedCount = selected ? size() : 0;
	}
	
	public void selectChecked(boolean checked){
		selectAll(false);
		for (Cheat cheat : this){
			if (cheat.checked == checked){
				cheat.selected = true;
				selectedCount++;
			}
		}
	}

	public void checkSelected(boolean checked){
		for (Cheat cheat : this){
			if (cheat.selected){
				if (cheat.checked != checked){
					if (cheat.chk()){
						checkedCount++;
					} else {
						checkedCount--;
					}
				}
			}
		}
	}

	public Cheats filtSelected(){
		Cheats selectedCheats = new Cheats();
		for (Cheat cheat: this){
			if (cheat.selected)
				selectedCheats.add(cheat);
		}
		return selectedCheats;
	}

	public void removeSelected(boolean selected){
		Iterator<Cheat> itr = iterator();
		while (itr.hasNext()){
			Cheat cheat = itr.next();
			if (cheat.selected == selected){
				itr.remove();
				map.remove(cheat.name);
				if (cheat.checked){
					checkedCount--;
				}
			}
		}
		if (selected){
			selectedCount = 0;
		}
	}

	public String getSelectedState(){
		return selectedCount + "/" + size();
	}
	
	public void countSelected(){
		int count = 0;
		for (Cheat cheat: this){
			if (cheat.selected)
				count++;
		}
		selectedCount = count;
	}
	//选中 结束
	
	//安全删除
	public Cheat mRemove(int index) {
		return map.remove(remove(index).name);
	}
	
	public void mRemove(Cheat cheat) {
		mRemove(indexOf(cheat));
	}

	@Override
	public void clear() {
		super.clear();
		map.clear();
		selectedCount = 0;
		mCheat = null;
	}
	
	/**
	 * 替换
	 */
	public void replaceAt(int index, String text, boolean ableMulti){
		if (ableMulti) {
			boolean checked = mRemove(index).checked;
			Cheats cheats = addAll(index, text);
			if (checked && cheats != null) {
				if (cheats.size() == 1) {
					map.get(cheats.get(0).name).checked = true;
					checkedCount++;
				}
			}
		} else {
			Cheat cheat = CBCoder.getInstance().formatAll(text).get(0);
			Cheat origin = get(index);
			origin.name = cheat.name;
			origin.addAll(cheat);
		}
	}
}
