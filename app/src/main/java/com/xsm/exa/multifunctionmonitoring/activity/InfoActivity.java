package com.xsm.exa.multifunctionmonitoring.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.xsm.exa.multifunctionmonitoring.R;
import com.xsm.exa.multifunctionmonitoring.param.ConfigParam;
import com.xsm.lib.util.UserLog;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    protected UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), false);
    private boolean IS_UI_UPDATE = true;
    private final long UI_UPDATE_DELAY = 1000;
    private boolean isUiUdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Info");
        setContentView(R.layout.activity_info);
        initView();
        new Thread() {
            public void run() {
                while (IS_UI_UPDATE) {
                    try {
                        DEBUG.log("ui update...");
                        sleep(UI_UPDATE_DELAY);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                uiUpdate();
                            }
                        });
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                DEBUG.log("up update Thread end!");
            }
        }.start();;
    }

    public void initView(){
        isUiUdate = true;
        findViewById(R.id.checkBox_DriRelay0).setOnClickListener(this);
        findViewById(R.id.checkBox_DriRelay1).setOnClickListener(this);
        findViewById(R.id.checkBox_DriRelay2).setOnClickListener(this);
        findViewById(R.id.checkBox_DriRelay3).setOnClickListener(this);
        ((SeekBar)findViewById(R.id.seekBar_DriFanPwm0)).setOnSeekBarChangeListener(this);
        ((SeekBar)findViewById(R.id.seekBar_DriFanPwm1)).setOnSeekBarChangeListener(this);
        ((SeekBar)findViewById(R.id.seekBar_DriFanPwm2)).setOnSeekBarChangeListener(this);
        ((SeekBar)findViewById(R.id.seekBar_DriFanPwm3)).setOnSeekBarChangeListener(this);
        ((SeekBar)findViewById(R.id.seekBar_DriLumPwm0)).setOnSeekBarChangeListener(this);
        isUiUdate = false;
    }

    //刷新页面的内容
    public void uiUpdate() {
        isUiUdate = true;
        //更新传感器
        ((TextView)findViewById(R.id.textView_Temperature0)).setText(String.valueOf(ConfigParam.MultiCard.getSensorTemperature(0)));
        ((TextView)findViewById(R.id.textView_Temperature1)).setText(String.valueOf(ConfigParam.MultiCard.getSensorTemperature(1)));

        ((TextView)findViewById(R.id.textView_Humidity0)).setText(String.valueOf(ConfigParam.MultiCard.getSensorHumidity(0)));
        ((TextView)findViewById(R.id.textView_Humidity1)).setText(String.valueOf(ConfigParam.MultiCard.getSensorHumidity(1)));

        ((TextView)findViewById(R.id.textView_OpenDoor)).setText(String.valueOf(ConfigParam.MultiCard.getSensorOpenDoor(0)));
        ((TextView)findViewById(R.id.textView_Vibrate)).setText(String.valueOf(ConfigParam.MultiCard.getSensorVibrate(0)));

        ((TextView)findViewById(R.id.textView_FanSpeed0)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(0)));
        ((TextView)findViewById(R.id.textView_FanSpeed1)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(1)));
        ((TextView)findViewById(R.id.textView_FanSpeed2)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(2)));
        ((TextView)findViewById(R.id.textView_FanSpeed3)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(3)));
        ((TextView)findViewById(R.id.textView_FanSpeed4)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(4)));
        ((TextView)findViewById(R.id.textView_FanSpeed5)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(5)));
        ((TextView)findViewById(R.id.textView_FanSpeed6)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(6)));
        ((TextView)findViewById(R.id.textView_FanSpeed7)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(7)));
        ((TextView)findViewById(R.id.textView_FanSpeed8)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(8)));
        ((TextView)findViewById(R.id.textView_FanSpeed9)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(9)));
        ((TextView)findViewById(R.id.textView_FanSpeed10)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(10)));
        ((TextView)findViewById(R.id.textView_FanSpeed11)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(11)));
        ((TextView)findViewById(R.id.textView_FanSpeed12)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(12)));
        ((TextView)findViewById(R.id.textView_FanSpeed13)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(13)));
        ((TextView)findViewById(R.id.textView_FanSpeed14)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(14)));
        ((TextView)findViewById(R.id.textView_FanSpeed15)).setText(String.valueOf(ConfigParam.MultiCard.getSensorFanSpeed(15)));

        ((TextView)findViewById(R.id.textView_LevelAngleX)).setText(String.valueOf(ConfigParam.MultiCard.getSensorLevelAngleX(0)));
        ((TextView)findViewById(R.id.textView_LevelAngleY)).setText(String.valueOf(ConfigParam.MultiCard.getSensorLevelAngleY(0)));
        ((TextView)findViewById(R.id.textView_LevelAngleZ)).setText(String.valueOf(ConfigParam.MultiCard.getSensorLevelAngleZ(0)));

        ((TextView)findViewById(R.id.textView_Voltage0)).setText(String.valueOf(ConfigParam.MultiCard.getSensorVoltage(0)));
        ((TextView)findViewById(R.id.textView_Voltage1)).setText(String.valueOf(ConfigParam.MultiCard.getSensorVoltage(1)));
        ((TextView)findViewById(R.id.textView_Voltage2)).setText(String.valueOf(ConfigParam.MultiCard.getSensorVoltage(2)));
        ((TextView)findViewById(R.id.textView_Voltage3)).setText(String.valueOf(ConfigParam.MultiCard.getSensorVoltage(3)));
        ((TextView)findViewById(R.id.textView_Voltage4)).setText(String.valueOf(ConfigParam.MultiCard.getSensorVoltage(4)));
        ((TextView)findViewById(R.id.textView_Voltage5)).setText(String.valueOf(ConfigParam.MultiCard.getSensorVoltage(5)));

        ((TextView)findViewById(R.id.textView_DiffPressure0)).setText(String.valueOf(ConfigParam.MultiCard.getSensorDiffPressure(0)));
        ((TextView)findViewById(R.id.textView_DiffPressure1)).setText(String.valueOf(ConfigParam.MultiCard.getSensorDiffPressure(1)));

        ((TextView)findViewById(R.id.textView_Smog)).setText(String.valueOf(ConfigParam.MultiCard.getSensorSmog(0)));
        ((TextView)findViewById(R.id.textView_Lum0)).setText(String.valueOf(ConfigParam.MultiCard.getSensorLum(0)));
        ((TextView)findViewById(R.id.textView_Lum1)).setText(String.valueOf(ConfigParam.MultiCard.getSensorLum(1)));
        ((TextView)findViewById(R.id.textView_teimout)).setText(String.valueOf(ConfigParam.MultiCard.isComTimeout()));
        //更新控制状态
        ((CheckBox)findViewById(R.id.checkBox_DriRelay0)).setChecked(ConfigParam.MultiCard.getDriRelay(0)!=0);
        ((CheckBox)findViewById(R.id.checkBox_DriRelay1)).setChecked(ConfigParam.MultiCard.getDriRelay(1)!=0);
        ((CheckBox)findViewById(R.id.checkBox_DriRelay2)).setChecked(ConfigParam.MultiCard.getDriRelay(2)!=0);
        ((CheckBox)findViewById(R.id.checkBox_DriRelay3)).setChecked(ConfigParam.MultiCard.getDriRelay(3)!=0);
        ((SeekBar)findViewById(R.id.seekBar_DriFanPwm0)).setProgress(ConfigParam.MultiCard.getDriFanPwm(0));
        ((SeekBar)findViewById(R.id.seekBar_DriFanPwm1)).setProgress(ConfigParam.MultiCard.getDriFanPwm(1));
        ((SeekBar)findViewById(R.id.seekBar_DriFanPwm2)).setProgress(ConfigParam.MultiCard.getDriFanPwm(2));
        ((SeekBar)findViewById(R.id.seekBar_DriFanPwm3)).setProgress(ConfigParam.MultiCard.getDriFanPwm(3));
        ((SeekBar)findViewById(R.id.seekBar_DriLumPwm0)).setProgress(ConfigParam.MultiCard.getDriLumPwm(0));
        isUiUdate = false;
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        IS_UI_UPDATE = false;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        IS_UI_UPDATE = true;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(!isUiUdate) ConfigParam.MultiCard.setCtlParamReq(1);
        switch (id){
            case R.id.checkBox_DriRelay0: {
                ConfigParam.MultiCard.setDriRelay(0, ((CheckBox)findViewById(id)).isChecked()?1:0);
                break;
            }
            case R.id.checkBox_DriRelay1: {
                ConfigParam.MultiCard.setDriRelay(1, ((CheckBox)findViewById(id)).isChecked()?1:0);
                break;
            }
            case R.id.checkBox_DriRelay2: {
                ConfigParam.MultiCard.setDriRelay(2, ((CheckBox)findViewById(id)).isChecked()?1:0);
                break;
            }
            case R.id.checkBox_DriRelay3: {
                ConfigParam.MultiCard.setDriRelay(3, ((CheckBox)findViewById(id)).isChecked()?1:0);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        DEBUG.log("set pwm = "+i);
        if(!isUiUdate) ConfigParam.MultiCard.setCtlParamReq(1);
        switch(seekBar.getId()) {
            case R.id.seekBar_DriFanPwm0: {
                ConfigParam.MultiCard.setDriFanPwm(0, i);
                break;
            }
            case R.id.seekBar_DriFanPwm1: {
                ConfigParam.MultiCard.setDriFanPwm(1, i);
                break;
            }
            case R.id.seekBar_DriFanPwm2: {
                ConfigParam.MultiCard.setDriFanPwm(2, i);
                break;
            }
            case R.id.seekBar_DriFanPwm3: {
                ConfigParam.MultiCard.setDriFanPwm(3, i);
                break;
            }
            case R.id.seekBar_DriLumPwm0: {
                ConfigParam.MultiCard.setDriLumPwm(0, i);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
