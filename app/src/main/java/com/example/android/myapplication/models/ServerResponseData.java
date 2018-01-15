package com.example.android.myapplication.models;

/**
 * Created by E460 on 12/01/2018.
 */

public class ServerResponseData {
    private int responseCode;
    private String message;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
