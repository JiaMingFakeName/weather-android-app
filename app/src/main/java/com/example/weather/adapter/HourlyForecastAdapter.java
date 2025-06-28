package com.example.weather.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.R;
import com.example.weather.model.HourlyForecast;

import java.util.List;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {

    private List<HourlyForecast> forecasts;

    public void setForecasts(List<HourlyForecast> forecasts) {
        this.forecasts = forecasts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HourlyForecast forecast = forecasts.get(position);
        holder.bind(forecast);
    }

    @Override
    public int getItemCount() {
        return forecasts == null ? 0 : forecasts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTime;
        private ImageView ivIcon;
        private TextView tvTemp;
        private TextView tvWind;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvWind = itemView.findViewById(R.id.tvWind);
        }

        public void bind(HourlyForecast forecast) {
            tvTime.setText(forecast.getTime());
            tvTemp.setText(forecast.getTemp() + "°");
            tvWind.setText(forecast.getWindDir() + forecast.getWindScale() + "级");

            // 根据图标代码设置图标
            int iconResId = getIconResource(forecast.getIcon());
            ivIcon.setImageResource(iconResId);
        }

        private int getIconResource(String iconCode) {
            // 根据和风天气的图标代码设置对应图标
            int code = Integer.parseInt(iconCode);
            if (code == 100) {
                return R.drawable.ic_sunny; // 晴
            } else if (code >= 101 && code <= 103) {
                return R.drawable.ic_partly_cloudy; // 多云
            } else if (code >= 300 && code <= 318) {
                return R.drawable.ic_rainy; // 雨
            } else if (code >= 400 && code <= 499) {
                return R.drawable.ic_snowy; // 雪
            } else {
                return R.drawable.ic_cloudy; // 其他情况默认多云
            }
        }
    }
}