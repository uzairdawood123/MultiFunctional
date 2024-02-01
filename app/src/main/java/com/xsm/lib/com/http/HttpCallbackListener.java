package com.xsm.lib.com.http;

//回调监听接口类
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
