package com.example.weather;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.weather.adapter.DailyForecastAdapter;
import com.example.weather.adapter.HourlyForecastAdapter;
import com.example.weather.manager.WeatherNotificationManager;
import com.example.weather.model.CityData;
import com.example.weather.model.DailyForecast;
import com.example.weather.model.HourlyForecast;
import com.example.weather.model.WeatherData;
import com.example.weather.model.WeatherWarning;
import com.example.weather.service.DiaryReminderService;
import com.example.weather.service.WeatherService;
import com.permissionx.guolindev.PermissionX;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private WeatherNotificationManager notificationManager;

    private Button btnSearch;
    private ImageButton btnLocation, btnMusic, btnSettings, btnDiary;
    private TextView tvCurrentCity, tvTemperature, tvWeatherCondition, tvFeelsLike;
    private TextView tvWind, tvHumidity, tvVisibility, tvPressure, tvUpdateTime;
    private ImageView ivWeatherIcon;
    private RecyclerView rvHourlyForecast, rvDailyForecast;
    private Button btnViewMoreHours, btnViewMoreDays;

    private WeatherService weatherService;
    private LocationClient locationClient;
    private static final String SHENYANG_CITY_ID = "101070101";
    private static final String SHENYANG_CITY_NAME = "沈阳";
    private String currentCityId = "";
    private String currentCityName = "";
    private static final int REQUEST_CODE_CITY_SELECTION = 1;

    private HourlyForecastAdapter hourlyAdapter;
    private DailyForecastAdapter dailyAdapter;

    // 添加通知权限请求码
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initServices();
        setupClickListeners();
        setupAdapters();

        // 默认加载北京天气
        loadDefaultWeather();
        // 在初始化后请求通知权限
        requestNotificationPermission();
    }

    private void initViews() {
        btnSearch = findViewById(R.id.btnSearch);
        btnLocation = findViewById(R.id.btnLocation);
        tvCurrentCity = findViewById(R.id.tvCurrentCity);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeatherCondition = findViewById(R.id.tvWeatherCondition);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvWind = findViewById(R.id.tvWind);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvVisibility = findViewById(R.id.tvVisibility);
        tvPressure = findViewById(R.id.tvPressure);
        tvUpdateTime = findViewById(R.id.tvUpdateTime);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        rvHourlyForecast = findViewById(R.id.rvHourlyForecast);
        rvDailyForecast = findViewById(R.id.rvDailyForecast);
        btnViewMoreHours = findViewById(R.id.btnViewMoreHours);
        btnViewMoreDays = findViewById(R.id.btnViewMoreDays);
        btnSettings = findViewById(R.id.btnSettings);
        notificationManager = new WeatherNotificationManager(this);
        btnMusic = findViewById(R.id.btnMusic);
        btnDiary = findViewById(R.id.btnDiary);
    }

    private void setupAdapters() {
        // 设置逐小时预报适配器
        hourlyAdapter = new HourlyForecastAdapter();
        rvHourlyForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHourlyForecast.setAdapter(hourlyAdapter);

        // 设置多日预报适配器
        dailyAdapter = new DailyForecastAdapter();
        rvDailyForecast.setLayoutManager(new LinearLayoutManager(this));
        rvDailyForecast.setAdapter(dailyAdapter);
    }

    private void initServices() {
        weatherService = new WeatherService();
        notificationManager = new WeatherNotificationManager(this);
        DiaryReminderService.scheduleReminderCheck(this);

        // 设置同意隐私政策
        LocationClient.setAgreePrivacy(true);

        try {
            // 初始化定位客户端
            locationClient = new LocationClient(getApplicationContext());

            // 设置定位选项
            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            option.setCoorType("bd09ll");
            option.setIsNeedAddress(true);
            option.setOpenGps(true);
            option.setScanSpan(0); // 单次定位
            option.setIsNeedLocationDescribe(true); // 获取位置描述信息

            // 添加以下重要设置
            option.setIsNeedLocationDescribe(true);
            option.setIsNeedLocationPoiList(true);
            option.setIgnoreKillProcess(false);
            option.SetIgnoreCacheException(false);
            option.setWifiCacheTimeOut(5*60*1000);
            option.setEnableSimulateGps(false);

            locationClient.setLocOption(option);

            locationClient.registerLocationListener(new BDAbstractLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation location) {
                    if (location == null) {
                        showError("定位失败: 位置为空");
                        return;
                    }

                    // 处理错误码
                    if (location.getLocType() != BDLocation.TypeGpsLocation &&
                            location.getLocType() != BDLocation.TypeNetWorkLocation) {

                        String errorInfo;
                        switch (location.getLocType()) {
                            case 162:
                                errorInfo = "API KEY错误或未配置";
                                break;
                            case 167:
                                errorInfo = "定位服务未启动";
                                break;
                            case 505:
                                errorInfo = "AK不存在或者非法";
                                break;
                            default:
                                errorInfo = "错误码: " + location.getLocType();
                        }
                        showError("定位失败: " + errorInfo);
                        return;
                    }

                    // 处理成功定位...
                    String city = location.getCity();
                    if (city != null && !city.isEmpty()) {
                        // 去掉"市"字
                        if (city.endsWith("市")) {
                            city = city.substring(0, city.length() - 1);
                        }
                        searchCityAndLoadWeather(city);
                    }
                }

                private void showError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                        locationClient.stop();
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "定位初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动城市选择界面
                Intent intent = new Intent(MainActivity.this, CitySelectionActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CITY_SELECTION);
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 先尝试正常定位
                requestLocationPermissionAndLocate();

                // 添加一个延迟处理，如果定位失败则自动切换到沈阳
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setCityToShenyang();
                    }
                }, 4000); // 4秒后检查定位结果
            }
        });

        btnViewMoreHours.setOnClickListener(v -> {
            // 跳转到逐小时预报详情页面
            Intent intent = new Intent(MainActivity.this, HourlyForecastActivity.class);
            intent.putExtra("cityId", currentCityId);
            startActivity(intent);
        });

        btnViewMoreDays.setOnClickListener(v -> {
            // 跳转到多日预报详情页面
            Intent intent = new Intent(MainActivity.this, DailyForecastActivity.class);
            intent.putExtra("cityId", currentCityId);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        btnMusic.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MusicPlayerActivity.class));
        });

        btnDiary.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DiaryListActivity.class));
        });
    }

    private void loadDefaultWeather() {
        // 默认加载北京天气
        currentCityId = "101010100";
        currentCityName = "北京";
        tvCurrentCity.setText("当前城市: " + currentCityName);
        loadWeather(currentCityId);
    }

    private void searchCityAndLoadWeather(String cityName) {
        weatherService.searchCities(cityName, new WeatherService.CityCallback() {
            @Override
            public void onSuccess(List<CityData> cities) {
                if (!cities.isEmpty()) {
                    CityData city = cities.get(0); // 选择第一个匹配的城市
                    currentCityId = city.getId();
                    currentCityName = city.getName();
                    tvCurrentCity.setText("当前城市: " + currentCityName);
                    loadWeather(currentCityId);
                } else {
                    Toast.makeText(MainActivity.this, "未找到该城市", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "搜索失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWeather(String locationId) {
        // 加载当前天气
        weatherService.getCurrentWeather(locationId, new WeatherService.WeatherCallback() {
            @Override
            public void onSuccess(WeatherData weatherData) {
                updateWeatherUI(weatherData);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "获取天气失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // 加载逐小时预报
        weatherService.getHourlyForecast(locationId, 24, new WeatherService.HourlyForecastCallback() {
            @Override
            public void onSuccess(List<HourlyForecast> forecasts) {
                hourlyAdapter.setForecasts(forecasts);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "获取小时预报失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // 加载多日预报
        weatherService.getDailyForecast(locationId, 3, new WeatherService.DailyForecastCallback() {
            @Override
            public void onSuccess(List<DailyForecast> forecasts) {
                dailyAdapter.setForecasts(forecasts);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "获取多日预报失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // 添加天气预警检查
        weatherService.getWeatherWarnings(locationId, new WeatherService.WeatherWarningCallback() {
            @Override
            public void onSuccess(List<WeatherWarning> warnings) {
                if (!warnings.isEmpty()) {
                    WeatherWarning mostImportant = warnings.get(0);

                    // 添加权限检查
                    if (notificationManager.hasNotificationPermission()) {
                        notificationManager.showWeatherWarning(mostImportant);
                    } else {
                        Log.w("MainActivity", "没有通知权限，无法显示预警");
                    }
                } else {
                    // 添加权限检查
                    if (notificationManager.hasNotificationPermission()) {
                        notificationManager.showNoWarningNotification();
                    } else {
                        Log.w("MainActivity", "没有通知权限，无法显示无预警通知");
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "获取天气预警失败: " + error, Toast.LENGTH_SHORT).show();

                // 添加权限检查
                if (notificationManager.hasNotificationPermission()) {
                    notificationManager.showNoWarningNotification();
                } else {
                    Log.w("MainActivity", "没有通知权限，无法显示无预警通知");
                }
            }
        });
    }

    private void updateWeatherUI(WeatherData weatherData) {
        tvTemperature.setText(weatherData.getTemperature() + "°C");
        tvWeatherCondition.setText(weatherData.getWeatherCondition());
        tvFeelsLike.setText("体感温度: " + weatherData.getFeelsLike() + "°C");
        tvWind.setText(weatherData.getWindDirection() + " " + weatherData.getWindScale() + "级");
        tvHumidity.setText(weatherData.getHumidity() + "%");
        tvVisibility.setText(weatherData.getVisibility() + "km");
        tvPressure.setText(weatherData.getPressure() + "hPa");

        // 格式化更新时间
        String updateTime = formatUpdateTime(weatherData.getUpdateTime());
        tvUpdateTime.setText("更新时间: " + updateTime);

        // 根据天气图标代码设置图标
        setWeatherIcon(weatherData.getWeatherIcon());
    }

    private void setWeatherIcon(String iconCode) {
        // 根据和风天气的图标代码设置对应图标
        int iconResId = R.drawable.ic_sunny; // 默认图标

        try {
            int code = Integer.parseInt(iconCode);
            if (code == 100) {
                iconResId = R.drawable.ic_sunny; // 晴
            } else if (code >= 101 && code <= 103) {
                iconResId = R.drawable.ic_partly_cloudy; // 多云
            } else if (code >= 300 && code <= 318) {
                iconResId = R.drawable.ic_rainy; // 雨
            } else if (code >= 400 && code <= 499) {
                iconResId = R.drawable.ic_snowy; // 雪
            } else {
                iconResId = R.drawable.ic_cloudy; // 其他情况默认多云
            }
        } catch (NumberFormatException e) {
            // 如果解析失败，使用默认图标
        }

        ivWeatherIcon.setImageResource(iconResId);
    }

    private String formatUpdateTime(String updateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(updateTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return updateTime;
        }
    }

    private void requestLocationPermissionAndLocate() {
        PermissionX.init(this)
                .permissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE, // 添加此权限
                        Manifest.permission.WRITE_EXTERNAL_STORAGE // 添加此权限
                )
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        startLocation();
                    }
                });
    }

    private void startLocation() {
        if (locationClient != null) {
            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            option.setCoorType("bd09ll");
            option.setScanSpan(0);
            option.setIsNeedAddress(true);
            option.setOpenGps(true);
            option.setLocationNotify(true);
            option.setIgnoreKillProcess(false);
            option.SetIgnoreCacheException(false);
            option.setWifiCacheTimeOut(5*60*1000);
            option.setEnableSimulateGps(false);

            locationClient.setLocOption(option);
            locationClient.start();

            Toast.makeText(this, "正在定位...", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCityToShenyang() {
        currentCityId = SHENYANG_CITY_ID;
        currentCityName = SHENYANG_CITY_NAME;
        tvCurrentCity.setText("当前城市: " + currentCityName);
        loadWeather(currentCityId);
        Toast.makeText(MainActivity.this, "已切换到沈阳", Toast.LENGTH_SHORT).show();
    }

    // 请求通知权限的方法
    private void requestNotificationPermission() {
        // 仅 Android 13+ 需要动态请求通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                // 请求通知权限
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，重新加载天气以显示通知
                loadWeather(currentCityId);
            } else {
                Toast.makeText(this, "通知权限被拒绝，预警功能将不可用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CITY_SELECTION && resultCode == RESULT_OK && data != null) {
            currentCityId = data.getStringExtra("cityId");
            currentCityName = data.getStringExtra("cityName");
            tvCurrentCity.setText("当前城市: " + currentCityName);
            loadWeather(currentCityId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationClient != null) {
            locationClient.stop();
        }
    }
}