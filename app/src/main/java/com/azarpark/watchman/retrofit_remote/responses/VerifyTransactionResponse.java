package com.azarpark.watchman.retrofit_remote.responses;

public class VerifyTransactionResponse {

    int success;
    String description;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
