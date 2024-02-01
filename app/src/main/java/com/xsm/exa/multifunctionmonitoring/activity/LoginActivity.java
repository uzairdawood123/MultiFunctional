package com.xsm.exa.multifunctionmonitoring.activity;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xsm.exa.multifunctionmonitoring.R;
import com.xsm.exa.multifunctionmonitoring.param.ConfigParam;
import com.xsm.lib.util.UserLog;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    protected UserLog DEBUG = new UserLog(this.getClass().getSimpleName(), true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Welcome");
        setContentView(R.layout.activity_login);
        initView();
    }
    public void initView(){
        ((Button) findViewById(R.id.btn_login)).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login: {
                String user0 = ((EditText) findViewById(R.id.editText_user)).getText().toString();
                String password0 = ((EditText) findViewById(R.id.editText_password)).getText().toString();
                String user1 = ConfigParam.getUserName();
                String password1 = ConfigParam.getPassword();
                if(user0.equals(user1) && password0.equals(password1)) {
                    Toast.makeText(getApplicationContext(), "login ok.", Toast.LENGTH_SHORT).show();
                    ConfigParam.setIsLogin(true);
                    this.finish();
                }
                else
                    Toast.makeText(getApplicationContext(), "user or password is error!!!", Toast.LENGTH_SHORT).show();
                break;
            }
            default:
                break;
        }
    }
}
