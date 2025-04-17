package com.example.facialflex;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RCAdapter extends RecyclerView.Adapter<RCAdapter.ViewHolder> {
    private Context context;
    private ArrayList<RCModel> modelArrayList;

    // Constructor
    public RCAdapter(Context context, ArrayList<RCModel> modelArrayList) {
        this.context = context;
        this.modelArrayList = modelArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RCModel model = modelArrayList.get(position);
        holder.title.setText(model.getTitle());

        // Load image with Glide or any other image loading library
        Glide.with(context)
                .load(model.getImageResId())
                .into(holder.image);

        // Set click listener on the entire item view
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VideoPlaylistActivity.class);
            intent.putExtra("CATEGORY_NAME", model.getTitle());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return modelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rc_title);
            image = itemView.findViewById(R.id.rc_image);
        }
    }
}



