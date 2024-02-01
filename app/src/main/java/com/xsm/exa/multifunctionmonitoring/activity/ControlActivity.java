package com.xsm.exa.multifunctionmonitoring.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.xsm.exa.multifunctionmonitoring.R;
import com.xsm.exa.multifunctionmonitoring.param.ConfigParam;
import com.xsm.lib.util.UserLog;

public class ControlActivity  extends AppCompatActivity implements View.OnClickListener {
    protected UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Control");
        setContentView(R.layout.activity_control);
        initView();
        uiUpdate();
    }

    public void initView(){
        findViewById(R.id.button_get).setOnClickListener(this);
        findViewById(R.id.button_set).setOnClickListener(this);
    }

    //刷新页面的内容
    public void uiUpdate() {
        //参数列表--上电初始化参数
        ((EditText)findViewById(R.id.editTextNumber_InitFanPwm0)).setText(String.valueOf(ConfigParam.MultiCard.getInitFanPwm(0)));
        ((EditText)findViewById(R.id.editTextNumber_InitFanPwm1)).setText(String.valueOf(ConfigParam.MultiCard.getInitFanPwm(1)));
        ((EditText)findViewById(R.id.editTextNumber_InitFanPwm2)).setText(String.valueOf(ConfigParam.MultiCard.getInitFanPwm(2)));
        ((EditText)findViewById(R.id.editTextNumber_InitFanPwm3)).setText(String.valueOf(ConfigParam.MultiCard.getInitFanPwm(3)));

        ((CheckBox)findViewById(R.id.checkBox_InitRelay0)).setChecked(ConfigParam.MultiCard.getInitRelay(0)!=0);
        ((CheckBox)findViewById(R.id.checkBox_InitRelay1)).setChecked(ConfigParam.MultiCard.getInitRelay(1)!=0);
        ((CheckBox)findViewById(R.id.checkBox_InitRelay2)).setChecked(ConfigParam.MultiCard.getInitRelay(2)!=0);
        ((CheckBox)findViewById(R.id.checkBox_InitRelay3)).setChecked(ConfigParam.MultiCard.getInitRelay(3)!=0);

        ((EditText)findViewById(R.id.editTextNumber_InitLumPwm0)).setText(String.valueOf(ConfigParam.MultiCard.getInitLumPwm(0)));
        //参数列表--自动控制
        ((CheckBox)findViewById(R.id.checkBox_AutoFanPwm0)).setChecked(ConfigParam.MultiCard.getAutoFanPwm(0)!=0);
        ((CheckBox)findViewById(R.id.checkBox_AutoFanPwm1)).setChecked(ConfigParam.MultiCard.getAutoFanPwm(1)!=0);
        ((CheckBox)findViewById(R.id.checkBox_AutoFanPwm2)).setChecked(ConfigParam.MultiCard.getAutoFanPwm(2)!=0);
        ((CheckBox)findViewById(R.id.checkBox_AutoFanPwm3)).setChecked(ConfigParam.MultiCard.getAutoFanPwm(3)!=0);
        ((EditText)findViewById(R.id.editTextNumber_StartFanTemp0)).setText(String.valueOf(ConfigParam.MultiCard.getStartFanTemp(0)));
        ((EditText)findViewById(R.id.editTextNumber_StartFanTemp1)).setText(String.valueOf(ConfigParam.MultiCard.getStartFanTemp(1)));
        ((EditText)findViewById(R.id.editTextNumber_StartFanTemp2)).setText(String.valueOf(ConfigParam.MultiCard.getStartFanTemp(2)));
        ((EditText)findViewById(R.id.editTextNumber_StartFanTemp3)).setText(String.valueOf(ConfigParam.MultiCard.getStartFanTemp(3)));
        ((EditText)findViewById(R.id.editTextNumber_StartFanPwm0)).setText(String.valueOf(ConfigParam.MultiCard.getStartFanPwm(0)));
        ((EditText)findViewById(R.id.editTextNumber_StartFanPwm1)).setText(String.valueOf(ConfigParam.MultiCard.getStartFanPwm(1)));
        ((EditText)findViewById(R.id.editTextNumber_StartFanPwm2)).setText(String.valueOf(ConfigParam.MultiCard.getStartFanPwm(2)));
        ((EditText)findViewById(R.id.editTextNumber_StartFanPwm3)).setText(String.valueOf(ConfigParam.MultiCard.getStartFanPwm(3)));
        ((EditText)findViewById(R.id.editTextNumber_StepTempFanPwm0)).setText(String.valueOf(ConfigParam.MultiCard.getStepTempFanPwm(0)));
        ((EditText)findViewById(R.id.editTextNumber_StepTempFanPwm1)).setText(String.valueOf(ConfigParam.MultiCard.getStepTempFanPwm(1)));
        ((EditText)findViewById(R.id.editTextNumber_StepTempFanPwm2)).setText(String.valueOf(ConfigParam.MultiCard.getStepTempFanPwm(2)));
        ((EditText)findViewById(R.id.editTextNumber_StepTempFanPwm3)).setText(String.valueOf(ConfigParam.MultiCard.getStepTempFanPwm(3)));

        ((CheckBox)findViewById(R.id.checkBox_AutoRelay0)).setChecked(ConfigParam.MultiCard.getAutoRelay(0)!=0);
        ((CheckBox)findViewById(R.id.checkBox_AutoRelay1)).setChecked(ConfigParam.MultiCard.getAutoRelay(1)!=0);
        ((CheckBox)findViewById(R.id.checkBox_AutoRelay2)).setChecked(ConfigParam.MultiCard.getAutoRelay(2)!=0);
        ((CheckBox)findViewById(R.id.checkBox_AutoRelay3)).setChecked(ConfigParam.MultiCard.getAutoRelay(3)!=0);
        ((EditText)findViewById(R.id.editTextNumber_CloseRelayTemp0)).setText(String.valueOf(ConfigParam.MultiCard.getCloseRelayTemp(0)));
        ((EditText)findViewById(R.id.editTextNumber_CloseRelayTemp1)).setText(String.valueOf(ConfigParam.MultiCard.getCloseRelayTemp(1)));
        ((EditText)findViewById(R.id.editTextNumber_CloseRelayTemp2)).setText(String.valueOf(ConfigParam.MultiCard.getCloseRelayTemp(2)));
        ((EditText)findViewById(R.id.editTextNumber_CloseRelayTemp3)).setText(String.valueOf(ConfigParam.MultiCard.getCloseRelayTemp(3)));
        ((EditText)findViewById(R.id.editTextNumber_CloseRelaySomg0)).setText(String.valueOf(ConfigParam.MultiCard.getCloseRelaySomg(0)));
        ((EditText)findViewById(R.id.editTextNumber_CloseRelaySomg1)).setText(String.valueOf(ConfigParam.MultiCard.getCloseRelaySomg(1)));
        ((EditText)findViewById(R.id.editTextNumber_CloseRelaySomg2)).setText(String.valueOf(ConfigParam.MultiCard.getCloseRelaySomg(2)));
        ((EditText)findViewById(R.id.editTextNumber_CloseRelaySomg3)).setText(String.valueOf(ConfigParam.MultiCard.getCloseRelaySomg(3)));
        //自动模式
        ((EditText)findViewById(R.id.editText_DevInitDegX)).setText(String.valueOf(ConfigParam.MultiCard.getDevInitDegX()));
        ((EditText)findViewById(R.id.editText_DevInitDegY)).setText(String.valueOf(ConfigParam.MultiCard.getDevInitDegY()));
        ((EditText)findViewById(R.id.editText_DevInitDegZ)).setText(String.valueOf(ConfigParam.MultiCard.getDevInitDegZ()));
        ((EditText)findViewById(R.id.editText_RunMode)).setText(String.valueOf(ConfigParam.MultiCard.getRunMode()));
        ((EditText)findViewById(R.id.editText_AuFanTempDown)).setText(String.valueOf(ConfigParam.MultiCard.getAuFanTempDown()));
        ((EditText)findViewById(R.id.editText_AuFanTempUp)).setText(String.valueOf(ConfigParam.MultiCard.getAuFanTempUp()));
        ((EditText)findViewById(R.id.editText_AuPowerTempDown)).setText(String.valueOf(ConfigParam.MultiCard.getAuPowerTempDown()));
        ((EditText)findViewById(R.id.editText_AuPowerSomgDown)).setText(String.valueOf(ConfigParam.MultiCard.getAuPowerSomgDown()));
        ((EditText)findViewById(R.id.editText_AuPowerHumDown)).setText(String.valueOf(ConfigParam.MultiCard.getAuPowerHumDown()));
        ((EditText)findViewById(R.id.editText_AuSysPowerTime)).setText(String.valueOf(ConfigParam.MultiCard.getAuSysPowerTime()));
        ((EditText)findViewById(R.id.editText_AuPowerDegXDown)).setText(String.valueOf(ConfigParam.MultiCard.getAuPowerDegXDown()));
        ((EditText)findViewById(R.id.editText_AuPowerDegYDown)).setText(String.valueOf(ConfigParam.MultiCard.getAuPowerDegYDown()));
        ((EditText)findViewById(R.id.editText_AuPowerDegZDown)).setText(String.valueOf(ConfigParam.MultiCard.getAuPowerDegZDown()));

        ((CheckBox)findViewById(R.id.checkBox_AutoLumPwm0)).setChecked(ConfigParam.MultiCard.getAutoLumPwm(0)!=0);
        ((EditText)findViewById(R.id.editTextNumber_StepSensorLumPwm0)).setText(String.valueOf(ConfigParam.MultiCard.getStepSensorLumPwm(0)));
    }

    public void dataUpdate() {
        //参数列表--上电初始化参数
        ConfigParam.MultiCard.setInitFanPwm(0, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_InitFanPwm0)).getText().toString()));
        ConfigParam.MultiCard.setInitFanPwm(1, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_InitFanPwm1)).getText().toString()));
        ConfigParam.MultiCard.setInitFanPwm(2, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_InitFanPwm2)).getText().toString()));
        ConfigParam.MultiCard.setInitFanPwm(3, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_InitFanPwm3)).getText().toString()));

        ConfigParam.MultiCard.setInitRelay(0, ((CheckBox)findViewById(R.id.checkBox_InitRelay0)).isChecked()?1:0);
        ConfigParam.MultiCard.setInitRelay(1, ((CheckBox)findViewById(R.id.checkBox_InitRelay1)).isChecked()?1:0);
        ConfigParam.MultiCard.setInitRelay(2, ((CheckBox)findViewById(R.id.checkBox_InitRelay2)).isChecked()?1:0);
        ConfigParam.MultiCard.setInitRelay(3, ((CheckBox)findViewById(R.id.checkBox_InitRelay3)).isChecked()?1:0);

        ConfigParam.MultiCard.setInitLumPwm(0, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_InitLumPwm0)).getText().toString()));
        //参数列表--自动控制
        ConfigParam.MultiCard.setAutoFanPwm(0, ((CheckBox)findViewById(R.id.checkBox_AutoFanPwm0)).isChecked()?1:0);
        ConfigParam.MultiCard.setAutoFanPwm(1, ((CheckBox)findViewById(R.id.checkBox_AutoFanPwm1)).isChecked()?1:0);
        ConfigParam.MultiCard.setAutoFanPwm(2, ((CheckBox)findViewById(R.id.checkBox_AutoFanPwm2)).isChecked()?1:0);
        ConfigParam.MultiCard.setAutoFanPwm(3, ((CheckBox)findViewById(R.id.checkBox_AutoFanPwm3)).isChecked()?1:0);
        ConfigParam.MultiCard.setStartFanTemp(0, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StartFanTemp0)).getText().toString()));
        ConfigParam.MultiCard.setStartFanTemp(1, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StartFanTemp1)).getText().toString()));
        ConfigParam.MultiCard.setStartFanTemp(2, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StartFanTemp2)).getText().toString()));
        ConfigParam.MultiCard.setStartFanTemp(3, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StartFanTemp3)).getText().toString()));
        ConfigParam.MultiCard.setStartFanPwm(0, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StartFanPwm0)).getText().toString()));
        ConfigParam.MultiCard.setStartFanPwm(1, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StartFanPwm1)).getText().toString()));
        ConfigParam.MultiCard.setStartFanPwm(2, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StartFanPwm2)).getText().toString()));
        ConfigParam.MultiCard.setStartFanPwm(3, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StartFanPwm3)).getText().toString()));
        ConfigParam.MultiCard.setStepTempFanPwm(0, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StepTempFanPwm0)).getText().toString()));
        ConfigParam.MultiCard.setStepTempFanPwm(1, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StepTempFanPwm1)).getText().toString()));
        ConfigParam.MultiCard.setStepTempFanPwm(2, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StepTempFanPwm2)).getText().toString()));
        ConfigParam.MultiCard.setStepTempFanPwm(3, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StepTempFanPwm3)).getText().toString()));

        ConfigParam.MultiCard.setAutoRelay(0, ((CheckBox)findViewById(R.id.checkBox_AutoRelay0)).isChecked()?1:0);
        ConfigParam.MultiCard.setAutoRelay(1, ((CheckBox)findViewById(R.id.checkBox_AutoRelay1)).isChecked()?1:0);
        ConfigParam.MultiCard.setAutoRelay(2, ((CheckBox)findViewById(R.id.checkBox_AutoRelay2)).isChecked()?1:0);
        ConfigParam.MultiCard.setAutoRelay(3, ((CheckBox)findViewById(R.id.checkBox_AutoRelay3)).isChecked()?1:0);
        ConfigParam.MultiCard.setCloseRelayTemp(0, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_CloseRelayTemp0)).getText().toString()));
        ConfigParam.MultiCard.setCloseRelayTemp(1, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_CloseRelayTemp1)).getText().toString()));
        ConfigParam.MultiCard.setCloseRelayTemp(2, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_CloseRelayTemp2)).getText().toString()));
        ConfigParam.MultiCard.setCloseRelayTemp(3, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_CloseRelayTemp3)).getText().toString()));
        ConfigParam.MultiCard.setCloseRelaySomg(0, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_CloseRelaySomg0)).getText().toString()));
        ConfigParam.MultiCard.setCloseRelaySomg(1, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_CloseRelaySomg1)).getText().toString()));
        ConfigParam.MultiCard.setCloseRelaySomg(2, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_CloseRelaySomg2)).getText().toString()));
        ConfigParam.MultiCard.setCloseRelaySomg(3, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_CloseRelaySomg3)).getText().toString()));
        //自动模式
        ConfigParam.MultiCard.setDevInitDegX(Integer.parseInt(((EditText)findViewById(R.id.editText_DevInitDegX)).getText().toString()));
        ConfigParam.MultiCard.setDevInitDegY(Integer.parseInt(((EditText)findViewById(R.id.editText_DevInitDegY)).getText().toString()));
        ConfigParam.MultiCard.setDevInitDegZ(Integer.parseInt(((EditText)findViewById(R.id.editText_DevInitDegZ)).getText().toString()));
        ConfigParam.MultiCard.setRunMode(Integer.parseInt(((EditText)findViewById(R.id.editText_RunMode)).getText().toString()));
        ConfigParam.MultiCard.setAuFanTempDown(Integer.parseInt(((EditText)findViewById(R.id.editText_AuFanTempDown)).getText().toString()));
        ConfigParam.MultiCard.setAuFanTempUp(Integer.parseInt(((EditText)findViewById(R.id.editText_AuFanTempUp)).getText().toString()));
        ConfigParam.MultiCard.setAuPowerTempDown(Integer.parseInt(((EditText)findViewById(R.id.editText_AuPowerTempDown)).getText().toString()));
        ConfigParam.MultiCard.setAuPowerSomgDown(Integer.parseInt(((EditText)findViewById(R.id.editText_AuPowerSomgDown)).getText().toString()));
        ConfigParam.MultiCard.setAuPowerHumDown(Integer.parseInt(((EditText)findViewById(R.id.editText_AuPowerHumDown)).getText().toString()));
        ConfigParam.MultiCard.setAuSysPowerTime(Integer.parseInt(((EditText)findViewById(R.id.editText_AuSysPowerTime)).getText().toString()));
        ConfigParam.MultiCard.setAuPowerDegXDown(Integer.parseInt(((EditText)findViewById(R.id.editText_AuPowerDegXDown)).getText().toString()));
        ConfigParam.MultiCard.setAuPowerDegYDown(Integer.parseInt(((EditText)findViewById(R.id.editText_AuPowerDegYDown)).getText().toString()));
        ConfigParam.MultiCard.setAuPowerDegZDown(Integer.parseInt(((EditText)findViewById(R.id.editText_AuPowerDegZDown)).getText().toString()));

        ConfigParam.MultiCard.setAutoLumPwm(0, ((CheckBox)findViewById(R.id.checkBox_AutoLumPwm0)).isChecked()?1:0);
        ConfigParam.MultiCard.setStepSensorLumPwm(0, Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_StepSensorLumPwm0)).getText().toString()));
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.button_get: {
                uiUpdate();
                break;
            }
            case R.id.button_set: {
                dataUpdate();
                ConfigParam.MultiCard.setSaveParamReq(1);
                DEBUG.log("disposeCmd SaveParamReq="+ConfigParam.MultiCard.getSaveParamReq());
                break;
            }
            default:
                break;
        }
    }
}
