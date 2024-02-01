package com.xsm.exa.multifunctionmonitoring.activity;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.xsm.exa.multifunctionmonitoring.R;
import com.xsm.exa.multifunctionmonitoring.param.ConfigParam;
import com.xsm.lib.util.UserLog;

public class SettingActivity extends AppCompatActivity {
    protected UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Setting");
        setContentView(R.layout.activity_setting);
        initView();
    }

    public void initView(){
        ((EditText)findViewById(R.id.editText_url)).setText(ConfigParam.getHttpReqUrl());
        ((EditText)findViewById(R.id.editTextNumber_delay)).setText(String.valueOf(ConfigParam.getHttpReqDelay()));
        ((EditText)findViewById(R.id.editTextNumber_timeout)).setText(String.valueOf(ConfigParam.getMultiTimeout()));
        ((EditText)findViewById(R.id.editText_userName)).setText(ConfigParam.getUserName());
        ((EditText)findViewById(R.id.editText_password)).setText(ConfigParam.getPassword());
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        ConfigParam.setHttpReqUrl(((EditText)findViewById(R.id.editText_url)).getText().toString());
        ConfigParam.setHttpReqDelay(Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_delay)).getText().toString()));
        ConfigParam.setMultiTimeout(Integer.parseInt(((EditText)findViewById(R.id.editTextNumber_timeout)).getText().toString()));
        ConfigParam.setUserName(((EditText)findViewById(R.id.editText_userName)).getText().toString());
        ConfigParam.setPassword(((EditText)findViewById(R.id.editText_password)).getText().toString());
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
}
