package com.example.weather;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.weather.manager.DiaryManager;
import com.example.weather.manager.UserManager;
import com.example.weather.model.Diary;
import java.util.Calendar;
import java.util.Date;

public class DiaryEditActivity extends AppCompatActivity {
    private EditText etTitle, etContent;
    private CheckBox cbSetReminder;
    private Button btnSetDate, btnSetTime, btnSave;
    private Calendar reminderCalendar = Calendar.getInstance();
    private int diaryId = -1; // -1 表示新建日记
    private Button btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_edit);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        cbSetReminder = findViewById(R.id.cbSetReminder);
        btnSetDate = findViewById(R.id.btnSetDate);
        btnSetTime = findViewById(R.id.btnSetTime);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        setupListeners();

        // 检查是否编辑现有日记
        if (getIntent().hasExtra("diary_id")) {
            diaryId = getIntent().getIntExtra("diary_id", -1);
            loadDiary(diaryId);
        }
    }

    private void setupListeners() {
        cbSetReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnSetDate.setEnabled(isChecked);
            btnSetTime.setEnabled(isChecked);
        });

        btnSetDate.setOnClickListener(v -> showDatePicker());
        btnSetTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveDiary());
        btnDelete.setOnClickListener(v -> deleteDiary());
    }

    private void loadDiary(int id) {
        DiaryManager diaryManager = new DiaryManager(this);
        Diary diary = diaryManager.getDiaryById(id);

        if (diary != null) {
            etTitle.setText(diary.getTitle());
            etContent.setText(diary.getContent());

            if (diary.isReminderSet() && diary.getReminderTime() != null) {
                cbSetReminder.setChecked(true);
                btnSetDate.setEnabled(true);
                btnSetTime.setEnabled(true);

                Calendar cal = Calendar.getInstance();
                cal.setTime(diary.getReminderTime());
                reminderCalendar = cal;
            }
        }
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                this::onDateSet,
                reminderCalendar.get(Calendar.YEAR),
                reminderCalendar.get(Calendar.MONTH),
                reminderCalendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void onDateSet(DatePicker view, int year, int month, int day) {
        reminderCalendar.set(Calendar.YEAR, year);
        reminderCalendar.set(Calendar.MONTH, month);
        reminderCalendar.set(Calendar.DAY_OF_MONTH, day);
    }

    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(this,
                this::onTimeSet,
                reminderCalendar.get(Calendar.HOUR_OF_DAY),
                reminderCalendar.get(Calendar.MINUTE),
                true);
        dialog.show();
    }

    private void onTimeSet(TimePicker view, int hour, int minute) {
        reminderCalendar.set(Calendar.HOUR_OF_DAY, hour);
        reminderCalendar.set(Calendar.MINUTE, minute);
    }

    private void saveDiary() {
        UserManager userManager = new UserManager(this);
        int userId = userManager.getCurrentUserId();

        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setTitle(etTitle.getText().toString());
        diary.setContent(etContent.getText().toString());
        diary.setDate(new Date()); // 当前日期

        if (cbSetReminder.isChecked()) {
            diary.setReminderTime(reminderCalendar.getTime());
            diary.setReminderSet(true);
        }

        DiaryManager diaryManager = new DiaryManager(this);

        if (diaryId == -1) {
            // 新建日记
            diaryManager.addDiary(diary);
        } else {
            // 更新日记
            diary.setId(diaryId);
            diaryManager.updateDiary(diary);
        }

        finish();
    }

    // 添加删除方法
    private void deleteDiary() {
        new AlertDialog.Builder(this)
                .setTitle("删除日记")
                .setMessage("确定要删除这篇日记吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    DiaryManager diaryManager = new DiaryManager(this);
                    boolean deleted = diaryManager.deleteDiary(diaryId);
                    if (deleted) {
                        Toast.makeText(this, "日记已删除", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}