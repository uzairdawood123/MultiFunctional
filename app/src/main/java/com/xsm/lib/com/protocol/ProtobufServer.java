package com.xsm.lib.com.protocol;

import android.os.SystemClock;

import com.xsm.lib.com.protocol.pack.Decoder;
import com.xsm.lib.com.protocol.pack.Encoder;
import com.xsm.lib.util.UserLog;

public abstract class ProtobufServer {
	protected UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), true);

	protected Thread receiveThread = null; //命令接收处理线程
	protected boolean isReceive = true;

	protected final static int RX_BUFFER_SIZE = 8192*2;
	protected int MAX_PACK_SIZE = 8192; //最大数据包大小，这个值可设范围为(4~RX_BUFFER_SIZE)
	protected long RX_TIMEOUT = 30000;

	public ProtobufServer() {this(true, 30000);}
	/**
	 * @param debug 是否启用调试信息
	 */
	public ProtobufServer(boolean debug) {
		this(debug, 30000);
	}

	/**
	 *
	 * @param debug 是否启用调试信息
	 * @param timeout 接收超时控制
	 */
	public ProtobufServer(boolean debug, long timeout) {
		DEBUG.setDebugKey(debug);
		this.RX_TIMEOUT = timeout;
	}

	/**
	 * 上一次接收到数据的系统毫秒计数
	 */
	protected long lastReadTime = 0;

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
	 * 命令处理函数
	 * 注:数据域是未进行解码的，如需要提取数据，则需要解码数据域
	 * @param cmd 接收到的命令
	 * @param pid 接收到的命令串ID
	 * @param decoder 接收到的数据解码器
	 * @param client 当前发送命令的客户端接口
	 * @return 如果返回null表示不回复，否则回复
	 */
	protected abstract Encoder disposeCmd(int cmd, int pid, Decoder decoder, ClientAbstract client);

	/**
	 * 发送已编码的数据
	 * @param encoder
	 * @return 已发送的长度
	 */
	public int sync_write(Encoder encoder) {
		byte[] data = Protobuf.encode(encoder);
	//	DEBUG.log("sync_write data:",data);
		return send(data,0,data.length);
	}

	/**
	 * 启动服务
	 */
	public void start() {
		DEBUG.log("server start...");
		//监听接收
		isReceive = true;
		receiveThread = new Thread(new Runnable(){
			@Override
			public void run() {
				byte[] readbuf = new byte[RX_BUFFER_SIZE*2];
				int buf_len=0,re_offset=0; //接收到的数据管理
				int rx_len;
				while(isReceive) {
					try {
						if((rx_len=receive(readbuf,buf_len,MAX_PACK_SIZE))>0) { //缓冲中有接收到数据
						//	DEBUG.log("receive size:"+rx_len+"; old_len:"+buf_len);
							//超时控制
							long now = SystemClock.uptimeMillis(); //获取开机以来非睡眠状态持续的毫秒数
							if (now - lastReadTime >= RX_TIMEOUT) { //时间超时,清空超时的数据
							//	DEBUG.log("receive timeout,remove date " + re_offset + "->" + buf_len);
								re_offset = buf_len;
							}
							buf_len += rx_len; //设定接收到的数据
							lastReadTime = now; //重置上一次通信时标
							//查找有效数据包并处理
							Protobuf.DecodeResultList list = Protobuf.decode(readbuf, re_offset, buf_len);
							re_offset = list.de_offset;
							for (Decoder decoder : list.decoders) {
								if (decoder != null) {
									int cmd = decoder.decode_control(Protobuf.TAG_CMD, Protobuf.CMD_UNKNOWN);
									if (cmd != Protobuf.CMD_UNKNOWN) {//命令有效，解码所有控制域
										int pid = decoder.decode_control(Protobuf.TAG_PID, 0);
										//   String company = decoder.decode_control(Protobuf.TAG_COMPANY, (String)null);
										String srcId = decoder.decode_control(Protobuf.TAG_SRC_ID, (String) null);
										Encoder encoder = disposeCmd(cmd, pid, decoder, new ClientAbstract() {
											@Override
											public int read(byte[] buf, int offset, int length) {
												// TODO Auto-generated method stub
												return receive(buf, offset, length);
											}

											@Override
											public int write(byte[] buf, int offset, int length) {
												// TODO Auto-generated method stub
												return send(buf, offset, length);
											}

										});
										//解码数据包
										if (encoder != null && decoder.decode_control(Protobuf.TAG_NO_RESPONSE, false) == false) { //需要回复且未定指定不回复
											if (srcId != null) { //存在源ID，则配置通道中转参数,指定接收命令的设备ID
												//	encoder.addControl(Protobuf.TAG_COMPANY, company);
												encoder.addControl(Protobuf.TAG_OBJ_ID, srcId);
											}
											byte[] ack = Protobuf.encode(encoder);
											send(ack, 0, ack.length);
										}
									}
								}
							}
							//未解码数据处理:查看数据是否为满，如已满则保留未解码数据，并清空已解码数据
							if (buf_len > RX_BUFFER_SIZE) {
								if (buf_len > re_offset) { //存在未处理的数据
									buf_len = buf_len - re_offset;
									for (int i = 0; i < buf_len; i++)
										readbuf[i] = readbuf[i + re_offset];
								} else buf_len = 0; //没有未解码的数据，则清空缓冲长度
								re_offset = 0; //重置读指针
							}
						}
						else Thread.sleep(100); //延时100ms，以减小资源开支
					} catch (Exception e) {
						e.printStackTrace();
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});
		receiveThread.start();
	}

	/**
	 * 停止服务
	 */
	public void stop() {
		DEBUG.log("server stop...");
		isReceive = false;
		try {
			if(receiveThread!=null) {
				receiveThread.interrupt();
				receiveThread = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
