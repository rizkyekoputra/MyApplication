package com.example.android.myapplication.remote;

import com.example.android.myapplication.models.FileInfo;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileService {

    @Multipart
    @POST("create")
    Call<FileInfo> upload(@Header("Access-Token") String token, @Part MultipartBody.Part file);

}
