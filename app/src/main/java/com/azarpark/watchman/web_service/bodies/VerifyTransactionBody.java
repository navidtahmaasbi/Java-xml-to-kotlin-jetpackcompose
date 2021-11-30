package com.azarpark.watchman.web_service.bodies;

public class VerifyTransactionBody {

    String bank_type;
    String state;
    String card_number;
    String bank_datetime;
    String trace_number;
    String result_message;

    public VerifyTransactionBody(String bank_type, String state, String card_number, String bank_datetime, String trace_number, String result_message) {
        this.bank_type = bank_type;
        this.state = state;
        this.card_number = card_number;
        this.bank_datetime = bank_datetime;
        this.trace_number = trace_number;
        this.result_message = result_message;
    }
}
