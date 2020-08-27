package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

// 天气详情 界面
public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity: ";

    private ImageView bingPicImg;

    //声名一些布局
    //界面总布局（父布局），因为页面可能很长所以用滚动布局
    private ScrollView weatherLayout;
    //顶部城市名称标题
    private TextView titleCity;
    //顶部的天气数据更新时间
    private TextView titleUpdateTime;
    //当前的温度
    private TextView degreeText;
    //当前的天气状况，比如：晴
    private TextView weatherInfoText;
    //未来6天的天气情况布局
    private LinearLayout forecastLayout;
    //空气质量指数
    private TextView aqiText;
    //pm2.5
    private TextView pm25Text;
    //生活建议 - 舒适度
    private TextView comfortText;
    //生活建议 - 洗车指数
    private TextView carWashText;
    //生活建议 - 运动建议
    private TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //14.5.4 状态栏沉浸模式？ 5.0 级以上
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            //活动布局显示在状态栏上面
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //设置状态栏背景颜色为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        //14.5.4
        bingPicImg = findViewById(R.id.bing_pic_img);
        //初始化各控件
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);

        //获取 weather 缓存数据
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        //如果存在
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            //显示天气信息
            showWeatherInfo(weather);
        } else {
            //无缓存时从服务器获取天气数据
            //无缓存，表示从县级列表选择进来的，所以要获取传过来的 weather_id
            String weatherId = getIntent().getStringExtra("weather_id");
            //父布局先设置成隐形不可见的，防止显示突兀？
            weatherLayout.setVisibility(View.INVISIBLE);
            //从服务器请求天气数据
            requestWeather(weatherId);
        }

        //14.5.4
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
    }

    /**
     * 根据天气 id 请求城市天气信息
     */
    public void requestWeather(final String weatherId) {
        //攒出对应请求地址
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key" +
                "=c1d9dc6ffb8545f2884e7d36752ce8b7";
        //发送请求
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //防止篡改所以弄个 final ？。获取请求结果。
                final String responseText = response.body().string();
                Log.d(TAG, "requestWeather onResponse: responseText is " + responseText);
                //解析结果返回成 Weather 类实例
                final Weather weather = Utility.handleWeatherResponse(responseText);
                //在子线程中修改 UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //如果 weather 存在并返回的 status 等于 ok 把 weather 数据保存到缓存 weather 中
                        if (weather != null && "ok".equals(weather.status)) {
                            //获取缓存编辑实例
                            SharedPreferences.Editor editor =
                                    PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            //设置键与值
                            editor.putString("weather", responseText);
                            //执行保存操作
                            editor.apply();
                            //显示 weather 信息
                            showWeatherInfo(weather);
                        } else {
                            //提示错误
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败！", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败！", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        //14.5.4
        loadBingPic();
    }

    //14.5.4
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 处理并展示实体类 Weather 中的数据
     */
    private void showWeatherInfo(Weather weather) {
        //获取 weather 实例中 basic 的 cityName
        String cityName = weather.basic.cityName;
        //获取 weather 实例中 basic 的 update 的 updateTime 值，因为是 2020-08-26 16:46
        // 这种格式，这里用具体时间就行了，所以分割成数组，取第 1 个值就具体时间了
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        //获取 weather 实例中 now 的 temperature
        String degree = weather.now.temperature + "℃";
        //获取 weather 实例中 now 的 more 的 info
        String weatherInfo = weather.now.more.info;

        //设置标题城市名称
        titleCity.setText(cityName);
        //设置标题天气更新时间
        titleUpdateTime.setText(updateTime);
        //设置温度
        degreeText.setText(degree);
        //设置天气状况
        weatherInfoText.setText(weatherInfo);

        //因为未来 6 天天气数据是集合列表所以先把 forecastLayout 布局中的所有子布局全部移除，防止累计追加
        forecastLayout.removeAllViews();
        //遍历 weather 中的 forecastList 集合
        for (Forecast forecast : weather.forecastList) {
            //引用 R.layout.forecast_item 子布局创建布局 view 对象
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout,
                    false);
            //获取 forecast_item 里面的子布局
            //未来日期
            TextView dateText = view.findViewById(R.id.date_text);
            //天气状况
            TextView infoText = view.findViewById(R.id.info_text);
            //最高温度
            TextView maxText = view.findViewById(R.id.max_text);
            //最低温度
            TextView minText = view.findViewById(R.id.min_text);

            //设置对应的数据
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);

            //追加到布局中
            forecastLayout.addView(view);
        }

        //如果空气质量有值
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        //设置。。。
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        comfortText.setText(comfort);

        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        carWashText.setText(carWash);

        String sport = "运动建议：" + weather.suggestion.sport.info;
        sportText.setText(sport);

        //最后设置父总局可见
        weatherLayout.setVisibility(View.VISIBLE);
    }
}