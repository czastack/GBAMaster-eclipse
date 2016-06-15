package cza.util;

import java.util.ArrayList;



public class Ary<T> extends ArrayList<T>{
	private static final long serialVersionUID = 4900206096118928799L;

	public Ary(){}

	public Ary(int len){
		super(len);
	}

	public boolean move(int before, int diff){
		return moveTo(before, before + diff);
	}

	public boolean moveTo(int before, int after){
		if (after > -1 && after < size()){
			add(after, remove(before));
			return true;
		}
		return false;
	}

	public void toString(StringBuilder sb){
		for (T e : this){
			sb.append(e.toString() + "\n");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}
}
