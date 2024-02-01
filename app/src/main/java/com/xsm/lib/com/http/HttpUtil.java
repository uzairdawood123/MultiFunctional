package com.xsm.lib.com.http;

import com.xsm.lib.util.UserLog;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    public final static UserLog DEBUG = new UserLog("HttpUtil", true);

    private static final int TIME_OUT = 5 * 1000; // 超时时间
    private static final String CHARSET = "utf-8"; // 设置编码

    public static void downloadByPost(String path, Map<String, String> params) throws IOException {
        StringBuilder sb = new StringBuilder();
        if(params!=null && !params.isEmpty()){
            for(Map.Entry<String, String> entry : params.entrySet()){
                sb.append(entry.getKey()).append('=')
                        .append(URLEncoder.encode(entry.getValue(), CHARSET)).append('&');
            }
            sb.deleteCharAt(sb.length()-1);
        }
        byte[] entitydata = sb.toString().getBytes();//得到实体的二进制数据
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(TIME_OUT);
        conn.setDoOutput(true);//如果通过post提交数据，必须设置允许对外输出数
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(entitydata.length));
        OutputStream outStream = conn.getOutputStream();
        outStream.write(entitydata);
        outStream.flush();
        outStream.close();
        if(conn.getResponseCode()==200){
            conn.getResponseMessage();
        }
    }


    public static boolean sendXML(String path, String xml)throws Exception{
        byte[] data = xml.getBytes();
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5 * 1000);
        conn.setDoOutput(true);//如果通过post提交数据，必须设置允许对外输出数
        conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(data.length));
        OutputStream outStream = conn.getOutputStream();
        outStream.write(data);
        outStream.flush();
        outStream.close();
        if(conn.getResponseCode()==200){
            return true;
        }
        return false;
    }

    public static boolean sendGetRequest(String path, Map<String, String> params, String enc) throws Exception{
        StringBuilder sb = new StringBuilder(path);
        sb.append('?');
        // ?method=save&title=435435435&timelength=89&
        for(Map.Entry<String, String> entry : params.entrySet()){
            sb.append(entry.getKey()).append('=')
                    .append(URLEncoder.encode(entry.getValue(), enc)).append('&');
        }
        sb.deleteCharAt(sb.length()-1);

        URL url = new URL(sb.toString());
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5 * 1000);
        if(conn.getResponseCode()==200){
            return true;
        }
        return false;
    }

    public static boolean sendPostRequest(String path, Map<String, String> params, String enc) throws Exception{
        // title=dsfdsf&timelength=23&method=save
        StringBuilder sb = new StringBuilder();
        if(params!=null && !params.isEmpty()){
            for(Map.Entry<String, String> entry : params.entrySet()){
                sb.append(entry.getKey()).append('=')
                        .append(URLEncoder.encode(entry.getValue(), enc)).append('&');
            }
            sb.deleteCharAt(sb.length()-1);
        }
        byte[] entitydata = sb.toString().getBytes();//得到实体的二进制数据
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5 * 1000);
        conn.setDoOutput(true);//如果通过post提交数据，必须设置允许对外输出数�?
        //Content-Type: application/x-www-form-urlencoded
        //Content-Length: 38
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(entitydata.length));
        OutputStream outStream = conn.getOutputStream();
        outStream.write(entitydata);
        outStream.flush();
        outStream.close();
        if(conn.getResponseCode()==200){
            conn.getResponseMessage();
            return true;
        }
        return false;
    }

    //SSL HTTPS Cookie
    public static boolean sendRequestFromHttpClient(String path, Map<String, String> params, String enc) throws Exception{
        List<NameValuePair> paramPairs = new ArrayList<NameValuePair>();
        if(params!=null && !params.isEmpty()){
            for(Map.Entry<String, String> entry : params.entrySet()){
                paramPairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        UrlEncodedFormEntity entitydata = new UrlEncodedFormEntity(paramPairs, enc);//得到经过编码过后的实体数�?
        HttpPost post = new HttpPost(path); //form
        post.setEntity(entitydata);
        DefaultHttpClient client = new DefaultHttpClient(); //浏览�?
        HttpResponse response = client.execute(post);//执行请求
        if(response.getStatusLine().getStatusCode()==200){
            //  HttpUtil.DEBUG.setDisplay(response.getEntity().toString());
            //  HttpUtil.DEBUG.setDisplay(response.getParams().getParameter("password").toString());
            //  HttpUtil.DEBUG.setDisplay(response.getParams().toString());
            return true;
        }
        return false;
    }

    //封装的发送请求函数
    public static void sendHttpPostRequest(final String address, final String json, final HttpCallbackListener listener) {
        if (!HttpUtil.isNetworkAvailable()){
            //这里写相应的网络设置处理
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try{
                    URL url = new URL(address);
                    //使用HttpURLConnection
                    connection = (HttpURLConnection) url.openConnection();
                    //设置方法和参数
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    //发送数据
                    if(json!=null) {
                        byte[] entitydata = json.getBytes();//得到实体的二进制数据
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestProperty("Content-Length", String.valueOf(entitydata.length));
                        OutputStream outStream = connection.getOutputStream();
                        outStream.write(entitydata);
                        outStream.flush();
                        outStream.close();
                    }
                    //成功则回调onFinish
                    if(connection.getResponseCode()==200 && listener != null) {
                        //listener.onFinish(connection.getResponseMessage());
                        //获取返回结果
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null){
                            response.append(line);
                        }
                        listener.onFinish(response.toString());
                    }
                    else {
                        if(listener != null) listener.onError(new Exception("ResponseCode="+connection.getResponseCode()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //出现异常则回调onError
                    if (listener != null){
                        listener.onError(e);
                    }
                }finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
    //封装的发送请求函数
    public static void sendHttpGetRequest(final String address, final HttpCallbackListener listener) {
        if (!HttpUtil.isNetworkAvailable()){
            //这里写相应的网络设置处理
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try{
                    URL url = new URL(address);
                    //使用HttpURLConnection
                    connection = (HttpURLConnection) url.openConnection();
                    //设置方法和参数
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    //获取返回结果
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    //成功则回调onFinish
                    if (listener != null){
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //出现异常则回调onError
                    if (listener != null){
                        listener.onError(e);
                    }
                }finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
    //组装出带参数的完整URL
    public static String getURLWithParams(String address, HashMap<String,String> params) throws UnsupportedEncodingException {
        //设置编码
        final String encode = "UTF-8";
        StringBuilder url = new StringBuilder(address);
        url.append("?");
        //将map中的key，value构造进入URL中
        for(Map.Entry<String, String> entry:params.entrySet())
        {
            url.append(entry.getKey()).append("=");
            url.append(URLEncoder.encode(entry.getValue(), encode));
            url.append("&");
        }
        //删掉最后一个&
        url.deleteCharAt(url.length() - 1);
        return url.toString();
    }
    //判断当前网络是否可用
    public static boolean isNetworkAvailable(){
        return true;
    }
}



