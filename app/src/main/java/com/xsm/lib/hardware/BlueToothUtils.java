package com.xsm.lib.hardware;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.xsm.lib.util.UserLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by shaolin on 5/23/16.
 */
public class BlueToothUtils {
    private UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), true);

//    private static final String TAG = "BlueToothUtils";
    private Context mContext;
    public static BlueToothUtils sInstance;
    private Runnable connectStatusRunnabl = null;
    private BluetoothAdapter mBA;
    // UUID.randomUUID()随机获取UUID
    private final UUID MY_UUID = UUID
            .fromString("db764ac8-4b08-7f25-aafe-59d03c27bae3");
    // 连接对象的名称
    private final String NAME = "LGL";

    // 这里本身即是服务端也是客户端，需要如下类
    private BluetoothSocket mSocket;
    private BluetoothDevice mOldDevice;
    private BluetoothDevice mCurDevice;
    // 输出流_客户端需要往服务端输出
    private OutputStream os;

    //线程类的实例
    private AcceptThread ac;

    public static synchronized BlueToothUtils getInstance() {
        if (sInstance == null) {
            sInstance = new BlueToothUtils();
        }
        return sInstance;
    }

    public static synchronized BlueToothUtils getInstance(Runnable runnable) {
        if (sInstance == null) {
            sInstance = new BlueToothUtils();
            sInstance.connectStatusRunnabl = runnable;
        }
        return sInstance;
    }

    public BlueToothUtils() {
        mBA = BluetoothAdapter.getDefaultAdapter();
        ac = new AcceptThread();
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public BluetoothAdapter getBA() {
        return mBA;
    }

    public AcceptThread getAc() {
        return ac;
    }

    public BluetoothDevice getCurDevice() {
        return mCurDevice;
    }

    /**
     * 判断是否打开蓝牙
     *
     * @return
     */
    public boolean isEnabled() {
        if (mBA.isEnabled()) {
            return true;
        }
        return false;
    }

    public void startBroadcastReceiver(Context context) {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        context.registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(BluetoothAdapter.EXTRA_CONNECTION_STATE);
//        context.registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(BluetoothAdapter.EXTRA_STATE);
//        context.registerReceiver(mReceiver, filter);
    }

    private String bondName = "";
    private BluetoothDevice bondDevice = null;
    /**
     * 搜索设备
     */
    public boolean createBond(Context context, String name) {
        if(!bondName.equals(name) || (bondDevice!=null && !bondDevice.getName().equals(name))) { //新的设备请求
            bondDevice = null;
        }
        if(bondDevice!=null) return true; //请备已连接
        else {
            DEBUG.log( "start createBond by "+name);
            bondName = name;
            if(mBA.isEnabled()) {
                List<BluetoothDevice> list = getBondedDevices();
                for(BluetoothDevice dev : list) {
                    if(dev.getName().equals(name)) {
                        bondDevice = dev;
                        DEBUG.log( "find device in BondedDevices");
                        break;
                    }
                }
                if(bondDevice==null) {
                    startBroadcastReceiver(context);
                    // 判断是否在搜索,如果在搜索，就取消搜索
                    if (mBA.isDiscovering()) {
                        mBA.cancelDiscovery();
                    }
                    // 开始搜索
                    mBA.startDiscovery();
                    DEBUG.log("正在搜索...");
                }
            }
            else {
                mBA.enable(); //启动设备
            }
        }
        return false;
    }

    private int mBluetoothHeadsetState = 0;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            DEBUG.log( "onReceive action->"+action);
            //找到设备
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 搜索到的不是已经配对的蓝牙设备
                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String name = device.getName();
                    DEBUG.log( "find device:"+ device.getName() + "/" + device.getAddress()+"/"+device.getUuids()+"/"+device.getType()+"/"+device.getBluetoothClass());
                    if(name!=null && device.getName().equals(bondName)) {
                        bondDevice = device;
                        DEBUG.log( "device->createBond");
                        createBond(device);
                    }
                }
            }
            //当绑定的状态改变时
            else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 搜索到的不是已经配对的蓝牙设备
                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String name = device.getName();
                    DEBUG.log( "state change device:"+ device.getName() + "/" + device.getAddress()+"/"+device.toString());
                    if(name!=null && device.getName().equals(bondName)) {
                        bondDevice = device;
                        DEBUG.log( "device->createBond");
                        createBond(device);
                    }
                }
            }
            else if(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                if(bondDevice!=null) {
                    DEBUG.log( "connect to device -> "+bondDevice.getName()+"; mac:"+bondDevice.toString());
                }
                if(sInstance.connectStatusRunnabl!=null) sInstance.connectStatusRunnabl.run();
            }
            //搜索完成
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                DEBUG.log("搜索完成");
//                if(mNewDevicesAdapter.getCount() == 0) {
//                    DEBUG.log("find over");
//                }
                if(bondDevice!=null) {
                    DEBUG.log("设备连接成功");
                }
            } //执行更新列表的代码
            else {
                // 检测蓝牙是否开启
                if (mBA.isEnabled()) {
                    DEBUG.log(  "action->"+action+"->open");
                } else{
                    DEBUG.log(  "action->"+action+"->close");
                }
            }
        }
    };

    /**
     * 搜索设备
     */
    public void searchDevices() {
        // 判断是否在搜索,如果在搜索，就取消搜索
        if (mBA.isDiscovering()) {
            mBA.cancelDiscovery();
        }
        // 开始搜索
        mBA.startDiscovery();
        DEBUG.log("正在搜索...");
    }

    /**
     * 获取已经配对的设备
     *
     * @return
     */
    public List<BluetoothDevice> getBondedDevices() {
        List<BluetoothDevice> devices = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = mBA.getBondedDevices();
        // 判断是否有配对过的设备
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devices.add(device);
                DEBUG.log("BondedDevice:" + device.getName());
            }
        }
        return devices;
    }

    /**
     * 与设备配对
     *
     * @param device
     */
    public void createBond(BluetoothDevice device) {
        try {
            Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
            createBondMethod.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 与设备解除配对
     *
     * @param device
     */
    public void removeBond(BluetoothDevice device) {
        try {
            Method removeBondMethod = device.getClass().getMethod("removeBond");
            removeBondMethod.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 与设备解除配对
     *
     */
    public void removeBond() {
        List<BluetoothDevice> list = getBondedDevices();
        DEBUG.log("removeBond" + list.toString());
        for(BluetoothDevice dev : list) {
            removeBond(dev);
        }
    }

    /**
     *
     * @param device
     * @param str  设置PIN码
     * @return
     */
    public boolean setPin(BluetoothDevice device, String str) {
        try {
            Method removeBondMethod = device.getClass().getDeclaredMethod("setPin",
                    new Class[]{byte[].class});
            Boolean returnValue = (Boolean) removeBondMethod.invoke(device,
                    new Object[]{str.getBytes()});
            DEBUG.log("returnValue", "" + returnValue);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 取消用户输入
     */
    public boolean cancelPairingUserInput(BluetoothDevice device) {
        Boolean returnValue = false;
        try {
            Method createBondMethod = device.getClass().getMethod("cancelPairingUserInput");
            returnValue = (Boolean) createBondMethod.invoke(device);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // cancelBondProcess()
        return returnValue.booleanValue();
    }

    /**
     * 取消配对
     */
    public boolean cancelBondProcess(BluetoothDevice device) {
        Boolean returnValue = null;
        try {
            Method createBondMethod = device.getClass().getMethod("cancelBondProcess");
            returnValue = (Boolean) createBondMethod.invoke(device);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return returnValue.booleanValue();
    }

    /**
     * @param strAddr
     * @param strPsw
     * @return
     */
    public boolean pair(String strAddr, String strPsw) {
        boolean result = false;
        mBA.cancelDiscovery();

        if (!mBA.isEnabled()) {
            mBA.enable();
        }

        if (!BluetoothAdapter.checkBluetoothAddress(strAddr)) { // 检查蓝牙地址是否有效
            DEBUG.log( "devAdd un effient!");
        }

        BluetoothDevice device = mBA.getRemoteDevice(strAddr);
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            DEBUG.log("NOT BOND_BONDED");
            try {
                setPin(device, strPsw); // 手机和蓝牙采集器配对
                createBond(device);
                result = true;
            } catch (Exception e) {
                DEBUG.log("setPiN failed!");
                e.printStackTrace();
            } //

        } else {
            DEBUG.log( "HAS BOND_BONDED");
            try {
                createBond(device);
                setPin(device, strPsw); // 手机和蓝牙采集器配对
                createBond(device);
                result = true;
            } catch (Exception e) {
                DEBUG.log("setPiN failed!");
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取device.getClass()这个类中的所有Method
     *
     * @param clsShow
     */
    public void printAllInform(Class clsShow) {
        try {
            // 取得所有方法
            Method[] hideMethod = clsShow.getMethods();
            int i = 0;
            for (; i < hideMethod.length; i++) {
                DEBUG.log("method name->"+hideMethod[i].getName() + ";and the i is:" + i);
            }
            // 取得所有常量
            Field[] allFields = clsShow.getFields();
            for (i = 0; i < allFields.length; i++) {
                DEBUG.log("Field name->"+allFields[i].getName());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开蓝牙
     */
    public void openBlueTooth() {
        if (!mBA.isEnabled()) {
            // 弹出对话框提示用户是后打开
            /*Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);*/
            // 不做提示，强行打开
            mBA.enable();
            showToast("打开蓝牙");
        } else {
            showToast("蓝牙已打开");
        }
    }

    /**
     * 关闭蓝牙
     */
    public void closeBlueTooth() {
        mBA.disable();
        showToast("关闭蓝牙");
    }

    /**
     * 弹出Toast窗口
     *
     * @param message
     */
    private void showToast(String message) {
        if (mContext != null) {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        } else {
            DEBUG.log("message:" + message);
        }
    }

    /**
     * 主动连接蓝牙
     *
     * @param device
     */
    public void connectDevice(BluetoothDevice device) {
        // 判断是否在搜索,如果在搜索，就取消搜索
        if (mBA.isDiscovering()) {
            mBA.cancelDiscovery();
        }
        try {
            // 获得远程设备
            if (mCurDevice == null || mCurDevice != mOldDevice) {
                mCurDevice = mBA.getRemoteDevice(device.getAddress());
                mOldDevice = mCurDevice;
                DEBUG.log("device:" + mCurDevice);
                mSocket = mCurDevice.createRfcommSocketToServiceRecord(MY_UUID);
                // 连接
                mSocket.connect();
                // 获得输出流
                os = mSocket.getOutputStream();
            }
            // 如果成功获得输出流

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 传输数据
     *
     * @param message
     */
    public void write(String message) {
        try {
            if (os != null) {
                os.write(message.getBytes("GBK"));
            }
            DEBUG.log("write:" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 服务端，需要监听客户端的线程类
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            showToast(String.valueOf(msg.obj));
            DEBUG.log("服务端:" + msg.obj);
            super.handleMessage(msg);
        }
    };


    // 线程服务类
    public class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;
        private BluetoothSocket socket;
        // 输入 输出流
        private OutputStream os;
        private InputStream is;

        public AcceptThread() {
            try {
                serverSocket = mBA.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            // 截获客户端的蓝牙消息
            try {
                socket = serverSocket.accept(); // 如果阻塞了，就会一直停留在这里
                is = socket.getInputStream();
                os = socket.getOutputStream();
                while (true) {
                    synchronized (this) {
                        byte[] tt = new byte[is.available()];
                        if (tt.length > 0) {
                            is.read(tt, 0, tt.length);
                            Message msg = new Message();
                            msg.obj = new String(tt, "GBK");
                            DEBUG.log("客户端:" + msg.obj);
                            handler.sendMessage(msg);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
