package com.azarpark.watchman.web_service.bodies;

public class LoginBody {

    int client_id = 6;
    String client_secret = "mxAE95luSOKcH43kv53QMiHDYV3w0XpBYLYmbGoR",
            grant_type = "password",
            username, password, serial_number;

    public LoginBody(String username, String password, String serialNumber) {
        this.username = username;
        this.password = password;
        serial_number = serialNumber;
    }

    public String getUsername() {
        return username;
    }
}
