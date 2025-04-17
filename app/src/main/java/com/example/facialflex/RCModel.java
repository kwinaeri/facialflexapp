package com.example.facialflex;

public class RCModel {
    private String title;
    private String imageResId; // Make sure this matches your usage (URL or drawable resource name)

    // Constructor
    public RCModel(String title, String imageResId) {
        this.title = title;
        this.imageResId = imageResId;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getImageResId() {
        return imageResId;
    }
}
