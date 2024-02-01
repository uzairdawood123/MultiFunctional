package com.xsm.lib.util.json;

import com.xsm.lib.util.UserLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现Json数据与对像的交互
 * 支持的基本类型如下:
 * Boolean Byte,Integer,Long,Float,Double
 * 支持的Array类型如下：
 * Byte[],Integer[],Long[],Float[],Double[],JsonAbstract[]及其子类
 * 支持的List类型如下：
 * List"Byte",List"Integer",List"Long",List"Float",List"Double"
 * 支持的Class类型如下：
 * JsonAbstract及其子类
 * @author daniel
 * 注意:这里只对private的值进行编解码
 */
public abstract class JsonAbstract {
	protected UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), false);

	/**
	 * @param debug 是否启用调试信息
	 */
	public JsonAbstract(boolean debug) {
		DEBUG.setDebugKey(debug);
	}

	/**
	 * 必需存在这个空的构造函数，不然parse构建对像会出错
	 */
	public JsonAbstract() {
	}

	/**
	 * 用一个json数据串初始化对像
	 * 注:这里只不处理final,static,public这些域
	 * @param jsonObject 用于初始化对像的json数据串
	 * @return 是否初始化成功
	 */
	@SuppressWarnings({ "rawtypes", "unchecked"})
	public boolean parse(JSONObject jsonObject) {
		if(jsonObject==null) return false;
		Field[] fields = this.getClass().getDeclaredFields(); //只返回本类中的属性，不会返回父类的。公有私有全返回
		for(Field field: fields) {
			try {
				field.setAccessible(true); //当类中的成员变量为private,则必须进行此操作
				int mod = field.getModifiers();
				String name = field.getName();
				DEBUG.log("field: "+ Modifier.toString(mod)+" "+field.getType().getSimpleName()+" "+name);
				if(!jsonObject.has(name)) {
					DEBUG.log("no find json by field "+name);
					continue; //不存在这个域则不处理
				}
				if(Modifier.isFinal(mod) || Modifier.isStatic(mod) || Modifier.isPublic(mod) || Modifier.isProtected(mod)) continue; //不处理静态域和固定参数域
				Class<?> field_class = field.getType();
				if(JsonAbstract.class.isAssignableFrom(field_class)) {
					DEBUG.log("parse->"+name+" is a JsonAbstract");
					JsonAbstract obj = (JsonAbstract) field_class.newInstance();
					if(!jsonObject.isNull(name)) {
						JSONObject j_obj = jsonObject.getJSONObject(name);
						obj.parse(j_obj);
					}
					else obj = null;
					field.set(this, obj);
				}
				else if(List.class.isAssignableFrom(field_class)) {
					JSONArray jary = jsonObject.getJSONArray(name);
					int len = jary.length();
					DEBUG.log("parse->"+name+" is a List len="+len);
					List list = new ArrayList();
					for(int i=0; i<len; i++) {
						list.add(jary.get(i));
					}
					field.set(this, list);
				}
				else if(byte.class.isAssignableFrom(field_class)) { //必需独立处理这个类，否则会报错
					DEBUG.log("parse->"+name+" is a Byte");
					field.set(this, (byte)jsonObject.getInt(name));
				}
				else if(float.class.isAssignableFrom(field_class)) { //必需独立处理这个类，否则会报错
					DEBUG.log("parse->"+name+" is a Float");
					field.set(this, (float)jsonObject.getDouble(name));
				}
				//限定所有有效的类型
				else if((int.class.isAssignableFrom(field_class))||
						(boolean.class.isAssignableFrom(field_class))||
						(long.class.isAssignableFrom(field_class))||
						(double.class.isAssignableFrom(field_class))||
						(String.class.isAssignableFrom(field_class))){
					DEBUG.log("parse->"+name+" is a other");
					field.set(this, jsonObject.get(name));
				}
				else if(field_class.isArray()) {
					JSONArray jary = jsonObject.getJSONArray(name);
					int len = jary.length();
					DEBUG.log("parse->"+name+" is a Array len="+len);
					Class<?> entry_class = field_class.getComponentType();
					Object arr = Array.newInstance(entry_class, len);
					for(int i=0; i<len; i++) {
						if(JsonAbstract.class.isAssignableFrom(entry_class)) { //type是否是Json的子类
							DEBUG.log("parse arrar "+i+"->"+name+" is a JsonAbstract");
							JsonAbstract obj = (JsonAbstract) entry_class.newInstance();
							if(!jary.isNull(i)) {
								JSONObject j_obj = jary.getJSONObject(i);
								obj.parse(j_obj);
							}
							else obj = null;
							Array.set(arr, i, obj);
						}
						else if(byte.class.isAssignableFrom(entry_class)) { //必需独立处理这个类，否则会报错
							DEBUG.log("parse arra r"+i+"->"+name+" is a Byte");
							Array.set(arr, i, (byte)jary.getInt(i));
						}
						else if(float.class.isAssignableFrom(entry_class)) { //必需独立处理这个类，否则会报错
							DEBUG.log("parse arrar "+i+"->"+name+" is a Float");
							Array.set(arr, i, (float)jary.getDouble(i));
						}
						//限定所有有效的类型
						else if((int.class.isAssignableFrom(entry_class))||
								(boolean.class.isAssignableFrom(entry_class))||
								(long.class.isAssignableFrom(entry_class))||
								(double.class.isAssignableFrom(entry_class))||
								(String.class.isAssignableFrom(entry_class))){
							DEBUG.log("parse arrar "+i+"->"+name+" is a other");
							Array.set(arr, i, jary.get(i));
						}
					}
					field.set(this, arr);
				}
			} catch (IllegalArgumentException | IllegalAccessException | ClassCastException | JSONException | InstantiationException e) {
				e.printStackTrace(); return false;
			} // TODO Auto-generated catch block
		}
		return true;
	}

	/**
	 * 将对像转换为json数据串
	 * 注:这里只不处理final,static,public这些域
	 * @return json数据
	 */
	@SuppressWarnings("rawtypes")
	public JSONObject toJson() {
		Field[] fields = this.getClass().getDeclaredFields(); //只返回本类中的属性，不会返回父类的。公有私有全返回
		JSONObject json = new JSONObject();
		for(Field field: fields){
			try {
				field.setAccessible(true); //当类中的成员变量为private,则必须进行此操作
				int mod = field.getModifiers();
				String name = field.getName();
				DEBUG.log("field: "+ Modifier.toString(mod)+" "+field.getType().getSimpleName()+" "+name);
				if(Modifier.isFinal(mod) || Modifier.isStatic(mod) || Modifier.isPublic(mod) || Modifier.isProtected(mod)) continue; //不处理静态域和固定参数域
				Class<?> field_class = field.getType();
				Object field_val = field.get(this);
				if(field_val==null) {
					DEBUG.log("toJson->"+name+" is null");
					json.put(name, null);
				}
				else if(JsonAbstract.class.isAssignableFrom(field_class)) {
					DEBUG.log("toJson->"+name+" is a JsonAbstract");
					json.put(name, ((JsonAbstract)field_val).toJson());
				}
				else if(field_class.isArray()) {
					int len = Array.getLength(field_val);
					DEBUG.log("toJson->"+name+" is a Array len="+len);
					JSONArray jary = new JSONArray();
					for(int i=0; i<len; i++) {
						Object entry = Array.get(field_val, i);
						if(entry == null) {
							jary.put(null);
						}
						else if(entry instanceof JsonAbstract) {
							DEBUG.log("toJson array->"+name+" is a JsonAbstract");
							jary.put(((JsonAbstract)entry).toJson());
						}
						else if((entry instanceof Byte)||
								(entry instanceof Boolean)||
								(entry instanceof Integer)||
								(entry instanceof Long)||
								(entry instanceof Float)||
								(entry instanceof Double)||
								(entry instanceof String)){
							DEBUG.log("toJson array->"+name+" is a other");
							jary.put(entry);
						}
					}
					json.put(name, jary);
				} else if(field_val instanceof List) {
					List list = (List)field_val;
					int len = ((List)field_val).size();
					DEBUG.log("toJson->"+name+" is a List len="+len);
					JSONArray jary = new JSONArray();
					for(int i=0; i<len; i++) {
						jary.put(list.get(i));
					}
					json.put(name, jary);
				}
				//限定所有有效的类型
				else if((field_val instanceof Byte)||
						(field_val instanceof Boolean)||
						(field_val instanceof Integer)||
						(field_val instanceof Long)||
						(field_val instanceof Float)||
						(field_val instanceof Double)||
						(field_val instanceof String)){
					DEBUG.log("toJson->"+name+" is a other");
					json.put(name, field_val);
				}
			} catch (IllegalArgumentException | IllegalAccessException | ClassCastException | JSONException e) {
				e.printStackTrace();
			}
		}
		return json;
	}

	/**
	 * 转换为字符串
	 * @return json字符串
	 */
	public String toString() {
		JSONObject json = this.toJson();
		if(json!=null) return json.toString();
		else return null;
	}

	/**
	 * 保存对像到文件
	 * @param path 文件路径(包含文件全名)
	 * @param charsetName 文件的字符集名称,默认为"UTF-8"
	 * @return 是否成功
	 */
	public boolean saveFile(String path, String charsetName) {
		try {
			if(charsetName==null) charsetName = "UTF-8";
			String json_srt = this.toJson().toString();
            saveFile(path, json_srt.getBytes(charsetName));
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 从指定的文件初始化对像
	 * @param path 文件路径(包含文件全名)
	 * @param charsetName 文件的字符集名称,默认为"UTF-8"
	 * @return 是否成功
	 */
	public boolean parseFile(String path, String charsetName) {
		File file = new File(path);
		if(file.exists()) {
			try {
				if(charsetName==null) charsetName = "UTF-8";
				DEBUG.log("parseFile \""+path+"\" is exists, charsetName = "+charsetName);
				byte[] buffer = (byte[])readFile(file.getAbsolutePath());
				String json = new String(buffer, charsetName);
				return this.parse(new JSONObject(json));
			} catch (IOException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else DEBUG.log("parseFile \""+path+"\" is no exists!");
		return false;
	}

    /**
     * 将一个byte[]流保存到指定的文件中
     * @param name 文件全名，包含路径
     * @param obj 要保存的数据
     * @throws IOException
     */
    public static void saveFile(String name, byte[] obj) throws IOException {
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
     * @throws IOException
     */
    public static Object readFile(String name) throws IOException {
        File file = new File(name);
        FileInputStream fis=new FileInputStream(file);
        byte[] read_buf = new byte[(int) file.length()];
        fis.read(read_buf); //读取所有数据
        fis.close();
        return read_buf;
    }

	/**
	 * 从文件初始化对像
	 * @param fileName 文件名
	 * @param obj 需要初始化的对像
	 * @param <T> 对像类型
	 * @return 初始化后的对像,如果失败返回Null
	 */
	public static <T extends JsonAbstract> T readFile(String fileName, T obj) {
		if(obj != null && obj.parseFile(fileName, null)) return obj;
		else return null;
	}
}
