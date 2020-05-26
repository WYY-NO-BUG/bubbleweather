package com.bubbleweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bubbleweather.android.db.SaveDate;
import com.bubbleweather.android.gson.Forecast;
import com.bubbleweather.android.gson.Lifestyle;
import com.bubbleweather.android.gson.Weather;
import com.bubbleweather.android.service.AutoUpdateService;
import com.bubbleweather.android.util.HttpUtil;
import com.bubbleweather.android.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.List;

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

    //定位功能
    private Button mlocation;

    //收藏城市功能
    private Button collect;
    private List<SaveDate> saveDateList;
    private String savecsName;
    private String savecsCode;

    //滑动选择
    public DrawerLayout drawerLayout;
    private Button navButton;

    //下拉刷新
    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

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

        //定位
        mlocation = (Button)findViewById(R.id.location);

        //城市收藏
        collect = (Button) findViewById(R.id.collect);


        //滑动切换
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);


        //下拉刷新代码
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
//        String weatherId = prefs.getString("weatherId",null);
        String weatherId = getIntent().getStringExtra("weather_id");

        //设置点击事件
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //定位按钮点击
        mlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = getIntent().getStringExtra("mlocation");
                requestWeather(id);
            }
        });


        //收藏按钮点击事件
        collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 从数据库查看，是否只存储了单一城市
                saveDateList = LitePal.where("csName=?", savecsName).find(SaveDate.class);
                if (saveDateList.size() > 0) {
                    collect.setBackgroundResource(R.drawable.collect1);
                    LitePal.deleteAll(SaveDate.class, "csName = ?", savecsName);
                    Toast.makeText(WeatherActivity.this, "已取消收藏" + " " + savecsName + " "
                            + savecsCode, Toast.LENGTH_SHORT).show();
                } else {
                    collect.setBackgroundResource(R.drawable.collect2);
                    SaveDate saveData = new SaveDate();
                    saveData.setCsName(savecsName);
                    saveData.setCsCode(savecsCode);
                    saveData.save();
                    Toast.makeText(WeatherActivity.this, "收藏成功" + " " + savecsName + " "
                            + savecsCode, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (weatherString != null) {
            //有缓存的时候直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

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
                            editor.putString("weatherId", weatherId);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
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

    //处理并展示weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        savecsName = weather.basic.cityName;
        savecsCode = weather.basic.weatherId;

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
            if (lifestyle.type.equals("comf")) {
                String comfort = "舒适度：" + lifestyle.info;
                comfortText.setText(comfort);
            }
            if (lifestyle.type.equals("cw")) {
                String carWash = "洗车指数：" + lifestyle.info;
                carWashText.setText(carWash);
            }
            if (lifestyle.type.equals("sport")) {
                String sport = "运动建议：" + lifestyle.info;
                sportText.setText(sport);
            }
        }

        //激活服务
        weatherLayout.setVisibility(View.VISIBLE);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }


}
