package android_serialport_api;

import android.hardware.display.DisplayManager;
import android.view.Surface;

import com.xsm.lib.util.UserLog;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort
{
    private static UserLog DEBUG = new UserLog("SerialPort", true);

//    public static final String DEV_UART0 = "/dev/ttyS0";
//    public static final String DEV_UART2 = "/dev/ttyS2";
//    public static final String DEV_UART1 = "/dev/ttyACM1";
    public static final String DEV_UART2 = "/dev/ttyACM2";
//    public static final String DEV_UART3 = "/dev/ttyACM3";
    //public static final String DEV_UART4 = "/dev/ttyACM3";
    public static final String DEV_UART4 = "/dev/ttyS4";
    public static final String DEV_UART3 = "/dev/ttyXRUSB2";
//    public static final String DEV_UART3 = "/dev/ttyS3";
    public static final String DEV_UART1 = "/dev/ttyXRUSB0";
    public static final String DEV_RS485 = "/dev/ttyXRUSB1";

    /*
     * Do not remove or rename the field mFd: it is used by native method
     * close();
     */
    private static FileDescriptor mFd = null;

    private FileInputStream mFileInputStream;

    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int baudrate)
            throws SecurityException, IOException
    {
        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/xbin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }
        if(mFd != null) {
            mFileInputStream = new FileInputStream(mFd);
            mFileOutputStream = new FileOutputStream(mFd);
            return;
        }
        mFd = open(device.getAbsolutePath(), baudrate,0);
        if (mFd == null)
        {
            DEBUG.log("native open returns null by "+device.getAbsolutePath());
            throw new IOException();
        }
        else DEBUG.log("native open ok by "+device.getAbsolutePath() + "; mFd="+mFd);
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream()
    {
        return mFileInputStream;
    }

    public OutputStream getOutputStream()
    {
        return mFileOutputStream;
    }

    public void close1() {
        close();
        mFd = null;
    }
    // JNI
    private native static FileDescriptor open(String path, int baudrate,
                                              int flags);

    private native void close();

    static
    {
        try
        {
            System.loadLibrary("serial_port");
            DEBUG.log("Trying to load serial_port.so");
        }
        catch(UnsatisfiedLinkError ule)
        {
            DEBUG.log("JIN err = "+ule.getMessage());
        }
    }
}
