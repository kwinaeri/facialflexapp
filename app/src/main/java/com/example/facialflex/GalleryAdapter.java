package com.example.facialflex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ImageData> imageList; // Change from imageUrls to ImageData list

    public GalleryAdapter(Context context, ArrayList<ImageData> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_image, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageView);
        TextView timestampView = convertView.findViewById(R.id.timestampTextView); // New TextView for timestamp

        // Get the image data
        ImageData imageData = imageList.get(position);

        // Load image into ImageView using Glide
        Glide.with(context).load(imageData.getImageUrl()).into(imageView);

        // Set the timestamp below the image
        timestampView.setText(imageData.getTimestamp());

        // Set click listener to open the image in a full-screen dialog
        imageView.setOnClickListener(v -> {
            // Create a new instance of ImageDialogFragment with the selected image URL
            ImageDialogFragment dialogFragment = ImageDialogFragment.newInstance(imageData.getImageUrl());

            // Show the dialog fragment
            // Check if the context is an instance of FragmentActivity to access the fragment manager
            if (context instanceof FragmentActivity) {
                FragmentActivity fragmentActivity = (FragmentActivity) context;
                dialogFragment.show(fragmentActivity.getSupportFragmentManager(), "image_dialog");
            }
        });

        return convertView;
    }
}
