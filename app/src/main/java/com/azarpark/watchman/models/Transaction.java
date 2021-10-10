package com.azarpark.watchman.models;

import com.azarpark.watchman.utils.Assistant;

public class Transaction {


    String amount;
    String our_token;
    String bank_token;
    int placeID;
    int status;
    String bank_type;
    String state;
    String card_number;
    String bank_datetime;
    String trace_number;
    String result_message;

    public Transaction(String amount, String our_token, String bank_token, int placeID, int status, String bank_type, String state, String card_number, String bank_datetime, String trace_number, String result_message) {

        if (Assistant.SELECTED_PAYMENT == Assistant.SAMAN)
            amount = amount.replace(".0","");

        this.amount = amount;
        this.our_token = our_token;
        this.bank_token = bank_token;
        this.placeID = placeID;
        this.status = status;
        this.bank_type = bank_type;
        this.state = state;
        this.card_number = card_number;
        this.bank_datetime = bank_datetime;
        this.trace_number = trace_number;
        this.result_message = result_message;
    }

    public String getAmount() {
        return amount;
    }

    public String getOur_token() {
        return our_token;
    }

    public String getBank_token() {
        return bank_token;
    }

    public int getPlaceID() {
        return placeID;
    }

    public int getStatus() {
        return status;
    }

    public String getBank_type() {
        return bank_type;
    }

    public String getState() {
        return state;
    }

    public String getCard_number() {
        return card_number;
    }

    public String getBank_datetime() {
        return bank_datetime;
    }

    public String getTrace_number() {
        return trace_number;
    }

    public String getResult_message() {
        return result_message;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void devideAmountByTen() {

        amount.replace(",", "");

        amount = Integer.toString((Integer.parseInt(amount) / 10));

    }
}
