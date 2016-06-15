package cza.hack;

import android.graphics.BitmapFactory;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipFile;

import cza.util.Pull;
import cza.util.XmlWriter;

public 
class GameBoid extends Emu {
	public static final String IMG = "screenshot.png";

	public GameBoid(){
		NAME = "GameBoid";
		packageName = "com.androidemu.gba";
	}

	public GameBoid(Game g) {
		this();
		load(g);
	}

	protected void onLoad() {
		saveDir = chtDir = gameDir;
	}

	@Override
	public File getSTFile(int i) {
		return withType("ss" + i);
	}

	@Override
	public void processST(GameST st) {
		try {
			ZipFile zip = new ZipFile(st.path);
			InputStream img = zip.getInputStream(zip.getEntry(IMG));
			if (img != null){
				st.bp = BitmapFactory.decodeStream(img);
			}
			zip.close();
		} catch (Exception e) {}
	}

	/**
	 * 获取相关文件
	 */
	@Override
	public File[] getRelativeFiles() {
		return getRelativeFiles(gameDir);
	}

	@Override
	public void parseCht(Pull pull, Cheats cheats) throws Exception {
		int type;
		while ((type = pull.parser.next()) != 1) {
			if (type != 2) continue;
			if ("item".equals(pull.parser.getName())){
				cheats.putCheat(pull.getValue("name"));
				cheats.putCode(pull.getValue("code"));
				cheats.setOn(!pull.getBoolean("disabled"));
			}
		}
	}

	@Override
	public void writeCht(XmlWriter writer, Cheat cheat){
		String name = cheat.name;
		for (String code: cheat.codes) {
			writer.startTag("item");
			if (!cheat.checked) {
				writer.attribute("disabled", "true");
			}
			writer.attribute("code", code);
			writer.attribute("name", name);
			writer.endTag("item");
			name = "同上";
		}
	}
}
