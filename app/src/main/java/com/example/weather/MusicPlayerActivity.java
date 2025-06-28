package com.example.weather;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.adapter.PlaylistAdapter;
import com.example.weather.service.MusicService;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayerActivity extends AppCompatActivity {
    private MusicService musicService;
    private boolean isBound = false;
    private TextView tvCurrentSong;
    private ImageButton btnPlayPause;
    private ListView listView;
    private List<String> playlist = new ArrayList<>();
    private RecyclerView rvPlaylist;
    private PlaylistAdapter playlistAdapter;
    private SeekBar seekBar; // 新增进度条
    private Handler progressHandler = new Handler();
    private Runnable updateProgressRunnable;
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 需要 READ_MEDIA_AUDIO
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                // 已经有权限，手动触发音乐加载
                if (isBound) {
                    musicService.loadLocalMusic();
                    updateUI();
                }
            }
        } else {
            // Android 12 及以下版本
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                // 已经有权限，手动触发音乐加载
                if (isBound) {
                    musicService.loadLocalMusic();
                    updateUI();
                }
            }
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;

            // 新增：绑定服务后立即加载音乐
            if (hasStoragePermission()) {
                musicService.loadLocalMusic();
            }

            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予后直接加载音乐并刷新
                if (isBound) {
                    musicService.loadLocalMusic(); // 主动加载
                    updateUI(); // 刷新UI
                }
            } else {
                Toast.makeText(this, "需要存储权限来加载本地音乐", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        tvCurrentSong = findViewById(R.id.tvCurrentSong);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        rvPlaylist = findViewById(R.id.rvPlaylist);
        seekBar = findViewById(R.id.seekBar); // 初始化进度条

        // 初始化适配器（使用空列表）
        playlistAdapter = new PlaylistAdapter(new ArrayList<>(), position -> {
            if (isBound) {
                musicService.playAtPosition(position);
                updateUI();
            }
        });
        // 设置播放列表适配器
        rvPlaylist.setLayoutManager(new LinearLayoutManager(this));
        rvPlaylist.setAdapter(playlistAdapter); // 提前设置适配器避免警告

        // 添加默认文本
        tvCurrentSong.setText("正在加载音乐...");

        // 设置进度条监听器
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                progressHandler.removeCallbacks(updateProgressRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startProgressUpdate();
            }
        });

        // 直接初始化服务，权限检查在服务内部处理
        initMusicService();
    }

    private void initMusicService() {
        // 绑定服务
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        startService(intent);

        // 请求必要的权限
        requestStoragePermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止进度更新
        progressHandler.removeCallbacks(updateProgressRunnable);
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    public void onPlayPauseClick(View view) {
        if (isBound) {
            if (musicService.isPlaying()) {
                musicService.pause();
            } else {
                musicService.play();
            }
            updateUI();
        }
    }

    public void onNextClick(View view) {
        if (isBound) {
            musicService.playNext();
            updateUI();
        }
    }

    public void onPrevClick(View view) {
        if (isBound) {
            musicService.playPrevious();
            updateUI();
        }
    }

    public void onStopClick(View view) {
        if (isBound) {
            musicService.stop();
            updateUI();
        }
    }

    private void updateUI() {
        if (isBound) {
            seekBar.setProgress(0);
            // 更新当前歌曲显示
            String currentSong = musicService.getCurrentSong();
            tvCurrentSong.setText(currentSong != null ?
                    "当前播放: " + currentSong : "选择音乐开始播放");

            // 更新播放按钮图标
            btnPlayPause.setImageResource(musicService.isPlaying() ?
                    R.drawable.ic_pause : R.drawable.ic_play);

            // 更新播放列表
            playlistAdapter.updatePlaylist(musicService.getPlaylist());

            startProgressUpdate();
        }
    }

    private void startProgressUpdate() {
        progressHandler.removeCallbacks(updateProgressRunnable);
        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (isBound && musicService != null) {
                    // 仅在播放时更新进度
                    if (musicService.isPlaying()) {
                        int currentPos = musicService.getCurrentPosition();
                        int duration = musicService.getDuration();

                        // 添加安全检查
                        if (duration > 0 && currentPos <= duration) {
                            seekBar.setMax(duration);
                            seekBar.setProgress(currentPos);
                        }
                    }

                    // 每秒更新一次
                    progressHandler.postDelayed(this, 1000);
                }
            }
        };
        progressHandler.post(updateProgressRunnable);
    }

    // 添加返回按钮点击处理
    public void onBackClick(View view) {
        finish(); // 关闭当前Activity，返回主页面
    }
}