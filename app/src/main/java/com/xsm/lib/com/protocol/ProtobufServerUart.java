package com.xsm.lib.com.protocol;

import com.xsm.lib.hardware.SerialPortManager;
import com.xsm.lib.util.UserLog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public abstract class ProtobufServerUart extends ProtobufServer {
    protected UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), true);
    private static final UserLog DebugLog = new UserLog("ProtobufServerUart--", true);
    private static String UART_NAME = "";

    private SerialPort mSerialPort = null;
//    private SerialPortManager mSerialPort2 = new SerialPortManager();

    public ProtobufServerUart() {
    //    super(true,3000);
        DEBUG.log("open");
    }

    private void open() {
        UART_NAME = SerialPort.DEV_UART3;
//        UART_NAME = SerialPort.DEV_UART4;
//        if(mSerialPort2.isOpen()) mSerialPort2.close();
//        mSerialPort2.open(SerialPortManager.DEV_UART4, 115200);
        try {
            if(mSerialPort != null) mSerialPort.close1();
            mSerialPort = new SerialPort(new File(UART_NAME), 115200);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DEBUG.log("open");
    }

    /**
     * 停止服务
     */
    @Override
    public void stop() {
        super.stop();
        if(mSerialPort!=null) mSerialPort.close1();
        mSerialPort = null;
//        if(mSerialPort2.isOpen()) mSerialPort2.close();
        DEBUG.log("stop");
    }

    @Override
    protected int receive(byte[] buf, int offset, int length) {
       // DebugLog.log("receive");
//        if(!mSerialPort2.isOpen()) open();
//        if(mSerialPort2.isOpen()) return mSerialPort2.read(buf, offset, length);

        if(mSerialPort==null) open();
        if(mSerialPort!=null) {
            InputStream s = mSerialPort.getInputStream();
            if(s!=null) {
                try {
                    //s.available()检测会一直为0，固需直接读取
                    //Thread.sleep(100);
                    int len = s.read(buf, offset, length);
                    if(s.available()>0) {
                        DEBUG.log(UART_NAME+"-->rx data", buf, offset, len);
                    }
                    return len;
//                    if(s.available()>0) {
//                       // Thread.sleep(100);
//                        int len = s.read(buf, offset, length);
//                        DEBUG.log("rx data", buf, offset, len);
//                        return len;
//                    }
                } catch (IOException e0) {
                    e0.printStackTrace();
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
                }
            }
            else DebugLog.log("InputStream is null");
        }
        return 0;
    }

    @Override
    protected int send(byte[] buf, int offset, int length) {
//        if(!mSerialPort2.isOpen()) open();
//        if(mSerialPort2.isOpen()) return mSerialPort2.send(buf, offset, length)?length:0;
        if(mSerialPort==null) open();
        if(mSerialPort!=null) {
            OutputStream s = mSerialPort.getOutputStream();
            if(s!=null) {
                try {
                    DEBUG.log(UART_NAME+"-->tx data", buf, offset, length);
                    s.write(buf, offset, length);
                    s.flush();
                    return length;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else DEBUG.log("OutputStream is null");
        }
        return 0;
    }

}
