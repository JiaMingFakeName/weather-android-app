package com.example.weather.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.R;
import com.example.weather.model.DailyForecast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.ViewHolder> {

    private List<DailyForecast> forecasts;

    public void setForecasts(List<DailyForecast> forecasts) {
        this.forecasts = forecasts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyForecast forecast = forecasts.get(position);
        holder.bind(forecast);
    }

    @Override
    public int getItemCount() {
        return forecasts == null ? 0 : forecasts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;
        private ImageView ivDayIcon;
        private TextView tvDayTemp;
        private ImageView ivNightIcon;
        private TextView tvNightTemp;
        private TextView tvHumidity;
        private TextView tvWindDay;
        private TextView tvWindNight;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivDayIcon = itemView.findViewById(R.id.ivDayIcon);
            tvDayTemp = itemView.findViewById(R.id.tvDayTemp);
            ivNightIcon = itemView.findViewById(R.id.ivNightIcon);
            tvNightTemp = itemView.findViewById(R.id.tvNightTemp);
            tvHumidity = itemView.findViewById(R.id.tvHumidity);
            tvWindDay = itemView.findViewById(R.id.tvWindDay);
            tvWindNight = itemView.findViewById(R.id.tvWindNight);
        }

        public void bind(DailyForecast forecast) {
            // 格式化日期
            tvDate.setText(formatDate(forecast.getDate()));
            tvDayTemp.setText(forecast.getMaxTemp() + "°");
            tvNightTemp.setText(forecast.getMinTemp() + "°");
            tvHumidity.setText("湿度: " + forecast.getHumidity() + "%");
            tvWindDay.setText(forecast.getWindDirDay() + forecast.getWindScaleDay() + "级");
            tvWindNight.setText(forecast.getWindDirNight() + forecast.getWindScaleNight() + "级");

            // 设置图标
            int dayIconRes = getIconResource(forecast.getDayIcon());
            int nightIconRes = getIconResource(forecast.getNightIcon());
            ivDayIcon.setImageResource(dayIconRes);
            ivNightIcon.setImageResource(nightIconRes);
        }

        private String formatDate(String dateStr) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd E", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateStr;
            }
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