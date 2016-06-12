package cza.hack;

public class CheatCoder extends Coder {
	/**
	 * Gameshark code types:
	 *
	 * NNNNNNNN 001DC0DE - ID code for the game (game 4 character name) from ROM
	 * DEADFACE XXXXXXXX - changes decryption seeds
	 * 0AAAAAAA 000000YY - 8-bit constant write
	 * 1AAAAAAA 0000YYYY - 16-bit constant write
	 * 2AAAAAAA YYYYYYYY - 32-bit constant write
	 * 3AAAAAAA YYYYYYYY - ??
	 * 6AAAAAAA 0000YYYY - 16-bit ROM Patch (address >> 1)
	 * 6AAAAAAA 1000YYYY - 16-bit ROM Patch ? (address >> 1)
	 * 6AAAAAAA 2000YYYY - 16-bit ROM Patch ? (address >> 1)
	 * 8A1AAAAA 000000YY - 8-bit button write
	 * 8A2AAAAA 0000YYYY - 16-bit button write
	 * 8A3AAAAA YYYYYYYY - 32-bit button write
	 * 80F00000 0000YYYY - button slow motion
	 * DAAAAAAA 0000YYYY - if address contains 16-bit value enable next code
	 * FAAAAAAA 0000YYYY - Master code function
	 *
	 * CodeBreaker codes types:
	 *
	 * 0000AAAA 000Y - Game CRC (Y are flags: 8 - CRC, 2 - DI)
	 * 1AAAAAAA YYYY - Master Code function (store address at ((YYYY << 0x16)
	 *                 + 0x08000100))
	 * 2AAAAAAA YYYY - 16-bit or
	 * 3AAAAAAA YYYY - 8-bit constant write
	 * 4AAAAAAA YYYY - Slide code
	 * XXXXCCCC IIII   (C is count and I is address increment, X is value increment.)
	 * 5AAAAAAA CCCC - Super code (Write bytes to address, CCCC is count)
	 * BBBBBBBB BBBB 
	 * 6AAAAAAA YYYY - 16-bit and
	 * 7AAAAAAA YYYY - if address contains 16-bit value enable next code
	 * 8AAAAAAA YYYY - 16-bit constant write
	 * 9AAAAAAA YYYY - change decryption (when first code only?)
	 * AAAAAAAA YYYY - if address does not contain 16-bit value enable next code
	 * BAAAAAAA YYYY - if 16-bit < YYYY
	 * CAAAAAAA YYYY - if 16-bit > YYYY
	 * D0000020 YYYY - if button keys equal value enable next code
	 * EAAAAAAA YYYY - increase value stored in address
	 *
	 * 以 4, 7, A, B, C, D 开头的CB码是两行代码的开头。
	 */

	public final static byte 
	UNKNOWN_CODE = -1,
	INT_8_BIT_WRITE = 0,
	INT_16_BIT_WRITE = 1,
	INT_32_BIT_WRITE = 2,
	GSA_16_BIT_ROM_PATCH = 3,
	GSA_8_BIT_GS_WRITE = 4,
	GSA_16_BIT_GS_WRITE = 5,
	GSA_32_BIT_GS_WRITE = 6,
	CBA_IF_KEYS_PRESSED = 7,
	CBA_IF_TRUE = 8,
	CBA_SLIDE_CODE = 9, //CB压缩码
	CBA_IF_FALSE = 10,
	CBA_AND = 11,
	GSA_8_BIT_GS_WRITE2 = 12,
	GSA_16_BIT_GS_WRITE2 = 13,
	GSA_32_BIT_GS_WRITE2 = 14,
	GSA_16_BIT_ROM_PATCH2 = 15,
	GSA_8_BIT_SLIDE = 16,
	GSA_16_BIT_SLIDE = 17,
	GSA_32_BIT_SLIDE = 18,
	GSA_8_BIT_IF_TRUE = 19,
	GSA_32_BIT_IF_TRUE = 20,
	GSA_8_BIT_IF_FALSE = 21,
	GSA_32_BIT_IF_FALSE = 22,
	GSA_8_BIT_FILL = 23,
	GSA_16_BIT_FILL = 24,
	GSA_8_BIT_IF_TRUE2 = 25,
	GSA_16_BIT_IF_TRUE2 = 26,
	GSA_32_BIT_IF_TRUE2 = 27,
	GSA_8_BIT_IF_FALSE2 = 28,
	GSA_16_BIT_IF_FALSE2 = 29,
	GSA_32_BIT_IF_FALSE2 = 30,
	GSA_SLOWDOWN = 31,
	CBA_ADD = 32,
	CBA_OR = 33,
	CBA_LT = 34, //小于
	CBA_GT = 35, //大于
	CBA_SUPER = 36,
	
	TYPE_CB = 0,
	TYPE_V1 = 1,
	TYPE_V3 = 2,
	TYPE_LIST[][] = { //CB V1 V3
		{
			INT_8_BIT_WRITE,
			INT_16_BIT_WRITE,
			CBA_SLIDE_CODE,
			CBA_OR,
			CBA_AND,
			CBA_IF_TRUE,
			CBA_IF_FALSE,
			CBA_LT,
			CBA_GT,
			CBA_IF_KEYS_PRESSED,
			CBA_ADD
		},
		{
			INT_8_BIT_WRITE,
			INT_16_BIT_WRITE,
			INT_32_BIT_WRITE,
			GSA_16_BIT_ROM_PATCH,
			GSA_8_BIT_GS_WRITE,
			GSA_16_BIT_GS_WRITE,
			GSA_32_BIT_GS_WRITE,
			CBA_IF_TRUE
		},
		{
			GSA_8_BIT_FILL,
			GSA_16_BIT_FILL,
			INT_32_BIT_WRITE,
			GSA_8_BIT_IF_TRUE,
			CBA_IF_TRUE,
			GSA_32_BIT_IF_TRUE,
			GSA_8_BIT_IF_FALSE,
			CBA_IF_FALSE,
			GSA_32_BIT_IF_FALSE,
			GSA_8_BIT_IF_TRUE2,
			GSA_16_BIT_IF_TRUE2,
			GSA_32_BIT_IF_TRUE2,
			GSA_8_BIT_IF_FALSE2,
			GSA_16_BIT_IF_FALSE2,
			GSA_32_BIT_IF_FALSE2,
			GSA_8_BIT_GS_WRITE2,
			GSA_16_BIT_GS_WRITE2,
			GSA_32_BIT_GS_WRITE2,
			GSA_16_BIT_ROM_PATCH2,
			GSA_8_BIT_SLIDE,
			GSA_16_BIT_SLIDE,
			GSA_32_BIT_SLIDE
		}
	};

	private static final long FLAG_7 = 0x0FFFFFFF,
	ROLLINGSEED = 0xC6EF3720L,
	SEED_INCREMENT = 0x9E3779B9L,
	seeds_v1[] = {0x09F4FBBD, 0x9681884AL, 0x352027E9, 0xF3DEE5A7L},
	seeds_v3[] = {0x7AA9648F, 0x7FAE6994, 0xC0EFAAD5L, 0x42712C57};

	/**
	 * 获取值大小
	 */
	public static int getBit(byte size) {
		switch (size) {
			case INT_8_BIT_WRITE:
			case GSA_8_BIT_GS_WRITE:
			case GSA_8_BIT_GS_WRITE2:
			case GSA_8_BIT_SLIDE:
			case GSA_8_BIT_IF_TRUE:
			case GSA_8_BIT_IF_FALSE:
			case GSA_8_BIT_FILL:
			case GSA_8_BIT_IF_TRUE2:
			case GSA_8_BIT_IF_FALSE2:
			case GSA_SLOWDOWN:
				return 8;
			case INT_16_BIT_WRITE:
			case GSA_16_BIT_ROM_PATCH:
			case GSA_16_BIT_GS_WRITE:
			case CBA_IF_KEYS_PRESSED:
			case CBA_IF_TRUE:
			case CBA_SLIDE_CODE:
			case CBA_IF_FALSE:
			case CBA_AND:
			case GSA_16_BIT_GS_WRITE2:
			case GSA_16_BIT_ROM_PATCH2:
			case GSA_16_BIT_SLIDE:
			case GSA_16_BIT_FILL:
			case GSA_16_BIT_IF_TRUE2:
			case GSA_16_BIT_IF_FALSE2:
			case CBA_ADD:
			case CBA_OR:
			case CBA_LT:
			case CBA_GT:
			case CBA_SUPER:
				return 16;
			case INT_32_BIT_WRITE:
			case GSA_32_BIT_GS_WRITE:
			case GSA_32_BIT_GS_WRITE2:
			case GSA_32_BIT_SLIDE:
			case GSA_32_BIT_IF_TRUE:
			case GSA_32_BIT_IF_FALSE:
			case GSA_32_BIT_IF_TRUE2:
			case GSA_32_BIT_IF_FALSE2:
				return 32;
			default:
				return 0;
		}
	}

	/**
	 * 截取的flag
	 */
	public static long getTypeFlag(byte type){
		return getBitFlag(getBit(type));
	}

	/**
	 * 截取的flag
	 */
	public static long getBitFlag(int bit){
		switch(bit){
			case 8:
				return FLAG_8BIT;
			case 16:
				return FLAG_16BIT;
			case 32:
			default:
				return FLAG_32BIT;
		}
	}

	/**
	 * 值长度
	 */
	public static int getTypeSize(byte type){
		return getBit(type) >> 3;
	}

	/**
	 * GS解码
	 */
	private static long[] decodeGS(long address, long value, boolean v3){
		long rollingseed = ROLLINGSEED,
			increment = SEED_INCREMENT,
			s1, s2, s3, s4, t1, t2, t3, t4;
		long[] seeds = v3 ? seeds_v3 : seeds_v1;
		int bitsleft = 32;
		while (bitsleft > 0) {
			s1 = (address << 4 & FLAG_32BIT) + seeds[2];
			s2 = (address + rollingseed);
			s3 = s1 ^ s2;
			s4 = (address >> 5) + seeds[3];
			value = (value - (s3 ^ s4)) & FLAG_32BIT;

			t1 = (value << 4 & FLAG_32BIT) + seeds[0];
			t2 = value + rollingseed;
			t3 = t1 ^ t2;
			t4 = (value >> 5) + seeds[1];
			address = (address - (t3 ^ t4)) & FLAG_32BIT;

			rollingseed = (rollingseed - increment) & FLAG_32BIT;
			bitsleft--;
		}
		return new long[]{address, value};
	}

	/**
	 * GS编码
	 */
	private static long[] encodeGS(long address, long value, boolean v3){
		long rollingseed = 0,
			increment = SEED_INCREMENT,
			s1, s2, s3, s4, t1, t2, t3, t4;
		long[] seeds = v3 ? seeds_v3 : seeds_v1;
		int bitsleft = 32;
		while (bitsleft > 0) {
			rollingseed = (rollingseed + increment) & FLAG_32BIT;

			t1 = (value << 4 & FLAG_32BIT) + seeds[0];
			t2 = value + rollingseed;
			t3 = t1 ^ t2;
			t4 = (value >> 5) + seeds[1];
			address = (address + (t3 ^ t4)) & FLAG_32BIT;

			s1 = (address << 4 & FLAG_32BIT) + seeds[2];
			s2 = address + rollingseed;
			s3 = s1 ^ s2;
			s4 = (address >> 5) + seeds[3];
			value = (value + (s3 ^ s4)) & FLAG_32BIT;

			bitsleft--;
		}
		return new long[]{address, value};
	}

	/**
	 * 生成作弊码
	 */
	public static void extensibleEncode(Code code){
		int bit = getBit(code.func);
		long addr = code.addr;
		long value = code.value;
		code.startBatchMode();
		do {
			code.addr = addr;
			code.value = value;
			encode(code);
			addr += bit >> 3;
			value >>= bit;
		} while (value > 0);
		code.endBatchMode();
	}

	/**
	 * 生成作弊码
	 */
	public static void encode(Code code){
		byte type = code.func;
		if (code.type == TYPE_CB){
			long address2 = 0;
			long value2 = 0;
			code.addr = (code.addr & FLAG_7) | 0x02000000 | (getCBType(type) << 28);
			if (type == INT_32_BIT_WRITE) {
				address2 = code.addr + 2;
				value2 = code.value >> 16;
			} else if (type == CBA_SLIDE_CODE) {
				address2 = (code.valueInc << 16) | code.dataSize;
				value2 = code.addrInc;
			}
			code.value &= FLAG_16BIT;
			code.toBeCB();
			if (address2 != 0) 
				code.addLine(rawToCB(address2, value2));
		} else if (code.type == TYPE_V3){
			/*
			 * raw 地址 -> v3 地址
			 * 把第2个数右移一个变成 0x00AAAAAAA
			 * 类型乘2作为前两个字母 0xTTAAAAAAA
			 */
			if (type != GSA_16_BIT_ROM_PATCH2)
				code.addr = (code.addr & 0x000FFFFF) | ((code.addr >> 4) & 0x00F00000) | (getV3TypeHex(type) << 25);
			code.value &= getTypeFlag(type);
			switch (code.func){
				case GSA_8_BIT_FILL:
				case GSA_16_BIT_FILL:
					code.value = code.value | ((code.dataSize - 1) << getBit(type));
					code.toBeGS(true);
					break;
				case INT_32_BIT_WRITE:
				case GSA_8_BIT_IF_TRUE:
				case CBA_IF_TRUE:
				case GSA_32_BIT_IF_TRUE:
				case GSA_8_BIT_IF_FALSE:
				case CBA_IF_FALSE:
				case GSA_32_BIT_IF_FALSE:
				case GSA_8_BIT_IF_TRUE2:
				case GSA_16_BIT_IF_TRUE2:
				case GSA_32_BIT_IF_TRUE2:
				case GSA_8_BIT_IF_FALSE2:
				case GSA_16_BIT_IF_FALSE2:
				case GSA_32_BIT_IF_FALSE2:
					code.toBeGS(true);
					break;
				case GSA_8_BIT_GS_WRITE2:
				case GSA_16_BIT_GS_WRITE2:
				case GSA_32_BIT_GS_WRITE2:
					code.setText(rawToGS(0, code.addr, true));
					code.addLine(rawToGS(code.value, 0, true));
					break;
				case GSA_16_BIT_ROM_PATCH2:
					code.addr = ((code.addr & 0x00FFFFFF) >> 1) | (getV3TypeHex(type) << 25);
					code.setText(rawToGS(0, code.addr, true));
					code.addLine(rawToGS(code.value, 0, true));
					break;
				case GSA_8_BIT_SLIDE:
				case GSA_16_BIT_SLIDE:
				case GSA_32_BIT_SLIDE: {
						/*
						 * Example: 
						 * raw=02001234:12345678
						 * data=10; Value Inc=1; Address Inc=2
						 * unencrypted:
						 * 00000000 80201234
						 * 12345678 010A0002
						 * args0 data, args1 value inc, args2 addr inc
						 */
						long value2;
						value2 = (code.valueInc << 24) | (code.dataSize << 16) | code.addrInc;
						code.setText(rawToGS(0, code.addr, true));
						code.addLine(rawToGS(code.value, value2, true));
					}
					break;
			}
		} else {
			code.addr &= FLAG_7;
			code.value &= getTypeFlag(type);
			switch (type) {
				case INT_8_BIT_WRITE:
				case INT_16_BIT_WRITE:
				case INT_32_BIT_WRITE:
					code.addr |= (getBit(type) & 0xf0) << 24;
					code.toBeGS(false);
					break;
				case GSA_16_BIT_ROM_PATCH:
					code.addr = (code.addr >> 1) | 0x64000000;
					code.toBeGS(false);
					break;
				case GSA_8_BIT_GS_WRITE:
				case GSA_16_BIT_GS_WRITE:
				case GSA_32_BIT_GS_WRITE:
					code.addr = code.addr & 0x0F0FFFFF | 0x80000000L | (((getBit(type) & 0xf0) + 0x10) << 16);
					code.toBeGS(false);
					break;
				case CBA_IF_TRUE:
					code.addr |= 0xDL << 28;
					code.toBeGS(false);
			}
		}
	}

	/**
	 * 生成CB
	 */
	public static String rawToCB(long address, long value){
		StringBuilder sb = new StringBuilder(13);
		sb.append(toHEX(address));
		ao(sb, 0, 8);
		sb.append(' ');
		sb.append(toHEX(value));
		ao(sb, 9, 4);
		return sb.toString();
	}

	/**
	 * 获取CB码对应的开头
	 */
	private static long getCBType(byte type){
		switch (type){
			case INT_8_BIT_WRITE:
				return 0x3;
			default:
			case INT_16_BIT_WRITE:
				return 0x8;
			case CBA_SLIDE_CODE:
				return 0x4;
			case CBA_OR:
				return 0x2;
			case CBA_SUPER:
				return 0x5;
			case CBA_AND:
				return 0x6;
			case CBA_IF_TRUE:
				return 0x7;
			case CBA_IF_FALSE:
				return 0xA;
			case CBA_LT:
				return 0xB;
			case CBA_GT:
				return 0xC;
			case CBA_IF_KEYS_PRESSED:
				return 0xD;
			case CBA_ADD:
				return 0xE;
		}
	}


	/**
	 * 判断作弊码类型
	 */
	public static int getCodeType(String code){
		if (code.matches(CB))
			return TYPE_CB;
		else if (code.matches(GS)) {
			if (code.charAt(8) == ' ')
				return TYPE_V3;
			else 
				return TYPE_V1;
		}
		return -1;
	}

	/**
	 * 入口：RAW字符串 转 GS
	 * @param code XXXXXXXX:XXXXXXXX
	 */
	public static String encodeGS(String code, boolean v3) {
		long address, value;
		address = hexToDec(code.substring(0, 8));
		value = hexToDec(code.substring(9, 17));
		return rawToGS(address, value, v3);
	}

	/**
	 * 入口：unencrypted格式的raw 转 GS
	 */
	public static String rawToGS(long address, long value, boolean v3) {
		long[] array = encodeGS(address, value, v3);
		address = array[0];
		value = array[1];
		StringBuilder sb = new StringBuilder(17);
		sb.append(toHEX(address));
		ao(sb, 0, 8);
		int valueStart = 8;
		if (v3) {
			sb.append(' ');
			valueStart++;
		}
		sb.append(toHEX(value));
		ao(sb, valueStart, 8);
		return sb.toString();
	}

	/**
	 * 获取V3类型码
	 */
	public static long getV3TypeHex(byte type){
		switch (type){
				//单行
			case GSA_8_BIT_FILL:
				return 0x00;
			case GSA_16_BIT_FILL:
				return 0x01;
			case INT_32_BIT_WRITE:
				return 0x02;
			case GSA_8_BIT_IF_TRUE:
				return 0x04;
			case CBA_IF_TRUE:
				return 0x05;
			case GSA_32_BIT_IF_TRUE:
				return 0x06;
			case GSA_8_BIT_IF_FALSE:
				return 0x08;
			case CBA_IF_FALSE:
				return 0x09;
			case GSA_32_BIT_IF_FALSE:
				return 0x0a;
			case GSA_8_BIT_IF_TRUE2:
				return 0x24;
			case GSA_16_BIT_IF_TRUE2:
				return 0x25;
			case GSA_32_BIT_IF_TRUE2:
				return 0x26;
			case GSA_8_BIT_IF_FALSE2:
				return 0x28;
			case GSA_16_BIT_IF_FALSE2:
				return 0x29;
			case GSA_32_BIT_IF_FALSE2:
				return 0x2a;
				//双行
			case GSA_SLOWDOWN:
				return 0x04;
			case GSA_8_BIT_GS_WRITE2:
				return 0x08;
			case GSA_16_BIT_GS_WRITE2:
				return 0x09;
			case GSA_32_BIT_GS_WRITE2:
				return 0x0a;
			case GSA_16_BIT_ROM_PATCH2:
				return 0x0c;
			case GSA_8_BIT_SLIDE:
				return 0x40;
			case GSA_16_BIT_SLIDE:
				return 0x41;
			case GSA_32_BIT_SLIDE:
				return 0x42;
			default:
				return 0;
		}
	}

	/**
	 * 入口：解析金手指
	 */
	public static void decode(String code, Code back) {
		long address, value;
		byte codeType;
		byte codeFunc = 0;
		if (code.matches(CB)) {
			codeType = TYPE_CB;
			address = hexToDec(code.substring(0, 8));
			value = hexToDec(code.substring(9, 13));
			if (back.waitForSecond) {
				//处理双行代码的第二条
				back.addrInc = (int)value;
				back.dataSize = (int)(address & FLAG_16BIT);
				address >>= 16;
				back.valueInc = (int)(address & FLAG_16BIT);
				back.waitForSecond = false;
				return;
			}
			int head = getFirst(address);
			address &= FLAG_7;
			switch (head) {
				case 0x3:
					codeFunc = INT_8_BIT_WRITE;
					break;
				case 0x8:
					codeFunc = INT_16_BIT_WRITE;
					break;
				case 0x4:
					codeFunc = CBA_SLIDE_CODE;
					back.waitForSecond = true;
					break;
				case 0x2:
					codeFunc = CBA_OR;
					break;
				case 0x5:
					codeFunc = CBA_SUPER;
					break;
				case 0x6:
					codeFunc = CBA_AND;
					break;
				case 0x7:
					codeFunc = CBA_IF_TRUE;
					break;
				case 0xA:
					codeFunc = CBA_IF_FALSE;
					break;
				case 0xB:
					codeFunc = CBA_LT;
					break;
				case 0xC:
					codeFunc = CBA_GT;
					break;
				case 0xD:
					codeFunc = CBA_IF_KEYS_PRESSED;
					break;
				case 0xE:
					codeFunc = CBA_ADD;
					break;
			}

		} else if (code.matches(GS)) {
			long[] temp;
			int valueStart = 8;
			boolean v3 = code.charAt(valueStart) == ' ';
			if (v3)
				valueStart++;
			address = hexToDec(code.substring(0, 8));
			value = hexToDec(code.substring(valueStart));
			temp = decodeGS(address, value, v3);
			address = temp[0];
			value = temp[1];
			//如果是ID识别码
			if (value == 0x1DC0DE) {
				//ROM识别码
				return;
			}
			if (v3) {
				codeType = TYPE_V3;
				if (back.waitForSecond) {
					//处理双行代码的第二条
					switch (back.func) {
						case GSA_8_BIT_GS_WRITE2:
						case GSA_16_BIT_GS_WRITE2:
						case GSA_32_BIT_GS_WRITE2:
						case GSA_16_BIT_ROM_PATCH2:
							back.value = address;
							break;
						case GSA_8_BIT_SLIDE:
						case GSA_16_BIT_SLIDE:
						case GSA_32_BIT_SLIDE:
							back.value = address;
							back.addrInc = (int)(value & FLAG_16BIT);
							value >>= 16;
							back.dataSize = (int)(value & FLAG_8BIT);
							value >>= 8;
							back.valueInc = (int)(value & FLAG_8BIT);
							break;
					}
					back.waitForSecond = false;
					return;
				}
				int type = (int)((address >> 25) & 0x7F); //最高位两个数除二
				address = (address & 0x00F00000) << 4 | (address & 0x0003FFFF);
				switch (type) {
					case 0x00:
						if (address == 0) {
							//对于特殊代码 地址其实是第一行的数值
							type = (int)((value >> 25) & 0x7F);
							address = (value & 0x00F00000) << 4 | (value & 0x0003FFFF);
							switch (type) {
								case 0x04:
									codeFunc = GSA_SLOWDOWN;
									break;
								case 0x08:
								case 0x09:
								case 0x0a:
									if (type == 0x08)
										codeFunc = GSA_8_BIT_GS_WRITE2;
									else if (type == 0x09)
										codeFunc = GSA_16_BIT_GS_WRITE2;
									else 
										codeFunc = GSA_32_BIT_GS_WRITE2;
									back.waitForSecond = true;
									value = 0;
									break;
								case 0x0c:
								case 0x0d:
								case 0x0e:
								case 0x0f:
									codeFunc = GSA_16_BIT_ROM_PATCH2;
									address = 0x08000000 | ((value & 0x00FFFFFF) << 1);
									back.waitForSecond = true;
									value = 0;
									break;
								case 0x40:
								case 0x41:
								case 0x42:
									if (type == 0x40)
										codeFunc = GSA_8_BIT_SLIDE;
									else if (type == 0x41)
										codeFunc = GSA_16_BIT_SLIDE;
									else 
										codeFunc = GSA_32_BIT_SLIDE;
									back.waitForSecond = true;
									value = 0;
									break;
								default: //UNKNOWN_CODE
									break;
							}
						} else {
							codeFunc = GSA_8_BIT_FILL;
							back.dataSize = (int)(value >> 8);
							value &= FLAG_8BIT;
						}
						break;
					case 0x01:
						codeFunc = GSA_16_BIT_FILL;
						back.dataSize = (int)(value >> 16);
						value &= FLAG_16BIT;
						break;
					case 0x02:
						codeFunc = INT_32_BIT_WRITE;
						break;
					case 0x04:
						codeFunc = GSA_8_BIT_IF_TRUE;
						break;
					case 0x05:
						codeFunc = CBA_IF_TRUE;
						break;
					case 0x06:
						codeFunc = GSA_32_BIT_IF_TRUE;
						break;
					case 0x08:
						codeFunc = GSA_8_BIT_IF_FALSE;
						break;
					case 0x09:
						codeFunc = CBA_IF_FALSE;
						break;
					case 0x0a:
						codeFunc = GSA_32_BIT_IF_FALSE;
						break;
					case 0x24:
						codeFunc = GSA_8_BIT_IF_TRUE2;
						break;
					case 0x25:
						codeFunc = GSA_16_BIT_IF_TRUE2;
						break;
					case 0x26:
						codeFunc = GSA_32_BIT_IF_TRUE2;
						break;
					case 0x28:
						codeFunc = GSA_8_BIT_IF_FALSE2;
						break;
					case 0x29:
						codeFunc = GSA_16_BIT_IF_FALSE2;
						break;
					case 0x2a:
						codeFunc = GSA_32_BIT_IF_FALSE2;
						break;
					default: //UNKNOWN_CODE
						break;
				}
			} else {
				//v1/v2
				codeType = TYPE_V1;
				int type = (int)((address >> 28) & 0xF); //取16进制第一位字母的10进制
				switch (type) {
					case 0: // 8位ram写入
					case 1: //16位ram写入
					case 2: //32位ram写入
						address &= FLAG_7;
						if (type == 0)
							codeFunc = INT_8_BIT_WRITE;
						else if (type == 1) 
							codeFunc = INT_16_BIT_WRITE;
						else 
							codeFunc = INT_32_BIT_WRITE;
						break;
					case 6: 
						//16位rom patch (补丁) GSA_16_BIT_ROM_PATCH
						address <<= 1;
						type = (int)((address >> 28) & 0xF);
						if (type == 0x0c) {
							codeFunc = GSA_16_BIT_ROM_PATCH;
							address &= FLAG_7;
							break;
						}
						//UNKNOWN_CODE
						break;
					case 8:
						int i = (int)((address >> 20) & 0xF); //取16进制第三位字母的10进制
						switch (i) {
							case 1:
							case 2:
							case 3:
								address &= 0x0F0FFFFF;
								if (type == 1)
									codeFunc = GSA_8_BIT_GS_WRITE;
								else if (type == 2) 
									codeFunc = GSA_16_BIT_GS_WRITE;
								else 
									codeFunc = GSA_32_BIT_GS_WRITE;
								break;
							case 15:
								codeFunc = GSA_SLOWDOWN;
								value &= 0xFF00;
								break;
							default: //UNKNOWN_CODE
								break;
						}
						break;
					case 0x0d:
						if (address != 0xDEADFACE) { 
							codeFunc = CBA_IF_TRUE;
							address &= FLAG_7;
						}
						break;
					default: //UNKNOWN_CODE
						break;
				}
			}
		} else {
			return;
		}
		back.addr = address;
		back.value = value;
		back.type = codeType;
		back.func = codeFunc;
	}


	/**
	 * 取最高位
	 */
	public static int getFirst(long num){
		return (int)((num >> 28) & 0xF);
	}

	public static boolean ableLongValue(byte type){
		switch (type){
			case INT_8_BIT_WRITE:
			case INT_16_BIT_WRITE:
			case INT_32_BIT_WRITE:
			case GSA_8_BIT_GS_WRITE:
			case GSA_16_BIT_GS_WRITE:
			case GSA_32_BIT_GS_WRITE:
			case GSA_8_BIT_GS_WRITE2:
			case GSA_16_BIT_GS_WRITE2:
			case GSA_32_BIT_GS_WRITE2:
			case GSA_16_BIT_ROM_PATCH:
			case GSA_16_BIT_ROM_PATCH2:
				return true;
			default:
				return false;
		}
	}
}
