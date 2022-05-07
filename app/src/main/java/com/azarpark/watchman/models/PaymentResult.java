package com.azarpark.watchman.models;

public class PaymentResult {
    public String versionName;                     /// Mandatory
    public String sessionId;                       /// Mandatory, received from CallerApp
    public int applicationId;                      /// Mandatory, received from CallerApp
    public String acquirerDiscountAmount;          // Optional, received from SW.
    public String transactionAmount;               /// Mandatory,
    public String billID;
    public String payID;
    public int resultCode;                         /// Mandatory
    public String resultDescription;               /// Optional
    public int retrievalReferencedNumber;          /// Mandatory
    public String referenceID;                     /// Mandatory
    public String dateOfTransaction;               /// Mandatory
    public String timeOfTransaction;               /// Mandatory
    public String maskedCardNumber;                /// Mandatory
    public String BIN;                             /// Mandatory
    public String terminalID;                      /// Mandatory
    public String acquirerMessage;                 /// Optional
    public String[] extras;                        /// Optional
}