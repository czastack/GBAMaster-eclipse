package cza.util;

import java.io.File;

import cza.file.FileUtils;

public class XmlWriter {
	static final char T = '\t', Q = '"', S = '<', EQ = '=', E = '>';
	private boolean self, endWithIndent;
	private String root;
	private StringBuilder sb, indent;

	public void start(){
		sb = new StringBuilder("<?xml version='1.0' encoding='UTF-8' ?>");
		indent = new StringBuilder("\n");
	}

	public void start(String tag){
		start();
		startTag(root = tag);
	}

	public void startTag(String tag){
		sb.append(indent.toString() + S + tag + E);
		indent.append(T);
		self = true;
	}

	public void attribute(String key, String val){
		sb.insert(sb.length() - 1, 
			" " + key + EQ + Q + val + Q);
	}

	public void text(String str){
		sb.append(str);
		self = false;
		endWithIndent = false;
	}

	public void endTag(String tag){
		indent.deleteCharAt(indent.length() - 1);
		if (self){
			sb.insert(sb.length() - 1, " /");
			self = false;
		} else {
			if (endWithIndent){
				sb.append(indent);
			}
			sb.append(S).append('/').append(tag).append(E);
		}
		endWithIndent = true;
	}

	public void end(){
		endTag(root);
	}

	public void write(File xml){
		FileUtils.writeStringToFile(sb.toString(), xml);
	}

	@Override
	public String toString() {
		return sb.toString();
	}
}
