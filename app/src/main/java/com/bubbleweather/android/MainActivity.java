package com.bubbleweather.android;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bubbleweather.android.util.HttpUtil;
import com.bubbleweather.android.util.Utility;
import com.bubbleweather.android.util.cityId;
import com.bubbleweather.android.util.cityInfo_basic;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //创建一个LocationClient实例
    public LocationClient mLocationClient;
    public TextView textview;

    public double LongitudeId;//经度
    public double LatitudeId; //纬度

    //每日一图
    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //使背景和标题栏融为一体
        //当版本号大于21，也就是系统5.0以上执行本代码
        //调用getWindow().getDecorView()方法拿到当前活动的DecorView;
        //再调用setSystemUiVisibility（）方法来改变系统UI显示
        //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN和 View.SYSTEM_UI_FLAG_LAYOUT_STABLE就表示活动的布局会显示在状态栏上面
        //setStatusBarColor（）状态栏透明
        if	(Build.VERSION.SDK_INT	>=	21)	{
            View decorView	=	getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
            setContentView(R.layout.activity_main);

        //初始化各控件
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = pref.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();

        }

        //LocationClient的构造函数接受一个Context参数
        //调用getApplicationContext（）方法来获取一个全局的Context参数并传入
        mLocationClient = new LocationClient(getApplicationContext());
        //然后调用LocationClient的registerLocationListener（）方法来注册一个定位监听器
        //获取到位置信息的时候，就会回调这个定位监听器
        mLocationClient.registerLocationListener(new MyLocationListener());


        //声明的权限中，ACCESS_COARSE_LOCATION、ACCESS_FINE_LOCATION、READ_PHONE_START、WRITE_EXTERNAL_STOPAGE
        //这四个权限需要进行运行时权限处理，
        // ACCESS_FINE_LOCATION和ACCESS_COARSE_LOCATION属于一个权限组，申明一个就可以
        //一次性申请3个权限，我们首先创建一个空的List集合，然后一次判断3个权限有没有授权，
        //如果没有就添加到List集合，最后讲List集合转换成数组，
        //最后调用Activityompat.requestPermissions()一次性申请
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

        //在其中加入一个缓存数据的判断
        //一开始先从SharedPreferences文件中读取缓存数据
        //如果不为null就说明之前已经请求过天气数据了。
        //没必要再让用户选择城市，而是直接跳到WeatherActivity
        /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getString("weather", null) != null) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }*/
    }

    private void requestLocation() {
        //知识调用一下LocationClient的start（）方法就能开始定位
        initLocation();
        mLocationClient.start();
    }

    //增加了一个initLocation（）方法
    //创建一个LocationClientOption对象，然后调用它的setScanSpan（）方法来设置更新的间隔
    //这里传入5000，表示每5秒会更新一下当前的位置
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(500000000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    //onRequestPermissionsResult()方法，通过循环讲申请的每一个权限进行判断，
    // 如果有仍和一个权限被拒绝,直接finish（）
    //只有所有权限被用户同意，才能调用requestPermissonResult（）方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意全部权限", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    //定位结果赋值
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            LongitudeId = location.getLongitude();
            LatitudeId = location.getLatitude();
            //根据经纬度，请求服务器数据
            requestCityInfo(LongitudeId, LatitudeId);
        }
    }

    //requestCityInfo函数，向服务器请求城市数据
    public void requestCityInfo(double longitudeId, double latitudeId) {
        String cityUrl = "https://search.heweather.net/find?location=" + longitudeId + "," +
                latitudeId + "&key=7aa2b75d9e5e406f9fc0ae5c829e53b0";

        HttpUtil.sendOkHttpRequest(cityUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "经纬度请求城市信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseCityInfoText = response.body().string();
                System.out.println("返回信息：" + responseCityInfoText);
                //把返回的数据交给Utility进行GSON解析
                cityId cityid = new cityId();
                cityid = Utility.handleCityIdResponse(responseCityInfoText);
                for (cityInfo_basic basic : cityid.basicList) {
                    //根据当前经纬度得出的城市ID，
                    //利用该ID直接向和风天气API请求城市的天气信息
                    String weatherId = basic.cityID;
                    Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //要在活动销毁的时候调用LocationClient的stop（）方法停止定位
        mLocationClient.stop();
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
}
