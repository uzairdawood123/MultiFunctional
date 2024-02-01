package com.xsm.lib.com.protocol;

import com.xsm.lib.hardware.SerialPortManager;

public class ProtobufClientUartT4 extends ProtobufClient {

    private SerialPortManager mSerialPort = null;


//    public ProtobufClientUart() {
//        super(true,3000, 1);
//    }

    private void open() {
        mSerialPort = new SerialPortManager();
        mSerialPort.open(SerialPortManager.DEV_UART4, 115200);
        DEBUG.log("open");
    }

    @Override
    protected void finalize() {
        if(mSerialPort==null) mSerialPort.close();
        DEBUG.log("finalize");
    }

    @Override
    protected int receive(byte[] buf, int offset, int length) {
        if(mSerialPort==null) open();
        if(mSerialPort!=null) {
            int len = mSerialPort.read(buf, offset, length);
            if(len>0) DEBUG.log("rx data", buf, offset, len);
            return len;
        }
        return 0;
    }

    @Override
    protected int send(byte[] buf, int offset, int length) {
        if(mSerialPort==null) open();
        if(mSerialPort!=null) {
            if(mSerialPort.send(buf,offset,length)) return length;
        }
        return 0;
    }

    @Override
    public int emptyReceive() {
        return 0;
    }
}
