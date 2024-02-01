package com.xsm.lib.com.protocol;

public abstract class ClientAbstract {
	/**
	 * 启动客户端
	 */
	public void start(){}
	/**
	 * 断开客户端
	 */
	public void stop(){}
	/**
	 * 从接口中取得数据输出，会阻断直接接收到数据
	 * @param buf 用于接收数据的缓冲
	 * @param offset 数据填充的起始位置
	 * @param length 本次读取数据的最大长度
	 * @return 实际读取到的数据的长度，如果为0，表示运行正常，但未取得任何数据
	 */
	public abstract int read(byte[] buf, int offset, int length);
	/**
	 * 将数据发送出去
	 * @param buf 要发送的数据的缓冲
	 * @param offset 有效数据的起始位置
	 * @param length 需要发送的长度
	 * @return 实际发送的字节数
	 */
	public abstract int write(byte[] buf, int offset, int length);
}
