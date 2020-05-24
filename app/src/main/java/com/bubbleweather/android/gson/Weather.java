package com.bubbleweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

//总的实例类，引用刚才创建的各个实体类
public class Weather {
    public String status;
    public Basic basic;
    public Now now;
    public Updata update;

    @SerializedName("hourly")
    public List<hourly> hourlies;

    @SerializedName("lifestyle")
    public List<Lifestyle> lifestyles;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
