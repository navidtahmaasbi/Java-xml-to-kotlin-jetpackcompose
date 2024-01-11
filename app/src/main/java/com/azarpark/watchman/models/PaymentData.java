package com.azarpark.watchman.models;

//{
// "applicationId":1000,
// "printPaymentDetails":false,
// "saveDetail":false,
// "sessionId":"sessionId1515774896730781896",
// "totalAmount":"1000",
// "transactionType":"PURCHASE",
// "versionName":"1.0.0"
// }

public class PaymentData {
    public enum TransactionType {
        PURCHASE,
        PAYMENT,
        MULTIPAYMENT
    }

    private String versionName; /// Mandatory
    private String sessionId; /// Mandatory
    private int applicationId; /// Mandatory
    private String totalAmount; /// Mandatory
    private TransactionType transactionType; /// Mandatory
//    private String accountId;
    private boolean printPaymentDetails = true;
    private String[] extras; /// Optional


    public static PaymentData create(
            String versionName, String sessionId, int applicationId, String totalAmount,
            TransactionType transactionType, String accountId, String[] extras
    ) {
        PaymentData data = new PaymentData();
        data.versionName = versionName;
        data.sessionId = sessionId;
        data.applicationId = applicationId;
        data.totalAmount = totalAmount;
        data.transactionType = transactionType;
//        data.accountId = accountId;
        data.extras = extras;
        return data;
    }
}