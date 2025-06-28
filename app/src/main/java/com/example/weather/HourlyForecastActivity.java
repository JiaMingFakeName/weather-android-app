package com.example.weather;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.adapter.HourlyForecastAdapter;
import com.example.weather.model.HourlyForecast;
import com.example.weather.service.WeatherService;

import java.util.List;

public class HourlyForecastActivity extends AppCompatActivity {

    private TextView tvTitle;
    private RecyclerView rvHourlyForecast;
    private HourlyForecastAdapter adapter;
    private WeatherService weatherService;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly_forecast);

        // 初始化返回按钮
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 关闭当前活动
            }
        });

        String cityId = getIntent().getStringExtra("cityId");
        if (cityId == null || cityId.isEmpty()) {
            finish();
            return;
        }

        initViews();
        loadHourlyForecast(cityId);
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        rvHourlyForecast = findViewById(R.id.rvHourlyForecast);

        weatherService = new WeatherService();
        adapter = new HourlyForecastAdapter();

        rvHourlyForecast.setLayoutManager(new LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,  // 横向排列
                false));
        rvHourlyForecast.setAdapter(adapter);
    }

    private void loadHourlyForecast(String cityId) {
        weatherService.getHourlyForecast(cityId, 24, new WeatherService.HourlyForecastCallback() {
            @Override
            public void onSuccess(List<HourlyForecast> forecasts) {
                adapter.setForecasts(forecasts);
                tvTitle.setText("24小时预报 (" + forecasts.size() + "条数据)");
            }

            @Override
            public void onError(String error) {
                tvTitle.setText("24小时预报 (获取失败)");
            }
        });
    }
}