package com.bubbleweather.android.util;

/*
 * 提供一个工具类
 * 解析和传力服务器返回的省市县级数据
 * 因为服务器返的省市县都是json格式的
 * */

import android.text.TextUtils;

import com.bubbleweather.android.db.City;
import com.bubbleweather.android.db.County;
import com.bubbleweather.android.db.Province;
import com.bubbleweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
   public static boolean handleProvinceResponse(String response) {
       if (!TextUtils.isEmpty(response)) {
           JSONArray allPtovinces = null;
           try {
               allPtovinces = new JSONArray(response);
               for (int i = 0; i < allPtovinces.length(); i++) {
                   JSONObject provinceObject = allPtovinces.getJSONObject(i);
                   Province province = new Province();
                   province.setPrivinceName(provinceObject.getString("name"));
                   province.setProvinceCode(provinceObject.getInt("id"));
                   province.save();
               }
               return true;
           } catch (JSONException e) {
               e.printStackTrace();
           }

       }
           return false;
   }
    public static boolean handleCityResponse(String response,int provinceId) {
        if (!TextUtils.isEmpty(response)){
            JSONArray allCities = null;
            try {
                allCities = new JSONArray(response);
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
    public static boolean handleCountyResponse(String response,int cityId) {
        if (!TextUtils.isEmpty(response)){
            JSONArray allCounties = null;
            try {
                allCounties = new JSONArray(response);
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

    //将返回的JSON数据解析称Weather实体类
    //该方法通过JSONObject和JSONArray将天气数据中的主体内容解析出来
    //之前已经按照数据格式定义国对应的GSON实体类，因此需要通过调用fromJ送（）方法，直接将JSON数据转换城Weather对象。
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
