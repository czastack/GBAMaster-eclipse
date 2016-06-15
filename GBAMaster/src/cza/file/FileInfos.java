package cza.file;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import cza.util.CheckableItems;

public class FileInfos extends CheckableItems<FileInfo> {
	private static final long serialVersionUID = 3053683542644198558L;
	public int dirCount, selection;
	public String path;

	public FileInfos(String dir){
		path = dir;
	}

	public FileInfos(File folder){
		this(folder.listFiles(FileUtils.VISIBLE));
		path = folder.getPath();
	}

	public FileInfos(File[] fileArr){
		super(fileArr.length);
		getData(fileArr);
	}

	/**
	 * 更新数据
	 * @param fileArr
	 */
	public void getData(File folder){
		getData(folder.listFiles(FileUtils.VISIBLE));
		path = folder.getPath();
	}
	
	/**
	 * 更新数据
	 * @param fileArr
	 */
	public void getData(File[] fileArr){
		LinkedList<FileInfo> mList,
			folders = new LinkedList<FileInfo>(),
			files = new LinkedList<FileInfo>();
		ListIterator<FileInfo> itr;
		for (File file: fileArr) {
			FileInfo info = new FileInfo(file);
			mList = info.isDir ? folders : files;
			itr = mList.listIterator();
			while (itr.hasNext()) {
				if (itr.next().after(info)) {
					itr.previous();
					break;
				}
			}
			itr.add(info);
		}
		addAll(folders);
		addAll(files);
		dirCount = folders.size();
	}

	/**
	 * 批量选中文件/文件夹
	 * @param isDir
	 */
	public void checkInfos(boolean isDir){
		boolean isF = !isDir;
		for (int i = 0; i < dirCount; i++)
			get(i).checked = isDir;
		for (int i = dirCount, size = size(); i < size; i++)
			get(i).checked = isF;
		checkedCount = isDir ? dirCount : size() - dirCount;
	}

	/**
	 * 获取选中的文件快照
	 * @return
	 */
	public FileInfos getCheckedInfos(){
		FileInfos chks = new FileInfos(path);
		for (FileInfo info: this){
			if (info.checked){
				chks.add(info);
				if (info.isDir){
					chks.dirCount++;
				}
			}
		}
		return chks;
	}

	/**
	 * 截取文件/文件夹部分
	 * @param isDir
	 * @return
	 */
	public List<FileInfo> getList(boolean isDir){
		return isDir ? subList(0, dirCount) : subList(dirCount, size());
	}

	/**
	 * 分离文件 /文件夹 
	 * @param isDir 是否是文件夹
	 * @return
	 */
	public FileInfos getInfos(boolean isDir){
		FileInfos infos = new FileInfos(path);
		infos.addAll(getList(isDir));
		infos.dirCount = isDir ? dirCount : 0;
		return infos;
	}
	
	/**
	 * 根据文件名获取位置
	 * @param name
	 * @return
	 */
	public int indexOf(String name){
		int index = -1;
		for (int i = 0, len = size(); i < len; i++) {
			if (get(i).name.equals(name)) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	public File getFileAt(int index){
		return new File(path, get(index).name);
	}
	
	public File[] toArray(){
		int length = size();
		File[] array = new File[length];
		for (int i = 0; i < length; i++)
			array[i] = getFileAt(i);
		return array;
	}
}
