package com.xsm.lib.com.protocol.pack;

import android.util.Base64;

import com.xsm.lib.com.protocol.Protobuf;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Encoder {
	//private UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), false);

	public static int SendPid = 0;

	/*****************************************************缓冲编码器************************************************************************/
	private List<Byte> controlList = new ArrayList<Byte>();
	private List<Byte> dataList = new ArrayList<Byte>();
	private Integer status = null;
	public int cmd = Protobuf.CMD_UNKNOWN;
	public int pid = -1;

	/**
	 * @param cmd 命令
	 */
	public Encoder(int cmd) {
		this(cmd, SendPid++, null);
	}

	/**
	 * @param cmd 命令
	 * @param pid 命令ID
	 */
	public Encoder(int cmd, int pid) {
		this(cmd, pid, null);
	}

	/**
	 * @param cmd 命令
	 * @param pid 命令ID
	 * @param status 状态
	 */
	public Encoder(int cmd, int pid, Integer status) {
		this.cmd = cmd;
		this.pid = pid;
		this.status = status;
		addControl(Protobuf.TAG_CMD, cmd);
		addControl(Protobuf.TAG_PID, pid);
		init(cmd, pid, status);
	}

	protected void init(int cmd, int pid, Integer status) {
	}

	/**
	 * 设定应答状态码
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return this.status;
	}
	/**
	 * 在控制域添加一个数据对像
	 * @param tag 数据标签
	 * @param val 要添加的值
	 */
	public void addControl(int tag, Object val) {
		int type = Protobuf.getTagType(tag);
		encode_entry(controlList, tag, type, val);
	}

	/**
	 * 在数据域添加一个数据对像
	 * @param tag 数据标签
	 * @param type 数据类型
	 * @param val 要添加的值
	 */
	public void addData(int tag, int type, Object val) {
		encode_entry(dataList, tag, type, val);
	}

	/**
	 * 编码一个从SQL里取回的数据域，使用编码的类型
	 * @param entry
	 */
	public void encodeSqlData(Decoder.DecodeEntry entry, String val) {
		try {
			switch(entry.WireType) {
				case Protobuf.WIRE_VARINT:
					encode_entry(dataList, entry.Tag, entry.WireType, Long.valueOf(val.length()==0?"0":val));
					break;
				case Protobuf.WIRE_FIXED64:
					encode_entry(dataList, entry.Tag, entry.WireType, Long.valueOf(val.length()==0?"0":val));
					break;
				case Protobuf.WIRE_LENGTH:
					encode_entry(dataList, entry.Tag, entry.WireType, Base64.decode(val, Base64.DEFAULT));
				case Protobuf.WIRE_GROUP_START:
				case Protobuf.WIRE_GROUP_END:
					break;
				case Protobuf.WIRE_FIXED32:
					encode_entry(dataList, entry.Tag, entry.WireType, Long.valueOf(val.length()==0?"0":val));
					break;
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
	}
	/**
	 * 编码一个从SQL里取回的数据域
	 * @param tag
	 * @param type
	 * @param val
	 */
	public void encodeSqlData(int tag, int type, String val) {
		try {
			switch(type) {
				case Protobuf.TYPE_ENUM:
				case Protobuf.TYPE_INT32:
					encode_entry(dataList, tag, type, Integer.valueOf(val.length()==0?"0":val));
					break;
				case Protobuf.TYPE_UINT32:
				case Protobuf.TYPE_INT64:
				case Protobuf.TYPE_UINT64:
					encode_entry(dataList, tag, type, Long.valueOf(val.length()==0?"0":val));
					break;
				case Protobuf.TYPE_SINT32:
					encode_entry(dataList, tag, type, Integer.valueOf(val.length()==0?"0":val));
					break;
				case Protobuf.TYPE_SINT64:
					encode_entry(dataList, tag, type, Long.valueOf(val.length()==0?"0":val));
					break;
				case Protobuf.TYPE_DOUBLE:
					encode_entry(dataList, tag, type, Double.valueOf(val.length()==0?"0":val));
					break;
				case Protobuf.TYPE_FIXED64:
					encode_entry(dataList, tag, type, Long.valueOf(val.length()==0?"0":val));
					break;
				case Protobuf.TYPE_SFIXED64:
					encode_entry(dataList, tag, type, Long.valueOf(val.length()==0?"0":val));
					break;
				case Protobuf.TYPE_FLOAT:
					encode_entry(dataList, tag, type, Float.valueOf(val.length()==0?"0":val));
					break;
				case Protobuf.TYPE_FIXED32:
					encode_entry(dataList, tag, type, Long.valueOf(val.length()==0?"0":val));
					break;
				case Protobuf.TYPE_SFIXED32:
					encode_entry(dataList, tag, type, Integer.valueOf(val.length()==0?"0":val));
					break;
				case Protobuf.TYPE_BOOL:
					encode_entry(dataList, tag, type, Boolean.valueOf(val.length()==0?"false":val));
					break;
				case Protobuf.TYPE_STRING:
					encode_entry(dataList, tag, type, val);
					break;
				case Protobuf.TYPE_MESSAGE:
				case Protobuf.TYPE_BYTES:
					encode_entry(dataList, tag, type, Base64.decode(val, Base64.DEFAULT));
					break;
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
	}

	/**
	 * 打包数据
	 * @return
	 */
	public List<Byte> pack() {
		List<Byte> list = new ArrayList<Byte>();
		list.addAll(controlList);
		if(status!=null) {
			int type = Protobuf.getTagType(Protobuf.TAG_STATUS);
			encode_entry(list, Protobuf.TAG_STATUS, type, status);
		}
		if(dataList.size()>0) {
			encode_head(list, Protobuf.TAG_DATA, Protobuf.TYPE_BYTES);
			encode_list(list, dataList);
		}
		return  list;
	}

	protected void encode_entry(List<Byte> list, int tag, int type, Object val) {
		if(val==null) val="";
		encode_head(list, tag, type);
		long temp64;
		try {
			switch(type) {
				case Protobuf.TYPE_ENUM:
				case Protobuf.TYPE_INT32:
					encode_varint32(list,(Integer)val);
					break;
				case Protobuf.TYPE_UINT32:
				case Protobuf.TYPE_INT64:
				case Protobuf.TYPE_UINT64:
					temp64 = val.getClass().equals(Integer.class)? (Integer)val: (Long)val;
					encode_varint64(list,temp64);
					break;
				case Protobuf.TYPE_SINT32:
					encode_zigzag32(list,(Integer)val);
					break;
				case Protobuf.TYPE_SINT64:
					temp64 = val.getClass().equals(Integer.class)? (Integer)val: (Long)val;
					encode_zigzag64(list,temp64);
					break;
				case Protobuf.TYPE_DOUBLE:
					encode_double(list,(double)(val.getClass().equals(Double.class)? (Double)val: (Float)val));
					break;
				case Protobuf.TYPE_FIXED64:
					temp64 = val.getClass().equals(Integer.class)? (Integer)val: (Long)val;
					encode_fixed64(list,temp64);
					break;
				case Protobuf.TYPE_SFIXED64:
					temp64 = val.getClass().equals(Integer.class)? (Integer)val: (Long)val;
					encode_sFixed64(list,temp64);
					break;
				case Protobuf.TYPE_FLOAT:
					encode_float(list, (float)(val.getClass().equals(Double.class)? (Double)val: (Float)val));
					break;
				case Protobuf.TYPE_FIXED32:
					temp64 = val.getClass().equals(Integer.class)? (Integer)val: (Long)val;
					encode_fixed32(list,temp64);
					break;
				case Protobuf.TYPE_SFIXED32:
					encode_sFixed32(list,(Integer)val);
					break;
				case Protobuf.TYPE_BOOL:
					encode_bool(list,(Boolean)val);
					break;
				case Protobuf.TYPE_STRING:
					encode_string(list,val.toString());
					break;
				case Protobuf.TYPE_MESSAGE:
				case Protobuf.TYPE_BYTES:
					encode_bytes(list,(byte[])val);
					break;
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
	}

	/*****************************************************数据编码器************************************************************************/

	public static void encode_head(List<Byte> list, int tag, int type) {
		int wire = Protobuf.getWireType(type);
		encode_varint32(list, tag<<3 | (wire&0x07));
	}

	public static void encode_varint32(List<Byte> list, int value) {
		byte b=0;
		while (true) {
			b = (byte)(value & 0x7f);
			//DEBUG.log(list.size()+" value = "+String.format("%#8x ", value)+"; byte="+String.format("%#2x ", b));
			value = (value >> 7)&0x1ffffff ;
			if(value!=0) list.add((byte)(0x80|b));
			else {
				list.add(b);
				break;
			}
		}
	}

	public static void encode_varint64(List<Byte> list, long value) {
		byte b=0;
		while (true) {
			b = (byte)(value & 0x7f);
			value = (value >> 7)&0x1ffffffffffffffL ;
			if(value!=0) list.add((byte)(0x80|b));
			else {
				list.add(b);
				break;
			}
		}
	}

	public static void encode_bool(List<Byte> list, boolean val) {
		encode_varint32(list, val?1: 0);
	}

	/**
	 * Encodes an integer with zigzag
	 * @param list
	 * @param value
	 */
	public static void encode_zigzag32(List<Byte> list, int value) {
		value = (value << 1) ^ ((value >> 32-1)&0x01);
		encode_varint32(list, value);
	}

	public static void encode_zigzag64(List<Byte> list, long value) {
		value = (value << 1) ^ ((value >> 64-1)&0x01);
		encode_varint64(list, value);
	}

	/**
	 * Encode an integer as a fixed of 32bits with sign
	 * @param value
	 */
	public static void encode_sFixed32(List<Byte> list, int value) {
		for(int shift=0; shift<32; shift += 8) {
			list.add((byte)((value>>shift)&0xff));
		}
	}

	/**
	 * Encode an integer as a fixed of 32bits without sign
	 * @param value
	 */
	public static void encode_fixed32(List<Byte> list, long value) {
		for(int shift=0; shift<32; shift += 8) {
			list.add((byte)((value>>shift)&0xff));
		}
	}

	/**
	 * Encode an integer as a fixed of 64bits with sign
	 * @param value
	 */
	public static void encode_sFixed64(List<Byte> list, long value) {
		for(int shift=0; shift<64; shift += 8) {
			list.add((byte)((value>>shift)&0xff));
		}
	}

	/**
	 * Encode an integer as a fixed of 64bits without sign
	 * @param value
	 */
	public static void encode_fixed64(List<Byte> list, long value) {
		for(int shift=0; shift<64; shift += 8) {
			list.add((byte)((value>>shift)&0xff));
		}
	}

	/**
	 * Encode a number as a 32bit float
	 * @param value
	 */
	public static void encode_float(List<Byte> list, float value) {
		int int_val = Float.floatToIntBits(value);
		for(int shift=0; shift<32; shift += 8) {
			list.add((byte)((int_val>>shift)&0xff));
		}
	}

	/**
	 * Encode a number as a 64bit double
	 * @param value
	 */
	public static void encode_double(List<Byte> list, double value) {
		long int_val = Double.doubleToLongBits(value);
		for(int shift=0; shift<64; shift += 8) {
			list.add((byte)((int_val>>shift)&0xff));
		}
	}

	public static void encode_bytes(List<Byte> list, byte[] buf) {
		encode_varint32(list, buf.length);
		for(int i=0; i<buf.length; i++) list.add(buf[i]);
	}

	public static void encode_string(List<Byte> list, String val) {
		try {
			byte[] buf = val.getBytes(Protobuf.defCharsetName);
			encode_varint32(list, buf.length);
			for(int i=0; i<buf.length; i++) list.add(buf[i]);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static void encode_list(List<Byte> list, List<Byte> buf) {
		encode_varint32(list, buf.size());
		list.addAll(buf);
	}
}


