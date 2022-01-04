package com.azarpark.watchman.web_service.responses;

public class EstimateParkPriceResponse {

    int success,price,hours,minutes,car_balance,users_count;
    String msg;
    public String description;
    String print_description;
    public int print_command = 1;


    public String getPrint_description() {
        return print_description;
    }

    public void setPrint_description(String print_description) {
        this.print_description = print_description;
    }

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

    public int getUsers_count() {
        return users_count;
    }

    public void setUsers_count(int users_count) {
        this.users_count = users_count;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
