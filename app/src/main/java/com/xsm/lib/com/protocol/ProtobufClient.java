package com.xsm.lib.com.protocol;

import android.os.SystemClock;

import com.xsm.lib.com.protocol.pack.Decoder;
import com.xsm.lib.com.protocol.pack.Encoder;
import com.xsm.lib.util.UserLog;

public abstract class ProtobufClient {
    protected UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), false);

    protected final static int RX_BUFFER_SIZE = 8192*2;
    protected final static int MAX_PACK_SIZE = 8192; //最大数据包大小，这个值可设范围为(4~RX_BUFFER_SIZE)
    protected byte[] readbuf = new byte[RX_BUFFER_SIZE*2];
    protected int RX_TIMEOUT = 5000;
    protected int RX_RETRY = 3;

    public boolean checkPid = true;
    public boolean checkCmd = false;
    public boolean checkError = false;

    public ProtobufClient() {this(false,5000, 3);}
    /**
     * @param debug 是否启用调试信息
     */
    public ProtobufClient(boolean debug) {
        this(debug, 5000, 3);
    }

    /**
     *
     * @param debug 是否启用调试信息
     * @param timeout 接收超时控制
     * @param retry 出错重试次数，为0里表示出错不再重试
     */
    public ProtobufClient(boolean debug, int timeout, int retry) {
        DEBUG.setDebugKey(debug);
        this.RX_TIMEOUT = timeout;
        this.RX_RETRY = retry;
    }

    /**
     * 从接口中取得数据输出，这个函数需要用户重载
     * 注:本函数可以配置为阻断(同步)读取或异步读取
     * @param buf 用于接收数据的缓冲
     * @param offset 数据填充的起始位置
     * @param length 本次读取数据的最大长度
     * @return 实际读取到的数据的长度，如果为0，表示运行正常，但未取得任何数据
     */
    protected abstract int receive(byte[] buf, int offset, int length);

    /**
     * 将数据发送出去
     * @param buf 要发送的数据的缓冲
     * @param offset 有效数据的起始位置
     * @param length 需要发送的长度
     * @return 实际发送的字节数
     */
    protected abstract int send(byte[] buf, int offset, int length);

    /**
     * 清空接收缓冲
     * @return 实际清除的字节个数
     */
    public abstract int emptyReceive();

    /**
     * 发送已编码的数据
     * @param encoder
     * @return 已发送的长度
     */
    public int sync_write(Encoder encoder) {
        byte[] data = Protobuf.encode(encoder);
        return send(data,0,data.length);
    }

    /**
     * 接收一个数据并解码控制域
     * @param timeout 接收超时控制
     * @return 如果接收到正确数据，则返回解码器，否则返回null
     */
    public Decoder sync_read(int timeout, int pid) {
        int buf_len = 0, re_offset = 0; //接收到的数据管理
        int rx_len;
        long lastReadTime = SystemClock.uptimeMillis(); //获取开机以来非睡眠状态持续的毫秒数
        while (true)
        {//超时控制
            long now = SystemClock.uptimeMillis(); //获取开机以来非睡眠状态持续的毫秒数
            if (now - lastReadTime >= timeout)
            { //时间超时,清空超时的数据
                return null;
            }
            if ((rx_len = receive(readbuf, buf_len, MAX_PACK_SIZE)) > 0)
            {
                //设定接收到的数据
                buf_len += rx_len;
                //查找有效数据包并处理
                Protobuf.DecodeResultList list = Protobuf.decode(readbuf, re_offset, buf_len);
                re_offset = list.de_offset;
                DEBUG.log("Protobuf.decode="+list.decoders.size(),readbuf,re_offset, buf_len);
                //处理解码数据
                for (Decoder decoder : list.decoders)
                {
                    if (decoder != null)
                    {
                        int get_pid = pid;
                        int get_cmd = Protobuf.CMD_RESPONSE;
                        int error = Protobuf.noError;
                        if (checkPid) get_pid = decoder.decode_control(Protobuf.TAG_PID, -1);
                        if (checkCmd) get_cmd = decoder.decode_control(Protobuf.TAG_CMD, Protobuf.CMD_UNKNOWN);
                        if (checkError) error = decoder.decode_control(Protobuf.TAG_STATUS, Protobuf.noError);
                        if (get_pid == pid && get_cmd == Protobuf.CMD_RESPONSE && error == Protobuf.noError) return decoder;
                    }
                }
                //未解码数据处理:查看数据是否为满，如已满则保留未解码数据，并清空已解码数据
                if (buf_len > RX_BUFFER_SIZE)
                {
                    if (buf_len > re_offset)
                    { //存在未处理的数据
                        buf_len = buf_len - re_offset;
                        for (int i = 0; i < buf_len; i++) readbuf[i] = readbuf[i + re_offset];
                    }
                    else buf_len = 0; //没有未解码的数据，则清空缓冲长度
                    re_offset = 0; //重置读指针
                }
            }
        }
    }

    /**
     * 与服务器进行会话,会话前会先回读一次数据，以清空缓冲
     * @param encoder 传送组服务器的数据
     * @param timeout 会话超时控制,单位ms
     * @param retry 会话超时或出错后重试的次数
     * @return 如果接收到正确数据，则返回解码器，否则返回null
     */
    public Decoder sync_dialogue(Encoder encoder,int timeout,int retry, boolean resetReceive) {
        byte[] data = Protobuf.encode(encoder);
        if (resetReceive) emptyReceive(); //清空接收缓冲
        for (int i = 0; i < (retry + 1); i++)
        {
            if (send(data, 0, data.length) > 0)
            {
                while(true)
                {
                    Decoder decoder = sync_read(timeout, encoder.pid);
                    if (decoder != null)
                    {
                        return decoder;
                    }
                    else break;
                }
            }
        }
        return null;
    }

    /**
     * 与服务器进行会话,会话前会先回读一次数据，以清空缓冲
     * @param encoder 待发送的已缓码数据
     * @return 如果接收到正确数据，则返回解码器，否则返回null
     */
    public Decoder sync_dialogue(Encoder encoder) {
        return sync_dialogue(encoder, RX_TIMEOUT, RX_RETRY, false);
    }
}
