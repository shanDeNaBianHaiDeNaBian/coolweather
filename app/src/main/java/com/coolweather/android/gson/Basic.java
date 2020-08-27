package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    //用 gson 中的 SerializedName 接口映射返回值中的键名为自己定义的字段名
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
