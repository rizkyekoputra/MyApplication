package com.example.android.myapplication.models;

/**
 * Created by E460 on 12/01/2018.
 */

public class DataModel {
    private int id;
    private String summary;
    private String thumbnail_url;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }

}
