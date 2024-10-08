package com.azarpark.watchman.web_service.responses;

import com.azarpark.watchman.models.Park;

import java.util.ArrayList;
import java.util.List;

public class DebtHistoryResponse {

    public int success;
    public String msg;
    public String description;
    public int balance;
    public List<DebtObject> objects;
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

    public List<DebtObject> getObjects() {
        return objects;
    }

    public void setObjects(List<DebtObject> objects) {
        this.objects = objects;
    }

    public ArrayList<Park> getItems() {
        return items;
    }

    public void setItems(ArrayList<Park> items) {
        this.items = items;
    }

    public int calculateTotalPrice(){
        int total = 0;
        for (DebtObject object : objects) {
            total += object.value;
        }
        return total;
    }
}
