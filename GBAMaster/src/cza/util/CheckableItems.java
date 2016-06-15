package cza.util;

import java.util.Iterator;

public class CheckableItems<T extends Checkable> extends Ary<T> {
	private static final long serialVersionUID = 1633020139322397956L;
	public int checkedCount;
	public CheckableItems(){}

	public CheckableItems(int len){
		super(len);
	}

	public void chkAt(int i){
		T t = get(i);
		if (t.chk())
			checkedCount++;
		else 
			checkedCount--;
	}

	public void mulChk(int start, int end){
		for (int i = start; i <= end; i++){
			chkAt(i);
		}
	}

	public void chkAll(){
		mulChk(0, size() - 1);
	}

	public void chkAll(boolean checked){
		for (T t : this){
			t.checked = checked;
		}
		checkedCount = checked ? size() : 0;
	}

	public void count(){
		int count = 0;
		for (T t: this){
			if (t.checked)
				count++;
		}
		checkedCount = count;
	}

	@Override
	public void clear() {
		super.clear();
		checkedCount = 0;
	}
	
	@Override
	public boolean moveTo(int before, int after) {
		T item = get(before);
		if (super.moveTo(before, after)){
			if (item.checked)
				checkedCount++;
			return true;
		}
		return false;
	}

	@Override
	public T remove(int index) {
		T t = super.remove(index);
		if (t.checked){
			checkedCount--;
		}
		return t;
	}

	public void remove(boolean checked){
		Iterator<T> itr = iterator();
		while(itr.hasNext()){
			if (itr.next().checked == checked){
				itr.remove();
			}
		}
		if (checked){
			checkedCount = 0;
		}
	}

	public boolean noChk(){
		return checkedCount == 0;
	}

	public String getState(){
		return checkedCount + "/" + size();
	}
}
