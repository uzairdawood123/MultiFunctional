package com.xsm.lib.com.protocol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class ProtobufClientUart extends ProtobufClient {

    private SerialPort mSerialPort = null;


    public ProtobufClientUart() {
        super(true,1000, 0);
    }

    private void open() {
        try {
            mSerialPort = new SerialPort(new File(SerialPort.DEV_UART4), 115200);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DEBUG.log("open");
    }

    @Override
    protected void finalize() {
        if(mSerialPort!=null) mSerialPort.close1();
        DEBUG.log("finalize");
    }

    @Override
    protected int receive(byte[] buf, int offset, int length) {
        //   DEBUG.log("receive");
        if(mSerialPort==null) open();
        if(mSerialPort!=null) {
            InputStream s = mSerialPort.getInputStream();
            if(s!=null) {
                try {
                    if(s.available()>0) {
                     //   Thread.sleep(100);
                        int len = s.read(buf, offset, length);
                        DEBUG.log("rx data", buf, offset, len);
                        return len;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else DEBUG.log("InputStream is null");
        }
        return 0;
    }

    @Override
    protected int send(byte[] buf, int offset, int length) {
        if(mSerialPort==null) open();
        if(mSerialPort!=null) {
            OutputStream s = mSerialPort.getOutputStream();
            if(s!=null) {
                try {
                    DEBUG.log("tx data", buf, offset, length);
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

    @Override
    public int emptyReceive() {
        return 0;
    }
}
