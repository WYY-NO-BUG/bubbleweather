package com.bubbleweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    //city城市，id城市对应的天气id
    //update钟的loc表示天气的更新时间
    @SerializedName("location")
    public String cityName;

    @SerializedName("cid")
    public String weatherId;


}
