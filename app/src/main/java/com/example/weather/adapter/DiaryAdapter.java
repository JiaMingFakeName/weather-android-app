package com.example.weather.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weather.R;
import com.example.weather.model.Diary;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    private final List<Diary> diaries;
    private final OnDiaryClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface OnDiaryClickListener {
        void onDiaryClick(Diary diary);
    }

    public DiaryAdapter(List<Diary> diaries, OnDiaryClickListener listener) {
        this.diaries = diaries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        Diary diary = diaries.get(position);
        holder.tvTitle.setText(diary.getTitle());
        holder.tvDate.setText(dateFormat.format(diary.getDate()));

        // 显示内容摘要（最多50字符）
        String contentPreview = diary.getContent().length() > 50 ?
                diary.getContent().substring(0, 50) + "..." : diary.getContent();
        holder.tvContentPreview.setText(contentPreview);

        holder.itemView.setOnClickListener(v -> listener.onDiaryClick(diary));
    }

    @Override
    public int getItemCount() {
        return diaries.size();
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvContentPreview;

        DiaryViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvContentPreview = itemView.findViewById(R.id.tvContentPreview);
        }
    }
}