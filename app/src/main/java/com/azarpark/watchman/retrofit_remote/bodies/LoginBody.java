package com.azarpark.watchman.retrofit_remote.bodies;

public class LoginBody {

    int client_id = 6;
    String client_secret = "mxAE95luSOKcH43kv53QMiHDYV3w0XpBYLYmbGoR",
            grant_type = "password",
            username, password;

    public LoginBody(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
}
