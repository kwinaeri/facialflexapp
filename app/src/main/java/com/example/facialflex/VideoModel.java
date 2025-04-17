package com.example.facialflex;

public class VideoModel {
    private String name;
    private String description;
    private String url;

    public VideoModel(String name, String description, String url) {
        this.name = name;
        this.description = description;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }
}
