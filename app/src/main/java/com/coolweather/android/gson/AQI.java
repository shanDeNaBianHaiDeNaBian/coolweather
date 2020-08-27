package com.coolweather.android.gson;

/*
空气质量 JSON 解析
aqi: {
    aqi: "33"
    pm25: "33"
}
 */
public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
