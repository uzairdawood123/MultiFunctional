package com.xsm.lib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileControl {

	/**
	 * 将一个byte[]流保存到指定的文件中
	 * @param name 文件全名，包含路径
	 * @param obj 要保存的数据
	 * @throws IOException
	 */
	public static void save(String name, byte[] obj) throws IOException {
		File file = new File(name);
		if  (!file.getParentFile().exists()  && !file.getParentFile().isDirectory()) file.getParentFile().mkdirs(); //如果目录不存在则创建目录
		if(file.exists()) file.delete(); //如果已存在，则先删除，重新建文件
		file.createNewFile();
		FileOutputStream file_out_stream = new FileOutputStream(file, true);  //如果追加方式用true
		file_out_stream.write(obj);
		file_out_stream.flush();
		file_out_stream.close();
	}
	/**
	 * 从一个文件中读回文件的所有内容，以byte[]流形式返回
	 * @param name 文件全名，包含路径
	 * @return 文件的内容，byte[]格式
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static Object read(String name) throws ClassNotFoundException, IOException {
		File file = new File(name);
		FileInputStream fis=new FileInputStream(file);
		byte[] read_buf = new byte[(int) file.length()];
		fis.read(read_buf); //读取所有数据
		fis.close();
		return read_buf;
	}


	/**
	 * Java文件操作 获取文件扩展名
	 * @param filename
	 * @return
	 */
	public static String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot >-1) && (dot < (filename.length() - 1))) {
				return filename.substring(dot + 1);
			}
		}
		return filename;
	}
	/**
	 * Java文件操作 获取不带扩展名的文件名
	 * @param filename
	 * @return
	 */
	public static String getFileNameNoEx(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot >-1) && (dot < (filename.length()))) {
				return filename.substring(0, dot);
			}
		}
		return filename;
	}

	/**
	 * 使用文件通道的方式复制文件
	 * @param s 源文件
	 * @param t 复制到的新文件
	 * @return
	 */
	public static boolean fileChannelCopy(String s, String t) {
		File fromFile = new File(s);
		if(fromFile.exists()) { //文件存在
			File saveFile = new File(t);
			saveFile.getParentFile().mkdirs();  //生成目录
			return fileChannelCopy(fromFile,saveFile);
		}
		return false;
	}

	/**
	 * 使用文件通道的方式复制文件
	 * @param s 源文件
	 * @param t 复制到的新文件
	 * @return
	 */
	public static boolean fileChannelCopy(File s, File t) {
		boolean sta = false;
		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			//	t.getParentFile().mkdirs(); //生成目录
			fi = new FileInputStream(s);
			fo = new FileOutputStream(t);
			in = fi.getChannel();//得到对应的文件通道
			out = fo.getChannel();//得到对应的文件通道
			in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
			sta = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fi.close();
				in.close();
				fo.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sta;
	}

	// 递归方法
	public static void copyDir(File file, File file2) {
		// 当找到目录时，创建目录
		if (file.isDirectory()) {
			file2.mkdir();
			File[] files = file.listFiles();
			for (File file3 : files) {
				// 递归
				copyDir(file3, new File(file2, file3.getName()));
			}
			//当找到文件时
		} else if (file.isFile()) {
			File file3 = new File(file2.getAbsolutePath());
			try {
				file3.createNewFile();
				fileChannelCopy(file.getAbsolutePath(), file3.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 清空一个目录下的所有文件和文件夹
	 * @param dir 目录的绝对路径名
	 */
	public static void clearDir(String dir){
		File file = new File(dir);
		if(file.exists()&&file .isDirectory()){   //判断文件是否存在
			File files[] = file.listFiles();
			for(int i=0;i<files.length;i++){
				if(files[i].isFile()) files[i].delete();
				else deleteDir(files[i]);
			}
		}

	}
	/**
	 * 递归删除目录下的所有文件及子目录下所有文件,并删除目录
	 * @param dir 将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful.
	 *                 If a deletion fails, the method stops attempting to
	 *                 delete and returns "false".
	 */
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			//递归删除目录中的子目录下
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件,并删除目录
	 * @param dir 将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful.
	 *                 If a deletion fails, the method stops attempting to
	 *                 delete and returns "false".
	 */
	public static boolean deleteDir(String dir) {
		return deleteDir(new File(dir));
	}
}
