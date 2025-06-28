package com.example.weather.model;

import java.util.Date;

public class Diary {
    private int id;
    private int userId;
    private String title;
    private String content;
    private Date date;
    private Date reminderTime;
    private boolean isReminderSet;

    // 构造方法
    public Diary() {}

    public Diary(int userId, String title, String content, Date date) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.date = date;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Date getReminderTime() { return reminderTime; }
    public void setReminderTime(Date reminderTime) { this.reminderTime = reminderTime; }

    public boolean isReminderSet() { return isReminderSet; }
    public void setReminderSet(boolean reminderSet) { isReminderSet = reminderSet; }
}