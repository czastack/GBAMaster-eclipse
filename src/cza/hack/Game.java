package cza.hack;

import java.io.File;

import cza.file.FileUtils;

public class Game {
	public String name, type, size, path, fullName, dir;

	public Game(String path){
		int n = path.lastIndexOf(File.separatorChar);
		dir = path.substring(0, n);
		fullName = path.substring(n + 1);
	}

	public Game(File rom){
		fullName = rom.getName();
		dir = rom.getParent();
		name = FileUtils.getMainName(fullName);
		type = FileUtils.getType(rom);
		size = FileUtils.size(rom);
		path = rom.getPath();
	}

	public void changeName(String name){
		path = path.replace(this.name, name);
		this.name = name;
		fullName = name + "." + type;
	}
}
