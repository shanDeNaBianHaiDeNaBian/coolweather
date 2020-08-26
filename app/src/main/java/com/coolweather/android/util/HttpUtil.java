package com.coolweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        //声明一个 连接 实例
        OkHttpClient client = new OkHttpClient();
        //声明一个 请求 实例
        // Request.Builder builder = new Request.Builder();
        // builder.url(address);
        // Request request = builder.build();
        Request request = new Request.Builder().url(address).build();
        //执行连接请求并执行回调队列
        client.newCall(request).enqueue(callback);
    }
}
