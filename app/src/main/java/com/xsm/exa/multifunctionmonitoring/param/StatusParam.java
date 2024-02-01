package com.xsm.exa.multifunctionmonitoring.param;

import com.xsm.lib.util.json.JsonAbstract;

public class StatusParam extends JsonAbstract {
    //传感器列表
    private int[] SensorTemperature = new int[2];//温度,单位0.001
    private int[] SensorHumidity = new int[2]; //湿度,单位0.001
    private int[] SensorOpenDoor = new int[1]; //开门
    private int[] SensorVibrate = new int[1]; //振动
    private int[] SensorFanSpeed = new int[16]; //风扇转速,单位(转/分)
    private int[] SensorLevelAngleX = new int[1];//水平角度X,单位(度)
    private int[] SensorLevelAngleY = new int[1];//水平角度Y,单位(度)
    private int[] SensorLevelAngleZ = new int[1];//水平角度Z,单位(度)
    private int[] SensorVoltage = new int[6];//电压检测,单位(mv)
    private int[] SensorDiffPressure = new int[2];//压强差
    private int[] SensorSmog = new int[1];//烟雾传感器,单位(ppm)
    private int[] SensorLum = new int[2];//亮度传感器,单位(0~255)
    //控制列表
    private int[] DriFanPwm = new int[4]; //风扇PWM驱动(0~63)
    private int[] DriRelay= new int[4]; //继电器(0~1)
    private int[] DriLumPwm = new int[1]; //风扇PWM驱动(0~63)
    //参数列表--上电初始化参数
    private int[] InitFanPwm = new int[4]; //风扇PWM驱动(0~63)
    private int[] InitRelay= new int[4]; //继电器(0~1)
    private int[] InitLumPwm = new int[1]; //风扇PWM驱动(0~63)
    //参数列表--自动控制
    private int[] AutoFanPwm = new int[4]; //是否开启自动控制
    private int[] StartFanTemp = new int[4]; //风扇自动启动的温度值
    private int[] StartFanPwm = new int[4]; //风扇自动启动的初始化PWM值(0~63)
    private int[] StepTempFanPwm = new int[4]; //每升高一度对应增加的PWM值(0~63)

    private int[] AutoRelay = new int[4]; //是否开启自动控制
    private int[] CloseRelayTemp = new int[4]; //设备自动关断的温度值
    private int[] CloseRelaySomg = new int[4]; //设备自动关断的烟雾值

    private int[] AutoLumPwm = new int[1]; //是否开启自动控制
    private int[] StepSensorLumPwm = new int[1]; //传感器每增加1对应增加的PWM值(0~63)
    //设备原始安装坐标参灵敏
    private int DevInitDegX = 0;
    private int DevInitDegY = 0;
    private int DevInitDegZ = 0;
    //自动运行配置
    private int RunMode = 0;
    private int AuFanTempDown = 0; //风扇关闭的温度值
    private int AuFanTempUp = 0; //风扇启动的温度值
    private int AuPowerTempDown = 0; //电源关闭的温度值
    private int AuPowerSomgDown = 0; //电源关闭的烟雾值
    private int AuPowerHumDown = 0; //电源关闭的温度值
    private int AuSysPowerTime = 0; //系统关闭后重新开打的时间
    //电源关闭的从标偏差
    private int AuPowerDegXDown = 0;
    private int AuPowerDegYDown = 0;
    private int AuPowerDegZDown = 0;

    //运行状态
    private boolean ComTimeout = false; //通讯超时
    private int SaveParamReq = 0; //保存参数请求,每发送一次减1，为0后表示不再需要操作
    private int CtlParamReq = 0; //控制参数请求,每发送一次减1，为0后表示不再需要操作

    public int getCtlParamReq() {
        return CtlParamReq;
    }

    public void setCtlParamReq(int ctlParamReq) {
        CtlParamReq = ctlParamReq;
    }

    public int getAutoFanPwm(int ch) {
        if(ch<AutoFanPwm.length) return AutoFanPwm[ch];
        else return 0;
    }

    public void setAutoFanPwm(int ch, int val) {
        if(ch<AutoFanPwm.length) AutoFanPwm[ch] = val;
    }

    public int getStartFanTemp(int ch) {
        if(ch<StartFanTemp.length) return StartFanTemp[ch];
        else return 0;
    }

    public void setStartFanTemp(int ch, int val) {
        if(ch<StartFanTemp.length) StartFanTemp[ch] = val;
    }

    public int getStartFanPwm(int ch) {
        if(ch<StartFanPwm.length) return StartFanPwm[ch];
        else return 0;
    }

    public void setStartFanPwm(int ch, int val) {
        if(ch<StartFanPwm.length) StartFanPwm[ch] = val;
    }

    public int getStepTempFanPwm(int ch) {
        if(ch<StepTempFanPwm.length) return StepTempFanPwm[ch];
        else return 0;
    }

    public void setStepTempFanPwm(int ch, int val) {
        if(ch<StepTempFanPwm.length) StepTempFanPwm[ch] = val;
    }

    public int getAutoRelay(int ch) {
        if(ch<AutoRelay.length) return AutoRelay[ch];
        else return 0;
    }

    public void setAutoRelay(int ch, int val) {
        if(ch<AutoRelay.length) AutoRelay[ch] = val;
    }

    public int getCloseRelayTemp(int ch) {
        if(ch<CloseRelayTemp.length) return CloseRelayTemp[ch];
        else return 0;
    }

    public void setCloseRelayTemp(int ch, int val) {
        if(ch<CloseRelayTemp.length) CloseRelayTemp[ch] = val;
    }

    public int getCloseRelaySomg(int ch) {
        if(ch<CloseRelaySomg.length) return CloseRelaySomg[ch];
        else return 0;
    }

    public void setCloseRelaySomg(int ch, int val) {
        if(ch<CloseRelaySomg.length) CloseRelaySomg[ch] = val;
    }

    public int getAutoLumPwm(int ch) {
        if(ch<AutoLumPwm.length) return AutoLumPwm[ch];
        else return 0;
    }

    public void setAutoLumPwm(int ch, int val) {
        if(ch<AutoLumPwm.length) AutoLumPwm[ch] = val;
    }

    public int getStepSensorLumPwm(int ch) {
        if(ch<StepSensorLumPwm.length) return StepSensorLumPwm[ch];
        else return 0;
    }

    public void setStepSensorLumPwm(int ch, int val) {
        if(ch<StepSensorLumPwm.length) StepSensorLumPwm[ch] = val;
    }

    public int getSaveParamReq() {
        return SaveParamReq;
    }

    public void setSaveParamReq(int saveParamReq) {
        SaveParamReq = saveParamReq;
    }

    public int getInitFanPwm(int ch) {
        if(ch<InitFanPwm.length) return InitFanPwm[ch];
        else return 0;
    }

    public void setInitFanPwm(int ch, int val) {
        if(ch<InitFanPwm.length) InitFanPwm[ch] = val;
    }

    public int getInitRelay(int ch) {
        if(ch<InitRelay.length) return InitRelay[ch];
        else return 0;
    }

    public void setInitRelay(int ch, int val) {
        if(ch<InitRelay.length) InitRelay[ch] = val;
    }

    public int getInitLumPwm(int ch) {
        if(ch<InitLumPwm.length) return InitLumPwm[ch];
        else return 0;
    }

    public void setInitLumPwm(int ch, int val) {
        if(ch<InitLumPwm.length) InitLumPwm[ch] = val;
    }

    public boolean isComTimeout() {
        return ComTimeout;
    }

    public void setComTimeout(boolean comTimeout) {
        ComTimeout = comTimeout;
        if(comTimeout) { //通讯超时后重置通讯请求
        //    SaveParamReq = 1;
        //    CtlParamReq = 1;
        }
    }

    public int getSensorTemperature(int ch) {
        if(ch<SensorTemperature.length) return SensorTemperature[ch];
        else return 0;
    }

    public void setSensorTemperature(int ch, int val) {
        if(ch<SensorTemperature.length) SensorTemperature[ch] = val;
    }

    public int getSensorHumidity(int ch) {
        if(ch<SensorHumidity.length) return SensorHumidity[ch];
        else return 0;
    }

    public void setSensorHumidity(int ch, int val) {
        if(ch<SensorHumidity.length) SensorHumidity[ch] = val;
    }

    public int getSensorOpenDoor(int ch) {
        if(ch<SensorOpenDoor.length) return SensorOpenDoor[ch];
        else return 0;
    }

    public void setSensorOpenDoor(int ch, int val) {
        if(ch<SensorOpenDoor.length) SensorOpenDoor[ch] = val;
    }

    public int getSensorVibrate(int ch) {
        if(ch<SensorVibrate.length) return SensorVibrate[ch];
        else return 0;
    }

    public void setSensorVibrate(int ch, int val) {
        if(ch<SensorVibrate.length) SensorVibrate[ch] = val;
    }

    public int getSensorFanSpeed(int ch) {
        if(ch<SensorFanSpeed.length) return SensorFanSpeed[ch];
        else return 0;
    }

    public void setSensorFanSpeed(int ch, int val) {
        if(ch<SensorFanSpeed.length) SensorFanSpeed[ch] = val;
    }

    public int getSensorLevelAngleX(int ch) {
        if(ch<SensorLevelAngleX.length) return SensorLevelAngleX[ch];
        else return 0;
    }

    public void setSensorLevelAngleX(int ch, int val) {
        if(ch<SensorLevelAngleX.length) SensorLevelAngleX[ch] = val;
    }

    public int getSensorLevelAngleY(int ch) {
        if(ch<SensorLevelAngleY.length) return SensorLevelAngleY[ch];
        else return 0;
    }

    public void setSensorLevelAngleY(int ch, int val) {
        if(ch<SensorLevelAngleY.length) SensorLevelAngleY[ch] = val;
    }

    public int getSensorLevelAngleZ(int ch) {
        if(ch<SensorLevelAngleZ.length) return SensorLevelAngleZ[ch];
        else return 0;
    }

    public void setSensorLevelAngleZ(int ch, int val) {
        if(ch<SensorLevelAngleZ.length) SensorLevelAngleZ[ch] = val;
    }

    public int getSensorVoltage(int ch) {
        if(ch<SensorVoltage.length) return SensorVoltage[ch];
        else return 0;
    }

    public void setSensorVoltage(int ch, int val) {
        if(ch<SensorVoltage.length) SensorVoltage[ch] = val;
    }

    public int getSensorDiffPressure(int ch) {
        if(ch<SensorDiffPressure.length) return SensorDiffPressure[ch];
        else return 0;
    }

    public void setSensorDiffPressure(int ch, int val) {
        if(ch<SensorDiffPressure.length) SensorDiffPressure[ch] = val;
    }

    public int getSensorSmog(int ch) {
        if(ch<SensorSmog.length) return SensorSmog[ch];
        else return 0;
    }

    public void setSensorSmog(int ch, int val) {
        if(ch<SensorSmog.length) SensorSmog[ch] = val;
    }

    public int getSensorLum(int ch) {
        if(ch<SensorLum.length) return SensorLum[ch];
        else return 0;
    }

    public void setSensorLum(int ch, int val) {
        if(ch<SensorLum.length) SensorLum[ch] = val;
    }

    public int getDriFanPwm(int ch) {
    //    DEBUG.log("get pwm "+ch+"/"+DriFanPwm.length+"="+DriFanPwm[ch]);
        if(ch<DriFanPwm.length) return DriFanPwm[ch];
        else return 0;
    }

    public void setDriFanPwm(int ch, int val) {
    //    DEBUG.log("set pwm "+ch+"/"+DriFanPwm.length+"="+val);
        if(ch<DriFanPwm.length) DriFanPwm[ch] = val;
    }

    public int getDriRelay(int ch) {
        if(ch<DriRelay.length) return DriRelay[ch];
        else return 0;
    }

    public void setDriRelay(int ch, int val) {
        if(ch<DriRelay.length) DriRelay[ch] = val;
    }

    public int getDriLumPwm(int ch) {
        if(ch<DriLumPwm.length) return DriLumPwm[ch];
        else return 0;
    }

    public void setDriLumPwm(int ch, int val) {
        if(ch<DriLumPwm.length) DriLumPwm[ch] = val;
    }

    public int getDevInitDegX() {
        return DevInitDegX;
    }

    public void setDevInitDegX(int devInitDegX) {
        DevInitDegX = devInitDegX;
    }

    public int getDevInitDegY() {
        return DevInitDegY;
    }

    public void setDevInitDegY(int devInitDegY) {
        DevInitDegY = devInitDegY;
    }

    public int getDevInitDegZ() {
        return DevInitDegZ;
    }

    public void setDevInitDegZ(int devInitDegZ) {
        DevInitDegZ = devInitDegZ;
    }

    public int getRunMode() {
        return RunMode;
    }

    public void setRunMode(int runMode) {
        RunMode = runMode;
    }

    public int getAuFanTempDown() {
        return AuFanTempDown;
    }

    public void setAuFanTempDown(int auFanTempDown) {
        AuFanTempDown = auFanTempDown;
    }

    public int getAuFanTempUp() {
        return AuFanTempUp;
    }

    public void setAuFanTempUp(int auFanTempUp) {
        AuFanTempUp = auFanTempUp;
    }

    public int getAuPowerTempDown() {
        return AuPowerTempDown;
    }

    public void setAuPowerTempDown(int auPowerTempDown) {
        AuPowerTempDown = auPowerTempDown;
    }

    public int getAuPowerSomgDown() {
        return AuPowerSomgDown;
    }

    public void setAuPowerSomgDown(int auPowerSomgDown) {
        AuPowerSomgDown = auPowerSomgDown;
    }

    public int getAuPowerHumDown() {
        return AuPowerHumDown;
    }

    public void setAuPowerHumDown(int auPowerHumDown) {
        AuPowerHumDown = auPowerHumDown;
    }

    public int getAuSysPowerTime() {
        return AuSysPowerTime;
    }

    public void setAuSysPowerTime(int auSysPowerTime) {
        AuSysPowerTime = auSysPowerTime;
    }

    public int getAuPowerDegXDown() {
        return AuPowerDegXDown;
    }

    public void setAuPowerDegXDown(int auPowerDegXDown) {
        AuPowerDegXDown = auPowerDegXDown;
    }

    public int getAuPowerDegYDown() {
        return AuPowerDegYDown;
    }

    public void setAuPowerDegYDown(int auPowerDegYDown) {
        AuPowerDegYDown = auPowerDegYDown;
    }

    public int getAuPowerDegZDown() {
        return AuPowerDegZDown;
    }

    public void setAuPowerDegZDown(int auPowerDegZDown) {
        AuPowerDegZDown = auPowerDegZDown;
    }
}
