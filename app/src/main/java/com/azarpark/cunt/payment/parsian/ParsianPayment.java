package com.azarpark.cunt.payment.parsian;

import android.app.Activity;
import android.content.Intent;
import android.device.PrinterManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.azarpark.cunt.activities.QRScanerActivity;
import com.azarpark.cunt.enums.PlateType;
import com.azarpark.cunt.models.Transaction;
import com.azarpark.cunt.payment.PaymentService;
import com.azarpark.cunt.payment.ShabaType;
import com.azarpark.cunt.utils.Assistant;
import com.azarpark.cunt.utils.Constants;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.azarpark.cunt.web_service.WebService;

public class ParsianPayment extends PaymentService {
    public static int PAYMENT_REQUEST_CODE = 103;
    String PARSIAN = "parsian";


    public ParsianPayment(@NonNull AppCompatActivity activity, @NonNull WebService webService, @NonNull OnPaymentCallback paymentCallback) {
        super(activity, webService, paymentCallback);
    }

    @Override
    public void onActivityResultHandler(int requestCode, int resultCode, @NonNull Intent data) {
        if (requestCode == PAYMENT_REQUEST_CODE) {
            Transaction transaction;

            if (resultCode == Activity.RESULT_OK) {
                Bundle b = data.getBundleExtra("response");
                if (b != null) {
                    int amount = Integer.parseInt(SharedPreferencesRepository.getValue(Constants.AMOUNT, "0"));
                    String result = b.getString("result");
                    String pan = b.getString("pan");
                    String rrn = b.getString("rrn");
                    Long date = b.getLong("date");
                    String trace = b.getString("trace");
                    String errorMessage = b.getString("message", "");
                    Long res_num = b.getLong("res_num");
                    int status = result.equals("succeed") ? 1 : -1;
                    int placeID = Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0"));


                    transaction = new Transaction(
                            Integer.toString(amount),
                            Long.toString(res_num),
                            (rrn == null || rrn.isEmpty()) ? "0" : rrn,
                            placeID,
                            status,
                            PARSIAN,
//                            result.equals("succeed") ? "1" : "-1",
                            result,
                            pan,
                            Long.toString(date),
                            trace,
                            result,
                            Assistant.getUnixTime());
                } else {
                    String amount = SharedPreferencesRepository.getValue(Constants.AMOUNT, "0");
                    String resNum = SharedPreferencesRepository.getValue(Constants.OUR_TOKEN, "0");
                    int placeID = Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0"));
                    String tag4 = SharedPreferencesRepository.getValue(Constants.TAG4, "0");

                    transaction = new Transaction(
                            amount,
                            resNum,
                            "0",
                            placeID,
                            -1,
                            PARSIAN,
                            "-1",
                            "****-****-****-****",
                            "0",
                            null,
                            "not succeed",
                            Assistant.getUnixTime());
                }


            } else {
                String amount = SharedPreferencesRepository.getValue(Constants.AMOUNT, "0");
                String resNum = SharedPreferencesRepository.getValue(Constants.OUR_TOKEN, "0");
                int placeID = Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0"));

                transaction = new Transaction(
                        amount,
                        resNum,
                        "0",
                        placeID,
                        -1,
                        PARSIAN,
                        "-1",
                        "****-****-****-****",
                        "0",
                        "",
                        "not succeed",
                        Assistant.getUnixTime());
            }

            SharedPreferencesRepository.updateTransactions02(transaction);
            verifyTransaction(transaction);
        } else if (requestCode == Constants.QR_SCANER_REQUEST_CODE) {
            if (data != null) {
                try {
                    String scannedData = data.getExtras().getString(Constants.QR_DATA);
                    int placeId = Integer.parseInt(scannedData.split("=")[scannedData.split("=").length - 1]);
                    getPaymentCallback().onScanDataReceived(placeId);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "معتبر نمیباشد", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void launchPayment(@NonNull ShabaType shabaType, long paymentToken, int amount, @NonNull PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {
        amount *= 10;
        Intent intent = new Intent("ir.totan.pos.view.cart.TXN");
        intent.putExtra("type", 3);
        intent.putExtra("amount", Integer.toString(amount));
        intent.putExtra("res_num", Long.toString(paymentToken));
        getActivity().startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }

    @Override
    public void launchQrCodeScanner() {
        getActivity().startActivityForResult(new Intent(getActivity(), QRScanerActivity.class), Constants.QR_SCANER_REQUEST_CODE);
    }

    @Override
    public void print(View view, int waitTime, OnPrintDone callback) {
        new Handler(getActivity().getMainLooper())
                .postDelayed(
                        () -> {
                            PrinterManager printer = new PrinterManager();
                            int setupResult = printer.setupPage(-1, -1);
                            Bitmap img = Assistant.viewToBitmap(view);
                            int ow = img.getWidth();
                            int oh = img.getHeight();
                            Bitmap b = Bitmap.createScaledBitmap(img, (int) (ow/1.9),(int) (oh/1.9), false);
                            printer.drawBitmap(b, 0, 0);
                            int printResult = printer.printPage(0);

                            if (callback != null)
                                callback.onDone();
                        },
                        waitTime
                );
    }

    @NonNull
    @Override
    public String getPaymentType() {
        return PARSIAN;
    }
}
