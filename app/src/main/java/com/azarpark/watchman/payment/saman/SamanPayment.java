package com.azarpark.watchman.payment.saman;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.azarpark.watchman.core.AppConfig;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.payment.PaymentService;
import com.azarpark.watchman.payment.ShabaType;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.Logger;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.WebService;

public class SamanPayment extends PaymentService {

    public static int PAYMENT_REQUEST_CODE = 104;
    public static int QR_SCANNER_REQUEST_CODE = 1004;
    public SamanServiceConnection connection;

    MessageDialog messageDialog;


    private String STATE = "State", SAMAN = "saman", SCANNER_RESULT = "ScannerResult";

    int STATE_SUCCESSFUL = 0;

    public SamanPayment(@NonNull AppCompatActivity activity, @NonNull WebService webService, @NonNull OnPaymentCallback paymentCallback) {
        super(activity, webService, paymentCallback);
    }

    @Override
    public void initialize() {
        super.initialize();

        Log.i("TAG", "initService()");
        connection = new SamanServiceConnection();
        Intent i = new Intent();
        i.setClassName("ir.sep.android.smartpos", "ir.sep.android.Service.Proxy");
        boolean ret = getActivity().bindService(i, connection, Context.BIND_AUTO_CREATE);
        Log.i("TAG", "initService() bound value: " + ret);
    }

    @Override
    public void stop() {
        super.stop();

        getActivity().unbindService(connection);
        connection = null;
        Log.d(TAG, "releaseService(): unbound.");
    }

    @Override
    public void onActivityResultHandler(int requestCode, int resultCode, @NonNull Intent data) {
        System.out.println("----------> handleResult " + resultCode);
        if (/*resultCode == Activity.RESULT_OK &&*/ requestCode == PAYMENT_REQUEST_CODE) {
            int state = data.getIntExtra(STATE, -1);
            System.out.println("----------> state : " + state);

            if (state != 0)
                Toast.makeText(getActivity(), state + "", Toast.LENGTH_LONG).show();

            Transaction transaction;
            if (state == STATE_SUCCESSFUL) // successful
            {
                String tag1 = SharedPreferencesRepository.getValue(Constants.TAG1, "0");
                String tag2 = SharedPreferencesRepository.getValue(Constants.TAG2, "0");
                String tag3 = SharedPreferencesRepository.getValue(Constants.TAG3, "0");
                String tag4 = SharedPreferencesRepository.getValue(Constants.TAG4, "0");

                if (tag2 == null) tag2 = "null";
                if (tag3 == null) tag3 = "null";
                if (tag4 == null) tag4 = "null";

                int status = state == 0 ? 1 : -1;
                String refNum = data.getExtras().getString("RefNum", "");
                String resNum = data.getExtras().getString("ResNum", "");
                String amount = data.getExtras().getString("Amount", "0");
                String pan = data.getExtras().getString("Pan", "");
                String dateTime = data.getExtras().getString("DateTime", "0");
                String traceNumber = data.getExtras().getString("TraceNumber", "");
                String result = data.getExtras().getString("result", "");

                transaction = new Transaction(
                        SharedPreferencesRepository.getValue(Constants.AMOUNT, "0"),
                        resNum,
                        refNum,
                        Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID)),
                        status,
                        SAMAN,
                        Integer.toString(state),
                        pan,
                        dateTime,
                        traceNumber,
                        result,
                        Assistant.getUnixTime());
            } else {
                String result = data.getExtras().getString("result", "");
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
                        SAMAN,
                        Integer.toString(state),
                        "****-****-****-****",
                        "0",
                        null,
                        result,
                        Assistant.getUnixTime());

                Log.e("saman payment", "Purchase did failed....");
//                messageDialog = new MessageDialog("خطا ی " + state, result, "خروج", () -> {
//                    if (messageDialog != null)
//                        messageDialog.dismiss();
//                });
                messageDialog = MessageDialog.newInstance(
                        "خطا ی " + state,
                        result,
                        "خروج",
                        () -> {
                            if (messageDialog != null)
                                messageDialog.dismiss();
                        }
                );

                messageDialog.show(getActivity().getSupportFragmentManager(), MessageDialog.TAG);
            }
            boolean isWageTransaction = Boolean.parseBoolean(SharedPreferencesRepository.getValue(Constants.IS_WAGE_TRANSACTION, "false"));
            transaction.setWage(isWageTransaction);
            SharedPreferencesRepository.updateTransactions02(transaction);
            verifyTransaction(transaction);
        } else if (resultCode == Activity.RESULT_OK && requestCode == QR_SCANNER_REQUEST_CODE) {
            try {
                String url = data.getStringExtra(SCANNER_RESULT);
                System.out.println("----------> url : " + url);
                int placeId = Integer.parseInt(url.split("=")[url.split("=").length - 1]);
                getPaymentCallback().onScanDataReceived(placeId);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "معتبر نمیباشد", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void print(View view, int waitTime, OnPrintDone callback) {
        new Handler(getActivity().getMainLooper())
                .postDelayed(
                        () -> {
                            connection.print(Assistant.viewToBitmap(view));
                            if (callback != null)
                                callback.onDone();
                        },
                        waitTime
                );
    }

    @Override
    public void launchPayment(ShabaType shabaType, long paymentToken, int amount, @NonNull PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {
        System.out.println("----------> tashimPaymentRequest ");

        String shaba = "000000000000000000000000000000:" + (amount * 10) + ":";
        if(AppConfig.Companion.isArdabil()){
            shaba += Constants.ARDABIL_SHABA;
        }
        else{ // TABRIZ
            boolean isWage = false;
            try{
                isWage = Boolean.parseBoolean(SharedPreferencesRepository.getValue(Constants.IS_WAGE_TRANSACTION, "false"));
            }
            catch (Exception e){
                e.printStackTrace();
            }
            if(isWage){
                shaba += SharedPreferencesRepository.getValue(Constants.WAGE_SHABA);
            }
            else if (shabaType == ShabaType.CHARGE) {
                shaba += Constants.CHARGE_SHABA;
            } else {
                shaba += Constants.NON_CHARGE_SHABA;
            }
        }
        Logger.d("--------------> Saman shaba: %s", shaba);
        String[] param = new String[]{shaba};


        Intent intent = new Intent();
        intent.putExtra("TransType", 3);
        intent.putExtra("Amount", String.valueOf(amount * 10));
        intent.putExtra("ResNum", Long.toString(paymentToken));
        intent.putExtra("AppId", "0");
        intent.putExtra("Tashim", param);


        Logger.d(
                "requesting Tashim payment: (\nTransType: %d\nAmount: %s\nResNum: %s\nAppId: %s\nTashim: %s\n)",
                intent.getExtras().getInt("TransType"),
                intent.getExtras().getString("Amount"),
                intent.getExtras().getString("ResNum"),
                intent.getExtras().getString("AppId"),
                intent.getExtras().getStringArray("Tashim")
        );

        intent.setComponent(new ComponentName("ir.sep.android.smartpos", "ir.sep.android.smartpos.ThirdPartyActivity"));
        getActivity().startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }


    @Override
    public void launchQrCodeScanner() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("ir.sep.android.smartpos",
                "ir.sep.android.smartpos.ScannerActivity"));
        getActivity().startActivityForResult(intent, SamanPayment.QR_SCANNER_REQUEST_CODE);
    }

    @Override
    public String getPaymentType() {
        return SAMAN;
    }
}
