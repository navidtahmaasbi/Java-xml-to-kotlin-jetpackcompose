package com.azarpark.watchman.models;

import com.azarpark.watchman.core.AppConfig;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;

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
    String createTime;

    public Transaction(String amount, String our_token, String bank_token, int placeID, int status, String bank_type, String state, String card_number, String bank_datetime, String trace_number, String result_message,String createTime) {

        if (AppConfig.Companion.getPaymentIsSaman())
            amount = amount.replace(".0", "");

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
        this.createTime = createTime;
    }

    public void updateTransaction(String bankToken, int status, String state, String cardNumber, String bankDateTime, String traceNumber, String resultMessage){
        this.bank_token = bankToken;
        this.status = status;
        this.state = state;
        this.card_number = cardNumber;
        this.bank_datetime = bankDateTime;
        this.trace_number = traceNumber;
        this.result_message = resultMessage;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
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

    public void setStatus(int status) {
        this.status = status;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String string() {

        StringBuilder sb = new StringBuilder();

        sb.append("amount : ");
        sb.append(amount + " - ");
        sb.append("our_token : ");
        sb.append(our_token + " - ");
        sb.append("bank_token : ");
        sb.append(bank_token + " - ");
        sb.append("placeID : ");
        sb.append(placeID + " - ");
        sb.append("status : ");
        sb.append(status + " - ");
        sb.append("card_number : ");
        sb.append(card_number + " - ");
        sb.append("bank_datetime : ");
        sb.append(bank_datetime + " - ");
        sb.append("trace_number : ");
        sb.append(trace_number + " - ");
        sb.append("result_message : ");
        sb.append(result_message + " - ");
        sb.append("createTime : ");
        sb.append(createTime + " - ");


        return sb.toString();

    }
}
