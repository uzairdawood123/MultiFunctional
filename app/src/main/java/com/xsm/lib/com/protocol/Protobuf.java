package com.xsm.lib.com.protocol;

import com.xsm.lib.com.protocol.pack.Decoder;
import com.xsm.lib.com.protocol.pack.Encoder;
import com.xsm.lib.util.UserLog;

import java.util.ArrayList;
import java.util.List;

public class Protobuf {
 //   private static UserLog DEBUG = new UserLog("Protobuf", true);

    public static int Version = 2;

    public static class V1
    {
        /// <summary>
        /// 包最大字节数
        /// </summary>
        public static final int PACKET_MAX_BYTES = 8192;
        /// <summary>
        /// 数据头
        /// </summary>
        public static final byte CODE_HEAD = (byte)0x02;
        /// <summary>
        /// 数据尾
        /// </summary>
        public static final byte CODE_END = (byte)0x03;
    }

    public static class V2
    {
        /// <summary>
        /// 同步头输出字节数
        /// </summary>
        public static int SYNC_BYTES = 7;

        /// <summary>
        /// 版本位标识
        /// </summary>
        public static byte Version = 0x02;
        /// <summary>
        /// 包最大字节数
        /// </summary>
        public static final int PACKET_MAX_BYTES = 8192;
        /// <summary>
        /// 数据包头,如果数据与这个值相同，则需要转议
        /// </summary>
        public static final byte PACKET_START = (byte)0xd5;
        /// <summary>
        /// 数据包尾,如果数据与这个值相同，则需要转议
        /// </summary>
        public static final byte PACKET_STOP = (byte)0x74;
        /// <summary>
        /// 数据转议码,
        /// 与已定义的特定数据相同的数据x编码为2byte的转议码传送：PACKET_ESCAPE,((x-PACKET_ESCAPE)&0xff)
        /// 比如:
        /// 0x1b->0x1b,0x00
        /// 0x55->0x1B,0x3a
        /// 0xD5->0x1B,0xba
        /// 0x74->0x1B,0x59
        /// </summary>
        public static final byte PACKET_ESCAPE = 0x1B;
        /// <summary>
        /// 数据包同步头，这个用是用低层协议的。如果数据与这个值相同，则需要转议
        /// </summary>
        public static final byte PACKET_SYNC = 0x55;
    }

    public static class DecodeResultList {
        /**
         * 取得的解码器列表
         */
        public List<Decoder> decoders = new ArrayList<Decoder>();
        /**
         * 当前读偏移,如果为负数，表示数据长包长度不足的字节数，否则为实际有效的数据包的长度
         */
        public int de_offset = 0;
    }

    public static class DecodeResult {
        /**
         * 取得的解码器
         */
        public Decoder decoder = null;
        /**
         * 当前读偏移,如果为负数，表示数据长包长度不足的字节数，否则为实际有效的数据包的长度
         */
        public int de_offset = 0;
    }

    /***************************************数据编码类型*********************************************************/
    public static final int WIRE_VARINT      = 0;
    public static final int WIRE_FIXED64     = 1;
    public static final int WIRE_LENGTH      = 2;
    public static final int WIRE_GROUP_START = 3;
    public static final int WIRE_GROUP_END   = 4;
    public static final int WIRE_FIXED32     = 5;
    public static final int WIRE_UNKNOWN     = -1;

    /***************************************数据类型*********************************************************/
    public static final int TYPE_DOUBLE   = 1;//小数所整数部分都全部传送，发送与接收端全相等
    public static final int TYPE_FLOAT    = 2;//小数部分会按精度取近拟值，发送与收到的值不绝对相等
    public static final int TYPE_INT64    = 3;
    public static final int TYPE_UINT64   = 4;
    public static final int TYPE_INT32    = 5;
    public static final int TYPE_FIXED64  = 6;//count,符号有效，小数部分无效
    public static final int TYPE_FIXED32  = 7;//count,符号有效，小数部分无效
    public static final int TYPE_BOOL     = 8;
    public static final int TYPE_STRING   = 9;
    public static final int TYPE_GROUP    = 10;
    public static final int TYPE_MESSAGE  = 11;
    public static final int TYPE_BYTES    = 12;
    public static final int TYPE_UINT32   = 13;
    public static final int TYPE_ENUM     = 14;
    public static final int TYPE_SFIXED32 = 15;
    public static final int TYPE_SFIXED64 = 16;
    public static final int TYPE_SINT32   = 17;
    public static final int TYPE_SINT64   = 18;
    public static final int TYPE_UNKNOWN  = -1;

    /***************************************通用操作*********************************************************/
    public static final String defCharsetName = "UTF-8"; //字符串的编码格式
    /*************************************命令定义*********************************************************/
    public static final int CMD_RESPONSE = 0;
    public static final int CMD_GET = 1;
    public static final int CMD_SET = 2;
    public static final int CMD_REPORT = 3;
    public static final int CMD_RECORD = 4;
    public static final int CMD_LOGIN = 5;
    public static final int CMD_PING = 6; //心跳命令
    /// <summary>
    /// 取得设备列表，主要用于多设备服务中管理端
    /// </summary>
    public static final int CMD_LIST_DEV = 7;
    /// <summary>
    /// 这个命令只对广播模式有效
    /// </summary>
    public static final int CMD_SET_ADDR = 8;

    public static final int CMD_KEY_GET = 32;
    public static final int CMD_KEY_SET = 33;
    //无效命令
    public static final int CMD_UNKNOWN = -1;
    
    /***********************通用控制域TAG定义******************************/
    //数据偏移定义
    public static final int TAG_CMD = 0;
    public static final int TAG_STATUS = 1;
    public static final int TAG_PID = 2; //串ID
    public static final int TAG_DATA = 3;
    //登入数据项,当本地访问时，这些项作为控制字，当远程登入时，这些项作为数据
    public static final int TAG_COMPANY= 4;
    public static final int TAG_USERID = 5;
    public static final int TAG_LICENSE = 6;
    //命令运行完时激活保存参数动作
    public static final int TAG_ACT_SAVE = 7;
    public static final int TAG_TIMESTAMP = 8;
    //用于用户向设备发命令时，禁止回应请求，默认为false,即需要接收回应信息
    //当传送文件数据时，通常设为true
    public static final int TAG_NO_RESPONSE = 9;
    //当前传送的数据在源数据流中的偏移
    public static final int TAG_OBJ_NAME = 10;
    public static final int TAG_OBJ_OFFSET = 11;
    public static final int TAG_OBJ_LENGTH = 12;
    //操作数据的类型
    public static final int TAG_OBJ_TYPE = 13;
    /*********************************************************
     * 用于建立通道通信时的ID,主要用于中转端确定中转的目标
     * 如果服务端有接收到TAG_SRC_ID则在应答时会配置TAG_OBJ_ID，否则不配置
     * 如果中转端接收到TAG_OBJ_ID，则会在自己维护的ID列表中找到目标并转发，
     * 中转端不区分接收到的数据的来源种类，只会将数据传到TAG_OBJ_ID指定的目标，如果TAG_OBJ_ID不存在，则丢弃
     * 如果客户端需要通过中转进行某目标的访问，则会使用使用TAG_OBJ_ID项
     * 客户端请求数据过程:
     * 1>客户端设定TAG_SRC_ID为自己的ID,并配置TAG_OBJ_ID为要访问的目标，然后将数据发送给中转器
     * 2>中转器维护一组在线列表，列表中包含了客户端和服务端，它们在中转端时是对等的。
     * 3>中转器收到客户端传入的数据，发现存在TAG_OBJ_ID项，具TAG_OBJ_ID项指定的目标存在在线列表中
     * 	则将数据包直接中转给目标
     * 4>服务端收到数据包后，如发现存在TAG_SRC_ID项，则将TAG_SRC_ID项配置给TAG_OBJ_ID后返回数据，并设定TAG_SRC_ID项为自身ID，当然TAG_SRC_ID的设定不是必需的
     * 5>中转端收到服务器返回的数据，发现存在TAG_OBJ_ID，则将数据直接转发给TAG_OBJ_ID指定的目标，即原来请求的客户端
     *********************************************************/
    public static final int TAG_SRC_ID = 14;
    public static final int TAG_OBJ_ID = 15;
    /***************************************出错状态定义*********************************************************/
    /**
     * 正确
     */
    public static final int noError = 0;
    /**
     * 数值大于预期的值，标签号将设为0
     */
    public static final int tooBig = 1;
    /**
     * 标签号为代理不支持的值
     */
    public static final int noSuchName = 2;
    /**
     * 这个出错只发生在设置运作其间，指示标签号的值是无效的（超出范围）
     */
    public static final int badValue = 3;
    /**
     * 这个出错只发生在设置运作其间，指示标签号指向的对像是不可写的
     */
    public static final int readOnly = 4;
    /**
     * 这个值指示出一些错误已经发生，但这个错误并不在已定义的出错代码中
     * 它是特定于应用程序的,需要引用
     * 开发文档来确定可能的错误。
     */
    public static final int genErr = 5;
    /**
     * 不能访问
     */
    public static final int noAccess = 6;
    /**
     * 出错类型
     */
    public static final int wrongType = 7;
    public static final int wrongLength = 8;
    public static final int wrongEncoding = 9;
    /**
     * 值是不合理的
     */
    public static final int wrongValue = 10;
    public static final int noCreation = 11;
    public static final int inconsistentValue = 12;
    public static final int resourceUnavailable = 13;
    public static final int commitFailed = 14;
    public static final int undoFailed = 15;
    public static final int authorizationError = 16;
    public static final int notWritable = 17;
    public static final int inconsistentName = 18;
    public static final int noLogin = 200; //未登录

    /**
     * 取得控制域的数据类型
     * @param tag
     * @return
     */
    public static int getTagType(int tag) {
        switch(tag) {
            case TAG_CMD:
            case TAG_STATUS:
            case TAG_PID:
            case TAG_OBJ_OFFSET:
            case TAG_OBJ_LENGTH:
            case TAG_OBJ_TYPE:
                return TYPE_INT32;
            case TAG_DATA:
                return TYPE_BYTES;
            case TAG_COMPANY:
            case TAG_USERID:
            case TAG_LICENSE:
            case TAG_OBJ_NAME:
            case TAG_SRC_ID:
            case TAG_OBJ_ID:
                return TYPE_STRING;
            case TAG_ACT_SAVE:
            case TAG_NO_RESPONSE:
                return TYPE_BOOL;
            case TAG_TIMESTAMP:
                return TYPE_UINT32;
            default:
                return TYPE_UNKNOWN;
        }
    }


    /**
     * 取得数据类型对应的线类型
     * @param type
     * @return
     */
    public static int getWireType(int type) {
        switch(type) {
            case TYPE_INT32:
            case TYPE_INT64:
            case TYPE_UINT32:
            case TYPE_UINT64:
            case TYPE_SINT32:
            case TYPE_SINT64:
            case TYPE_BOOL:
            case TYPE_ENUM:
                return WIRE_VARINT;
            case TYPE_FIXED64:
            case TYPE_SFIXED64:
            case TYPE_DOUBLE:
                return WIRE_FIXED64;
            case TYPE_STRING:
            case TYPE_BYTES:
            case TYPE_MESSAGE:
                return WIRE_LENGTH;
            case TYPE_FIXED32:
            case TYPE_SFIXED32:
            case TYPE_FLOAT:
                return WIRE_FIXED32;
            default:
                return WIRE_UNKNOWN;
        }
    }

    /**
     * 取得值的默认类型
     * @param value
     * @return
     */
    public static int getValueType(Object value) {
        if(value.getClass().equals(Boolean.class)) {
            return TYPE_BOOL;
        }else if(value.getClass().equals(Integer.class)) {
            return TYPE_INT32;
        }
        else if(value.getClass().equals(Long.class)) {
            return TYPE_INT64;
        }
        else if(value.getClass().equals(byte[].class)) {
            return TYPE_BYTES;
        }
        else if(value.getClass().equals(String.class)) {
            return TYPE_STRING;
        }
        else if(value.getClass().equals(Float.class)) {
            return TYPE_FLOAT;
        }
        else if(value.getClass().equals(Double.class)) {
            return TYPE_DOUBLE;
        }
        else {
            return TYPE_UNKNOWN;
        }
    }

    /**
     * 解码数据，
     * 注:数据不含数据头和数据尾以及长度位,并且不作转义处理
     * @param buf 数据缓冲
     * @param offset 有效数据起始位置
     * @param len 有效数据的长度
     * @return 如果失败，则返回null,否则返回解码器
     */
    public static Decoder decodeWithoutSync(byte[] buf, int offset, int len)
    {
        return Decoder.parseBuffer(buf, offset, len);
    }

    /**
     * 解码数据，
     * 注:数据含数据头和数据尾以及长度位
     * @param buf 数据缓冲
     * @param start_ofs 有效数据起始位置
     * @param end_ofs 有效数据的结束位置
     * @return 如果失败，则返回result.decoders.size()=0,否则返回解码器
     */
    public static DecodeResultList decode(byte[] buf, int start_ofs, int end_ofs)
    {
//        DEBUG.log("decode "+start_ofs+"->"+end_ofs, buf);
        DecodeResultList list = new DecodeResultList();
        list.decoders = new ArrayList<Decoder>();
        list.de_offset = start_ofs;
        while (true)
        {
            DecodeResult result = null;
            switch (Version)
            {
                case 1: result = decode_v1(buf, list.de_offset, end_ofs); break;
                case 2: result = decode_v2(buf, list.de_offset, end_ofs); break;
            }
            if (result.decoder == null) break;
            else list.decoders.add(result.decoder);
            list.de_offset = result.de_offset;
        }
        //  de_offset = end_ofs; //解初化为解码全部数据，以在没有解码器时可以退出
        //   return (list.Count > 0) ? list[list.Count - 1] : null; //返回最新的数据
        //   return (list.Count > 0) ? list[0] : null; //返回最前的数据
        return list;
    }

    /**
     * 解码数据，
     * 注:数据含数据头和数据尾以及长度位
     * @param buf 数据缓冲
     * @param start_ofs 有效数据起始位置
     * @param end_ofs 有效数据的结束位置
     * @return 如果失败，则返回result/decoder=null,否则返回解码器
     */
    public static DecodeResult decode_v1(byte[] buf, int start_ofs, int end_ofs)
    {
        DecodeResult result = new DecodeResult(); //解码结果
        int re_offset = start_ofs; //当前读偏移
        while (re_offset < end_ofs)
        { //查找有效数据包并处理
            if ((buf[re_offset] & 0xff) == V1.CODE_HEAD)
            { //发现数据头
                if ((re_offset + 4) <= end_ofs) //数据满足最小长度需求(len:2byte,end:1byte)
                {
                    int data_len = buf[re_offset + 1] & 0xff;
                    data_len |= (((int)(buf[re_offset + 2] & 0xff)) << 8);
                    if (data_len > (V1.PACKET_MAX_BYTES - 4)) { re_offset++; continue; } //数据包长度无效,则表示当前不是正确的数据头
                    else if (re_offset + data_len + 4 > end_ofs) { break; } //未接收完整个数据包，则等待接收更多数据
                    else if ((buf[re_offset + data_len + 3] & 0xff) != V1.CODE_END) { re_offset++; continue; }//结束符无效,则表示当前不是正确的数据头
                    else if ((result.decoder = Decoder.parseBuffer(buf, re_offset + 3, data_len)) != null) //接收到正确的数据包
                    {
                        re_offset += data_len + 4;
                        break;
                    }
                    else //解码出错,则表示当前不是正确的数据头
                    {
                        re_offset++;
                        continue;
                    }
                }
                else //等待接收到最小数据长度
                {
                    break;
                }
            }
            else re_offset++; //当前不是数据头
        }
        result.de_offset = re_offset;
        return result;
    }

    /**
     * 解码数据，
     * 注:数据含数据头和数据尾以及长度位
     * @param buf 数据缓冲
     * @param start_ofs 有效数据起始位置
     * @param end_ofs 有效数据的结束位置
     * @return 如果失败，则返回result/decoder=null,否则返回解码器
     */
    public static DecodeResult decode_v2(byte[] buf, int start_ofs, int end_ofs)
    {
    //    DEBUG.log("decode_v2 in",buf,start_ofs,end_ofs-start_ofs);
        DecodeResult result = new DecodeResult(); //解码结果
        int re_offset = start_ofs; //当前读偏移
        //查找数据包的开始位置
        while (re_offset < end_ofs)
        {
            if (buf[re_offset] == V2.PACKET_START) break;//发现数据头
            else {
           //     DEBUG.log("decode_v2 head check[ofs:"+re_offset+";rx:"+buf[re_offset]+"; tx:"+V2.PACKET_START+"]");
                re_offset++; //当前不是数据头
            }
        }
        //查找数据包的结束位置
        int stop_ofs = re_offset;
    //    DEBUG.log("decode_v2 pack[stop_ofs:"+stop_ofs+"; re_offset"+re_offset+"]");
        for (int i = re_offset; i < end_ofs; i++)
        {
            if (buf[i] == V2.PACKET_START) re_offset = i; //找到包头，则重新开始
            else if (buf[i] == V2.PACKET_STOP) //找到包尾
            {
                stop_ofs = i;
                break;
            }
        }
   //     DEBUG.log("decode_v2 pack[stop_ofs:"+stop_ofs+"; re_offset"+re_offset+"]");
        //数据包有效
        if (stop_ofs > re_offset)
        {
            int v_start = re_offset;
            int v_len = stop_ofs - re_offset + 1;
            re_offset = stop_ofs + 1; //跳过已解码的数据包
            //转码
            List<Byte> list = new ArrayList<>();
            boolean is_escape = false;
            for (int ofs = 0; ofs < v_len; ofs++)
            {
                int index = v_start + ofs;
                if (buf[index] == V2.PACKET_ESCAPE) is_escape = true;
                else if (buf[index] == V2.PACKET_START || buf[index] == V2.PACKET_STOP || buf[index] == V2.PACKET_SYNC) is_escape = false;
                else
                {
                    list.add(is_escape ? (byte)((buf[index] + V2.PACKET_ESCAPE) & 0xff) : buf[index]);
                    is_escape = false;
                }
            }
            if(list.size()>0) list.remove(0); //移除版本号
            //解码
            byte[] bytes = new byte[list.size()];
            for(int i=0; i<bytes.length; i++) bytes[i] = list.get(i);
            result.decoder = Decoder.parseBuffer(bytes, 0, bytes.length);
        }
        else if (end_ofs - re_offset >= V2.PACKET_MAX_BYTES) //包长无效
        {
            re_offset = end_ofs;
        }
        result.de_offset = re_offset;
        return result;
    }

    public static byte[] encodeWithoutSync(Encoder coder)
    {
        List<Byte> list = coder.pack();
        byte[] out = new byte[list.size()];
        for(int i=0; i<out.length; i++) out[i] = list.get(i);
        return out;
    }

    /**
     * 将缓码器封包后用于发送
     * 注:返回的数据已包含数据头和数据尾以及数据长度位
     * @param coder
     * @return
     */
    public static byte[] encode(Encoder coder)
    {
        Byte[] bytes = coder.pack().toArray(new Byte[0]);
        List<Byte> list = new ArrayList<Byte>();
        switch(Version)
        {
            case 1: {list = encode_v1(bytes); break;}
            case 2: {list = encode_v2(bytes); break;}
        }
        byte[] out = new byte[list.size()];
        for(int i=0; i<out.length; i++) out[i] = list.get(i);
        return out;
    }

    /**
     * 打包数据
     * 在这里会对特殊数据进行转义操作，
     * 并会添加数据包头与包尾
     * @param buf 等编码数据
     * @return 用于传送的数据流
     */
    public static List<Byte> encode_v1(Byte[] buf)
    {
        List<Byte> list = new ArrayList<Byte>();
        list.add(V1.CODE_HEAD);
        list.add((byte)((buf.length >> 0) & 0xff));
        list.add((byte)((buf.length >> 8) & 0xff));
        for (int i = 0; i < buf.length; i++) list.add(buf[i]);
        list.add(V1.CODE_END);
        return list;
    }

    /**
     * 打包数据
     * 在这里会对特殊数据进行转义操作，
     * 并会添加数据包头与包尾
     * @param buf 等编码数据
     * @return 用于传送的数据流
     */
    public static List<Byte> encode_v2(Byte[] buf)
    {
        List<Byte> list = new ArrayList<Byte>();
        for (int i = 0; i < V2.SYNC_BYTES; i++) list.add(V2.PACKET_SYNC);
        list.add(V2.PACKET_START);
        list.add(V2.Version); //添加版本号
        for (int i = 0; i < buf.length; i++)
        {
            if (buf[i] == V2.PACKET_START ||
                    buf[i] == V2.PACKET_ESCAPE ||
                    buf[i] == V2.PACKET_SYNC ||
                    buf[i] == V2.PACKET_STOP)
            {
                list.add(V2.PACKET_ESCAPE);
                list.add((byte)((buf[i] - V2.PACKET_ESCAPE) & 0xff));
            }
            else list.add(buf[i]);
        }
        list.add(V2.PACKET_STOP);
        return list;
    }
}
