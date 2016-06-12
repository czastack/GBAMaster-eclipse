package cza.hack;

import android.graphics.BitmapFactory;

import java.io.File;

import cza.app.App;
import cza.util.ArrayUtils;
import cza.util.Pull;
import cza.util.XmlWriter;

public 
class MyBoy extends Emu {
	public static final String 
	KEY = "isMyBoy",
	TYPE_CB = "cb",
	TYPE_GS1 = "gs1",
	TYPE_GS3 = "gs3",
	TYPE_LIST[] = {TYPE_CB, TYPE_GS1, TYPE_GS3};

	public MyBoy(){
		NAME = "MyBoy";
		packageName = "com.fastemulator.gba";
		File home = new File(App.SD, "MyBoy");
		chtDir = new File(home, "cheat");
		saveDir = new File(home, "save");
	}

	public MyBoy(Game game){
		this();
		load(game);
	}

	@Override
	protected void onLoad() {}

	/**
	 * 测试cht文件是否是Myboy的cht格式
	 * @param cht
	 * @return
	 */
	public static boolean testCht(File cht){
		Pull pull = new Pull();
		try {
			pull.start(cht);
			int type;
			while ((type = pull.parser.next()) != 1) {
				if (type != 2) continue;
				String tag = pull.parser.getName();
				if("cheat".equals(tag))
					return true;
				else if("item".equals(tag))
					return false;
			}
		} catch (Exception e) {}
		return false;
	}

	@Override
	public File getSTFile(int i) {
		return new File(saveDir, mGame.name + ".st" + i);
	}

	public File getSTImg(File ss) {
		return new File(ss.getPath() + ".png");
	}

	@Override
	public void processST(GameST st) {
		st.bp = BitmapFactory.decodeFile(st.path + ".png");
	}

	@Override
	public void moveST(File from, File to) {
		super.moveST(from, to);
		getSTImg(from).renameTo(getSTImg(to));
	}

	/**
	 * 获取相关文件
	 */
	@Override
	public File[] getRelativeFiles() {
		File[] files1 = getRelativeFiles(gameDir);
		File[] files2 = getRelativeFiles(chtDir);
		File[] files3 = getRelativeFiles(saveDir);
		File[] output = (File[])ArrayUtils.combine(files1, files2, files3);
		return output;
	}

	@Override
	public void parseCht(Pull pull, Cheats cheats) throws Exception {
		int type;
		while ((type = pull.parser.next()) != 1) {
			if (type != 2) continue;
			String tag = pull.parser.getName();
			if ("cheat".equals(tag)) {
				cheats.putCheat(pull.getValue("name"));
				cheats.setType(pull.getValue("type"));
				cheats.setOn(!"false".equals(pull.getValue("enabled")));
			} else if ("code".equals(tag)) {
				String code = pull.getText();
				if (cheats.mCheat.isGs1) 
					code = code.replace(" ", "");
				cheats.putCode(code);
			}
		}
	}

	@Override
	public void writeCht(XmlWriter writer, Cheat cheat){
		int length = cheat.codes.size();
		if (length == 0)
			return;
		int codeType = -1;
		int tempType;
		int p;
		String code;
		for (p = 0; p < length; p++){
			code = cheat.codes.get(p);
			tempType = CheatCoder.getCodeType(code);
			if (codeType != tempType){
				codeType = tempType;
				if (p > 0)
					writer.endTag("cheat");
				writer.startTag("cheat");
				writer.attribute("type", TYPE_LIST[codeType]);
				writer.attribute("name", cheat.name);
				if (!cheat.checked) {
					writer.attribute("enabled", "false");
				}
			}
			writer.startTag("code");
			writer.text(code);
			writer.endTag("code");
		}
		writer.endTag("cheat");
	}
}
