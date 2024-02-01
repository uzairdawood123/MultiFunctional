package com.xsm.lib.com.protocol.pack;

import android.util.Base64;

import com.xsm.lib.com.protocol.Protobuf;
import com.xsm.lib.util.UserLog;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Decoder {
	private UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), false);
	private static UserLog DEMO_DEBUG = new UserLog("Decoder DEMO", false);

	public static class DataPoint {
		/**
		 * 有效数据在解码缓冲中的偏移
		 */
		public int Offset = 0;
		/**
		 * 有效数据的长度
		 */
		public int Len = 0;

		public DataPoint(int offset, int len) {
			Offset = offset;
			Len = len;
		}
	}

	public static class DecodeEntry {
		/**
		 * 数据的编码类型
		 */
		public int WireType = 0;
		/**
		 * 数据的tag标签号
		 */
		public int Tag = 0;
		/**
		 * 数据在解码缓冲中的位置及长度
		 */
		public DataPoint Data=null;

		public DecodeEntry(int wire_type, int tag, int offset, int len) {
			WireType = wire_type;
			Tag = tag;
			Data = new DataPoint(offset, len);
		}
	}
	//
//    /**
//     * 命令表管理
//     */
//	public static CmdManage cmdManage = new CmdManage();
//
//	/*****************************************************运行解码器************************************************************************/
//	public static List<Byte> run(Context context, byte[] buf, int offset, int len) {
//		List<Byte> ack = null;
//		int cmd = Protobuf.CMD_UNKNOWN;
//		Decoder decoder = new Decoder(buf, offset, len);
//		cmd = decoder.decode_control(Protobuf.TAG_CMD, Protobuf.CMD_UNKNOWN);
//		//DEBUG.log("cmd="+cmd);
//		if(cmd != Protobuf.CMD_UNKNOWN) { //命令有效，解码所有控制域
//    		int pid = decoder.decode_control(Protobuf.TAG_PID, 0);
//    		boolean is_save = decoder.decode_control(Protobuf.TAG_ACT_SAVE, false);
//    		DEBUG.log("cmd="+cmd+"; pid="+pid+"; is_save="+is_save+"; status="+decoder.decode_control(Protobuf.TAG_STATUS, Protobuf.noError));
//    		//decode_test(decoder);
//    		List<Byte> bytes = cmdManage.dispose(context, cmd, pid, decoder, is_save);
//    		if(!decoder.decode_control(Protobuf.TAG_NO_RESPONSE, false)) { //如果需要回复
//    			if(bytes==null) {
//    				ack = new Encoder(Protobuf.CMD_RESPONSE, pid, Protobuf.noSuchName).pack(); //不支持的命令
//    				DEBUG.log("CMD is noSuchName!");
//    			}
//    			else {
//    				if(bytes.size()>0) ack = bytes;
//    			}
//    		}
//    		if(is_save) { //是否激活保存参数
//    			DEBUG.log("save paramater...");
//    		}
//		}
//    	return ack;
//
//	}
//
	public void decode_test(Decoder decoder) {
		decoder.data_init(); //解码数据域
		DEBUG.log("decoder tag 1",decoder.decode_data(1, Protobuf.TYPE_BOOL, false));
		DEBUG.log("decoder tag 2",decoder.decode_data(2, Protobuf.TYPE_INT32, 0));
		DEBUG.log("decoder tag 3",decoder.decode_data(3, Protobuf.TYPE_SINT32, 0));
		DEBUG.log("decoder tag 4",""+decoder.decode_data(4, Protobuf.TYPE_SINT64, 0L));
		DEBUG.log("decoder tag 5",""+decoder.decode_data(5, Protobuf.TYPE_DOUBLE, 0d));
		DEBUG.log("decoder tag 6",""+decoder.decode_data(6, Protobuf.TYPE_FIXED64, 0L));
		DEBUG.log("decoder tag 7",""+decoder.decode_data(7, Protobuf.TYPE_SFIXED64, 0L));
		DEBUG.log("decoder tag 8",""+decoder.decode_data(8, Protobuf.TYPE_FLOAT, 0f));
		DEBUG.log("decoder tag 9",decoder.decode_data(9, Protobuf.TYPE_FIXED32, 0));
		DEBUG.log("decoder tag 10",decoder.decode_data(10, Protobuf.TYPE_SFIXED32, 0));
		DEBUG.log("decoder tag 11",decoder.decode_data(11, Protobuf.TYPE_STRING, ""));
		DEBUG.log("decoder tag 12",decoder.decode_data(12, Protobuf.TYPE_BYTES, new byte[0]));
	}
	/*****************************************************缓冲解码器************************************************************************/
	public byte[] DecodeBuf = new byte[0];
	public List<DecodeEntry> controlList = new ArrayList<DecodeEntry>();
	public List<DecodeEntry> dataList = new ArrayList<DecodeEntry>();

//	/**
//	 * 初始化缓解码数据缓冲，并解码控制域
//	 * 注意:数据缓冲并没有复制，所以传入的缓冲不可更改，否则会产生数据出错
//	 * @param buf 接收到的数据缓冲，
//	 * @param offset 缓冲中有效的数据的偏移
//	 * @param len 有数据数据长度
//	 */
//	public Decoder(byte[] buf, int offset, int len) {
//		DecodeBuf = buf;
//		controlList = decode(DecodeBuf, offset, len);
//	}

	/**
	 * 初始化缓解码数据缓冲，并解码控制域
	 * 注意:数据缓冲并没有复制，所以传入的缓冲不可更改，否则会产生数据出错
	 * @param buf 接收到的数据缓冲，
	 * @param offset 缓冲中有效的数据的偏移
	 * @param len 有数据数据长度
	 */
	public static Decoder parseBuffer(byte[] buf, int offset, int len) {
		Decoder decoder = new Decoder();
		decoder.DecodeBuf = buf;
		decoder.controlList = Decoder.decode(buf, offset, len);
//		for(int i=0; i<decoder.controlList.size(); i++) {
//			DEMO_DEBUG.log("parseBuffer controlList "+i+"; tag="+decoder.controlList.get(i).Tag+
//					"; type="+decoder.controlList.get(i).WireType+
//					"; offset="+decoder.controlList.get(i).Data.Offset+
//					"; len="+decoder.controlList.get(i).Data.Len);
//		}
		if(decoder.controlList.size()>0) return decoder;
		else return null;
	}

	/**
	 * 初始化数据列表，即解码数据域到数据列表
	 * @return
	 */
	public boolean data_init() {
		DecodeEntry entry = find_entry(controlList, Protobuf.TAG_DATA, Protobuf.TYPE_BYTES);
		if(entry != null) {
			DEMO_DEBUG.log("data_init entry tag="+entry.Tag+"; type="+entry.WireType+"; offset="+entry.Data.Offset+"; len="+entry.Data.Len);
			dataList = decode(DecodeBuf, entry.Data.Offset, entry.Data.Len);
			DEMO_DEBUG.log("data_init dataList size="+dataList.size());
			return true;
		}
		else {
			dataList = new ArrayList<DecodeEntry>();
			return false;
		}
	}

	/**
	 * 取得控制域的一个数据项的值
	 * @param tag 数据标签
	 * @param def 未找到时返回的默认值
	 * @return
	 */
	public <T> T decode_control(int tag, T def) {
		return decode_control(tag, Protobuf.getTagType(tag), def);
	}

	/**
	 * 取得控制域的一个数据项的值
	 * @param tag 数据标签
	 * @param type 数据类型
	 * @param def 未找到时返回的默认值
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T decode_control(int tag, int type, T def) {
		try {
			return (T)decode_entry(controlList, tag,type,def);
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) { //索引出界
			e.printStackTrace();
		} catch (ClassCastException e) { //类型转换出错
			e.printStackTrace();
		}
		return def;
	}

	public boolean has_control(int tag) {
		return has_control(tag, Protobuf.getTagType(tag));
	}

	public boolean has_control(int tag, int type) {
		return (find_entry(controlList, tag, type)!=null);
	}

	/**
	 * 取得数据域的一个数据项的值
	 * @param tag 数据标签
	 * @param type 数据类型
	 * @param def 未找到时返回的默认值
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T decode_data(int tag, int type, T def) {
		try {
			return (T)decode_entry(dataList, tag,type,def);
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) { //索引出界
			e.printStackTrace();
		} catch (ClassCastException e) { //类型转换出错
			e.printStackTrace();
		}
		return def;
	}

	public boolean has_data(int tag, int type) {
		return (find_entry(dataList, tag, type)!=null);
	}

	/**
	 * 解码一个域
	 * @param list 待解码的数据标签查找源
	 * @param tag 要解码的数据标签号
	 * @return 如果解码失败，则返回null,否则返回long或byte[]
	 */
	protected Object decode_entry(List<DecodeEntry> list, int tag) {
		for(int i=0; i<list.size(); i++) {
			DecodeEntry entry = list.get(i);
			//	DEBUG.log("decode_entry by "+i+" tag="+entry.Tag+":"+tag+"; WireType="+entry.WireType+"; offset="+entry.Data.Offset+"; Len="+entry.Data.Len);
			if(entry.Tag==tag) {
				switch(entry.WireType) {
					case Protobuf.WIRE_VARINT:
						return decode_varint64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
					case Protobuf.WIRE_FIXED64:
						return decode_fixed64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
					case Protobuf.WIRE_LENGTH:
						return decode_bytes(DecodeBuf, entry.Data.Offset, entry.Data.Len);
					case Protobuf.WIRE_GROUP_START:
						return null;
					case Protobuf.WIRE_GROUP_END:
						return null;
					case Protobuf.WIRE_FIXED32:
						return decode_fixed32(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				}
				return null;
			}
		}
		return null;
	}

	protected Object decode_entry(List<DecodeEntry> list, int tag, int type) {
		DecodeEntry entry = find_entry(list, tag, type);
		if(entry != null) {
			switch(type) {
				case Protobuf.TYPE_ENUM:
				case Protobuf.TYPE_INT32:
					return decode_varint32(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_UINT32:
				case Protobuf.TYPE_INT64:
				case Protobuf.TYPE_UINT64:
					return decode_varint64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_SINT32:
					return decode_zigzag32(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_SINT64:
					return decode_zigzag64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_DOUBLE:
					return decode_double(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_FIXED64:
					return decode_fixed64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_SFIXED64:
					return decode_sFixed64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_FLOAT:
					return decode_float(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_FIXED32:
					return decode_fixed32(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_SFIXED32:
					return decode_sFixed32(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_BOOL:
					return decode_bool(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_STRING:
					return decode_string(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_MESSAGE:
				case Protobuf.TYPE_BYTES:
					return decode_bytes(DecodeBuf, entry.Data.Offset, entry.Data.Len);
			}
		}
		return null;
	}

	/**
	 * 解码一个用于保存到SQL的数据域,使用编码的类型
	 * @param buf 源始数据缓冲
	 * @param entry
	 * @return 数据域的值
	 */
	public static String decodeSqlData(byte[] buf, DecodeEntry entry) {
		try {
			switch(entry.WireType) {
				case Protobuf.WIRE_VARINT:
					return decode_varint64(buf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.WIRE_FIXED64:
					return decode_fixed64(buf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.WIRE_LENGTH:
					return Base64.encodeToString(decode_bytes(buf, entry.Data.Offset, entry.Data.Len), Base64.DEFAULT);
				case Protobuf.WIRE_GROUP_START:
					return null;
				case Protobuf.WIRE_GROUP_END:
					return null;
				case Protobuf.WIRE_FIXED32:
					return decode_fixed32(buf, entry.Data.Offset, entry.Data.Len)+"";
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) { //索引出界
			e.printStackTrace();
		} catch (ClassCastException e) { //类型转换出错
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解码一个用于保存到SQL的数据域,使用编码的类型
	 * @param entry
	 * @return 数据域的值
	 */
	public String decodeSqlData(DecodeEntry entry) {
		return decodeSqlData(DecodeBuf, entry);
	}

	/**
	 * 解码一个用于保存到SQL的数据域
	 * @param entry
	 * @param type
	 * @return 数据域的值
	 */
	public String decodeSqlData(DecodeEntry entry, int type) {
		try {
			switch(type) {
				case Protobuf.TYPE_ENUM:
				case Protobuf.TYPE_INT32:
					return decode_varint32(DecodeBuf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.TYPE_UINT32:
				case Protobuf.TYPE_INT64:
				case Protobuf.TYPE_UINT64:
					return decode_varint64(DecodeBuf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.TYPE_SINT32:
					return decode_zigzag32(DecodeBuf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.TYPE_SINT64:
					return decode_zigzag64(DecodeBuf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.TYPE_DOUBLE:
					return decode_double(DecodeBuf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.TYPE_FIXED64:
					return decode_fixed64(DecodeBuf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.TYPE_SFIXED64:
					return decode_sFixed64(DecodeBuf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.TYPE_FLOAT:
					return decode_float(DecodeBuf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.TYPE_FIXED32:
					return decode_fixed32(DecodeBuf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.TYPE_SFIXED32:
					return decode_sFixed32(DecodeBuf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.TYPE_BOOL:
					return decode_bool(DecodeBuf, entry.Data.Offset, entry.Data.Len)+"";
				case Protobuf.TYPE_STRING:
					return decode_string(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_MESSAGE:
				case Protobuf.TYPE_BYTES:
					return Base64.encodeToString(decode_bytes(DecodeBuf, entry.Data.Offset, entry.Data.Len), Base64.DEFAULT);
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) { //索引出界
			e.printStackTrace();
		} catch (ClassCastException e) { //类型转换出错
			e.printStackTrace();
		}
		return null;
	}

	private <T> Object decode_entry(List<DecodeEntry> list, int tag, int type, T def) {
		DecodeEntry entry = find_entry(list, tag, type);
		if(entry != null) {
			switch(type) {
				case Protobuf.TYPE_ENUM:
				case Protobuf.TYPE_INT32:
					return (int)decode_varint32(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_UINT32:
				case Protobuf.TYPE_INT64:
				case Protobuf.TYPE_UINT64:
					if(def.getClass().equals(Integer.class)) return (int)decode_varint64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
					else return (long)decode_varint64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_SINT32:
					if(def.getClass().equals(Integer.class)) return (int)decode_zigzag32(DecodeBuf, entry.Data.Offset, entry.Data.Len);
					else return (long)decode_zigzag32(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_SINT64:
					if(def.getClass().equals(Integer.class)) return (int)decode_zigzag64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
					else return (long)decode_zigzag64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_DOUBLE:
					return (double)decode_double(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_FIXED64:
					if(def.getClass().equals(Integer.class)) return (int)decode_fixed64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
					else return (long)decode_fixed64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_SFIXED64:
					if(def.getClass().equals(Integer.class)) return (int)decode_sFixed64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
					else return (long)decode_sFixed64(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_FLOAT:
					return (float)decode_float(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_FIXED32:
					if(def.getClass().equals(Integer.class)) return (int)decode_fixed32(DecodeBuf, entry.Data.Offset, entry.Data.Len);
					else return (long)decode_fixed32(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_SFIXED32:
					if(def.getClass().equals(Integer.class)) return (int)decode_sFixed32(DecodeBuf, entry.Data.Offset, entry.Data.Len);
					else return (long)decode_sFixed32(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_BOOL:
					return decode_bool(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_STRING:
					return decode_string(DecodeBuf, entry.Data.Offset, entry.Data.Len);
				case Protobuf.TYPE_MESSAGE:
				case Protobuf.TYPE_BYTES:
					return decode_bytes(DecodeBuf, entry.Data.Offset, entry.Data.Len);
			}
		}
		return def;
	}

	/*****************************************************数据解码器************************************************************************/

	/**
	 * 某个列表中是否存在某个项
	 * @param list
	 * @param tag
	 * @param type
	 * @return
	 */
	public static boolean has(List<DecodeEntry> list, int tag, int type) {
		return (find_entry(list, tag, type)!=null);
	}
	/**
	 * 解码得到数据列表
	 * @param buf 数据缓冲
	 * @param offset 有效数据的偏移
	 * @param len
	 * @return
	 */
	public static List<DecodeEntry> decode(byte[] buf, int offset, int len) {
		List<DecodeEntry> list = new ArrayList<DecodeEntry>();
		int varint_len = 0, varint = 0, wire=Protobuf.WIRE_UNKNOWN, tag=0, d_len=0;
		int old_offset = offset;
		DEMO_DEBUG.log("decode buffer",buf,offset,len);
		while((offset-old_offset)<len) {
			varint_len = get_varint_len(buf, offset, len); //数据的编码长度
			varint = decode_varint32(buf, offset, len);
			wire = varint & 0x7;
			tag = varint >> 3;
			DEMO_DEBUG.log("decode head "+offset+" wire ="+wire+"; tag="+tag+"; varint_len ="+varint_len);
			offset += varint_len; //跳过数据头
			//计算数据长度
			if(wire== Protobuf.WIRE_VARINT) d_len=get_varint_len(buf, offset, len);
			else if(wire==Protobuf.WIRE_FIXED32) d_len=4;
			else if(wire==Protobuf.WIRE_FIXED64) d_len=8;
			else if(wire==Protobuf.WIRE_LENGTH) {
				d_len = decode_varint32(buf, offset, len);
				offset += get_varint_len(buf, offset, len); //跳过数据头
			}
			else d_len=-1;
			DEMO_DEBUG.log("decode data at "+(offset-old_offset)+" d_len ="+d_len+"; len="+len+":"+offset+"/"+buf.length);
			if(d_len>=0 && ((offset-old_offset)+d_len)<=len) list.add(new DecodeEntry(wire, tag, offset, d_len)); //数据有效
			else break; //出现无效数据，结束解码
			offset += d_len; //跳过当前数据
		}
		DEMO_DEBUG.log("decode data control size "+list.size()+" buf offset "+old_offset+"->"+offset+" len:"+len);
		if((offset-old_offset)!=len) list.clear(); //解码失败，则清空数据
		return list;
	}

	/**
	 * 查找有效的数据标签
	 * @param src 查找源
	 * @param tag 要查找的标签号
	 * @param type 要查到的数据类型
	 * @return 如果查到失败则返回null;
	 */
	public static DecodeEntry find_entry(List<DecodeEntry> src, int tag, int type) {
		int wire = Protobuf.getWireType(type);
		for(int i=0; i<src.size(); i++) {
			DecodeEntry item = src.get(i);
			//	DEBUG.log("find_entry by "+i+" tag="+item.Tag+":"+tag+"; WireType="+item.WireType+":"+wire+"; offset="+item.Data.Offset+"; Len="+item.Data.Len,);
			if(item.Tag==tag && item.WireType==wire) {
				return item;
			}
		}
		return null;
	}

	/**
	 * 取得编码的长度
	 * @return
	 */
	public static int get_varint_len(List<Byte> buf, int offset, int len) {
		int result=0;
		int b = 0;
		do {
			result++;
			if(offset>=buf.size()) break;
			b = buf.get(offset++)&0xff;
			//DEBUG.log("get_varint_len "+(offset-1)+" value="+b+"; result="+result+"; len="+len);
		} while ((b & 0x80)!=0 && result<len && result<10);
		return result;
	}
	/**
	 * 解码一个可变长整数
	 * @return
	 */
	public static int decode_varint32(List<Byte> buf, int offset, int len) {
		int result=0;
		int b = 0,shift=0;
		do {
			if(offset>=buf.size()) break;
			b = buf.get(offset++)&0xff;
			result |= ((long)(b & 0x7f)) << shift;
			shift += 7;
		} while (b > 0x7f);
		return result;
	}

	/**
	 * 取得编码的长度
	 * @return
	 */
	public static int get_varint_len(byte[] buf, int offset, int len) {
		int result=0;
		int b = 0;
		do {
			result++;
			if(offset>=buf.length) break;
			b = buf[offset++]&0xff;
			//DEBUG.log("get_varint_len "+(offset-1)+" value="+b+"; result="+result+"; len="+len);
		} while ((b & 0x80)!=0 && result<len && result<10);
		return result;
	}

	/**
	 * 解码一个可变长整数
	 * @return
	 */
	public static int decode_varint32(byte[] buf, int offset, int len) {
		int result=0;
		int b = 0,shift=0;
		do {
			if(offset>=buf.length) break;
			b = buf[offset++]&0xff;
			result |= ((long)(b & 0x7f)) << shift;
			shift += 7;
		} while (b > 0x7f);
		return result;
	}

	/**
	 * 解码一个可变长整数
	 * @return
	 */
	public static long decode_varint64(byte[] buf, int offset, int len) {
		long result=0;
		int b = 0,shift=0;
		do {
			if(offset>=buf.length) break;
			b = buf[offset++]&0xff;
			result |= ((long)(b & 0x7f)) << shift;
			shift += 7;
		} while (b > 0x7f);
		return result;
	}

	public static boolean decode_bool(byte[] buf, int offset, int len) {
		return decode_varint32(buf, offset, len)>0;
	}

	/**
	 * Decodes a zigzag integer of the given bits
	 * @return
	 */
	public static int decode_zigzag32(byte[] buf, int offset, int len) {
		int number = decode_varint32(buf, offset, len);
		return ((number >> 1)&0x7fffffff) ^ (-(number & 1));
	}

	/**
	 * Decodes a zigzag integer of the given bits
	 * @return
	 */
	public static long decode_zigzag64(byte[] buf, int offset, int len) {
		long number = decode_varint64(buf, offset, len);
		return ((number >> 1)&0x7fffffffffffffffL) ^ (-(number & 1));
	}

	/**
	 * Decode a fixed 32bit integer with sign
	 * @return
	 */
	public static int decode_sFixed32(byte[] buf, int offset, int len) {
		int result = 0;
		if(offset+4>=buf.length) return 0;
		for(int shift=0; shift<32; shift += 8) {
			result |= (((long)(buf[offset++]&0xff))<<shift);
		}
		return result;
	}

	/**
	 * Decode a fixed 32bit integer without sign
	 * @return
	 */
	public static long decode_fixed32(byte[] buf, int offset, int len) {
		long result = 0;
		if(offset+4>=buf.length) return 0;
		for(int shift=0; shift<32; shift += 8) {
			result |= (((int)(buf[offset++]&0xff))<<shift);
		}
		return result;
	}

	/**
	 * Decode a fixed 62bit integer with sign
	 * @return
	 */
	public static long decode_sFixed64(byte[] buf, int offset, int len) {
		long result = 0;
		if(offset+8>=buf.length) return 0;
		for(int shift=0; shift<64; shift += 8) {
			result |= (((long)(buf[offset++]&0xff))<<shift);
		}
		return result;
	}

	/**
	 * Decode a fixed 62bit integer without sign
	 * @return
	 */
	public static long decode_fixed64(byte[] buf, int offset, int len) {
		return decode_sFixed64(buf, offset, len);
	}

	/**
	 * Decode a 32bit float
	 * @return
	 */
	public static float decode_float(byte[] buf, int offset, int len) {
		int result = 0;
		if(offset+4>=buf.length) return 0;
		for(int shift=0; shift<32; shift += 8) {
			result |= (((int)(buf[offset++]&0xff))<<shift);
		}
		return Float.intBitsToFloat(result);
	}

	/**
	 * Decode a 64bit double
	 * @return
	 */
	public static double decode_double(byte[] buf, int offset, int len) {
		long result = 0;
		if(offset+8>=buf.length) return 0;
		for(int shift=0; shift<64; shift += 8) {
			result |= (((long)(buf[offset++]&0xff))<<shift);
		}
		//DEBUG.log("decode_double long"+String.format("%#8x ", result));
		return Double.longBitsToDouble(result);
	}

	public static byte[] decode_bytes(byte[] buf, int offset, int len) {
		if(len > buf.length-offset) len = buf.length-offset;
		byte[] bytes = new byte[len];
		for(int i=0; i<bytes.length; i++) bytes[i]=buf[i+offset];
		return bytes;
	}

	public static String decode_string(byte[] buf, int offset, int len) {
		String val = "";
		try {
			val = new String(decode_bytes(buf, offset, len), Protobuf.defCharsetName);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return val;
	}
}
