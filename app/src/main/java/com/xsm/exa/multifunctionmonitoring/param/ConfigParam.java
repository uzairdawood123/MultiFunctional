package com.xsm.exa.multifunctionmonitoring.param;

import android.content.Context;
import android.content.SharedPreferences;

import com.xsm.lib.util.UserLog;

public class ConfigParam {

    private static UserLog DEBUG = new UserLog("ConfigParam", true);

    private static SharedPreferences mSharedPreferences;
    public static StatusParam MultiCard = new StatusParam();
    //配置参数
    private static long HttpReqDelay = 1000; //HTTP请求延时
    private static String HttpReqUrl = "http://192.168.100.111:8080";//"http://www.kuaidi100.com/query?type=yuantong&postid=11111111111";//http://192.168.100.111:8080";
    private static long MultiTimeout = 30000; //监控卡通讯超时参数
    //PID列表
    private static final String pid_Config= "config";
    private static final String pid_UserName = "con_UserName";
    private static final String pid_Password = "con_Password";
    private static final String pid_isLogin = "con_isLogin";
    private static final String pid_httpDelay = "con_httpDelay";
    private static final String pid_httpUrl = "con_httpUrl";
    private static final String pid_MultiTimeout = "con_MultiTimeout";

    public static void init(Context context) {
        if(mSharedPreferences==null) {
            mSharedPreferences = context.getSharedPreferences(pid_Config, Context.MODE_PRIVATE);
            DEBUG.log("mSharedPreferences=" + mSharedPreferences);
            String user = getUserName();
            String password = getPassword();
            if(user.length()==0) setUserName("Admin");
            if(password.length()==0) setPassword("2020666");
            DEBUG.log("getUserName=" + getUserName());
            DEBUG.log("getPassword=" + getPassword());
            //参数上电加载
            HttpReqDelay = mSharedPreferences.getLong(pid_httpDelay,1000);
            HttpReqUrl = mSharedPreferences.getString(pid_httpUrl,"http://www.kuaidi100.com/query?type=yuantong&postid=11111111111");
            MultiTimeout = mSharedPreferences.getLong(pid_MultiTimeout,30000);
        }
    }

    public static void destroy(Context context) {
//        if(mSharedPreferences!=null) {
//            mSharedPreferences.edit().putLong(pid_httpDelay, HttpReqDelay).apply();
//            mSharedPreferences.edit().putString(pid_httpUrl, HttpReqUrl).apply();
//        }
    }

    public static long getMultiTimeout() {
        return MultiTimeout;
    }

    public static void setMultiTimeout(long multiTimeout) {
        MultiTimeout = multiTimeout;
        mSharedPreferences.edit().putLong(pid_MultiTimeout, MultiTimeout).apply();
    }

    public static long getHttpReqDelay() {
        return HttpReqDelay;
    }

    public static void setHttpReqDelay(long httpReqDelay) {
        HttpReqDelay = httpReqDelay;
        mSharedPreferences.edit().putLong(pid_httpDelay, HttpReqDelay).apply();
    }

    public static String getHttpReqUrl() {
        return HttpReqUrl;
    }

    public static void setHttpReqUrl(String httpReqUrl) {
        HttpReqUrl = httpReqUrl;
        mSharedPreferences.edit().putString(pid_httpUrl, HttpReqUrl).apply();
    }

    public static void setUserName(String name) {
        mSharedPreferences.edit().putString(pid_UserName, name).apply();
    }

    public static String getUserName() {
        return mSharedPreferences.getString(pid_UserName,"");
    }

    public static void setPassword(String val) {
        mSharedPreferences.edit().putString(pid_Password, val).apply();
    }

    public static String getPassword() {
        return mSharedPreferences.getString(pid_Password,"");
    }

    public static void setIsLogin(boolean val) {
        mSharedPreferences.edit().putBoolean(pid_isLogin, val).apply();
    }

    public static boolean getIsLogin() {
        return mSharedPreferences.getBoolean(pid_isLogin,false);
    }
}
