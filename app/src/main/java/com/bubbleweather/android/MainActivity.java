package com.bubbleweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //在其中加入一个缓存数据的判断
        //一开始先从SharedPreferences文件中读取缓存数据
        //如果不为null就说明之前已经请求过天气数据了。
        //没必要再让用户选择城市，而是直接跳到WeatherActivity
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getString("weather", null) != null) {

            Intent intent = new Intent(this, WeatherActivity.class);

            startActivity(intent);

            finish();

        }
    }
}
