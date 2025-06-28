package com.example.weather.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.weather.model.CityData;
import com.example.weather.model.DailyForecast;
import com.example.weather.model.HourlyForecast;
import com.example.weather.model.WeatherData;
import com.example.weather.model.WeatherWarning;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherService {
    private static final String TAG = "WeatherService";
    private static final String BASE_URL = "https://n65ctu5gam.re.qweatherapi.com";
    private static final String API_KEY = "5bdf704a39274456b1572b9e7c0da063";

    private OkHttpClient client;
    private Gson gson;
    private ExecutorService executor;
    private Handler mainHandler;

    public WeatherService() {
        client = new OkHttpClient();
        gson = new Gson();
        executor = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface WeatherCallback {
        void onSuccess(WeatherData weatherData);
        void onError(String error);
    }

    public interface CityCallback {
        void onSuccess(List<CityData> cities);
        void onError(String error);
    }

    public interface DailyForecastCallback {
        void onSuccess(List<DailyForecast> forecasts);
        void onError(String error);
    }

    public interface HourlyForecastCallback {
        void onSuccess(List<HourlyForecast> forecasts);
        void onError(String error);
    }

    public void searchCities(String query, CityCallback callback) {
        String url = BASE_URL + "/geo/v2/city/lookup?key=" + API_KEY + "&location=" + query;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("网络请求失败: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "City search response: " + responseBody);

                    try {
                        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                        String code = jsonObject.get("code").getAsString();

                        if ("200".equals(code)) {
                            JsonArray locationArray = jsonObject.getAsJsonArray("location");
                            List<CityData> cities = new ArrayList<>();

                            for (int i = 0; i < locationArray.size(); i++) {
                                JsonObject cityObj = locationArray.get(i).getAsJsonObject();
                                CityData city = new CityData();
                                city.setName(cityObj.get("name").getAsString());
                                city.setId(cityObj.get("id").getAsString());
                                city.setCountry(cityObj.get("country").getAsString());
                                city.setAdm1(cityObj.get("adm1").getAsString());
                                city.setAdm2(cityObj.get("adm2").getAsString());
                                cities.add(city);
                            }

                            mainHandler.post(() -> callback.onSuccess(cities));
                        } else {
                            mainHandler.post(() -> callback.onError("搜索失败，错误代码: " + code));
                        }
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("数据解析失败: " + e.getMessage()));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("请求失败，响应码: " + response.code()));
                }
            }
        });
    }

    public void getCurrentWeather(String locationId, WeatherCallback callback) {
        String url = BASE_URL + "/v7/weather/now?key=" + API_KEY + "&location=" + locationId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("网络请求失败: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Weather response: " + responseBody);

                    try {
                        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                        String code = jsonObject.get("code").getAsString();

                        if ("200".equals(code)) {
                            JsonObject nowObj = jsonObject.getAsJsonObject("now");
                            WeatherData weatherData = new WeatherData();

                            weatherData.setTemperature(nowObj.get("temp").getAsString());
                            weatherData.setWeatherCondition(nowObj.get("text").getAsString());
                            weatherData.setFeelsLike(nowObj.get("feelsLike").getAsString());
                            weatherData.setWindDirection(nowObj.get("windDir").getAsString());
                            weatherData.setWindScale(nowObj.get("windScale").getAsString());
                            weatherData.setHumidity(nowObj.get("humidity").getAsString());
                            weatherData.setVisibility(nowObj.get("vis").getAsString());
                            weatherData.setPressure(nowObj.get("pressure").getAsString());
                            weatherData.setWeatherIcon(nowObj.get("icon").getAsString());
                            weatherData.setUpdateTime(jsonObject.get("updateTime").getAsString());

                            mainHandler.post(() -> callback.onSuccess(weatherData));
                        } else {
                            mainHandler.post(() -> callback.onError("获取天气失败，错误代码: " + code));
                        }
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("数据解析失败: " + e.getMessage()));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("请求失败，响应码: " + response.code()));
                }
            }
        });
    }

    public void getDailyForecast(String locationId, int days, DailyForecastCallback callback) {
        String url = BASE_URL + "/v7/weather/" + days + "d?key=" + API_KEY + "&location=" + locationId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("网络请求失败: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Daily forecast response: " + responseBody);

                    try {
                        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                        String code = jsonObject.get("code").getAsString();

                        if ("200".equals(code)) {
                            JsonArray dailyArray = jsonObject.getAsJsonArray("daily");
                            List<DailyForecast> forecasts = new ArrayList<>();

                            for (int i = 0; i < dailyArray.size(); i++) {
                                JsonObject dailyObj = dailyArray.get(i).getAsJsonObject();
                                DailyForecast forecast = new DailyForecast();

                                forecast.setDate(dailyObj.get("fxDate").getAsString());
                                forecast.setMinTemp(dailyObj.get("tempMin").getAsString());
                                forecast.setMaxTemp(dailyObj.get("tempMax").getAsString());
                                forecast.setDayCondition(dailyObj.get("textDay").getAsString());
                                forecast.setNightCondition(dailyObj.get("textNight").getAsString());
                                forecast.setDayIcon(dailyObj.get("iconDay").getAsString());
                                forecast.setNightIcon(dailyObj.get("iconNight").getAsString());
                                forecast.setWindDirDay(dailyObj.get("windDirDay").getAsString());
                                forecast.setWindScaleDay(dailyObj.get("windScaleDay").getAsString());
                                forecast.setWindDirNight(dailyObj.get("windDirNight").getAsString());
                                forecast.setWindScaleNight(dailyObj.get("windScaleNight").getAsString());
                                forecast.setHumidity(dailyObj.get("humidity").getAsString());
                                forecast.setUvIndex(dailyObj.get("uvIndex").getAsString());

                                forecasts.add(forecast);
                            }

                            mainHandler.post(() -> callback.onSuccess(forecasts));
                        } else {
                            mainHandler.post(() -> callback.onError("获取天气预报失败，错误代码: " + code));
                        }
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("数据解析失败: " + e.getMessage()));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("请求失败，响应码: " + response.code()));
                }
            }
        });
    }

    public void getHourlyForecast(String locationId, int hours, HourlyForecastCallback callback) {
        String url = BASE_URL + "/v7/weather/" + hours + "h?key=" + API_KEY + "&location=" + locationId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("网络请求失败: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Hourly forecast response: " + responseBody);

                    try {
                        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                        String code = jsonObject.get("code").getAsString();

                        if ("200".equals(code)) {
                            JsonArray hourlyArray = jsonObject.getAsJsonArray("hourly");
                            List<HourlyForecast> forecasts = new ArrayList<>();

                            for (int i = 0; i < hourlyArray.size(); i++) {
                                JsonObject hourlyObj = hourlyArray.get(i).getAsJsonObject();
                                HourlyForecast forecast = new HourlyForecast();

                                String time = hourlyObj.get("fxTime").getAsString();
                                forecast.setTime(formatHourlyTime(time));
                                forecast.setTemp(hourlyObj.get("temp").getAsString());
                                forecast.setCondition(hourlyObj.get("text").getAsString());
                                forecast.setIcon(hourlyObj.get("icon").getAsString());
                                forecast.setWindDir(hourlyObj.get("windDir").getAsString());
                                forecast.setWindScale(hourlyObj.get("windScale").getAsString());
                                forecast.setHumidity(hourlyObj.get("humidity").getAsString());

                                forecasts.add(forecast);
                            }

                            mainHandler.post(() -> callback.onSuccess(forecasts));
                        } else {
                            mainHandler.post(() -> callback.onError("获取小时预报失败，错误代码: " + code));
                        }
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("数据解析失败: " + e.getMessage()));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("请求失败，响应码: " + response.code()));
                }
            }
        });
    }

    private String formatHourlyTime(String time) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return time.substring(11, 16); // 格式为 "2023-06-13T20:00+08:00"
        }
    }

    // 在WeatherService.java中添加这些接口和方法

    public interface WeatherWarningCallback {
        void onSuccess(List<WeatherWarning> warnings);
        void onError(String error);
    }

    public void getWeatherWarnings(String locationId, WeatherWarningCallback callback) {
        String url = BASE_URL + "/v7/warning/now?key=" + API_KEY + "&location=" + locationId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("网络请求失败: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Weather warning response: " + responseBody);

                    try {
                        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                        String code = jsonObject.get("code").getAsString();

                        if ("200".equals(code)) {
                            JsonArray warningArray = jsonObject.getAsJsonArray("warning");
                            List<WeatherWarning> warnings = new ArrayList<>();

                            if (warningArray != null) {
                                for (int i = 0; i < warningArray.size(); i++) {
                                    JsonObject warningObj = warningArray.get(i).getAsJsonObject();
                                    WeatherWarning warning = new WeatherWarning();

                                    warning.setId(warningObj.get("id").getAsString());
                                    warning.setTitle(warningObj.get("title").getAsString());
                                    warning.setStatus(warningObj.get("status").getAsString());
                                    warning.setLevel(warningObj.get("level").getAsString());
                                    warning.setType(warningObj.get("type").getAsString());
                                    warning.setText(warningObj.get("text").getAsString());
                                    warning.setPubTime(warningObj.get("pubTime").getAsString());
                                    warning.setStartTime(warningObj.get("startTime").getAsString());
                                    warning.setEndTime(warningObj.get("endTime").getAsString());

                                    warnings.add(warning);
                                }
                            }

                            mainHandler.post(() -> callback.onSuccess(warnings));
                        } else {
                            mainHandler.post(() -> callback.onError("获取天气预警失败，错误代码: " + code));
                        }
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("数据解析失败: " + e.getMessage()));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("请求失败，响应码: " + response.code()));
                }
            }
        });
    }
}