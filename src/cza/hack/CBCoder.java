package cza.hack;

/**
 * CB码处理
 */
import java.util.regex.Matcher;

public class CBCoder extends Coder {
	private int increment = 2;
	public static char P = 32;
	private static CBCoder instance;
	public int addrOffset, valueOffset;

	public static CBCoder getInstance(){
		if (instance == null)
			instance = new CBCoder();
		return instance;
	}

	public void reset(){
		addrOffset = valueOffset = 0;
	}

	public boolean noOffset(){
		return addrOffset == 0 && valueOffset == 0;
	}

	public Cheats formatAll(String in){
		Cheats cheats = new Cheats();
		formatAll(in, cheats);
		return cheats;
	}

	public void formatAll(String in, Cheats cheats){
		String[] lines = in.split("\n");
		for (String line : lines){
			line = line.replaceAll("(?m)^\\s+|\\s+$|——|：$|(?i)on=", "")
				.replace(";", "\n");
			Matcher m = CODE.matcher(line);
			String name = m.replaceAll("").trim();
			cheats.putCheat(name);
			cheats.checkCheat(); //以免开头的无名称
			m.reset();
			while (m.find()){
				formatCode(m.group(), cheats.mCheat);
			}
		}
	}


	/**
	 * 解析作弊码字符串
	 */
	public void formatCode(String code, Cheat cheat){
		code = upper(code);
		if (code.matches(GS)){
			cheat.addGS(code);
			return;
		}
		if (noOffset() && code.matches(CB)){
			parseStandard(code, cheat);
			return;
		}
		//把地址和数值分开
		String[] ary = code.split("[ :,]", 2);
		if (ary.length < 2)
			return;
		long addr, value, tempAddr, tempValue;
		int acc = 0;
		addr = hexToDec(ary[0]);
		boolean noHead = (addr >> 28) == 0;
		String valueString = ary[1];
		if (valueString.contains(",")) 
			value = parseECValue(valueString);
		else 
			value = hexToDec(valueString);
		if (!cheat.isSlide) {
			addr += addrOffset;
			//修复无前缀地址（不足8位数的）
			if ((addr >> 24 & 0xF) == 0)
				addr |= 0x02000000;
		}
		//依次从末尾截取低位数值
		do {
			tempAddr = addr + acc;
			tempValue = value & FLAG_16BIT;
			if (!cheat.isSlide) {
				value += valueOffset;
				if (noHead) {
					if (tempValue >> 8 > 0)
						tempAddr |= 8L << 28;
					else 
						tempAddr |= 3 << 28;
				}
			}
			parseStandard(CheatCoder.rawToCB(tempAddr, tempValue), cheat);
			acc += increment;
			value >>= 16;
		} while (value > 0);
	}

	public Cheat formatCode(String code){
		Cheat cheat = new Cheat();
		formatCode(code, cheat);
		return cheat;
	}

	public static String formatCode(char head, int addr, int val, int deviant){
		StringBuilder sb = new StringBuilder();
		sb.append(head)
			.append(ao(toHex(addr + deviant), 7))
			.append(P)
			.append(ao(toHex(val), 4));
		return upper(sb.toString());
	}
	
	/**
	 * 解析标志CB码
	 */
	public static void parseStandard(String code, Cheat cheat){
		switch (code.charAt(0)){
			case '4':
			case '7':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
				cheat.addSlide(code);
				break;
			case '2':
			case '6':
				cheat.add(code);
				break;
			case '0':
			case '3':
			case '8':
			case 'E':
				cheat.addNormal(code);
		}
	}


	public static long parseECValue(String valueString){
		String[] bytes = valueString.split(",");
		long temp = 0;
		for (int i = bytes.length - 1; i > -1; i--){
			temp <<= 8;
			temp |= fromHex(bytes[i]);
		}
		return temp;
	}
}
