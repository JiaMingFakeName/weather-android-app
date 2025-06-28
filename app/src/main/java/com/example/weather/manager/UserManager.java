package com.example.weather.manager;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.weather.database.DatabaseHelper;
import com.example.weather.model.User;

public class UserManager {
    private static final String PREFS_NAME = "weather_app_prefs";
    private static final String KEY_CURRENT_USER_ID = "current_user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private Context context;

    public UserManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_PASSWORD, password);

        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        db.close();

        if (result != -1) {
            // 创建默认设置
            createDefaultSettings((int) result);
            return true;
        }
        return false;
    }

    public User loginUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {DatabaseHelper.COLUMN_USER_ID, DatabaseHelper.COLUMN_USERNAME, DatabaseHelper.COLUMN_AVATAR};
        String selection = DatabaseHelper.COLUMN_USERNAME + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(0));
            user.setUsername(cursor.getString(1));
            user.setAvatar(cursor.getString(2));

            // 保存登录状态
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_CURRENT_USER_ID, user.getId());
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.apply();
        }

        cursor.close();
        db.close();
        return user;
    }

    public void logoutUser() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_CURRENT_USER_ID);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getCurrentUserId() {
        return prefs.getInt(KEY_CURRENT_USER_ID, -1);
    }

    public User getCurrentUser() {
        if (!isLoggedIn()) return null;

        int userId = getCurrentUserId();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {DatabaseHelper.COLUMN_USER_ID, DatabaseHelper.COLUMN_USERNAME, DatabaseHelper.COLUMN_AVATAR};
        String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(0));
            user.setUsername(cursor.getString(1));
            user.setAvatar(cursor.getString(2));
        } else {
            // 用户不存在但登录状态有效 - 清除无效登录状态
            Log.e("UserManager", "User data missing for logged-in ID: " + userId);
            logoutUser();
            return null;
        }

        cursor.close();
        db.close();
        return user;
    }

    private void createDefaultSettings(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SETTING_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_THEME, "default");
        values.put(DatabaseHelper.COLUMN_NOTIFICATION_ENABLED, 1);
        values.put(DatabaseHelper.COLUMN_AUTO_UPDATE, 1);
        values.put(DatabaseHelper.COLUMN_UPDATE_INTERVAL, 30);

        db.insert(DatabaseHelper.TABLE_SETTINGS, null, values);
        db.close();
    }

    // 更新用户名
    public boolean updateUsername(int userId, String newUsername) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, newUsername);

        int rowsAffected = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        return rowsAffected > 0;
    }

    // 更新密码
    public boolean updateUserPassword(int userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PASSWORD, newPassword);

        int rowsAffected = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        return rowsAffected > 0;
    }

    // 验证密码
    public boolean checkPassword(int userId, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {DatabaseHelper.COLUMN_USER_ID};
        String selection = DatabaseHelper.COLUMN_USER_ID + " = ? AND " +
                DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {String.valueOf(userId), password};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );

        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        return isValid;
    }

    // 删除用户
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 删除用户设置
        db.delete(
                DatabaseHelper.TABLE_SETTINGS,
                DatabaseHelper.COLUMN_SETTING_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        // 删除用户收藏
        db.delete(
                DatabaseHelper.TABLE_FAVORITE_CITIES,
                DatabaseHelper.COLUMN_SETTING_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        // 删除用户
        int rowsAffected = db.delete(
                DatabaseHelper.TABLE_USERS,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        return rowsAffected > 0;
    }
}