package com.example.weather;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.weather.manager.UserManager;
import com.example.weather.model.User;

public class ProfileActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private ImageView ivAvatar;
    private Button btnSave;
    private UserManager userManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 初始化视图
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        ivAvatar = findViewById(R.id.ivAvatar);
        btnSave = findViewById(R.id.btnSave);

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("个人资料");
        toolbar.setNavigationOnClickListener(v -> finish());

        // 初始化用户管理器
        userManager = new UserManager(this);
        currentUser = userManager.getCurrentUser();

        // 加载用户数据
        if (currentUser != null) {
            etUsername.setText(currentUser.getUsername());
            // 注意：实际应用中密码不应显示
        }

        // 保存按钮点击事件
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String newUsername = etUsername.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        if (newUsername.isEmpty()) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新用户名
        if (!newUsername.equals(currentUser.getUsername())) {
            if (userManager.updateUsername(currentUser.getId(), newUsername)) {
                currentUser.setUsername(newUsername);
                Toast.makeText(this, "用户名更新成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "用户名更新失败", Toast.LENGTH_SHORT).show();
            }
        }

        // 更新密码（如果有输入）
        if (!newPassword.isEmpty()) {
            if (newPassword.length() < 6) {
                Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userManager.updateUserPassword(currentUser.getId(), newPassword)) {
                Toast.makeText(this, "密码更新成功", Toast.LENGTH_SHORT).show();
                etPassword.setText(""); // 清空密码字段
            } else {
                Toast.makeText(this, "密码更新失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}