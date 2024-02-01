package com.xsm.lib.hardware;

import com.friendlyarm.FriendlyThings.HardwareControler;
import com.xsm.lib.util.UserLog;

public class SerialPortManager {
    private UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), true);

    public static final String DEV_UART0 = "/dev/ttyS0";
    public static final String DEV_UART2 = "/dev/ttyS2";
    public static final String DEV_UART4 = "/dev/ttyS4";
    // NanoPC-T4 UART4
    private String devName = "/dev/ttyS4";
    private int speed = 115200;
    private int dataBits = 8;
    private int stopBits = 1;
    private int devfd = -1;

    /**
     * 端口是否已打开
     * @return
     */
    public boolean isOpen() {
        return (devfd > 0);
    }

    /**
     * 关闭端口
     */
    public void close() {
        if (devfd != -1) {
            HardwareControler.close(devfd);
            devfd = -1;
        }
    }

    /**
     * 打开串口
     * @param dev 设备名
     * @param baudrate 波特率
     * @return 是否成功
     */
    public boolean open(String dev, int baudrate) {
        this.devName = dev;
        this.speed = baudrate;
        devfd = HardwareControler.openSerialPort( devName, speed, dataBits, stopBits );
        if (devfd < 0) {
            devfd = -1;
            DEBUG.log("Fail to open " + devName + "!");
        }
        else {
            String winTitle = devName + "," + speed + "," + dataBits + "," + stopBits;
            DEBUG.log("open \"" + winTitle + "\", fd="+devfd);
        }
        return (devfd >= 0);
    }

    /**
     * 发送数据
     * @param buf 等发送数据缓冲
     * @param offset 有效数据的开始位置
     * @param len 有效数据的长度
     * @return 发送是否成功
     */
    public boolean send(byte[] buf, int offset, int len)
    {
        int ret = 0;
        if(offset!=0 || len!=buf.length) {
            if(len + offset <= buf.length) {
                byte[] tbuf = new byte[len];
                System.arraycopy(buf, offset, tbuf, 0, len);
                ret = HardwareControler.write(devfd, buf);
            }
            else ret=0;
        }
        else ret = HardwareControler.write(devfd, buf);
        if (ret > 0) {
            DEBUG.log("send data ok. devfd=" + devfd, buf, offset, len);
        } else {
            DEBUG.log("Fail to send! devfd=" + devfd);
        }
        return (ret > 0);
    }

    /**
     * 回读数据
     * @param buf 接收数据的缓冲
     * @param offset 接收数据偏移
     * @param len 请求接收的长度
     * @return 实际读到的数据长度
     */
    public int read(byte[] buf, int offset, int len) {
        if (buf!=null && len>0 && HardwareControler.select(devfd, 0, 0) == 1) {
            if(len-offset > buf.length) len = buf.length-offset;
            int retSize = 0;
            if(offset>0) {
                byte[] tbuf = new byte[len];
                retSize = HardwareControler.read(devfd, tbuf, len);
                System.arraycopy(tbuf, 0, buf, offset, retSize);
            }
            else {
                retSize = HardwareControler.read(devfd, buf, len);
            }
            DEBUG.log("read data. devfd=" + devfd, buf, offset, retSize);
            return retSize;
        }
        return 0;
    }
}
