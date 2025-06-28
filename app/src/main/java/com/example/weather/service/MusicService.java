package com.example.weather.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.example.weather.MusicPlayerActivity;
import com.example.weather.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {
    private static final String CHANNEL_ID = "MusicServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private MediaPlayer mediaPlayer;
    private List<String> playlist = new ArrayList<>();
    private int currentPosition = 0;
    private boolean isPaused = false;

    public static class MusicBinder extends Binder {
        private final MusicService service;

        public MusicBinder(MusicService service) {
            this.service = service;
        }

        public MusicService getService() {
            return service;
        }
    }

    public void loadLocalMusic() {
        playlist.clear(); // 清空当前列表

        // 添加在线音乐（保留）
        playlist.add("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
        playlist.add("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3");
        playlist.add("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3");

        // 使用 MediaStore API 查询本地音乐
        if (checkStoragePermission()) {
            loadMusicFromMediaStore();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android 6.0 以下默认有权限
    }

    private void loadMusicFromMediaStore() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME, // 确保获取文件名
//                MediaStore.Audio.Media.ARTIST
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        try (Cursor cursor = contentResolver.query(uri, projection, selection, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                do {
                    String path = cursor.getString(pathIndex);
                    String name = cursor.getString(nameIndex); // 获取文件名
                    if (path != null) {
                        playlist.add(path);
                        Log.d("MusicService", "找到音乐文件: " + name); // 使用文件名记录
                    }
                } while (cursor.moveToNext());
            } else {
                Log.w("MusicService", "没有找到音乐文件");
            }
        } catch (Exception e) {
            Log.e("MusicService", "加载本地音乐失败", e);
        }
    }

    // 添加获取播放列表的方法
    public List<String> getPlaylist() {
        return playlist;
    }

    // 添加播放指定位置的方法
    public void playAtPosition(int position) {
        if (position >= 0 && position < playlist.size()) {
            currentPosition = position;
            play();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        createNotificationChannel();

        mediaPlayer.setOnCompletionListener(mp -> playNext());
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.e("MusicService", "播放错误: " + what + ", " + extra);
            playNext(); // 出错时播放下一个
            return true;
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("PLAY_PAUSE".equals(action)) {
                if (isPlaying()) {
                    pause();
                } else {
                    play();
                }
            } else if ("PREV".equals(action)) {
                playPrevious();
            } else if ("NEXT".equals(action)) {
                playNext();
            }
        }

        startForeground(NOTIFICATION_ID, createNotification());
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder(this); // 传递当前Service实例
    }

    public void play() {
        // 确保播放列表已加载
        if (playlist.isEmpty()) {
            loadLocalMusic();
        }
        if (isPaused) {
            mediaPlayer.start();
            isPaused = false;
        } else {
            try {
                resetMediaPlayer(); // 使用安全重置方法
                mediaPlayer.reset();
                mediaPlayer.setDataSource(playlist.get(currentPosition));
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    updateNotification(); // 准备完成后更新通知
                });
            } catch (IOException e) {
                Log.e("MusicService", "播放失败: " + e.getMessage());
            }
        }
        updateNotification();
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        }
        updateNotification();
    }

    public void stop() {
        mediaPlayer.stop();
        isPaused = false;
        updateNotification();
    }

    public void playNext() {
        currentPosition = (currentPosition + 1) % playlist.size();
        play();
    }

    public void playPrevious() {
        currentPosition = (currentPosition - 1 + playlist.size()) % playlist.size();
        play();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public String getCurrentSong() {
        if (playlist.isEmpty() || currentPosition < 0 || currentPosition >= playlist.size()) {
            return null;
        }

        String path = playlist.get(currentPosition);
        if (path.startsWith("http")) {
            return "在线: " + path.substring(path.lastIndexOf('/') + 1);
        } else {
            return "本地: " + new File(path).getName();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "音乐服务通道",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MusicPlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // 创建通知操作
        PendingIntent prevIntent = PendingIntent.getService(this, 0,
                new Intent(this, MusicService.class).setAction("PREV"), PendingIntent.FLAG_IMMUTABLE);

        PendingIntent playPauseIntent = PendingIntent.getService(this, 0,
                new Intent(this, MusicService.class).setAction("PLAY_PAUSE"), PendingIntent.FLAG_IMMUTABLE);

        PendingIntent nextIntent = PendingIntent.getService(this, 0,
                new Intent(this, MusicService.class).setAction("NEXT"), PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("天气音乐")
                .setContentText(isPlaying() ? "正在播放: " + getCurrentSong() : "音乐已暂停")
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_prev, "上一首", prevIntent)
                .addAction(isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play,
                        isPlaying() ? "暂停" : "播放", playPauseIntent)
                .addAction(R.drawable.ic_next, "下一首", nextIntent)
                .build();
    }

    private PendingIntent createActionIntent(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private void updateNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, createNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    // 新增：进度控制方法
    public int getCurrentPosition() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying() ?
                    mediaPlayer.getCurrentPosition() : 0;
        } catch (IllegalStateException e) {
            return 0;
        }
    }

    public int getDuration() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying() ?
                    mediaPlayer.getDuration() : 0;
        } catch (IllegalStateException e) {
            return 0;
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null && position >= 0 && position <= mediaPlayer.getDuration()) {
            mediaPlayer.seekTo(position);
        }
    }

    // 新增：安全重置媒体播放器
    private void resetMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.reset();
                isPaused = false;
            } catch (IllegalStateException e) {
                Log.e("MusicService", "重置播放器失败", e);
                // 创建全新实例作为后备
                mediaPlayer.release();
                mediaPlayer = new MediaPlayer();
            }
        }
    }



}
