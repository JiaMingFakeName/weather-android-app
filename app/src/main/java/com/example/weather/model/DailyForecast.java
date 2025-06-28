package com.example.weather.model;

public class DailyForecast {
    private String date;
    private String minTemp;
    private String maxTemp;
    private String dayCondition;
    private String nightCondition;
    private String dayIcon;
    private String nightIcon;
    private String windDirDay;
    private String windScaleDay;
    private String windDirNight;
    private String windScaleNight;
    private String humidity;
    private String uvIndex;

    public DailyForecast() {}

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMinTemp() { return minTemp; }
    public void setMinTemp(String minTemp) { this.minTemp = minTemp; }

    public String getMaxTemp() { return maxTemp; }
    public void setMaxTemp(String maxTemp) { this.maxTemp = maxTemp; }

    public String getDayCondition() { return dayCondition; }
    public void setDayCondition(String dayCondition) { this.dayCondition = dayCondition; }

    public String getNightCondition() { return nightCondition; }
    public void setNightCondition(String nightCondition) { this.nightCondition = nightCondition; }

    public String getDayIcon() { return dayIcon; }
    public void setDayIcon(String dayIcon) { this.dayIcon = dayIcon; }

    public String getNightIcon() { return nightIcon; }
    public void setNightIcon(String nightIcon) { this.nightIcon = nightIcon; }

    public String getWindDirDay() { return windDirDay; }
    public void setWindDirDay(String windDirDay) { this.windDirDay = windDirDay; }

    public String getWindScaleDay() { return windScaleDay; }
    public void setWindScaleDay(String windScaleDay) { this.windScaleDay = windScaleDay; }

    public String getWindDirNight() { return windDirNight; }
    public void setWindDirNight(String windDirNight) { this.windDirNight = windDirNight; }

    public String getWindScaleNight() { return windScaleNight; }
    public void setWindScaleNight(String windScaleNight) { this.windScaleNight = windScaleNight; }

    public String getHumidity() { return humidity; }
    public void setHumidity(String humidity) { this.humidity = humidity; }

    public String getUvIndex() { return uvIndex; }
    public void setUvIndex(String uvIndex) { this.uvIndex = uvIndex; }
}