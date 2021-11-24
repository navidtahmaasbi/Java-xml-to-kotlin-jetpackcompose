package com.azarpark.watchman.retrofit_remote.responses;

import com.azarpark.watchman.models.Park;

import java.util.ArrayList;

public class DebtHistoryResponse {

    public int success;
    public String msg;
    public String description;
    public int balance;
    public ArrayList<Park> items;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public ArrayList<Park> getItems() {
        return items;
    }

    public void setItems(ArrayList<Park> items) {
        this.items = items;
    }
}
