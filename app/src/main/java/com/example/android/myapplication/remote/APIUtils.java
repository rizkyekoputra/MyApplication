package com.example.android.myapplication.remote;

public class APIUtils {

    private APIUtils(){
    }

    public static final String API_URL = "https://test-mobile.neo-fusion.com/data/";

    public static FileService getFileService(){
        return RetrofitClient.getClient(API_URL).create(FileService.class);
    }
}
