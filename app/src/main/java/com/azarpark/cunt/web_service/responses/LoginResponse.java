package com.azarpark.cunt.web_service.responses;

public class LoginResponse {

    public String token_type;
    public double expires_in;
    public String access_token;
    public String refresh_token;
    public String description;

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public double getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(double expires_in) {
        this.expires_in = expires_in;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }
}
