package com.example.weather.manager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.weather.MainActivity;
import com.example.weather.R;
import com.example.weather.model.WeatherWarning;

public class WeatherNotificationManager {
    private static final String CHANNEL_ID = "weather_warnings";
    private static final String CHANNEL_NAME = "天气预警";
    private static final String CHANNEL_DESCRIPTION = "接收天气预警通知";
    private static final int WARNING_NOTIFICATION_ID = 1001; // 修改为常量
    private static final int NO_WARNING_NOTIFICATION_ID = 1002; // 新增无预警通知ID

    private Context context;
    private NotificationManagerCompat notificationManager;

    public WeatherNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void showWeatherWarning(WeatherWarning warning) {
        // 添加详细的权限检查和处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("WeatherNotification", "缺少通知权限，无法显示预警通知");
                return;
            }
        }
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("天气预警：" + warning.getTitle())
                .setContentText(warning.getText())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(warning.getText()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 500, 1000})
                .setLights(0xFFFF0000, 3000, 3000);

        try {
            notificationManager.notify(WARNING_NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            Log.e("WeatherNotification", "显示通知时发生安全异常: " + e.getMessage());
        }
    }

    // 新增无预警通知方法
    public void showNoWarningNotification() {
        // 添加详细的权限检查和处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("WeatherNotification", "缺少通知权限，无法显示无预警通知");
                return;
            }
        }
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sunny)
                .setContentTitle("天气预警状态")
                .setContentText("暂无天气预警")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            notificationManager.notify(NO_WARNING_NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            Log.e("WeatherNotification", "显示通知时发生安全异常: " + e.getMessage());
        }
    }

    // 检查通知权限的方法
    public boolean hasNotificationPermission() {
        // Android 13+ 需要动态权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        // Android 13 以下版本不需要动态权限
        return true;
    }

//    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
//    public void showWeatherUpdate(String cityName, String temperature, String condition) {
//        Intent intent = new Intent(context, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                context,
//                0,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                .setSmallIcon(R.drawable.ic_sunny)
//                .setContentTitle(cityName + "天气更新")
//                .setContentText(temperature + "°C, " + condition)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setAutoCancel(true)
//                .setContentIntent(pendingIntent);
//
//        notificationManager.notify(NOTIFICATION_ID + 1, builder.build());
//    }
}