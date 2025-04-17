package com.example.facialflex;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WebViewAdapter extends RecyclerView.Adapter<WebViewAdapter.WebViewHolder> {
    private List<String> videoUrls;

    public WebViewAdapter(List<String> videoUrls) {
        this.videoUrls = videoUrls;
    }

    @NonNull
    @Override
    public WebViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate a layout with a WebView (You can also create the WebView programmatically)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_webview, parent, false);
        return new WebViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WebViewHolder holder, int position) {
        String videoId = videoUrls.get(position);
        String videoUrl = "https://www.youtube.com/embed/" + videoId;

        // Set up the WebView to display the YouTube video
        holder.webView.loadUrl(videoUrl);
        WebSettings webSettings = holder.webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript
        holder.webView.setWebViewClient(new WebViewClient());
    }

    @Override
    public int getItemCount() {
        return videoUrls.size();
    }

    public static class WebViewHolder extends RecyclerView.ViewHolder {
        WebView webView;

        public WebViewHolder(@NonNull View itemView) {
            super(itemView);
            webView = itemView.findViewById(R.id.webView);
        }
    }
}
