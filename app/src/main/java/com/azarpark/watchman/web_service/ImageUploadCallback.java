package com.azarpark.watchman.web_service;

public interface ImageUploadCallback {
    void onProgressUpdate(int percentage);
    void onError(String message);
    void onSuccess(String message);
}