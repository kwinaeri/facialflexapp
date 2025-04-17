package com.example.facialflex;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VideoPagerAdapter extends RecyclerView.Adapter<VideoPagerAdapter.ViewHolder> {

    private Context context;
    private List<VideoModel> videoList;
    private int restTimeInSeconds = 11; // Adjust the rest time as needed
    private CountDownTimer countDownTimer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable onFinishRestRunnable;

    public VideoPagerAdapter(Context context, List<VideoModel> videoList) {
        this.context = context;
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoModel video = videoList.get(position);
        holder.title.setText(video.getName());

        // Convert HTML to Spanned
        Spanned description = Html.fromHtml(video.getDescription(), Html.FROM_HTML_MODE_LEGACY);
        holder.description.setText(description);

        // Set up the VideoView for the current video
        Uri videoUri = Uri.parse(video.getUrl());
        holder.videoView.setVideoURI(videoUri);

        // Show loading text while preparing video
        holder.loadingText.setVisibility(View.VISIBLE);
        holder.videoView.setOnPreparedListener(mp -> {
            // Hide loading text when video starts playing
            holder.loadingText.setVisibility(View.GONE);
            holder.videoView.start();
        });

        holder.videoView.setOnCompletionListener(mp -> {
            startRestCountdown(holder);
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.videoView.stopPlayback(); // Stop playback when view is recycled
        // Stop and cancel countdown timer when view is recycled
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        // Hide the rest icon and timer when view is recycled
        holder.restIcon.setVisibility(View.GONE);
        holder.restTimer.setVisibility(View.GONE);
        // Hide loading text
        holder.loadingText.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    private void startRestCountdown(final ViewHolder holder) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Show the rest timer and icon
        holder.restTimer.setVisibility(View.VISIBLE);
        holder.restIcon.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(restTimeInSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the rest timer UI with remaining time
                long secondsRemaining = millisUntilFinished / 1000;
                holder.restTimer.setText(String.format("%d seconds remaining to rest", secondsRemaining));
            }

            @Override
            public void onFinish() {
                // Hide the rest timer and icon
                holder.restTimer.setVisibility(View.GONE);
                holder.restIcon.setVisibility(View.GONE);
                // Call the Runnable to transition to the next page
                handler.post(onFinishRestRunnable);
            }
        }.start();
    }

    public void setOnFinishRestRunnable(Runnable runnable) {
        this.onFinishRestRunnable = runnable;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        ImageView restIcon;
        TextView restTimer;
        VideoView videoView;
        TextView loadingText; // Added

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.video_title);
            description = itemView.findViewById(R.id.video_description);
            videoView = itemView.findViewById(R.id.video_view);
            restIcon = itemView.findViewById(R.id.rest_icon);
            restTimer = itemView.findViewById(R.id.rest_timer);
            loadingText = itemView.findViewById(R.id.loading_text); // Initialize
        }
    }
}
