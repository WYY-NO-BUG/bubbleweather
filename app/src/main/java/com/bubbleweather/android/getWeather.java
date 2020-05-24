package com.bubbleweather.android;

import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.bubbleweather.android.db.City;
import com.bubbleweather.android.db.County;
import com.bubbleweather.android.db.Province;
import com.bubbleweather.android.util.HttpUtil;
import com.bubbleweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.LitePalApplication.getContext;

public class getWeather {
    public List<Province> provinceList;
    public List<City> cityList;
    public List<County> countyList;

    public Province selectedProvince;
    public City selectedCity;
    public County selectedCounty;

    public BDLocation location;

    public getWeather(BDLocation location) {
        this.location = location;
    }

    public void getProvinces() {
        provinceList = LitePal.select("provinceCode").where("provinceName = ?", location.getProvince()).
                find(Province.class);
        if (provinceList.size() > 0) {
            selectedProvince = provinceList.get(0);
            getCities();
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    public void getCities() {
        cityList = LitePal.select("cityCode").where("cityName = ?", location.getCity()).
                find(City.class);
        if (cityList.size() > 0) {
            //获取定位城市
            selectedCity = cityList.get(0);
        } else {
            int proviceId = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china" + proviceId;
            queryFromServer(address, "city");
        }
    }

    public void getCounties() {
        countyList = LitePal.select("weatherId").where("countyName = ?", location.getDistrict()).
                find(County.class);
        if (countyList.size() > 0) {
            //获取定位城市
            selectedCounty = countyList.get(0);
        } else {
            int proviceId = selectedProvince.getProvinceCode();
            int cityId = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china" + proviceId + cityId;
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type) {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {

                    if ("province".equals(type)) {
                        getProvinces();
                    } else if ("city".equals(type)) {
                        getCities();
                    } else if ("county".equals(type)) {
                        getCounties();
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT)
                        .show();

            }
        });
    }

    public String getId() {
        return selectedCounty.getWeatherId();
    }

}
