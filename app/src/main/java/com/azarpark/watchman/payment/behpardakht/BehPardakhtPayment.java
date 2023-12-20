package com.azarpark.watchman.payment.behpardakht;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.azarpark.watchman.activities.QRScanerActivity;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.PaymentData;
import com.azarpark.watchman.models.PaymentResult;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.payment.PaymentService;
import com.azarpark.watchman.payment.Printer;
import com.azarpark.watchman.payment.ShabaType;
import com.azarpark.watchman.payment.behpardakht.device.Device;
import com.azarpark.watchman.payment.behpardakht.device.IPosPrinterEvent;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import com.azarpark.watchman.web_service.responses.CreateTransactionResponse;
import com.azarpark.watchman.web_service.responses.VerifyTransactionResponse;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BehPardakhtPayment extends PaymentService {

    public static final int PAYMENT_REQUEST_CODE = 105;
    private final String versionName = "1.0.0";
    private final String BEH_PARDAKHT = "beh_pardakht";
    private final int applicationId = 0; //todo: given from beh pardakht

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

            System.out.println("---------> resss : " + data.getStringExtra("PaymentResult"));

            Transaction transaction = new Transaction(
                    paymentResult.transactionAmount == null || paymentResult.transactionAmount.isEmpty()
                            ? SharedPreferencesRepository.getValue(Constants.AMOUNT, "0")
                            : Integer.toString(Integer.parseInt(paymentResult.transactionAmount) / 10),
                    paymentResult.sessionId,
                    paymentResult.referenceID,
                    Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "-1")),
                    paymentResult.resultCode.equals("000") ? 1 : 0,
                    getPaymentType(),
                    paymentResult.resultDescription == null ? "" : paymentResult.resultDescription,
                    paymentResult.maskedCardNumber,
                    paymentResult.dateOfTransaction,
                    paymentResult.referenceID,
                    paymentResult.resultDescription,
                    paymentResult.dateOfTransaction
            );
            verifyTransaction(transaction);
        }
    }

    @Override
    public void launchPayment(@NonNull ShabaType shabaType, long paymentToken, int amount, @NonNull PlateType plateType, @NonNull String tag1, @NonNull String tag2, @NonNull String tag3, @NonNull String tag4, int placeID) {
        String shaba = shabaType == ShabaType.CHARGE ? Constants.CHARGE_SHABA : Constants.NON_CHARGE_SHABA;

        String[] extras = new String[1];
        extras[0] = Integer.toString(placeID);

        PaymentData paymentData = new PaymentData(
                versionName,
                Long.toString(paymentToken),
                applicationId,
                Integer.toString(amount * 10),
                shaba,
                extras
        );

        Intent intent = new Intent("com.bpmellat.merchant");
        intent.putExtra("PaymentData", gson.toJson(paymentData));
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
        new Printer(Assistant.viewToBitmap(view), waitTime, callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class Printer extends AsyncTask{
        private Bitmap bitmap;
        private int initialWaitTime;
        private OnPrintDone onPrintDone;

        Printer(Bitmap bitmap, int initialWaitTime, OnPrintDone onPrintDone)
        {
            this.bitmap = bitmap;
            this.initialWaitTime = initialWaitTime;
            this.onPrintDone = onPrintDone;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            try {
                Thread.sleep(initialWaitTime);
                device.print(bitmap, new IPosPrinterEvent() {
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
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            onPrintDone.onDone();
        }
    }
}

