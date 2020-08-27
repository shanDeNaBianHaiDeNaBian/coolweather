package com.coolweather.android.util;

import android.text.TextUtils;
import android.util.Log;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    private static final String TAG = "aa";

    /**
     * 解析和处理服务器返回的省数据（解析后保存到数据库）
     *
     * @param response 服务器返回的省数据。格式：[{"id":1,"name":"北京"},{"id":2,"name":"上海"},{"id":3,
     *                 *                   "name":"天津"},{"id":4,"name":"重庆"},{"id":5,"name":"香港"}
     *                 ,{"id":6,
     *                 *                   "name":"澳门"},{"id":7,"name":"台湾"},{"id":8,"name":"黑龙江"
     *                 },{"id":9,
     *                 *                   "name":"吉林"},{"id":10,"name":"辽宁"},{"id":11,
     *                 "name":"内蒙古"},{"id":12,
     *                 *                   "name":"河北"},{"id":13,"name":"河南"},{"id":14,
     *                 "name":"山西"},{"id":15,
     *                 *                   "name":"山东"},{"id":16,"name":"江苏"},{"id":17,
     *                 "name":"浙江"},{"id":18,
     *                 *                   "name":"福建"},{"id":19,"name":"江西"},{"id":20,
     *                 "name":"安徽"},{"id":21,
     *                 *                   "name":"湖北"},{"id":22,"name":"湖南"},{"id":23,
     *                 "name":"广东"},{"id":24,
     *                 *                   "name":"广西"},{"id":25,"name":"海南"},{"id":26,
     *                 "name":"贵州"},{"id":27,
     *                 *                   "name":"云南"},{"id":28,"name":"四川"},{"id":29,
     *                 "name":"西藏"},{"id":30,
     *                 *                   "name":"陕西"},{"id":31,"name":"宁夏"},{"id":32,
     *                 "name":"甘肃"},{"id":33,
     *                 *                   "name":"青海"},{"id":34,"name":"新疆"}]
     * @return
     */
    public static boolean handleProvinceResponse(String response) {
        // Log.d(TAG, "handleProvinceResponse: response: " + response);
        if (!TextUtils.isEmpty(response)) {
            try {
                //组成 json 数组
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    //获取数组中对应 json 对象
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    //实例省数据库对象
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    //保存数据库
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市数据
     *
     * @param response   服务器返回的市数据。格式：[{"id":113,"name":"南京"},{"id":114,"name":"无锡"},{"id":115,
     *                   "name":"镇江"},{"id":116,"name":"苏州"},{"id":117,"name":"南通"},{"id":118,
     *                   "name":"扬州"},{"id":119,"name":"盐城"},{"id":120,"name":"徐州"},{"id":121,
     *                   "name":"淮安"},{"id":122,"name":"连云港"},{"id":123,"name":"常州"},{"id":124,
     *                   "name":"泰州"},{"id":125,"name":"宿迁"}]
     * @param provinceId 当前市所属省的 id
     * @return
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县数据
     *
     * @param response 服务器返回的县数据。格式：[{"id":932,"name":"镇江","weather_id":"CN101190301"},{"id
     *                 ":933,"name":"丹阳","weather_id":"CN101190302"},{"id":934,"name":"扬中",
     *                 "weather_id":"CN101190303"},{"id":935,"name":"句容",
     *                 "weather_id":"CN101190304"},{"id":936,"name":"丹徒",
     *                 "weather_id":"CN101190305"}]
     * @param cityId   当前市所属市的 id
     * @return
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            Log.d(TAG, "handleCountyResponse: response: " + response);
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 把服务器获取的JSON数据
     * {"HeWeather":[{"basic":{"cid":"CN101090502","location":"丰南","parent_city":"唐山",
     * "admin_area":"河北","cnty":"中国","lat":"36.67580795","lon":"117.00092316","tz":"+8.00",
     * "city":"丰南","id":"CN101090502","update":{"loc":"2020-08-26 15:52","utc":"2020-08-26 07:52"
     * }},"update":{"loc":"2020-08-26 15:52","utc":"2020-08-26 07:52"},"status":"ok","now":{
     * "cloud":"10","cond_code":"104","cond_txt":"阴","fl":"8","hum":"60","pcpn":"0.0",
     * "pres":"1015","tmp":"12","vis":"16","wind_deg":"261","wind_dir":"西风","wind_sc":"3",
     * "wind_spd":"17","cond":{"code":"104","txt":"阴"}},"daily_forecast":[{"date":"2020-08-27",
     * "cond":{"txt_d":"阴"},"tmp":{"max":"17","min":"6"}},{"date":"2020-08-28","cond":{"txt_d
     * ":"晴"},"tmp":{"max":"14","min":"3"}},{"date":"2020-08-29","cond":{"txt_d":"晴"},"tmp":{"max
     * ":"15","min":"6"}},{"date":"2020-08-30","cond":{"txt_d":"多云"},"tmp":{"max":"20","min":"6"}
     * },{"date":"2020-08-31","cond":{"txt_d":"晴"},"tmp":{"max":"18","min":"5"}},{"date":"2020-09
     * -01","cond":{"txt_d":"多云"},"tmp":{"max":"19","min":"7"}}],"aqi":{"city":{"aqi":"112",
     * "pm25":"60","qlty":"轻度污染"}},"suggestion":{"comf":{"type":"comf","brf":"舒适",
     * "txt":"白天不太热也不太冷，风力不大，相信您在这样的天气条件下，应会感到比较清爽和舒适。"},"sport":{"type":"sport","brf":"较适宜",
     * "txt":"阴天，较适宜进行各种户内外运动。"},"cw":{"type":"cw","brf":"较适宜",
     * "txt":"较适宜洗车，未来一天无雨，风力较小，擦洗一新的汽车至少能保持一天。"}},"msg
     * ":"所有天气数据均为模拟数据，仅用作学习目的使用，请勿当作真实的天气预报软件来使用。"}]}
     * 解析成 Weather 实体类
     *
     * @param response
     * @return
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            //获取 HeWeather 数组
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            //获取数组中每 1 个对象转换成字符串
            String weatherContent = jsonArray.getJSONObject(0).toString();
            //返回解析后的 Weather 实体对象
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
