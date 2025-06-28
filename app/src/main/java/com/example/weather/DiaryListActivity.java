package com.example.weather;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.adapter.DiaryAdapter;
import com.example.weather.manager.DiaryManager;
import com.example.weather.manager.UserManager;
import com.example.weather.model.Diary;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DiaryListActivity extends AppCompatActivity implements DiaryAdapter.OnDiaryClickListener {
    private RecyclerView recyclerView;
    private DiaryAdapter adapter;
    private MaterialToolbar toolbar;
    private Button btnAddDiary; // 添加按钮引用
    private MaterialAutoCompleteTextView filterView;
    private int currentFilter = 0; // 0=全部, 1=日, 2=周, 3=月


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_list);

        // 初始化工具栏
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish()); // 返回按钮功能

        btnAddDiary = findViewById(R.id.btnAddDiary);
        btnAddDiary.setOnClickListener(v -> onAddDiaryClick(v));

        recyclerView = findViewById(R.id.rvDiaries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 正确获取筛选器视图
        TextInputLayout filterLayout = findViewById(R.id.filterLayout);
        filterView = (MaterialAutoCompleteTextView) filterLayout.findViewById(R.id.spinnerFilter);

        // 设置筛选器
        setupFilterSpinner();
        loadDiaries();
    }

    private void setupFilterSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.diary_filter_options, R.layout.filter_dropdown_item);
        adapter.setDropDownViewResource(R.layout.filter_dropdown_item);

        filterView.setAdapter(adapter);

        filterView.setOnItemClickListener((parent, view, position, id) -> {
            currentFilter = position;
            loadDiaries();
        });

        // 设置默认选中项
        filterView.setText(adapter.getItem(0), false);
    }

    private void loadDiaries() {
        UserManager userManager = new UserManager(this);
        int userId = userManager.getCurrentUserId();
        DiaryManager diaryManager = new DiaryManager(this);

        List<Diary> diaries;
        Calendar calendar = Calendar.getInstance();

        switch (currentFilter) {
            case 1: // 日
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Date todayStart = calendar.getTime();

                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                Date todayEnd = calendar.getTime();

                diaries = diaryManager.getDiariesByDateRange(userId, todayStart, todayEnd);
                break;

            case 2: // 周
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Date weekStart = calendar.getTime();

                calendar.add(Calendar.DAY_OF_WEEK, 6);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                Date weekEnd = calendar.getTime();

                diaries = diaryManager.getDiariesByDateRange(userId, weekStart, weekEnd);
                break;

            case 3: // 月
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Date monthStart = calendar.getTime();

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                Date monthEnd = calendar.getTime();

                diaries = diaryManager.getDiariesByDateRange(userId, monthStart, monthEnd);
                break;

            default: // 全部
                diaries = diaryManager.getDiariesByUser(userId);
        }

        adapter = new DiaryAdapter(diaries, this);
        recyclerView.setAdapter(adapter);

        // 显示筛选结果信息
        showFilterResultMessage(diaries.size());
    }

    private void showFilterResultMessage(int count) {
        String message = "";
        switch (currentFilter) {
            case 0: message = "全部日记 (" + count + "篇)"; break;
            case 1: message = "今日日记 (" + count + "篇)"; break;
            case 2: message = "本周日记 (" + count + "篇)"; break;
            case 3: message = "本月日记 (" + count + "篇)"; break;
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDiaryClick(Diary diary) {
        // 跳转到编辑界面
        Intent intent = new Intent(this, DiaryEditActivity.class);
        intent.putExtra("diary_id", diary.getId());
        startActivity(intent);
    }

    // 添加按钮点击事件
    public void onAddDiaryClick(View view) {
        startActivity(new Intent(this, DiaryEditActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDiaries(); // 返回时刷新列表
    }
}