package com.xsm.lib.com.protocol;

import android.content.Context;
import android.os.SystemClock;

import com.xsm.lib.com.protocol.pack.Decoder;
import com.xsm.lib.com.protocol.pack.Encoder;
import com.xsm.lib.util.UserLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public abstract class TcpProtobufServer {
	protected UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), false);

	private Thread listenThread=null; //监听线程
	private Thread pingThread = null; //ping运行线程,用于超时处理

	protected ServerSocket serverSocket = null; //网关连接字
	protected List<ClientEntry> clientList = new ArrayList<ClientEntry>();

	protected Context context = null;
	public int ServerPort = 11162;
	protected long PingTick = 10000;//ping间隔
	protected int MaxClients = 10; //同时存在的客户端最大个数

	public TcpProtobufServer(Context con, boolean debug) {
		DEBUG.setDebugKey(debug);
		this.context=con;
	}

	/**
	 * 命令处理函数
	 * 注:数据域是未进行解码的，如需要提取数据，则需要解码数据域
	 * @param cmd 接收到的命令
	 * @param pid 接收到的命令串ID
	 * @param decoder 接收到的数据解码器
	 * @param client 当前发送命令的客户端接口
	 * @return 如果返回null表示不回复，否则回复
	 */
	protected abstract Encoder dispose(int cmd, int pid, Decoder decoder, ClientAbstract client);

	/**
	 * 超时检测间隔事件，第个间隔会自动调用一次，用于计时事件
	 * 如果用户没有间隔事件则可以不作处理
	 * @param tick 运行的间隔，单位ms
	 */
	protected void pingTickEvent(long tick) {}

	/**
	 * 启动服务
	 */
	public void start(int port) {
		ServerPort = port;
		DEBUG.log("server start at "+ServerPort);
		listenThread = new Thread(new Runnable(){
			@Override
			public void run() {
				while(true) {
					boolean is_ok = false;
					try {
						if((serverSocket==null)||(serverSocket.isClosed())) {
							DEBUG.log("new serverSocket at "+ServerPort);
							Thread.sleep(5000);
							serverSocket = new ServerSocket(ServerPort);
						}
						Socket socket = serverSocket.accept();
						ClientEntry client = new ClientEntry(socket,context);
						client.start();
						DEBUG.log("client add start from= "+socket.getRemoteSocketAddress()+"; client old size="+clientList.size());
						if(clientList.size()>MaxClients) { //客户端过多，则先移除最长时间没有通讯的
							while(true) {
								long min_time = Long.MAX_VALUE,last_time=0;
								int index = 0;
								for(int i=0; i<clientList.size(); i++) {
									last_time = clientList.get(i).getLastTime();
									if(!clientList.get(i).isValid()) {
										DEBUG.log("add->client timeout remove from= "+(clientList.get(i).socket!=null?clientList.get(i).socket.getRemoteSocketAddress():"null"));
										clientList.remove(i);
										if(i>0) i--;
										else break;
									}
									else if(min_time>last_time) {
										index = i;
										min_time=last_time;
									}
								}
								if(clientList.size()<=MaxClients) break;
								else {
									DEBUG.log("add->clientlist too size remove from= "+(clientList.get(index).socket!=null?clientList.get(index).socket.getRemoteSocketAddress():"null"));
									clientList.remove(index);
								}
							}
						}
						clientList.add(client); //将已连接的客户端添加到队列中
						is_ok = true;
						continue;
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IndexOutOfBoundsException e) { //索引出界
						e.printStackTrace();
					} catch (ClassCastException e) { //类型转换出错
						e.printStackTrace();
					} catch (SocketException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if(!is_ok) {
							DEBUG.log("serverSocket error. close it!");
							try {
								if(serverSocket!=null) serverSocket.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							serverSocket=null;
						}
					}
				}
			}
		});
		listenThread.start();
		pingThread = new Thread(new Runnable(){
			@Override
			public void run() {
				while(true) {
					try {
						//	DEBUG.log("ping runnig..."+(int)((SystemClock.uptimeMillis())/1000));
						while(true) {
							long min_time = Long.MAX_VALUE,last_time=0;
							int index = 0;
							for(int i=0; i<clientList.size(); i++) {
								last_time = clientList.get(i).getLastTime();
								if(!clientList.get(i).isValid()) {
									DEBUG.log("ping->client timeout remove from= "+(clientList.get(i).socket!=null?clientList.get(i).socket.getRemoteSocketAddress():"null"));
									clientList.remove(i);
									if(i>0) i--;
									else break;
								}
								else if(min_time>last_time) {
									index = i;
									min_time=last_time;
								}
							}
							if(clientList.size()<=MaxClients) break;
							else {
								DEBUG.log("ping->clientlist too size remove from= "+(clientList.get(index).socket!=null?clientList.get(index).socket.getRemoteSocketAddress():"null"));
								clientList.remove(index);
							}
						}
						pingTickEvent(PingTick);
						Thread.sleep(PingTick);
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IndexOutOfBoundsException e) { //索引出界
						e.printStackTrace();
					} catch (ClassCastException e) { //类型转换出错
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		pingThread.start();
	}

	/**
	 * 停止服务
	 */
	public void stop() {
		DEBUG.log("server stop...");
		try {
			for(int i=0; i<clientList.size(); i++) clientList.get(i).stop();
			if(listenThread!=null) {listenThread.interrupt();listenThread=null;}
			if(pingThread!=null) {pingThread.interrupt();pingThread=null;}
			if(serverSocket!=null) {
				serverSocket.close();
				serverSocket=null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class ClientEntry extends ProtobufServer {
		public Socket socket = null; //网关连接字
		private InputStream input_stream = null;
		private long lastTime = SystemClock.uptimeMillis(); //获取开机以来非睡眠状态持续的毫秒数
		private final static long CLIENT_TIMEOUT = 60*1000; //客户端无活动超时处理,ms

		public ClientEntry(Socket socket, Context con) {
			this.socket=socket;
		}

		@Override
		protected int receive(byte[] buf, int offset, int length) {
			int len = 0;
			try {
				if(socket!=null) {
					if(input_stream==null) input_stream = socket.getInputStream();
					len = input_stream.read(buf,offset,length);
				}
			} catch (NullPointerException e) {
				//e.printStackTrace();
				socket=null;
			} catch (IllegalArgumentException e) {
				//e.printStackTrace();
				socket=null;
			} catch (IndexOutOfBoundsException e) { //索引出界
				//e.printStackTrace();
				socket=null;
			} catch (ClassCastException e) { //类型转换出错
				//e.printStackTrace();
				socket=null;
			} catch (SocketException e) {
				//e.printStackTrace();
				socket=null;
			} catch (IOException e) {
				//e.printStackTrace();
				socket=null;
			}
			if(len>0) lastTime = SystemClock.uptimeMillis(); //获取开机以来非睡眠状态持续的毫秒数
			return len;
		}

		@Override
		protected int send(byte[] buf, int offset, int length) {
			try {
				//	if(!NetWorkUtil.isConnect(context)) {socket=null;return 0;}
				if(buf.length<=0 || length==0) return 0;
				OutputStream out  = socket.getOutputStream();
				out.write(buf,offset,length);
				out.flush();
				if(length>0) lastTime = SystemClock.uptimeMillis(); //获取开机以来非睡眠状态持续的毫秒数
				return length;
			} catch (NullPointerException e) {
				//e.printStackTrace();
				socket=null;
			} catch (IllegalArgumentException e) {
				//e.printStackTrace();
				socket=null;
			} catch (IndexOutOfBoundsException e) { //索引出界
				//e.printStackTrace();
				socket=null;
			} catch (ClassCastException e) { //类型转换出错
				//e.printStackTrace();
				socket=null;
			} catch (SocketException e) {
				//e.printStackTrace();
				socket=null;
			} catch (Exception e) {
				//e.printStackTrace();
				socket=null;
			}
			return 0;
		}

		public long getLastTime() {
			return lastTime;
		}

		public boolean isValid() {
			return (socket!=null && (SystemClock.uptimeMillis()-lastTime)<CLIENT_TIMEOUT);
		}

		@Override
		protected Encoder disposeCmd(int cmd, int pid, Decoder decoder, ClientAbstract client) {
			// TODO Auto-generated method stub
			DEBUG.log("disposeCmd: cmd= "+cmd+"; pid="+pid);
			return dispose(cmd, pid, decoder, client);
		}
	}
}
