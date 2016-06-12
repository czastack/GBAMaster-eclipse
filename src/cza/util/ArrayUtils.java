package cza.util;

import java.lang.reflect.Array;

public class ArrayUtils {
	public static String join(String[] strAry){
        return join(strAry, ",");
    }

	public static String join(String[] strAry, CharSequence sep){
        StringBuilder sb = new StringBuilder(strAry[0]);
		for(int i = 1; i < strAry.length; i++){
			sb.append(sep + strAry[i]);
        }
        return sb.toString();
    }

	public static void reverse(String[] arr){
		int len = arr.length;
		String temp;
		for (int i = 0; i < len / 2; i++){
			temp = arr[i];
			int key = len - 1 - i;
			arr[i] = arr[key];
			arr[key] = temp;
		}
	}
	
	public static Object combine(Object[]...list) {
		int i;
		int length = 0;
		int start = 0;
		for (i = 0; i < list.length; i++){
			length += list[i].length;
		}
		Object newArray = Array.newInstance(list[0].getClass().getComponentType(), length);
		for (i = 0; i < list.length; i++){
			int mLength = list[i].length;
			System.arraycopy(list[i], 0, newArray, start, mLength);
			start += mLength;
		}
		return newArray;
	}
	
	/**
	 * 查找下标
	 * @param array
	 * @param e
	 * @return
	 */
	public static int indexOf(int[] array, int e){
		int index = -1;
		int i;
		for (i = 0; i < array.length; i++){
			if (array[i] == e){
				index = i;
				break;
			}
		}
		return index;
	}
	
	/**
	 * 查找下标
	 * @param array
	 * @param e
	 * @return
	 */
	public static int indexOf(byte[] array, byte e){
		int index = -1;
		int i;
		for (i = 0; i < array.length; i++){
			if (array[i] == e){
				index = i;
				break;
			}
		}
		return index;
	}
	
	/**
	 * 移动元素
	 * @param array
	 * @param before
	 * @param diff
	 * @return
	 */
	public static boolean move(int[] array, int before, int diff){
		return moveTo(array, before, before + diff);
	}
	
	/**
	 * 移动元素
	 * @param array
	 * @param before
	 * @param after
	 * @return
	 */
	public static boolean moveTo(int[] array, int before, int after){
		if (before != after && before > -1 && before < array.length 
				&& after > -1 && after < array.length){
			int temp = array[before];
			if(before < after){
				System.arraycopy(array, before + 1, array, before, after - before);
			} else {
				int[] tempArray = new int[before - after];
				System.arraycopy(array, after, tempArray, 0, tempArray.length);
				System.arraycopy(tempArray, 0, array, after + 1, tempArray.length);
			}
			array[after] = temp;
			return true;
		}
		return false;
	}
}
