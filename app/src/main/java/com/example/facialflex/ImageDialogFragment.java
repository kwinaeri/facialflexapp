package com.example.facialflex;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageDialogFragment extends DialogFragment {

    private static final String ARG_IMAGE_URL = "image_url";

    public static ImageDialogFragment newInstance(String imageUrl) {
        ImageDialogFragment fragment = new ImageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_image_view, container, false);
        ImageView imageView = view.findViewById(R.id.fullImageView);

        // Get the image URL from arguments and load it into the ImageView
        String imageUrl = getArguments().getString(ARG_IMAGE_URL);
        Glide.with(this).load(imageUrl).into(imageView);

        // Set a click listener to dismiss the dialog when clicked
        view.setOnClickListener(v -> dismiss());

        return view;
    }
}
