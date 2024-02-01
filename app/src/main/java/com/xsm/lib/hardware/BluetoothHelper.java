package com.xsm.lib.hardware;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.xsm.lib.util.UserLog;

import java.util.ArrayList;
import java.util.List;

//    <!--使用蓝牙所需要的权限-->
//    <uses-permission android:name="android.permission.BLUETOOTH"/>
//    <!--使用扫描和设置蓝牙的权限（要使用这一个权限必须申明BLUETOOTH权限）-->
//    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
//    <!--
//    在 Android 6.0 及以上，还需要打开位置权限。
//    如果应用没有位置权限，蓝牙扫描功能不能使用（其它蓝牙操作例如连接蓝牙设备和写入数据不受影响）。
//    -->
//    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
//    /**
//     * 获取基于地理位置的动态权限
//     */
//    private void bluetoothPermissions() {
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{
//                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
//        }
//    }

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothHelper {
    private UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), true);

    private Context context;
    private final BluetoothManager bluetoothManager;
    private final BluetoothAdapter mBluetoothAdapter;

    public BluetoothHelper(Context context) {
        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
     //   mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean setBluetoothState(boolean key) {
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if(key) { // 检测蓝牙是否开启
            if(!bt.isEnabled()) bt.enable();
            DEBUG.log("bluetooth is open");
        }
        else {
            if(bt.isEnabled()) bt.disable();
            DEBUG.log("bluetooth is close");
        }
        return (bt.isEnabled()==key);
    }
    /**
     * 扫描蓝牙设备
     */
    public void scanLeDevice(final String name) {
        DEBUG.log("scanLeDevice->"+name);
        mBluetoothAdapter.startDiscovery();
        mBluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                DEBUG.log("onLeScan ->"+device.getName());
                if(device.getName().equals(name)) {
                    mBluetoothAdapter.stopLeScan(null);
                    mBluetoothGatt = device.connectGatt(context, true, mGattCallback);
                }
            }
        });
    }


    private List<BluetoothDevice> deviceList = new ArrayList<>();
    /**
     * 扫描蓝牙设备
     * @param enable
     */
    public void scanLeDevice(final boolean enable) {
        DEBUG.log("scanLeDevice->"+enable);
        DEBUG.log("local addr="+mBluetoothAdapter.getAddress()+"; " +
                "local name="+mBluetoothAdapter.getName()+"; " +
                "isDiscovering="+mBluetoothAdapter.isDiscovering()+"; " +
                "local getState="+mBluetoothAdapter.getState());
        for(BluetoothDevice dev :mBluetoothAdapter.getBondedDevices()) {
            DEBUG.log("dev->"+dev.toString());
        }
        mBluetoothAdapter.startDiscovery();
        if (enable) {
            deviceList.clear();
            // 预先定义停止蓝牙扫描的时间（因为蓝牙扫描需要消耗较多的电量）
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                }
//            }, SCAN_PERIOD);

            // 定义一个回调接口供扫描结束处理
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
    final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        /**
         * 一旦发现蓝牙设备，LeScanCallback 就会被回调，直到 stopLeScan 被调用。
         * 出现在回调中的设备会重复出现，所以如果我们需要通过 BluetoothDevice 获取外围设备的地址手动过滤掉已经发现的外围设备。
         *
         * @param device 蓝牙设备的类，可以通过这个类建立蓝牙连接获取关于这一个设备的一系列详细的参数，例如名字，MAC 地址等等；
         * @param rssi 蓝牙的信号强弱指标，通过蓝牙的信号指标，我们可以大概计算出蓝牙设备离手机的距离。计算公式为：d = 10^((abs(RSSI) - A) / (10 * n))
         * @param scanRecord 蓝牙广播出来的广告数据。
         */
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            DEBUG.log("onLeScan ->"+device.toString());
            //重复过滤方法，列表中包含不该设备才加入列表中，并刷新列表
            if (!deviceList.contains(device)) {
                //将设备加入列表数据中
                deviceList.add(device);
            }
        }
    };

    private BluetoothGatt mBluetoothGatt;
    /**
     * 连接蓝牙
     * @param index
     */
    public void connectBluetoothDevice(int index) {
        BluetoothDevice bluetoothDevice = deviceList.get(index);
        DEBUG.log(bluetoothDevice.getAddress());
        /**
         * autoConnect 是否需要自动连接。true, 表示如果设备断开了，会不断的尝试自动连接。false, 表示只进行一次连接尝试。
         * mGattCallback 连接后进行的一系列操作的回调类。
         */
        mBluetoothGatt = bluetoothDevice.connectGatt(context, true, mGattCallback);
    }

    /**
     * 连接蓝牙
     * @param dev
     */
    public void connectBluetoothDevice(BluetoothDevice dev) {
        DEBUG.log(dev.getAddress());
        /**
         * autoConnect 是否需要自动连接。true, 表示如果设备断开了，会不断的尝试自动连接。false, 表示只进行一次连接尝试。
         * mGattCallback 连接后进行的一系列操作的回调类。
         */
        mBluetoothGatt = dev.connectGatt(context, true, mGattCallback);
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * 连接状态改变
         *
         * @param gatt 蓝牙设备的 Gatt 服务连接类
         * @param status 代表是否成功执行了连接操作，
         *               如果为 BluetoothGatt.GATT_SUCCESS 表示成功执行连接操作，
         *               第三个参数才有效，否则说明这次连接尝试不成功
         * @param newState 代表当前设备的连接状态，
         *                 如果 newState == BluetoothProfile.STATE_CONNECTED 说明设备已经连接，
         *                 可以进行下一步的操作了（发现蓝牙服务，也就是 Service）。
         *                 当蓝牙设备断开连接时，这一个方法也会被回调，
         *                 其中的 newState == BluetoothProfile.STATE_DISCONNECTED
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            DEBUG.log("连接状态:" + newState);
            if (BluetoothGatt.STATE_CONNECTED == newState) {
                DEBUG.log("连接成功:");
                //必须有，可以让onServicesDiscovered显示所有Services
                gatt.discoverServices();
            } else if (BluetoothGatt.STATE_DISCONNECTED == newState) {
                DEBUG.log("断开连接:");
            }
        }

        /**
         * 发现服务，在蓝牙连接的时候会调用
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
            if(mBluetoothGatt!=null) {
                List<BluetoothGattService> list = mBluetoothGatt.getServices();
                for (BluetoothGattService bluetoothGattService : list) {
                    String str = bluetoothGattService.getUuid().toString();
                    DEBUG.log(" BluetoothGattService：" + str);
                    List<BluetoothGattCharacteristic> gattCharacteristics = bluetoothGattService
                            .getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        DEBUG.log(" BluetoothGattCharacteristic：" + gattCharacteristic.getUuid());
                        if ("00002a19-0000-1000-8000-00805f9b34fb".equals(gattCharacteristic.getUuid().toString())) {
                            BluetoothGattCharacteristic alertLevel = gattCharacteristic;
                            DEBUG.log(alertLevel.getUuid().toString());
                            gatt.readCharacteristic(alertLevel);
                        }
                    }
                }
            }
        }

        /**
         * 读取数据
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic,
                                         final int status) {

            DEBUG.log("callback characteristic read status " + status
                    + " characteristic " + characteristic);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                DEBUG.log( "read value: " + characteristic.getValue());
            }

        }

    };

}
