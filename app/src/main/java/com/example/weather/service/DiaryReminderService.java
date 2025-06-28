package com.example.weather.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.weather.R;
import com.example.weather.manager.DiaryManager;
import com.example.weather.model.Diary;
import com.example.weather.DiaryListActivity;
import java.util.Date;
import java.util.List;

public class DiaryReminderService extends BroadcastReceiver {
    private static final String CHANNEL_ID = "diary_reminder_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        DiaryManager diaryManager = new DiaryManager(context);
        List<Diary> diaries = diaryManager.getDiariesWithReminders();

        Date now = new Date();
        for (Diary diary : diaries) {
            // 检查提醒时间是否已到（当前时间 >= 提醒时间）
            if (diary.getReminderTime() != null &&
                    !now.before(diary.getReminderTime())) {

                // 发送通知
                sendDiaryNotification(context, diary);

                // 清除已触发的提醒
                diary.setReminderSet(false);
                diaryManager.updateDiary(diary);
            }
        }
    }

    private void sendDiaryNotification(Context context, Diary diary) {
        createNotificationChannel(context);

        // 创建点击通知后打开的Intent
        Intent intent = new Intent(context, DiaryListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                diary.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_diary)
                .setContentTitle("日记提醒: " + diary.getTitle())
                .setContentText(diary.getContent())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(diary.getContent()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(diary.getId(), builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "日记提醒";
            String description = "日记日程提醒通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // 设置定时检查
    public static void scheduleReminderCheck(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DiaryReminderService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 每30分钟检查一次
        long interval = 30 * 60 * 1000; // 30分钟
        long triggerAtMillis = System.currentTimeMillis() + interval;

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                interval,
                pendingIntent
        );
    }
}