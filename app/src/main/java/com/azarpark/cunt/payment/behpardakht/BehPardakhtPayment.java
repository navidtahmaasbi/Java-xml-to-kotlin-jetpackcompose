package com.azarpark.cunt.payment.behpardakht;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.azarpark.cunt.activities.QRScanerActivity;
import com.azarpark.cunt.enums.PlateType;
import com.azarpark.cunt.models.PaymentData;
import com.azarpark.cunt.models.PaymentResult;
import com.azarpark.cunt.models.Transaction;
import com.azarpark.cunt.payment.PaymentService;
import com.azarpark.cunt.payment.ShabaType;
import com.azarpark.cunt.payment.behpardakht.device.Device;
import com.azarpark.cunt.payment.behpardakht.device.IPosPrinterEvent;
import com.azarpark.cunt.utils.Assistant;
import com.azarpark.cunt.utils.Constants;
import com.azarpark.cunt.utils.Logger;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.azarpark.cunt.web_service.WebService;
import com.google.gson.Gson;


public class BehPardakhtPayment extends PaymentService {

    public static final int PAYMENT_REQUEST_CODE = 105;
    private final String versionName = "1.2.0";
    private final String BEH_PARDAKHT = "beh_pardakht";
    private final int applicationId = 10073;

    private Gson gson;
    private Device device;


    public BehPardakhtPayment(@NonNull AppCompatActivity activity, @NonNull WebService webService, @NonNull OnPaymentCallback paymentCallback) {
        super(activity, webService, paymentCallback);
        gson = new Gson();
        device = Device.getInstance(activity);
    }

    @Override
    public void onActivityResultHandler(int requestCode, int resultCode, @NonNull Intent data) {
        if (requestCode == Constants.QR_SCANER_REQUEST_CODE && resultCode == PAYMENT_REQUEST_CODE && data != null) {
            try {

                String scannedData = data.getExtras().getString(Constants.QR_DATA);
                int placeId = Integer.parseInt(scannedData.split("=")[scannedData.split("=").length - 1]);
                getPaymentCallback().onScanDataReceived(placeId);

            } catch (Exception e) {
                Toast.makeText(getActivity(), "معتبر نمیباشد", Toast.LENGTH_LONG).show();
            }

        }

        if (requestCode != PAYMENT_REQUEST_CODE) {
            System.out.println("----------> This result is not for Behpardakht payment");
        } else if (resultCode != Activity.RESULT_OK) {
            System.out.println("----------> Behpardakht resultCode is " + resultCode);
        } else {
            PaymentResult paymentResult = gson.fromJson(data.getStringExtra("PaymentResult"), PaymentResult.class);
            int payResultCode = -1;
            try{
                payResultCode = Integer.parseInt(paymentResult.resultCode);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            Logger.i("---------> resss : " + data.getStringExtra("PaymentResult"));

            if(payResultCode == 0) // means SUCCESSFUL
            {
                Transaction transaction = new Transaction(
                        paymentResult.transactionAmount == null || paymentResult.transactionAmount.isEmpty()
                                ? SharedPreferencesRepository.getValue(Constants.AMOUNT, "0")
                                : Integer.toString(Integer.parseInt(paymentResult.transactionAmount) / 10),
                        paymentResult.sessionId,
                        paymentResult.referenceID,
                        Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "-1")),
                        1,
                        getPaymentType(),
                        paymentResult.resultDescription == null ? "" : paymentResult.resultDescription,
                        paymentResult.maskedCardNumber,
                        paymentResult.dateOfTransaction,
                        paymentResult.referenceID,
                        paymentResult.resultDescription,
                        paymentResult.dateOfTransaction
                );
                Logger.i("------------> behpardakh transaction: %s", transaction);
                verifyTransaction(transaction);
            }
        }
    }

//    public String shaba(TransactionAmount transactionAmount) {
//        return "000000000000000000000000000000:" + (transactionAmount.getAmount() * 10) + ":" + transactionAmount.getShaba();
//    }
//
//    @Override
//    public void launchPayment(ShabaType shabaType, long paymentToken, int amount, @NonNull PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {
//
//        launchPayment(shabaType, paymentToken, Collections.emptyList(), amount, plateType, tag1, tag2, tag3, tag4, placeID);
//    }
//
//    @Override
//    public void launchPayment(@NonNull ShabaType shabaType, long paymentToken, @NonNull List<TransactionAmount> amountPartList, int amount, @NonNull PlateType plateType, @Nullable String tag1, @Nullable String tag2, @Nullable String tag3, @Nullable String tag4, int placeID) {
//        String[] param;
//
//        boolean isWage = false;
//        try {
//            isWage = Boolean.parseBoolean(SharedPreferencesRepository.getValue(Constants.IS_WAGE_TRANSACTION, "false"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (isWage) {
//            param = new String[amountPartList.size()];
//            for (int i = 0; i < param.length; i++) {
//                param[i] = shaba(amountPartList.get(i));
//            }
//        } else {
//            param = new String[1];
//            if (AppConfig.Companion.isArdabil()) {
//                {
//                    param[0] = this.shaba(new TransactionAmount(amount, Constants.ARDABIL_SHABA));
//                }
//            } else { // TABRIZ
//                if (shabaType == ShabaType.CHARGE) {
//                    param[0] = this.shaba(new TransactionAmount(amount, Constants.CHARGE_SHABA));
//                } else if (shabaType == ShabaType.NON_CHARGE) {
//                    param[0] = this.shaba(new TransactionAmount(amount, Constants.NON_CHARGE_SHABA));
//                }
//            }
//        }
//
//
//        Logger.d("--------------> Params: %s", Arrays.toString(param));
//
//        Intent intent = new Intent();
//        intent.putExtra("TransType", 3);
//        intent.putExtra("Amount", String.valueOf(amount * 10));
//        intent.putExtra("ResNum", Long.toString(paymentToken));
//        intent.putExtra("AppId", "0");
//        intent.putExtra("Tashim", param);
//
//
//        Logger.d(
//                "requesting Tashim payment: (\nTransType: %d\nAmount: %s\nResNum: %s\nAppId: %s\nTashim: %s\n)",
//                intent.getExtras().getInt("TransType"),
//                intent.getExtras().getString("Amount"),
//                intent.getExtras().getString("ResNum"),
//                intent.getExtras().getString("AppId"),
//                intent.getExtras().getStringArray("Tashim")
//        );
//
//        intent.setComponent(new ComponentName("ir.sep.android.smartpos", "ir.sep.android.smartpos.ThirdPartyActivity"));
//        getActivity().startActivityForResult(intent, PAYMENT_REQUEST_CODE);
//    }

    @Override
    public void launchPayment(@NonNull ShabaType shabaType, long paymentToken, int amount, @NonNull PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {
        String accountId = shabaType == ShabaType.CHARGE ? "1" : "2";

        String[] extras = new String[1];
        extras[0] = Integer.toString(placeID);

        PaymentData paymentData = PaymentData.create(
                versionName,
                Long.toString(paymentToken),
                applicationId,
                Integer.toString(amount * 10),
                PaymentData.TransactionType.PURCHASE,
                accountId,
                extras
        );

        String pd = gson.toJson(paymentData);
        Logger.i("PaymentData: %s", pd);

        Intent intent = new Intent("com.bpmellat.merchant");
        intent.putExtra("PaymentData", pd);
        getActivity().startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }

    @Override
    public void launchQrCodeScanner() {
        getActivity().startActivityForResult(new Intent(getActivity(), QRScanerActivity.class), Constants.QR_SCANER_REQUEST_CODE);
    }

    @NonNull
    @Override
    public String getPaymentType() {
        return BEH_PARDAKHT;
    }

    @Override
    public void print(View view, int waitTime, OnPrintDone callback) {
        Logger.d("BehPardakht launching print...");
        new Handler(getActivity().getMainLooper())
                .postDelayed(
                        () -> {
                            Logger.d("BehPardakht launching print... STARTED");
                            try {
                                device.print(Assistant.viewToBitmap(view), new IPosPrinterEvent() {
                                    @Override
                                    public void onPrintStarted() {

                                    }

                                    @Override
                                    public void onPrinterError(int error) {

                                    }

                                    @Override
                                    public void onPrintEnd() {

                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            if (callback != null)
                                callback.onDone();
                        },
                        waitTime
                );
    }
}

