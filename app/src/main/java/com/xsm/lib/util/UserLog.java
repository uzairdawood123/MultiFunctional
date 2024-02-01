package com.xsm.lib.util;

import android.util.Log;

import java.util.List;

public class UserLog {

    private final String TAG;
    private boolean EN_DEBUG = true;
    private boolean KEY_DEBUG;
    /**
     * 控制调试输出的开关
     * @param en
     */
    public void setDebugKey(boolean en){
        EN_DEBUG = en;
        KEY_DEBUG = en;
    }

    public boolean getDebugKey(){
        return EN_DEBUG;
    }

    public UserLog(String tag, boolean en){
        TAG = tag;
        KEY_DEBUG = en;
    }

    public void log(String name, byte[] buf){
        if(!(EN_DEBUG&&KEY_DEBUG)) return;
        String message =name+"("+buf.length+")=";
        for(int i=0;i<buf.length;i++)
        {
            message += String.format("%#2x ", buf[i]);
        }
        if(EN_DEBUG&&KEY_DEBUG) Log.i(TAG, message);
    }

    public void log(String name, byte[] buf, int offset, int len){
        if(!(EN_DEBUG&&KEY_DEBUG)) return;
        String message =name+"("+len+")=";
        for(int i=0;i<len;i++)
        {
            message += String.format("%#2x ", buf[i+offset]);
        }
        if(EN_DEBUG&&KEY_DEBUG) Log.i(TAG, message);
    }

    public void log(byte[] buf){
        if(!(EN_DEBUG&&KEY_DEBUG)) return;
        String message =buf.length+": ";
        for(int i=0;i<buf.length;i++)
        {
            message += String.format("%#2x ", buf[i]);
        }
        if(EN_DEBUG&&KEY_DEBUG) Log.i(TAG, message);
    }

    public void log(String name, List<Byte> list){
        if(!(EN_DEBUG&&KEY_DEBUG)) return;
        String message =name+"("+list.size()+")=";
        for(int i=0;i<list.size();i++)
        {
            message += String.format("%#2x ", list.get(i));
        }
        if(EN_DEBUG&&KEY_DEBUG) Log.i(TAG, message);
    }

    public void log(String name, List<Byte> list, int offset, int len){
        if(!(EN_DEBUG&&KEY_DEBUG)) return;
        String message =name+"("+len+")=";
        for(int i=0;i<len;i++)
        {
            message += String.format("%#2x ", list.get(i+offset));
        }
        if(EN_DEBUG&&KEY_DEBUG) Log.i(TAG, message);
    }

    public void log(byte[] buf,int offset, int len){
        if(!(EN_DEBUG&&KEY_DEBUG)) return;
        String message =len+": ";
        for(int i=0;i<len;i++)
        {
            message += String.format("%#2x ", buf[i+offset]);
        }
        if(EN_DEBUG&&KEY_DEBUG) Log.i(TAG, message);
    }

    public void log(String name, int[] buf){
        if(!(EN_DEBUG&&KEY_DEBUG)) return;
        String message =name+"("+buf.length+")=";
        for(int i=0;i<buf.length;i++)
        {
            if(i==buf.length-1) message += buf[i];
            else  message += buf[i] + ".";
        }
        if(EN_DEBUG&&KEY_DEBUG) Log.i(TAG, message);
    }

    public void log(String str){
        if(EN_DEBUG&&KEY_DEBUG) Log.i(TAG, str);
    }

    public void log(String name, String str){
        if(EN_DEBUG&&KEY_DEBUG) Log.i(TAG, name + " = " + str);
    }

    public void log(String name, int str){
        if(EN_DEBUG&&KEY_DEBUG) Log.i(TAG, name + " = " + str);
    }

    public void log(String name, boolean str){
        if(EN_DEBUG&&KEY_DEBUG) Log.i(TAG, name + " = " + str);
    }

}
