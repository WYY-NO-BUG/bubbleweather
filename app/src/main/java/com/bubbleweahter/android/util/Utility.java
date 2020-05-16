package com.bubbleweahter.android.util;

/*
 * 提供一个工具类
 * 解析和传力服务器返回的省市县级数据
 * 因为服务器返的省市县都是json格式的
 * */

import android.text.TextUtils;

import com.bubbleweahter.android.db.City;
import com.bubbleweahter.android.db.County;
import com.bubbleweahter.android.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
   public static boolean handleProvinceResponse(String response) {
       if (!TextUtils.isEmpty(response)) {
           JSONArray allPtovince = null;
           try {
               allPtovince = new JSONArray(response);
               for (int i = 0; i < allPtovince.length(); i++) {
                   JSONObject provinceObject = allPtovince.getJSONObject(i);
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
                    county.getCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;
    }
}
