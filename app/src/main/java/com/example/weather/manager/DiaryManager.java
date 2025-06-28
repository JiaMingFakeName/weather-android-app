package com.example.weather.manager;

import static com.example.weather.database.DatabaseHelper.COLUMN_DIARY_ID;
import static com.example.weather.database.DatabaseHelper.TABLE_DIARIES;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.weather.database.DatabaseHelper;
import com.example.weather.model.Diary;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiaryManager {
    private DatabaseHelper dbHelper;

    public DiaryManager(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // 添加日记
    public long addDiary(Diary diary) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DIARY_USER_ID, diary.getUserId());
        values.put(DatabaseHelper.COLUMN_TITLE, diary.getTitle());
        values.put(DatabaseHelper.COLUMN_CONTENT, diary.getContent());
        values.put(DatabaseHelper.COLUMN_DATE, diary.getDate().getTime());

        if(diary.getReminderTime() != null) {
            values.put(DatabaseHelper.COLUMN_REMINDER_TIME, diary.getReminderTime().getTime());
            values.put(DatabaseHelper.COLUMN_IS_REMINDER_SET, 1);
        }

        long id = db.insert(TABLE_DIARIES, null, values);
        db.close();
        return id;
    }

    // 获取用户所有日记（按日期排序）
    public List<Diary> getDiariesByUser(int userId) {
        List<Diary> diaries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_DIARIES +
                " WHERE " + DatabaseHelper.COLUMN_DIARY_USER_ID + " = ?" +
                " ORDER BY " + DatabaseHelper.COLUMN_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Diary diary = new Diary();
                diary.setId(cursor.getInt(0));
                diary.setUserId(cursor.getInt(1));
                diary.setTitle(cursor.getString(2));
                diary.setContent(cursor.getString(3));
                diary.setDate(new Date(cursor.getLong(4)));

                if(!cursor.isNull(5)) {
                    diary.setReminderTime(new Date(cursor.getLong(5)));
                    diary.setReminderSet(cursor.getInt(6) == 1);
                }

                diaries.add(diary);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return diaries;
    }

    // 按日期范围查询日记
    public List<Diary> getDiariesByDateRange(int userId, Date startDate, Date endDate) {
        List<Diary> diaries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_DIARIES +
                " WHERE " + DatabaseHelper.COLUMN_DIARY_USER_ID + " = ?" +
                " AND " + DatabaseHelper.COLUMN_DATE + " BETWEEN ? AND ?" +
                " ORDER BY " + DatabaseHelper.COLUMN_DATE + " DESC";

        String[] selectionArgs = {
                String.valueOf(userId),
                String.valueOf(startDate.getTime()),
                String.valueOf(endDate.getTime())
        };

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                Diary diary = new Diary();
                diary.setId(cursor.getInt(0));
                diary.setUserId(cursor.getInt(1));
                diary.setTitle(cursor.getString(2));
                diary.setContent(cursor.getString(3));
                diary.setDate(new Date(cursor.getLong(4)));

                if(!cursor.isNull(5)) {
                    diary.setReminderTime(new Date(cursor.getLong(5)));
                    diary.setReminderSet(cursor.getInt(6) == 1);
                }

                diaries.add(diary);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return diaries;
    }

    // 更新日记
    public int updateDiary(Diary diary) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, diary.getTitle());
        values.put(DatabaseHelper.COLUMN_CONTENT, diary.getContent());
        values.put(DatabaseHelper.COLUMN_DATE, diary.getDate().getTime());

        if(diary.getReminderTime() != null) {
            values.put(DatabaseHelper.COLUMN_REMINDER_TIME, diary.getReminderTime().getTime());
            values.put(DatabaseHelper.COLUMN_IS_REMINDER_SET, diary.isReminderSet() ? 1 : 0);
        } else {
            values.putNull(DatabaseHelper.COLUMN_REMINDER_TIME);
            values.put(DatabaseHelper.COLUMN_IS_REMINDER_SET, 0);
        }

        int rowsAffected = db.update(
                TABLE_DIARIES,
                values,
                COLUMN_DIARY_ID + " = ?",
                new String[]{String.valueOf(diary.getId())}
        );

        db.close();
        return rowsAffected;
    }

    // 删除日记
    public boolean deleteDiary(int diaryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(
                TABLE_DIARIES,
                COLUMN_DIARY_ID + " = ?",
                new String[]{String.valueOf(diaryId)}
        );
        db.close();
        return rowsDeleted > 0;
    }

    // 获取需要提醒的日记
    public List<Diary> getDiariesWithReminders() {
        List<Diary> diaries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_DIARIES +
                " WHERE " + DatabaseHelper.COLUMN_IS_REMINDER_SET + " = 1";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Diary diary = new Diary();
                diary.setId(cursor.getInt(0));
                diary.setUserId(cursor.getInt(1));
                diary.setTitle(cursor.getString(2));
                diary.setContent(cursor.getString(3));
                diary.setDate(new Date(cursor.getLong(4)));

                if(!cursor.isNull(5)) {
                    diary.setReminderTime(new Date(cursor.getLong(5)));
                    diary.setReminderSet(cursor.getInt(6) == 1);
                }

                diaries.add(diary);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return diaries;
    }

    public Diary getDiaryById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DIARIES +
                " WHERE " + COLUMN_DIARY_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            Diary diary = new Diary();
            diary.setId(cursor.getInt(0));
            diary.setUserId(cursor.getInt(1));
            diary.setTitle(cursor.getString(2));
            diary.setContent(cursor.getString(3));
            diary.setDate(new Date(cursor.getLong(4)));

            if(!cursor.isNull(5)) {
                diary.setReminderTime(new Date(cursor.getLong(5)));
                diary.setReminderSet(cursor.getInt(6) == 1);
            }

            cursor.close();
            db.close();
            return diary;
        }

        cursor.close();
        db.close();
        return null;
    }
}