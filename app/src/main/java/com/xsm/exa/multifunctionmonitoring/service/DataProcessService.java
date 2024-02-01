package com.xsm.exa.multifunctionmonitoring.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.xsm.exa.multifunctionmonitoring.R;
import com.xsm.exa.multifunctionmonitoring.param.ConfigParam;
import com.xsm.lib.com.http.HttpCallbackListener;
import com.xsm.lib.com.http.HttpUtil;
import com.xsm.lib.com.protocol.ClientAbstract;
import com.xsm.lib.com.protocol.Protobuf;
import com.xsm.lib.com.protocol.ProtobufServer;
import com.xsm.lib.com.protocol.ProtobufServerUart;
import com.xsm.lib.com.protocol.pack.Decoder;
import com.xsm.lib.com.protocol.pack.Encoder;
import com.xsm.lib.util.UserLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class DataProcessService extends Service {

    private static final String CONTENT_TRANSIT_URL = "content://sensorData.provider";
    public static final Uri CONTENT_TRANSIT_URI = Uri.parse(CONTENT_TRANSIT_URL);
    public static UserLog DEBUG = new UserLog("DataProcessService"/*this.getClass().getSimpleName()*/, true);
    /**
     * id不可设置为0,否则不能设置为前台service
     */
    private static final int NOTIFICATION_DOWNLOAD_PROGRESS_ID = 0x1231;
    private boolean isRemove=false;//是否需要移除
    //硬件控制
    private static Context context;
    private static Toast toast = null;
    //串口发送线程
    long uartRxLastTime =  SystemClock.uptimeMillis(); //获取开机以来非睡眠状态持续的毫秒数
    private Thread uartSendThread = new Thread(new Runnable() {
        @Override
        public void run() {
            int step = 0;
            while(uartSendThread!=null){
                try {
                    long now = SystemClock.uptimeMillis(); //获取开机以来非睡眠状态持续的毫秒数
                    ConfigParam.MultiCard.setComTimeout(now - uartRxLastTime >= ConfigParam.getMultiTimeout()); //时间超时状态更新
                    if(now - uartRxLastTime >= ConfigParam.getMultiTimeout()) {
                        DEBUG.log("ConfigParam setComTimeout="+now+"->"+uartRxLastTime+"; timeout="+ConfigParam.getMultiTimeout());
                    }
                    switch(step) {
                        case 0: {
                            //请求读回传感器---参数块1
                            mProtobufServer.sync_write(new Encoder(Protobuf.CMD_KEY_GET, 55){
                                @Override
                                protected void init(int cmd, int pid, Integer status) {
                                    for(int tag=0; tag<18; tag++) {
                                        this.addData(tag, Protobuf.TYPE_UINT32, 0x00);
                                    }
                                }
                            });
                            Thread.sleep(300);
                            //请求读回传感器---参数块3
                            mProtobufServer.sync_write(new Encoder(Protobuf.CMD_KEY_GET,56){
                                @Override
                                protected void init(int cmd, int pid, Integer status) {
                                    for(int tag=18; tag<36; tag++) {
                                        this.addData(tag, Protobuf.TYPE_UINT32, 0x00);
                                    }
                                }
                            });
                            break;
                        }
                        case 1: {
                            //发送控制参数
                            if(ConfigParam.MultiCard.getCtlParamReq()>0) {
                                DEBUG.log("ConfigParam getCtlParamReq="+ConfigParam.MultiCard.getCtlParamReq());
                                mProtobufServer.sync_write(new Encoder(Protobuf.CMD_KEY_SET, 12){
                                    @Override
                                    protected void init(int cmd, int pid, Integer status) {
                                        int tag = 0;
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriFanPwm(0));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriFanPwm(1));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriFanPwm(2));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriFanPwm(3));

                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriRelay(0));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriRelay(1));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriRelay(2));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriRelay(3));

                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriLumPwm(0));
                                    }
                                });
                            }
                            //请求读回控制参数---参数块4
                            else {
                                mProtobufServer.sync_write(new Encoder(Protobuf.CMD_KEY_GET,57){
                                    @Override
                                    protected void init(int cmd, int pid, Integer status) {
                                        for(int tag=36; tag<45; tag++) {
                                            this.addData(tag, Protobuf.TYPE_UINT32, 0x00);
                                        }
                                    }
                                });
                            }
                            break;
                        }
                        case 2: {
                            //发送控制参数
                            if(ConfigParam.MultiCard.getSaveParamReq()>0) {
                                DEBUG.log("ConfigParam getSaveParamReq="+ConfigParam.MultiCard.getCtlParamReq());
                                mProtobufServer.sync_write(new Encoder(Protobuf.CMD_SET, 13){
                                    @Override
                                    protected void init(int cmd, int pid, Integer status) {
                                        int tag = 10;
                                        this.addControl(Protobuf.TAG_ACT_SAVE, ConfigParam.MultiCard.getSaveParamReq()>0);
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitFanPwm(0));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitFanPwm(1));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitFanPwm(2));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitFanPwm(3));

                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitRelay(0));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitRelay(1));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitRelay(2));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitRelay(3));

                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitLumPwm(0));

                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoFanPwm(0));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoFanPwm(1));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoFanPwm(2));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoFanPwm(3));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanTemp(0));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanTemp(1));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanTemp(2));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanTemp(3));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanPwm(0));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanPwm(1));
                                    }
                                });
                                Thread.sleep(300);
                                mProtobufServer.sync_write(new Encoder(Protobuf.CMD_SET, 14){
                                    @Override
                                    protected void init(int cmd, int pid, Integer status) {
                                        int tag = 29;
                                        this.addControl(Protobuf.TAG_ACT_SAVE, ConfigParam.MultiCard.getSaveParamReq()>0);
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanPwm(2));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanPwm(3));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStepTempFanPwm(0));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStepTempFanPwm(1));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStepTempFanPwm(2));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStepTempFanPwm(3));

                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoRelay(0));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoRelay(1));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoRelay(2));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoRelay(3));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelayTemp(0));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelayTemp(1));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelayTemp(2));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelayTemp(3));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelaySomg(0));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelaySomg(1));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelaySomg(2));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelaySomg(3));

                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoLumPwm(0));
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStepSensorLumPwm(0));
                                    }
                                });
                            }
                            //请求读回控制参数---参数块4
                            else {
                                mProtobufServer.sync_write(new Encoder(Protobuf.CMD_GET,58){
                                    @Override
                                    protected void init(int cmd, int pid, Integer status) {
                                        for(int tag=10; tag<29; tag++) {
                                            this.addData(tag, Protobuf.TYPE_UINT32, 0x00);
                                        }
                                    }
                                });
                                Thread.sleep(300);
                                mProtobufServer.sync_write(new Encoder(Protobuf.CMD_GET,59){
                                    @Override
                                    protected void init(int cmd, int pid, Integer status) {
                                        for(int tag=29; tag<49; tag++) {
                                            this.addData(tag, Protobuf.TYPE_UINT32, 0x00);
                                        }
                                    }
                                });
                            }
                            break;
                        }
                        case 3: {
                            //发送控制参数
                            if(ConfigParam.MultiCard.getSaveParamReq()>0) {
                                DEBUG.log("ConfigParam getSaveParamReq="+ConfigParam.MultiCard.getCtlParamReq());
                                mProtobufServer.sync_write(new Encoder(Protobuf.CMD_SET, 15){
                                    @Override
                                    protected void init(int cmd, int pid, Integer status) {
                                        int tag = 49;
                                        this.addControl(Protobuf.TAG_ACT_SAVE, ConfigParam.MultiCard.getSaveParamReq()>0);
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDevInitDegX());
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDevInitDegY());
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDevInitDegZ());

                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getRunMode());
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuFanTempDown());
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuFanTempUp());
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuPowerTempDown());
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuPowerSomgDown());
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuPowerHumDown());
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuSysPowerTime());
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuPowerDegXDown());
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuPowerDegYDown());
                                        this.addData(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuPowerDegZDown());
                                    }
                                });
                            }
                            //请求读回控制参数---参数块5
                            else {
                                mProtobufServer.sync_write(new Encoder(Protobuf.CMD_GET,60){
                                    @Override
                                    protected void init(int cmd, int pid, Integer status) {
                                        for(int tag=49; tag<61; tag++) {
                                            this.addData(tag, Protobuf.TYPE_UINT32, 0x00);
                                        }
                                    }
                                });
                            }
                            break;
                        }
                    }
                    Thread.sleep(300);
                    if(step<3) step++;
                    else step = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            }
        }
    });
    //串口服务器
    private ProtobufServer mProtobufServer = new ProtobufServerUart() {

        @Override
        protected Encoder disposeCmd(int cmd, int pid, Decoder decoder, ClientAbstract client) {
            int error = decoder.decode_control(Protobuf.TAG_STATUS, Protobuf.noError);
            boolean haveData = decoder.data_init();
            long now = SystemClock.uptimeMillis(); //获取开机以来非睡眠状态持续的毫秒数
            if(cmd == Protobuf.CMD_RESPONSE && error==Protobuf.noError) uartRxLastTime = now;
            DataProcessService.DEBUG.log("disposeCmd cmd=" + cmd+"; pid="+pid+"; error="+error+"; haveData="+haveData+"; datalist="+decoder.dataList.size());
            if(cmd == Protobuf.CMD_RESPONSE && error==Protobuf.noError && pid==12 && ConfigParam.MultiCard.getCtlParamReq()>0) {
                ConfigParam.MultiCard.setCtlParamReq(ConfigParam.MultiCard.getCtlParamReq()-1);
                DataProcessService.DEBUG.log("disposeCmd clearCtlParamReq="+ConfigParam.MultiCard.getCtlParamReq());
            }
            if(cmd == Protobuf.CMD_RESPONSE && error==Protobuf.noError && (pid==13||pid==14||pid==15) && ConfigParam.MultiCard.getSaveParamReq()>0) {
                ConfigParam.MultiCard.setSaveParamReq(ConfigParam.MultiCard.getSaveParamReq()-1);
                DataProcessService.DEBUG.log("disposeCmd clearSaveParamReq="+ConfigParam.MultiCard.getSaveParamReq());
            }
            if(cmd == Protobuf.CMD_RESPONSE && error==Protobuf.noError && haveData) {
                int tag = 0;
                if(pid == 55) {
                    tag = 0;
                    ConfigParam.MultiCard.setSensorTemperature(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorTemperature(0)));
                    ConfigParam.MultiCard.setSensorTemperature(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorTemperature(1)));

                    ConfigParam.MultiCard.setSensorHumidity(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorHumidity(0)));
                    ConfigParam.MultiCard.setSensorHumidity(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorHumidity(1)));

                    ConfigParam.MultiCard.setSensorOpenDoor(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorOpenDoor(0)));
                    ConfigParam.MultiCard.setSensorVibrate(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorVibrate(0)));

                    ConfigParam.MultiCard.setSensorFanSpeed(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(0)));
                    ConfigParam.MultiCard.setSensorFanSpeed(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(1)));
                    ConfigParam.MultiCard.setSensorFanSpeed(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(2)));
                    ConfigParam.MultiCard.setSensorFanSpeed(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(3)));
                    ConfigParam.MultiCard.setSensorFanSpeed(4, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(4)));
                    ConfigParam.MultiCard.setSensorFanSpeed(5, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(5)));
                    ConfigParam.MultiCard.setSensorFanSpeed(6, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(6)));
                    ConfigParam.MultiCard.setSensorFanSpeed(7, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(7)));
                    ConfigParam.MultiCard.setSensorFanSpeed(8, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(8)));
                    ConfigParam.MultiCard.setSensorFanSpeed(9, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(9)));
                    ConfigParam.MultiCard.setSensorFanSpeed(10, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(10)));
                    ConfigParam.MultiCard.setSensorFanSpeed(11, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(11)));
                }
                else if(pid == 56) {
                    tag = 18;
                    ConfigParam.MultiCard.setSensorFanSpeed(12, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(12)));
                    ConfigParam.MultiCard.setSensorFanSpeed(13, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(13)));
                    ConfigParam.MultiCard.setSensorFanSpeed(14, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(14)));
                    ConfigParam.MultiCard.setSensorFanSpeed(15, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorFanSpeed(15)));

                    ConfigParam.MultiCard.setSensorLevelAngleX(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorLevelAngleX(0)));
                    ConfigParam.MultiCard.setSensorLevelAngleY(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorLevelAngleY(0)));
                    ConfigParam.MultiCard.setSensorLevelAngleZ(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorLevelAngleZ(0)));

                    ConfigParam.MultiCard.setSensorVoltage(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorVoltage(0)));
                    ConfigParam.MultiCard.setSensorVoltage(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorVoltage(1)));
                    ConfigParam.MultiCard.setSensorVoltage(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorVoltage(2)));
                    ConfigParam.MultiCard.setSensorVoltage(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorVoltage(3)));
                    ConfigParam.MultiCard.setSensorVoltage(4, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorVoltage(4)));
                    ConfigParam.MultiCard.setSensorVoltage(5, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorVoltage(5)));

                    ConfigParam.MultiCard.setSensorDiffPressure(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorDiffPressure(0)));
                    ConfigParam.MultiCard.setSensorDiffPressure(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorDiffPressure(1)));

                    ConfigParam.MultiCard.setSensorSmog(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorSmog(0)));
                    ConfigParam.MultiCard.setSensorLum(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorLum(0)));
                    ConfigParam.MultiCard.setSensorLum(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getSensorLum(1)));
                }
                else if(pid == 57) {
                    tag = 36;
                    ConfigParam.MultiCard.setDriFanPwm(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriFanPwm(0)));
                    ConfigParam.MultiCard.setDriFanPwm(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriFanPwm(1)));
                    ConfigParam.MultiCard.setDriFanPwm(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriFanPwm(2)));
                    ConfigParam.MultiCard.setDriFanPwm(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriFanPwm(3)));

                    ConfigParam.MultiCard.setDriRelay(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriRelay(0)));
                    ConfigParam.MultiCard.setDriRelay(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriRelay(1)));
                    ConfigParam.MultiCard.setDriRelay(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriRelay(2)));
                    ConfigParam.MultiCard.setDriRelay(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriRelay(3)));

                    ConfigParam.MultiCard.setDriLumPwm(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDriLumPwm(0)));

//                    DataProcessService.DEBUG.log("disposeCmd getDriFanPwm=["+ConfigParam.MultiCard.getDriFanPwm(0)+"]"+
//                            "["+ConfigParam.MultiCard.getDriFanPwm(1)+"]"+
//                            "["+ConfigParam.MultiCard.getDriFanPwm(2)+"]"+
//                            "["+ConfigParam.MultiCard.getDriFanPwm(3)+"]");
//                    DataProcessService.DEBUG.log("disposeCmd getDriRelay=["+ConfigParam.MultiCard.getDriRelay(0)+"]"+
//                            "["+ConfigParam.MultiCard.getDriRelay(1)+"]"+
//                            "["+ConfigParam.MultiCard.getDriRelay(2)+"]"+
//                            "["+ConfigParam.MultiCard.getDriRelay(3)+"]");
//                    DataProcessService.DEBUG.log("disposeCmd getDriLumPwm=["+ConfigParam.MultiCard.getDriLumPwm(0)+"]");
                }
                else if(pid == 58) {
                    tag = 10;
                    ConfigParam.MultiCard.setInitFanPwm(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitFanPwm(0)));
                    ConfigParam.MultiCard.setInitFanPwm(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitFanPwm(1)));
                    ConfigParam.MultiCard.setInitFanPwm(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitFanPwm(2)));
                    ConfigParam.MultiCard.setInitFanPwm(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitFanPwm(3)));

                    ConfigParam.MultiCard.setInitRelay(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitRelay(0)));
                    ConfigParam.MultiCard.setInitRelay(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitRelay(1)));
                    ConfigParam.MultiCard.setInitRelay(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitRelay(2)));
                    ConfigParam.MultiCard.setInitRelay(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitRelay(3)));

                    ConfigParam.MultiCard.setInitLumPwm(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getInitLumPwm(0)));

                    ConfigParam.MultiCard.setAutoFanPwm(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoFanPwm(0)));
                    ConfigParam.MultiCard.setAutoFanPwm(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoFanPwm(1)));
                    ConfigParam.MultiCard.setAutoFanPwm(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoFanPwm(2)));
                    ConfigParam.MultiCard.setAutoFanPwm(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoFanPwm(3)));
                    ConfigParam.MultiCard.setStartFanTemp(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanTemp(0)));
                    ConfigParam.MultiCard.setStartFanTemp(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanTemp(1)));
                    ConfigParam.MultiCard.setStartFanTemp(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanTemp(2)));
                    ConfigParam.MultiCard.setStartFanTemp(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanTemp(3)));
                    ConfigParam.MultiCard.setStartFanPwm(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanPwm(0)));
                    ConfigParam.MultiCard.setStartFanPwm(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanPwm(1)));
                }
                else if(pid == 59) {
                    tag = 29;
                    ConfigParam.MultiCard.setStartFanPwm(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanPwm(2)));
                    ConfigParam.MultiCard.setStartFanPwm(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStartFanPwm(3)));

                    ConfigParam.MultiCard.setStepTempFanPwm(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStepTempFanPwm(0)));
                    ConfigParam.MultiCard.setStepTempFanPwm(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStepTempFanPwm(1)));
                    ConfigParam.MultiCard.setStepTempFanPwm(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStepTempFanPwm(2)));
                    ConfigParam.MultiCard.setStepTempFanPwm(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStepTempFanPwm(3)));

                    ConfigParam.MultiCard.setAutoRelay(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoRelay(0)));
                    ConfigParam.MultiCard.setAutoRelay(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoRelay(1)));
                    ConfigParam.MultiCard.setAutoRelay(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoRelay(2)));
                    ConfigParam.MultiCard.setAutoRelay(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoRelay(3)));
                    ConfigParam.MultiCard.setCloseRelayTemp(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelayTemp(0)));
                    ConfigParam.MultiCard.setCloseRelayTemp(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelayTemp(1)));
                    ConfigParam.MultiCard.setCloseRelayTemp(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelayTemp(2)));
                    ConfigParam.MultiCard.setCloseRelayTemp(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelayTemp(3)));
                    ConfigParam.MultiCard.setCloseRelaySomg(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelaySomg(0)));
                    ConfigParam.MultiCard.setCloseRelaySomg(1, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelaySomg(1)));
                    ConfigParam.MultiCard.setCloseRelaySomg(2, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelaySomg(2)));
                    ConfigParam.MultiCard.setCloseRelaySomg(3, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getCloseRelaySomg(3)));

                    ConfigParam.MultiCard.setAutoLumPwm(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAutoLumPwm(0)));
                    ConfigParam.MultiCard.setStepSensorLumPwm(0, decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getStepSensorLumPwm(0)));
                }
                else if(pid == 60) {
                    tag = 49;
                    ConfigParam.MultiCard.setDevInitDegX(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDevInitDegX()));
                    ConfigParam.MultiCard.setDevInitDegY(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDevInitDegY()));
                    ConfigParam.MultiCard.setDevInitDegZ(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getDevInitDegZ()));
                    ConfigParam.MultiCard.setRunMode(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getRunMode()));
                    ConfigParam.MultiCard.setAuFanTempDown(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuFanTempDown()));
                    ConfigParam.MultiCard.setAuFanTempUp(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuFanTempUp()));
                    ConfigParam.MultiCard.setAuPowerTempDown(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuPowerTempDown()));
                    ConfigParam.MultiCard.setAuPowerSomgDown(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuPowerSomgDown()));
                    ConfigParam.MultiCard.setAuPowerHumDown(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuPowerHumDown()));
                    ConfigParam.MultiCard.setAuSysPowerTime(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuSysPowerTime()));
                    ConfigParam.MultiCard.setAuPowerDegXDown(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuPowerDegXDown()));
                    ConfigParam.MultiCard.setAuPowerDegYDown(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuPowerDegYDown()));
                    ConfigParam.MultiCard.setAuPowerDegZDown(decoder.decode_data(tag++, Protobuf.TYPE_UINT32, ConfigParam.MultiCard.getAuPowerDegZDown()));
                }
            }
            return null;
        }
    };
    //http发送线程
    private Thread httpSendThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(httpSendThread!=null){
                try {
                    Thread.sleep(ConfigParam.getHttpReqDelay());
                    ContentValues values = new ContentValues();
                    values.put("sensorData", ConfigParam.MultiCard.toString());
                    getApplicationContext().getContentResolver().insert(CONTENT_TRANSIT_URI, values);

                    Log.d("TAG", "feeding content provider");
                    Log.e("Tag","in httpSendThread"+ ConfigParam.MultiCard.toString());
                    HttpUtil.sendHttpPostRequest(ConfigParam.getHttpReqUrl(), ConfigParam.MultiCard.toString(), new HttpCallbackListener() {
                        @Override
                        public void onFinish(String response) {
                         //   DataProcessService.DEBUG.log("HttpCallback onFinish=" + response);
                            try {
                                ConfigParam.MultiCard.parse(new JSONObject(response));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                        //    DataProcessService.DEBUG.log("HttpCallback onError=" + e.getMessage());
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                }
            }
        }
    });

    public static void showToast(String s) {
        if (toast == null) {
            toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast.setText(s);
            toast.show();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        try {
            PackageInfo packinfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(packinfo.lastUpdateTime);
            String info = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH) + " " +
                    c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
            DEBUG.log("start \"" + this.getPackageName() + "\" version:" + packinfo.versionName +
                    "; versionCode:" + packinfo.versionCode +
                    "; lastUpdateTime:" + info);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //初始化参数配置
        ConfigParam.init(this);
        //线程启动
        mProtobufServer.start();
        uartSendThread.start();
        httpSendThread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        DEBUG.log("service is onBind");
        // TODO: Return the communication channel to the service.
        //     throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onDestroy() {
        DEBUG.log("service is onDestroy");
        if(uartSendThread!=null) {
            uartSendThread.interrupt();
        }
        if(httpSendThread!=null) {
            httpSendThread.interrupt();
        }
        if(mProtobufServer!=null) mProtobufServer.stop();
        ConfigParam.destroy(context);
        //移除前台服务
        if (isRemove) {
            stopForeground(true);
        }
        isRemove=false;
        super.onDestroy(); // 可以不用
    }

    /**
     * Notification
     */
    public void createNotification(){
        //使用兼容版本
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        //设置状态栏的通知图标
        builder.setSmallIcon(R.drawable.ic_logo);
        //设置通知栏横条的图标
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_service));
        //禁止用户点击删除按钮删除
        builder.setAutoCancel(false);
        //禁止滑动删除
        builder.setOngoing(true);
        //右上角的时间显示
        builder.setShowWhen(true);
        //设置通知栏的标题内容
        builder.setContentTitle(this.getClass().getSimpleName());
        // 【适配Android8.0】给NotificationManager对象设置NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channel_id = this.getClass().getSimpleName()+NOTIFICATION_DOWNLOAD_PROGRESS_ID;
            String channel_name = this.getClass().getSimpleName();
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            //【channel_id】唯一id，String类型；
            //【channel_name】对用户可见的channel名称，String类型；
            //【importance_level】通知的重要程度。
            //
            //importance_level主要有七种层次：
            //
            //IMPORTANCE_NONE：      (0)关闭通知。在任何地方都不会显示，被阻塞
            //IMPORTANCE_MIN：          (1)开启通知。不弹出，不发提示音，状态栏中无显示
            //IMPORTANCE_LOW：        (2)开启通知。不弹出，不发提示音，状态栏中显示
            //IMPORTANCE_DEFAULT：(3)开启通知。不弹出，发出提示音，状态栏中显示【默认】
            //IMPORTANCE_HIGH：       (4)开启通知。会弹出，发出提示音，状态栏中显示
            //
            //IMPORTANCE_MAX：        (5)开启通知。会弹出，发出提示音，可以使用full screen intents(比如来电)。重要程度最高
            //IMPORTANCE_UNSPECIFIED：(-1000)表示用户未设重要值。该值是为了持久的首选项，且永不应该与实际通知相关联
            NotificationChannel channel = new NotificationChannel(
                    channel_id,
                    channel_name,
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
            // 【适配Android8.0】设置Notification的Channel_ID,否则不能正常显示
            builder.setChannelId(channel_id);
        }
        //创建通知->设置为前台服务
        startForeground(NOTIFICATION_DOWNLOAD_PROGRESS_ID, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isRemove) {
            createNotification();
        }
        isRemove=true;
//        int i=intent.getExtras().getInt("cmd");
//        if(i==0){
//            if(!isRemove) {
//                createNotification();
//            }
//            isRemove=true;
//        }else {
//            //移除前台服务
//            if (isRemove) {
//                stopForeground(true);
//            }
//            isRemove=false;
//        }
        //   return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}
