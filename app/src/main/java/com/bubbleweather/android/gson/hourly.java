package com.bubbleweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class hourly {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("time")
    public String time;

    @SerializedName("cond_txt")
    public String info;
}
