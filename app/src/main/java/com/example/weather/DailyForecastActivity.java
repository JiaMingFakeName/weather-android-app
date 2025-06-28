package com.example.weather;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.adapter.DailyForecastAdapter;
import com.example.weather.model.DailyForecast;
import com.example.weather.service.WeatherService;

import java.util.List;

public class DailyForecastActivity extends AppCompatActivity {

    private TextView tvTitle;
    private RecyclerView rvDailyForecast;
    private DailyForecastAdapter adapter;
    private WeatherService weatherService;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);

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
        loadDailyForecast(cityId);
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        rvDailyForecast = findViewById(R.id.rvDailyForecast);

        weatherService = new WeatherService();
        adapter = new DailyForecastAdapter();

        rvDailyForecast.setLayoutManager(new LinearLayoutManager(this));
        rvDailyForecast.setAdapter(adapter);
    }

    private void loadDailyForecast(String cityId) {
        weatherService.getDailyForecast(cityId, 15, new WeatherService.DailyForecastCallback() {
            @Override
            public void onSuccess(List<DailyForecast> forecasts) {
                adapter.setForecasts(forecasts);
                tvTitle.setText("15天预报 (" + forecasts.size() + "条数据)");
            }

            @Override
            public void onError(String error) {
                tvTitle.setText("15天预报 (获取失败)");
            }
        });
    }
}