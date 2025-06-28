package com.example.weather;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.adapter.CityAdapter;
import com.example.weather.model.CityData;
import com.example.weather.service.WeatherService;

import java.util.ArrayList;
import java.util.List;

public class CitySelectionActivity extends AppCompatActivity {

    private Button btnBack, btnCitySearch;
    private EditText etCitySearch;
    private TextView tvSearchResult;
    private RecyclerView rvHotCities, rvSearchResults;

    private CityAdapter hotCitiesAdapter, searchResultsAdapter;
    private WeatherService weatherService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_selection);

        initViews();
        initAdapters();
        setupClickListeners();
        loadHotCities();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCitySearch = findViewById(R.id.btnCitySearch);
        etCitySearch = findViewById(R.id.etCitySearch);
        tvSearchResult = findViewById(R.id.tvSearchResult);
        rvHotCities = findViewById(R.id.rvHotCities);
        rvSearchResults = findViewById(R.id.rvSearchResults);

        weatherService = new WeatherService();
    }

    private void initAdapters() {
        hotCitiesAdapter = new CityAdapter();
        hotCitiesAdapter.setOnCityClickListener(this::selectCity);
        rvHotCities.setLayoutManager(new GridLayoutManager(this, 4));
        rvHotCities.setAdapter(hotCitiesAdapter);

        searchResultsAdapter = new CityAdapter();
        searchResultsAdapter.setOnCityClickListener(this::selectCity);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(searchResultsAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCitySearch.setOnClickListener(v -> {
            String query = etCitySearch.getText().toString().trim();
            if (!query.isEmpty()) {
                searchCities(query);
            }
        });

        etCitySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() > 1) {
                    searchCities(query);
                } else {
                    tvSearchResult.setVisibility(View.GONE);
                    searchResultsAdapter.setCities(new ArrayList<>());
                }
            }
        });
    }

    private void loadHotCities() {
        List<CityData> hotCities = new ArrayList<>();
        hotCities.add(new CityData("北京", "101010100", "中国", "北京", "北京"));
        hotCities.add(new CityData("上海", "101020100", "中国", "上海", "上海"));
        hotCities.add(new CityData("广州", "101280101", "中国", "广东", "广州"));
        hotCities.add(new CityData("深圳", "101280601", "中国", "广东", "深圳"));
        hotCities.add(new CityData("杭州", "101210101", "中国", "浙江", "杭州"));
        hotCities.add(new CityData("南京", "101190101", "中国", "江苏", "南京"));
        hotCities.add(new CityData("成都", "101270101", "中国", "四川", "成都"));
        hotCities.add(new CityData("西安", "101110101", "中国", "陕西", "西安"));

        hotCitiesAdapter.setCities(hotCities);
    }

    private void searchCities(String query) {
        weatherService.searchCities(query, new WeatherService.CityCallback() {
            @Override
            public void onSuccess(List<CityData> cities) {
                tvSearchResult.setVisibility(View.VISIBLE);
                searchResultsAdapter.setCities(cities);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CitySelectionActivity.this, "搜索失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectCity(CityData city) {
        Intent result = new Intent();
        result.putExtra("cityId", city.getId());
        result.putExtra("cityName", city.getName());
        setResult(RESULT_OK, result);
        finish();
    }
}