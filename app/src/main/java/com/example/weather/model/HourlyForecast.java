package com.example.weather.model;

public class HourlyForecast {
    private String time;
    private String temp;
    private String condition;
    private String icon;
    private String windDir;
    private String windScale;
    private String humidity;

    public HourlyForecast() {}

    // Getters and Setters
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getTemp() { return temp; }
    public void setTemp(String temp) { this.temp = temp; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getWindDir() { return windDir; }
    public void setWindDir(String windDir) { this.windDir = windDir; }

    public String getWindScale() { return windScale; }
    public void setWindScale(String windScale) { this.windScale = windScale; }

    public String getHumidity() { return humidity; }
    public void setHumidity(String humidity) { this.humidity = humidity; }
}