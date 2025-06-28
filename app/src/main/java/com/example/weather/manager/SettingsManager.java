package com.example.weather.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.weather.database.DatabaseHelper;
import com.example.weather.model.AppSettings;

public class SettingsManager {
    private DatabaseHelper dbHelper;
    private Context context;

    public SettingsManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    public AppSettings getUserSettings(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {
                DatabaseHelper.COLUMN_SETTING_ID,
                DatabaseHelper.COLUMN_SETTING_USER_ID,
                DatabaseHelper.COLUMN_THEME,
                DatabaseHelper.COLUMN_NOTIFICATION_ENABLED,
                DatabaseHelper.COLUMN_AUTO_UPDATE,
                DatabaseHelper.COLUMN_UPDATE_INTERVAL
        };
        String selection = DatabaseHelper.COLUMN_SETTING_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(DatabaseHelper.TABLE_SETTINGS, columns, selection, selectionArgs, null, null, null);

        AppSettings settings = null;
        if (cursor.moveToFirst()) {
            settings = new AppSettings();
            settings.setId(cursor.getInt(0));
            settings.setUserId(cursor.getInt(1));
            settings.setTheme(cursor.getString(2));
            settings.setNotificationEnabled(cursor.getInt(3) == 1);
            settings.setAutoUpdate(cursor.getInt(4) == 1);
            settings.setUpdateInterval(cursor.getInt(5));
        }

        cursor.close();
        db.close();
        return settings;
    }

    public boolean updateSettings(AppSettings settings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_THEME, settings.getTheme());
        values.put(DatabaseHelper.COLUMN_NOTIFICATION_ENABLED, settings.isNotificationEnabled() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_AUTO_UPDATE, settings.isAutoUpdate() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_UPDATE_INTERVAL, settings.getUpdateInterval());

        String whereClause = DatabaseHelper.COLUMN_SETTING_USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(settings.getUserId())};

        int result = db.update(DatabaseHelper.TABLE_SETTINGS, values, whereClause, whereArgs);
        db.close();

        return result > 0;
    }
}