package cza.hack;

import java.util.ArrayList;

import cza.util.Checkable;

public class Cheat extends Checkable {
	public String name, type = MyBoy.TYPE_CB;
	public ArrayList<String> codes = new ArrayList<String>();
	public boolean isGs1;
	public boolean selected;
	public boolean isSlide; //是压缩码的后半部分

	public boolean select(){
		return selected = !selected;
	}

	public void add(String code){
		codes.add(code);
	}
	
	public void addNormal(String code){
		if (isSlide || !codes.contains(code))
			add(code);
		isSlide = false;
	}
	
	public void addSlide(String code){
		add(code);
		isSlide = true;
	}
	
	public void addGS(String code){
		if (code.charAt(8) == CBCoder.P)
			type = MyBoy.TYPE_GS3;
		else 
			type = MyBoy.TYPE_GS1;
		add(code);
		isSlide = false;
	}

	public void clear(){
		codes.clear();
	}

	public void addAll(Cheat cheat){
		codes.addAll(cheat.codes);
	}

	public String getLastCode(){
		return codes.get(codes.size() - 1);
	}

	public void toString(StringBuilder sb){
		sb.append('\n').append(name);
		for (String code: codes){
			sb.append('\n').append(code);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.deleteCharAt(0).toString();
	}
}
