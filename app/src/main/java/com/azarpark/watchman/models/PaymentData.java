package com.azarpark.watchman.models;

public class PaymentData {
    private String versionName;/// Mandatory
    private String sessionId;/// Mandatory
    private int applicationId;/// Mandatory
    private String totalAmount;/// Mandatory
    private TransactionType transactionType;/// Mandatory
    private String billID;
    private String payID;
    private String payerId;/// Optional
    private String accountId;/// Complementary
    private String paymentDetail;/// Complementary
    private String merchantMessage;/// Optional
    private String merchantAdditionalData;/// Optional
    private boolean printPaymentDetails = false;/// Optional, default = false
    private String[] extras;/// Optional

    public PaymentData(String versionName, String sessionId, int applicationId, String totalAmount, TransactionType transactionType) {
        this.versionName = versionName;
        this.sessionId = sessionId;
        this.applicationId = applicationId;
        this.totalAmount = totalAmount;
        this.transactionType = transactionType;
    }

    public PaymentData(String versionName, String sessionId, int applicationId, String totalAmount, String paymentDetail) {
        this.versionName = versionName;
        this.sessionId = sessionId;
        this.applicationId = applicationId;
        this.totalAmount = totalAmount;
        this.transactionType = TransactionType.MULTIPAYMENT;
        this.paymentDetail = paymentDetail;
    }

    public enum TransactionType {
        PURCHASE, PAYMENT,
        MULTIPAYMENT,
        BILLPAYMENT
    }
}
