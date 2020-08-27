package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

// JSON 数据解析，今天的天气
public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }
}
