package com.example.weather;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.weather.manager.UserManager;
import com.example.weather.model.User;

public class AccountActivity extends AppCompatActivity {
    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword, btnDeleteAccount;
    private UserManager userManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("账号设置");
        toolbar.setNavigationOnClickListener(v -> finish());

        // 初始化视图
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        // 初始化用户管理器
        userManager = new UserManager(this);
        currentUser = userManager.getCurrentUser();

        // 修改密码按钮点击事件
        btnChangePassword.setOnClickListener(v -> changePassword());

        // 删除账号按钮点击事件
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void changePassword() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证旧密码
        if (!userManager.checkPassword(currentUser.getId(), oldPassword)) {
            Toast.makeText(this, "旧密码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新密码
        if (userManager.updateUserPassword(currentUser.getId(), newPassword)) {
            Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
            // 清空输入框
            etOldPassword.setText("");
            etNewPassword.setText("");
            etConfirmPassword.setText("");
        } else {
            Toast.makeText(this, "密码修改失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("删除账号")
                .setMessage("确定要永久删除您的账号吗？此操作不可逆！")
                .setPositiveButton("删除", (dialog, which) -> deleteAccount())
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteAccount() {
        if (userManager.deleteUser(currentUser.getId())) {
            Toast.makeText(this, "账号已删除", Toast.LENGTH_SHORT).show();
            // 跳转到登录页面
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity(); // 关闭所有Activity
        } else {
            Toast.makeText(this, "账号删除失败", Toast.LENGTH_SHORT).show();
        }
    }
}