package com.bubbleweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    //发起一条http请求，只需要调用sendOkHttpRuqest（），
    //传入请求地址，并注册一个回调来处理服务器响应就可以了
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
