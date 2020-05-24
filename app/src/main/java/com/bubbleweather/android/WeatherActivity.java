package com.bubbleweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bubbleweather.android.gson.Forecast;
import com.bubbleweather.android.gson.Lifestyle;
import com.bubbleweather.android.gson.Weather;
import com.bubbleweather.android.service.AutoUpdateService;
import com.bubbleweather.android.util.HttpUtil;
import com.bubbleweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
//    private LinearLayout aqiLayout;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    public DrawerLayout drawerLayout;
    private Button navButton;



    //下拉刷新
    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //初始化控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
//        aqiLayout = (LinearLayout) findViewById(R.id.aqi_layout);

        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        String weatherId = getIntent().getStringExtra("weather_id");
        if (weatherString != null) {
            //有缓存的时候直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

    }

    //根据天气id请求城市天气信息
    public void requestWeather(final String weatherId) {
        String weatherUri = "https://free-api.heweather.net/s6/weather/?location=" +
                weatherId + "&key=7aa2b75d9e5e406f9fc0ae5c829e53b0";
        HttpUtil.sendOkHttpRequest(weatherUri, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    //处理并站视weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime;
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        /*aqiLayout.removeAllViews();*/

        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more);
            maxText.setText(forecast.max);
            minText.setText(forecast.min);
            forecastLayout.addView(view);
        }
        /*for (hourly hour:weather.hourlies) {
            View view = LayoutInflater.from(this).inflate(R.layout.aqi_item,aqiLayout,false);
            TextView time_hour = (TextView) view.findViewById(R.id.time_hour);
            TextView txt = (TextView) view.findViewById(R.id.txt);
            TextView tmp = (TextView) view.findViewById(R.id.time_hour);
            tmp.setText(hour.temperature);
            txt.setText(hour.info);
            time_hour.setText(hour.time);
            aqiLayout.addView(view);

        }*/
        for (Lifestyle lifestyle : weather.lifestyles) {
            if (lifestyle.type.equals("comf") ) {
                String comfort = "舒适度：" + lifestyle.info;
                comfortText.setText(comfort);
            }
            if (lifestyle.type.equals("cw") ) {
                String carWash = "洗车指数：" + lifestyle.info;
                carWashText.setText(carWash);
            }
            if (lifestyle.type.equals("sport")) {
                String sport = "运动建议：" + lifestyle.info;
                sportText.setText(sport);
            }
        }
        weatherLayout.setVisibility(View.VISIBLE);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }



}
