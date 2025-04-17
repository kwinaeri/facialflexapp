package com.example.facialflex;

public class ImageData {
    private String imageUrl;
    private String timestamp;

    public ImageData(String imageUrl, String timestamp) {
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
