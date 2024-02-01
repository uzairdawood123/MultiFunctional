package com.xsm.exa.multifunctionmonitoring;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xsm.exa.multifunctionmonitoring.activity.BaseActivity;
import com.xsm.exa.multifunctionmonitoring.activity.ControlActivity;
import com.xsm.exa.multifunctionmonitoring.activity.InfoActivity;
import com.xsm.exa.multifunctionmonitoring.activity.LoginActivity;
import com.xsm.exa.multifunctionmonitoring.activity.SettingActivity;
import com.xsm.exa.multifunctionmonitoring.param.ConfigParam;
import com.xsm.exa.multifunctionmonitoring.service.DataProcessService;
import com.xsm.lib.util.UserLog;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    protected UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), true);
    // 所需的权限
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE
    };
    // 所需的动态库文件
    private static final String[] LIBRARIES = new String[]{
            "libfriendlyarm-things.so",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("uzairr","uzair");
        DEBUG.log("ConfigParam.init start...");
        //初始化参数配置
        ConfigParam.init(this);
        // 校验库是否存在
        if(checkSoFile(LIBRARIES)) {
            initPermissions(NEEDED_PERMISSIONS); //初始化权限，
        }
        else showToast("lib is not find!");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        ConfigParam.setIsLogin(false);
    }

    @Override
    protected void initPermissionDone(int requestCode, boolean isAllGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(new Intent(this, DataProcessService.class));
        }
        else {
            this.startService(new Intent(this, DataProcessService.class));
        }
        initView();
        if(!ConfigParam.getIsLogin()) startActivity(new Intent(this, LoginActivity.class));
    }

    public void initView(){
        ((Button) findViewById(R.id.btn_info)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_control)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_setting)).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_info: {
                startActivity(new Intent(this, InfoActivity.class));
                break;
            }
            case R.id.btn_control: {
                startActivity(new Intent(this, ControlActivity.class));
                break;
            }
            case R.id.btn_setting: {
                startActivity(new Intent(this, SettingActivity.class));
                showToast("btn_setting");
                break;
            }
            default:
                break;
        }
    }
}