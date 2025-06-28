package com.example.weather.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private List<String> playlist; // 移除final修饰符
    private final OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public PlaylistAdapter(List<String> playlist, OnItemClickListener listener) {
        this.playlist = playlist;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 添加空列表检查
        if (playlist == null || position >= playlist.size()) return;
        String path = playlist.get(position);
        String name = "未知曲目";

        try {
            if (path.startsWith("http")) {
                name = "在线音乐: " + path.substring(path.lastIndexOf('/') + 1);
            } else {
                name = "本地音乐: " + new File(path).getName();
            }
        } catch (Exception e) {
            Log.e("PlaylistAdapter", "解析路径失败", e);
        }

        holder.textView.setText(name);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position < playlist.size()) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlist.size();
    }

    // 新增方法：更新播放列表
    public void updatePlaylist(List<String> newPlaylist) {
        this.playlist = newPlaylist;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(android.R.id.text1);
            textView.setTextColor(view.getContext().getColor(android.R.color.black));
        }
    }
}