package com.azarpark.watchman.retrofit_remote.responses;

public class EstimateParkPriceResponse {

    int success,price,hours,minutes,car_balance;
    String msg;
    public String description;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getCar_balance() {
        return car_balance;
    }

    public void setCar_balance(int car_balance) {
        this.car_balance = car_balance;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
