package com.example.weather.model;

public class WeatherWarning {
    private String id;
    private String title;
    private String status;
    private String level;
    private String type;
    private String text;
    private String pubTime;
    private String startTime;
    private String endTime;

    // Constructors
    public WeatherWarning() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getPubTime() { return pubTime; }
    public void setPubTime(String pubTime) { this.pubTime = pubTime; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}