package cza.hack;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import cza.app.App;
import cza.file.FileNameFixFilter;
import cza.file.FileUtils;
import cza.util.Pull;
import cza.util.XmlWriter;

public abstract class Emu{
	public String NAME,
		packageName,
		activityName = "EmulatorActivity";
	public Game mGame;
	public File gameFile;
	public File chtDir, saveDir, gameDir;
	public GameST st0;
	public static final int ST_MAX_COUNT = 10;

	/**
	 * 加载游戏处理
	 */
	abstract protected void onLoad();

	/**
	 * 获取相关文件
	 * @return
	 */
	abstract public File[] getRelativeFiles();
	
	/**
	 * 获取即时存档文件
	 * @param i
	 * @return
	 */
	abstract public File getSTFile(int i);

	/**
	 * 加工即时存档信息
	 * @param st
	 */
	abstract public void processST(GameST st);

	/**
	 * 解析cht文件
	 * @param pull
	 * @param cheats
	 * @throws Exception
	 */
	abstract public void parseCht(Pull pull, Cheats cheats) throws Exception;

	/**
	 * 写入cht文件
	 * @param writer
	 * @param cheat
	 */
	abstract public void writeCht(XmlWriter writer, Cheat cheat);

	/**
	 * 载入游戏数据
	 * @param g
	 */
	public void load(Game g) {
		if (g != null) {
			mGame = g;
			gameFile = new File(g.path);
			gameDir = gameFile.getParentFile();
		}
		onLoad();
	}

	/**
	 * 载入游戏数据
	 * @param path 游戏文件路径
	 */
	public void load(String path) {
		gameFile = new File(path);
		if (gameFile.exists()) {
			mGame = new Game(gameFile);
			gameDir = gameFile.getParentFile();
		}
		onLoad();
	}
	
	/**
	 * 是否已卸载游戏
	 * @return
	 */
	public boolean unloaded(){
		return gameFile == null;
	}

	/**
	 * 获取快捷存档
	 */
	public void chkST0(){
		if (st0 == null || st0.time == 0) 
			st0 = getST(0);
	}
	
	/**
	 * 获取即时存档数量
	 */
	public int getSTCount(){
		int count = 0;
		for (int i = 0; i < ST_MAX_COUNT; i++){
			if (getSTFile(i).exists())
				count++;
		}
		return count;
	}

	/**
	 * 获取即时存档
	 */
	public GameST getST(int i) {
		GameST st = new GameST();
		File ss = getSTFile(i);
		st.index = i;
		st.path = ss.getPath();
		if (ss.exists()){
			st.setTime(ss);
			processST(st);
		}
		return st;
	}
	
	/**
	 * 调整即时存档
	 */
	public void adjustSTSTo(ArrayList<GameST> list){
		File tempDir = new File(gameDir, "tempDir");
		tempDir.mkdir();
		int len = list.size();
		File[] temps = new File[len],
			tos = new File[len];
		for (int i = 0; i < len; i++) {
			GameST st = list.get(i);
			File from = new File(st.path);
			File to = getSTFile(st.mIndex);
			File temp = new File(tempDir, to.getName());
			st.path = to.getPath();
			moveST(from, temp);
			temps[i] = temp;
			tos[i] = to;
		}
		for (int i = 0; i < len; i++){
			moveST(temps[i], tos[i]);
		}
		tempDir.delete();
	}

	public void moveST(File from, File to){
		from.renameTo(to);
	}
	
	/**
	 * 获取作弊文件
	 */
	public File getChtFile() {
		return new File(chtDir, mGame.name + ".cht");
	}

	/**
	 * 获取电池记忆文件
	 */
	public File getSavFile() {
		return withType("sav");
	}
	
	/**
	 * 获取即时存档
	 */
	public SparseArray<File> getSTFiles(){
		SparseArray<File> list = new SparseArray<File>();
		for (int i = 0; i < 10; i++) {
			File st = getSTFile(i);
			if (st.exists()) {
				list.put(i, st);
			}
		}
		return list;
	}
	
	/**
	 * 获取Rom的附带文件
	 */
	public File withType(String type){
		return new File(gameDir, mGame.name + "." + type);
	}

	/**
	 * 获取作弊文件列表
	 */
	public ChtInfos getChtInfos() {
		File cht = getChtFile();
		File folder = cht.getParentFile();
		ChtInfos infos = new ChtInfos();
		if (folder.exists()){
			String [] names = folder.list(new FileNameFixFilter(cht.getName(), ".cht"));
			Arrays.sort(names, App.COMPARATOR);
			String name = cht.getName();
			infos.path = cht.getPath();
			int preLen = name.length();
			for (int i = 0; i < names.length; i++){
				File file = new File(folder, names[i]);
				if (i == 0){
					name = ChtInfos.DEFAULT;
				} else {
					name = file.getName();
					name = name.substring(preLen, name.length() - 4);
				}
				ChtInfo info = new ChtInfo(name);
				info.size = FileUtils.size(file);
				info.time = FileUtils.getTime(file);
				infos.add(info);
			}
		} else {
			folder.mkdirs();
		}
		return infos;
	}
	
	/**
	 * 获取相关文件
	 */
	public File[] getRelativeFiles(File dir){
		String [] names = dir.list(new FileNameFixFilter(mGame.name, null));
		Arrays.sort(names, App.COMPARATOR);
		int length = names.length;
		File[] files = new File[length];
		for (int i = 0; i < length; i++)
			files[i] = new File(dir, names[i]);
		return files;
	}

	/**
	 * 解析作弊文件
	 */
	public Cheats parseCht(File cht) {
		Cheats cheats = new Cheats();
		Pull pull = new Pull();
		try {
			pull.start(cht);
			parseCht(pull, cheats);
		} catch (Exception e) {}
		cheats.mCheat = null;
		return cheats;
	}

	/**
	 * 写入作弊文件
	 */
	public XmlWriter writeCht(Cheats cheats){
		XmlWriter writer = new XmlWriter();
		writer.start("cheats");
		for (Cheat cheat : cheats) {
			writeCht(writer, cheat);
		}
		writer.end();
		return writer;
	}

	public void writeCht(Cheats cheats, File cht){
		writeCht(cheats).write(cht);
	}

	/**
	 * 运行游戏
	 */
	public Intent getOpenRomIntent() {
		return new Intent("android.intent.action.VIEW", Uri.fromFile(gameFile))
			.setComponent(new ComponentName(packageName, packageName + "." + activityName));
	}
	
	/**
	 * 获取rom信息
	 */
	public String[] getRomInfo() throws Exception{
		byte[] title = new byte[12];
		byte[] code = new byte[4];
		byte[] maker = new byte[2];
		String[] output = new String[3];
		RomReader reader = new RomReader(mGame.path);
		reader.read(0xA0, title);
		reader.mStream.read(code);
		reader.mStream.read(maker);
		reader.close();
		output[0] = new String(title);
		output[1] = new String(code);
		output[2] = new String(maker);
		return output;
	}
}
