package com.example.weather;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.weather.manager.SettingsManager;
import com.example.weather.manager.UserManager;
import com.example.weather.model.AppSettings;
import com.example.weather.model.User;

public class SettingsActivity extends AppCompatActivity {
    private UserManager userManager;
    private SettingsManager settingsManager;
    private User currentUser;
    private AppSettings userSettings;

    private TextView tvUsername, tvTheme;
    private Switch switchNotification, switchAutoUpdate;
    private LinearLayout layoutProfile, layoutTheme, layoutNotification, layoutAutoUpdate;
    private LinearLayout layoutAccount, layoutLogout;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userManager = new UserManager(this);
        settingsManager = new SettingsManager(this);
        currentUser = userManager.getCurrentUser();

        initViews();
        setupToolbar();
        loadUserSettings();
        setupClickListeners();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvTheme = findViewById(R.id.tvTheme);
        switchNotification = findViewById(R.id.switchNotification);
        switchAutoUpdate = findViewById(R.id.switchAutoUpdate);
        layoutProfile = findViewById(R.id.layoutProfile);
        layoutTheme = findViewById(R.id.layoutTheme);
        layoutNotification = findViewById(R.id.layoutNotification);
        layoutAutoUpdate = findViewById(R.id.layoutAutoUpdate);
        layoutAccount = findViewById(R.id.layoutAccount);
        layoutLogout = findViewById(R.id.layoutLogout);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("设置");

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserSettings() {
        if (currentUser != null) {
            tvUsername.setText(currentUser.getUsername());
            userSettings = settingsManager.getUserSettings(currentUser.getId());

            if (userSettings != null) {
                tvTheme.setText(getThemeDisplayName(userSettings.getTheme()));
                switchNotification.setChecked(userSettings.isNotificationEnabled());
                switchAutoUpdate.setChecked(userSettings.isAutoUpdate());
            }
        } else {
            tvUsername.setText("未登录");
            layoutProfile.setEnabled(false);
            layoutNotification.setEnabled(false);
            layoutAutoUpdate.setEnabled(false);
            btnSave.setEnabled(false);
        }
    }

    private void setupClickListeners() {
        layoutProfile.setOnClickListener(v -> {
            if (currentUser != null) {
                startActivity(new Intent(this, ProfileActivity.class));
            }
        });

        layoutTheme.setOnClickListener(v -> showThemeDialog());

        layoutAccount.setOnClickListener(v -> {
            if (currentUser == null) {
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                startActivity(new Intent(this, AccountActivity.class));
            }
        });

        layoutLogout.setOnClickListener(v -> showLogoutDialog());

        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void showThemeDialog() {
        String[] themes = {"默认", "浅色", "深色"};
        String[] themeValues = {"default", "light", "dark"};

        int currentIndex = 0;
        if (userSettings != null) {
            for (int i = 0; i < themeValues.length; i++) {
                if (themeValues[i].equals(userSettings.getTheme())) {
                    currentIndex = i;
                    break;
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("选择主题")
                .setSingleChoiceItems(themes, currentIndex, (dialog, which) -> {
                    if (userSettings != null) {
                        userSettings.setTheme(themeValues[which]);
                        tvTheme.setText(themes[which]);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showLogoutDialog() {
        if (currentUser == null) return;

        new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    userManager.logoutUser();
                    Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveSettings() {
        if (userSettings == null || currentUser == null) return;

        userSettings.setNotificationEnabled(switchNotification.isChecked());
        userSettings.setAutoUpdate(switchAutoUpdate.isChecked());

        if (settingsManager.updateSettings(userSettings)) {
            Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private String getThemeDisplayName(String theme) {
        switch (theme) {
            case "light": return "浅色";
            case "dark": return "深色";
            default: return "默认";
        }
    }
}