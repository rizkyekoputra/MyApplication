package com.example.android.myapplication.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FileInfo {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("thumbnail_url")
    @Expose
    private String thumbnail_url;
    @SerializedName("original_url")
    @Expose
    private String original_url;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }

    public String getOriginal_url() {
        return original_url;
    }

    public void setOriginal_url(String original_url) {
        this.original_url = original_url;
    }
}
