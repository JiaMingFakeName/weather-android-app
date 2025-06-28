package com.example.weather.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "weather_app.db";
    private static final int DATABASE_VERSION = 2;

    // 用户表
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_AVATAR = "avatar";
    public static final String COLUMN_CREATED_AT = "created_at";

    // 设置表
    public static final String TABLE_SETTINGS = "settings";
    public static final String COLUMN_SETTING_ID = "setting_id";
    public static final String COLUMN_SETTING_USER_ID = "user_id";
    public static final String COLUMN_THEME = "theme";
    public static final String COLUMN_NOTIFICATION_ENABLED = "notification_enabled";
    public static final String COLUMN_AUTO_UPDATE = "auto_update";
    public static final String COLUMN_UPDATE_INTERVAL = "update_interval";

    // 收藏城市表
    public static final String TABLE_FAVORITE_CITIES = "favorite_cities";
    public static final String COLUMN_CITY_ID = "city_id";
    public static final String COLUMN_CITY_NAME = "city_name";
    public static final String COLUMN_CITY_LOCATION_ID = "location_id";

    // 日记表
    public static final String TABLE_DIARIES = "diaries";
    public static final String COLUMN_DIARY_ID = "diary_id";
    public static final String COLUMN_DIARY_USER_ID = "user_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_REMINDER_TIME = "reminder_time";
    public static final String COLUMN_IS_REMINDER_SET = "is_reminder_set";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("Database", "Creating tables...");
        // 创建用户表
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE NOT NULL,"
                + COLUMN_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_AVATAR + " TEXT,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        // 设置表 (修复多余逗号)
        String CREATE_SETTINGS_TABLE = "CREATE TABLE " + TABLE_SETTINGS + "("
                + COLUMN_SETTING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SETTING_USER_ID + " INTEGER,"
                + COLUMN_THEME + " TEXT DEFAULT 'default',"
                + COLUMN_NOTIFICATION_ENABLED + " INTEGER DEFAULT 1,"
                + COLUMN_AUTO_UPDATE + " INTEGER DEFAULT 1,"
                + COLUMN_UPDATE_INTERVAL + " INTEGER DEFAULT 30" // 移除行尾逗号
                + ")";  // 移除外键约束（可选）

        // 收藏城市表 (修复多余逗号)
        String CREATE_FAVORITE_CITIES_TABLE = "CREATE TABLE " + TABLE_FAVORITE_CITIES + "("
                + COLUMN_CITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SETTING_USER_ID + " INTEGER,"
                + COLUMN_CITY_NAME + " TEXT NOT NULL,"
                + COLUMN_CITY_LOCATION_ID + " TEXT NOT NULL" // 移除行尾逗号
                + ")";  // 移除外键约束（可选）

        // 日记表 (修复关键错误)
        String CREATE_DIARIES_TABLE = "CREATE TABLE " + TABLE_DIARIES + "("
                + COLUMN_DIARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DIARY_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_TITLE + " TEXT NOT NULL,"
                + COLUMN_CONTENT + " TEXT NOT NULL,"
                + COLUMN_DATE + " DATETIME NOT NULL,"
                + COLUMN_REMINDER_TIME + " DATETIME,"
                + COLUMN_IS_REMINDER_SET + " INTEGER DEFAULT 0" // 移除行尾逗号
                + ")";  // 暂时移除外键约束

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_SETTINGS_TABLE);
        db.execSQL(CREATE_FAVORITE_CITIES_TABLE);
        db.execSQL(CREATE_DIARIES_TABLE);

        Log.d("Database", "diaries table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("Database", "Upgrading DB from " + oldVersion + " to " + newVersion);
        // 安全删除顺序：先删除依赖表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIARIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE_CITIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);  // 重建表结构
    }
}