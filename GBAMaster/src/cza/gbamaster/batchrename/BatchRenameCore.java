package cza.gbamaster.batchrename;

import java.io.File;
import java.util.ArrayList;

import cza.file.FileInfo;
import cza.file.FileInfos;
import cza.file.FileUtils;

/**
 * 批量重命名核心类
 * @author CZA
 *
 */
public class BatchRenameCore {

	public boolean ableR, ableUndo, isUndo;
	public FileInfos mInfos;
	public static final String[] TYPES = {"自动补０", "数字编号", "高级编号", "增删字符", "查找替换", "导入列表"};
	public String newType;
	public Unit[] mPendingDirs;
	public ArrayList<Data> mDatas;
	public Namer mNamer;
	private int mId = -1;
	private final static int 
		ID_ZERO = 0,
		ID_NUMBER = 1,
		ID_NUMBERADVANCED = 2,
		ID_INSERT = 3,
		ID_REGEX = 4,
		ID_FROMLIST = 5;

	public BatchRenameCore(){
		mDatas = new ArrayList<Data>();
	}
	
	public String getTitle(){
		return TYPES[mId];
	}
	
	/**
	 * 设置数据源
	 */
	public void setInfos(FileInfos infos){
		mInfos = infos;
	}
	
	/**
	 * 获取主名
	 */
	public static String getMain(FileInfo info) {
		return FileUtils.getMainName(info.name);
	}
	
	/**
	 * 获取拓展名
	 */
	public static String getType(FileInfo info) {
		return info.type.isEmpty() ? "" : "." + info.type;
	}
	
	public boolean changeType(int type){
		if (type != mId){
			mId = type;
			switch(type){
			case ID_ZERO:
				mNamer = new ZeroNamer(); break;
			case ID_NUMBER:
				mNamer = new NumberNamer(); break;
			case ID_NUMBERADVANCED:
				mNamer = new NumberAdvancedNamer(); break;
			case ID_INSERT:
				mNamer = new InsertNamer(); break;
			case ID_REGEX:
				mNamer = new RegexNamer(); break;
			case ID_FROMLIST:
				mNamer = new FromListNamer(); break;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 生成预览结果
	 */
	public void build() {
		mDatas.clear();
		if (ableR){
			//递归处理目录
			//递归模式下不处理选中的目录，
			//而是其中的文件
			int length = mInfos.dirCount;
			int index = 0;
			boolean includeCurrentDir = length < mInfos.size();
			if (includeCurrentDir)
				length++;
			mPendingDirs = new Unit[length];
			if (includeCurrentDir)
				//包含当前目录
				mPendingDirs[index++] = new Unit(mInfos.getInfos(false));
			for (FileInfo info : mInfos.getInfos(true)){
				//重置递归增量
				mNamer.onRecursionReset();
				File folder = new File(mInfos.path, info.name);
				//只处理文件
				mPendingDirs[index++] = new Unit(new FileInfos(folder).getInfos(false));
			}
		} else {
			//只处理当前目录
			mPendingDirs = new Unit[]{new Unit(mInfos)};
		}
		isUndo = false;
	}

	/**
	 * 开始重命名
	 */
	public void start() {
		for (Unit dir : mPendingDirs) {
			dir.start();
		}
		if (isUndo) {
			isUndo = false;
			ableUndo = false;
		} else {
			ableUndo = true;
		}
	}
	
	/**
	 * 撤销重命名
	 */
	public void undo() {
		if (!ableUndo)
			return;
		if (!isUndo) 
			isUndo = true;
		for (Unit dir : mPendingDirs) 
			dir.undo();
	}


	/**
	 * 内部类：目录处理单元
	 */
	private class Unit{
		private String path;
		private String[] names, newNames;
		private int mStartIndex;

		public Unit(FileInfos infos){
			path = infos.path;
			if(mNamer instanceof NumberAdvancedNamer)
			((NumberAdvancedNamer)mNamer).pName = path.substring(path.lastIndexOf(47) + 1);
			int len = infos.size();
			int index = 0;
			names = new String[len];
			newNames = new String[len];
			mDatas.add(new Data(path));
			mStartIndex = mDatas.size();
			for (FileInfo info : infos) {
				String name = info.name,
					newName = mNamer.getName(info);
				if (!info.isDir && !newType.isEmpty()) {
					newName = FileUtils.getMainName(newName) + "." + newType;
				}
				names[index] = name;
				newNames[index++] = newName;
				mDatas.add(new Data(name, newName));
			}
		}

		/**
		 * 真正的重命名
		 */
		public void start(){
			int start = mStartIndex;
			for (int i = 0; i < names.length; i++) {
				Data data = mDatas.get(start + i);
				data.origin = names[i];
				data.current = newNames[i];
				if (data.origin.equals(data.current)) {
					data.state = "忽略";
				} else {
					File file = new File(path, data.origin);
					File newFile = new File(path, data.current);
					if (newFile.exists()) {
						data.state = "文件已存在";
					} else {
						file.renameTo(newFile);
						data.state = "完成";
					}
				}
			}
		}

		public void undo(){
			int start = mStartIndex;
			String[] temp;
			temp = newNames;
			newNames = names;
			names = temp;
			for (int i = 0; i < names.length; i++){
				Data data = mDatas.get(start + i);
				data.origin = names[i];
				data.current = newNames[i];
				data.state = null;
			}
		}
	}

	/**
	 * 内部类：数据
	 */
	public class Data {
		public String origin, current, state;
		public boolean isDir;
		
		public Data(String path){
			origin = path + "/";
			isDir = true;
		}
		
		public Data(String origin, String current){
			this.origin = origin;
			this.current = current;
		}
	}
}
