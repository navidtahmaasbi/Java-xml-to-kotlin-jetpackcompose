package com.azarpark.watchman.payment.saman;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.web_service.responses.CreateTransactionResponse;
import com.azarpark.watchman.web_service.responses.VerifyTransactionResponse;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import com.google.gson.Gson;

import java.util.Set;

import ir.sep.android.Service.IProxy;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SamanPayment {

    Activity activity;
    Context context;
    public static int PAYMENT_REQUEST_CODE = 1003;
    public static int QR_SCANNER_REQUEST_CODE = 1004;
    public IProxy service;
    public MyServiceConnection connection;
    SamanPaymentCallBack samanPaymentCallBack;
    MessageDialog messageDialog;
    FragmentManager fragmentManager;

    private String STATE = "State",
            REF_NUM = "RefNum",
            SAMAN = "saman",
            SCANNER_RESULT = "ScannerResult",
            RES_NUM = "ResNum";

    int STATE_SUCCESSFUL = 0;

    public SamanPayment(FragmentManager fragmentManager, Context context, Activity activity, SamanPaymentCallBack samanPaymentCallBack) {

        this.activity = activity;
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.samanPaymentCallBack = samanPaymentCallBack;

        initService();

    }

    private void initService() {

        Log.i("TAG", "initService()");
        connection = new MyServiceConnection(service);
        Intent i = new Intent();
        i.setClassName("ir.sep.android.smartpos", "ir.sep.android.Service.Proxy");
        boolean ret = activity.bindService(i, connection, Context.BIND_AUTO_CREATE);
        Log.i("TAG", "initService() bound value: " + ret);
    }

    //------------------------------------------------------------------------------------------------------------------------------

    public void paymentRequest(String resNum, int amount, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {


        SharedPreferencesRepository.setValue(Constants.PLATE_TYPE, plateType.toString());
        SharedPreferencesRepository.setValue(Constants.TAG1, tag1);
        SharedPreferencesRepository.setValue(Constants.TAG2, tag2);
        SharedPreferencesRepository.setValue(Constants.TAG3, tag3);
        SharedPreferencesRepository.setValue(Constants.TAG4, tag4);
        SharedPreferencesRepository.setValue(Constants.TAG4, tag4);
        SharedPreferencesRepository.setValue(Constants.AMOUNT, String.valueOf(amount));
        SharedPreferencesRepository.setValue(Constants.PLACE_ID, Integer.toString(placeID));
        SharedPreferencesRepository.setValue(Constants.OUR_TOKEN, resNum);

        Intent intent = new Intent();
        intent.putExtra("TransType", 1);
        intent.putExtra("Amount", String.valueOf(amount));
        intent.putExtra("ResNum", resNum);
        intent.putExtra("AppId", "0");

//        for (String key:intent.getExtras().keySet()) {
        Log.d("-----> TransType", String.valueOf(intent.getExtras().getInt("TransType")));
        Log.d("-----> Amount", intent.getExtras().getString("Amount"));
        Log.d("-----> ResNum", intent.getExtras().getString("ResNum"));
        Log.d("-----> AppId", intent.getExtras().getString("AppId"));

//        }

        intent.setComponent(new ComponentName("ir.sep.android.smartpos", "ir.sep.android.smartpos.ThirdPartyActivity"));

        activity.startActivityForResult(intent, PAYMENT_REQUEST_CODE);

    }

    public void tashimPaymentRequest(String shaba, String resNum, int amount, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {

        System.out.println("----------> tashimPaymentRequest ");

        String[] param = new String[1];
        param[0] = shaba;


        Intent intent = new Intent();
        intent.putExtra("TransType", 3);
        intent.putExtra("Amount", String.valueOf(amount));
        intent.putExtra("ResNum", resNum);
        intent.putExtra("AppId", "0");
        intent.putExtra("Tashim", param);


//        for (String key:intent.getExtras().keySet()) {
        Log.d("-----> TransType", String.valueOf(intent.getExtras().getInt("TransType")));
        Log.d("-----> Amount", intent.getExtras().getString("Amount"));
        Log.d("-----> ResNum", intent.getExtras().getString("ResNum"));
        Log.d("-----> AppId", intent.getExtras().getString("AppId"));
//        }

        Gson gson = new Gson();
        System.out.println("---------> intent : " + gson.toJson(intent));

        intent.setComponent(new ComponentName("ir.sep.android.smartpos", "ir.sep.android.smartpos.ThirdPartyActivity"));

        activity.startActivityForResult(intent, PAYMENT_REQUEST_CODE);

    }

    public void handleResult(int requestCode, int resultCode, Intent data) {

        System.out.println("----------> handleResult " + resultCode);

        if (/*resultCode == Activity.RESULT_OK &&*/ requestCode == PAYMENT_REQUEST_CODE) {


            int state = data.getIntExtra(STATE, -1);

            System.out.println("----------> state : " + state);

            if (state != 0)
                Toast.makeText(context, state + "", Toast.LENGTH_LONG).show();

//            AdditionalData :
//            ++State : 55
//            ++RefNum : 819972850538
//            ++ResNum : 8019291329
//            ++Amount : 1000.0
//            ++Pan : 622106-fdaffd-8750
//            ++DateTime : 211009182155
//            ++TraceNumber : 598994
//            ++result : رمز اشتباه واردشده است
//            --TerminalId : 00002280
//            --AmountAffective : 1000.0


//        ++result : (succeed / unsucceed)
//        ++rrn : 801663199541
//        ++res_num : -1 (our_token)
//        ++amount : 000000001000
//        ++pan : 589210***2557
//        ++date : 23732049000
//        ++trace : 000015
//        ++message :    (this will have value if there is an error)

            Transaction transaction;

            if (state == STATE_SUCCESSFUL) // successful
            {
//                Log.e("saman payment", "Purchase did successful....");


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
                        SharedPreferencesRepository.getValue(Constants.AMOUNT),
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


//                if (!result.isEmpty())
//                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();

                Log.e("saman payment", "Purchase did failed....");
                messageDialog = new MessageDialog("خطا ی " + state, result, "خروج", () -> {
                    if (messageDialog != null)
                        messageDialog.dismiss();
                });

                messageDialog.show(fragmentManager, MessageDialog.TAG);
            }

            SharedPreferencesRepository.updateTransactions02(transaction);

            Gson gson = new Gson();

            verifyTransaction(transaction);


        } else if (resultCode == Activity.RESULT_OK && requestCode == QR_SCANNER_REQUEST_CODE) {

            try {

                String url = data.getStringExtra(SCANNER_RESULT);
                System.out.println("----------> url : " + url);
                int placeId = Integer.parseInt(url.split("=")[url.split("=").length - 1]);
                samanPaymentCallBack.getScannerData(placeId);

            } catch (Exception e) {
                Toast.makeText(context, "معتبر نمیباشد", Toast.LENGTH_LONG).show();
            }


        }

    }

    public void releaseService() {
        activity.unbindService(connection);
        connection = null;
        Log.d(TAG, "releaseService(): unbound.");
    }

    public void createTransaction(String shaba, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int amount, int placeID, int transactionType, String mobile) {

        Runnable functionRunnable = () -> createTransaction(shaba, plateType, tag1, tag2, tag3, tag4, amount, placeID, transactionType, mobile);

        String t1 = tag1 == null ? "" : tag1;
        String t2 = tag2 == null ? "-1" : tag2;
        String t3 = tag3 == null ? "-1" : tag3;
        String t4 = tag4 == null ? "-1" : tag4;

        WebService.getClient(context).createTransaction(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), t1, t2, t3, t4, amount, transactionType, mobile).enqueue(new Callback<CreateTransactionResponse>() {
            @Override
            public void onResponse(Call<CreateTransactionResponse> call, Response<CreateTransactionResponse> response) {

                if (NewErrorHandler.apiResponseHasError(response, context))
                    return;

                long our_token = response.body().our_token;

                Transaction transaction = new Transaction(
                        Integer.toString(amount),
                        Long.toString(our_token),
                        "0",
                        Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0")),
                        0,
                        SAMAN,
                        "0",
                        "",
                        "",
                        "",
                        "",
                        Assistant.getUnixTime());

                SharedPreferencesRepository.addToTransactions02(transaction);

                SharedPreferencesRepository.setValue(Constants.PLATE_TYPE, plateType.toString());
                SharedPreferencesRepository.setValue(Constants.TAG1, tag1);
                SharedPreferencesRepository.setValue(Constants.TAG2, tag2);
                SharedPreferencesRepository.setValue(Constants.TAG3, tag3);
                SharedPreferencesRepository.setValue(Constants.TAG4, tag4);
                SharedPreferencesRepository.setValue(Constants.AMOUNT, String.valueOf(amount));
                SharedPreferencesRepository.setValue(Constants.PLACE_ID, Integer.toString(placeID));
                SharedPreferencesRepository.setValue(Constants.OUR_TOKEN, Long.toString(our_token));

                tashimPaymentRequest("0:" + (amount * 10) + ":" + shaba, Long.toString(our_token), (amount * 10), plateType, tag1, tag2, tag3, tag4, placeID);


            }

            @Override
            public void onFailure(Call<CreateTransactionResponse> call, Throwable t) {
                NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable);
            }
        });

    }

    public void verifyTransaction(Transaction transaction) {

        Runnable functionRunnable = () -> verifyTransaction(transaction);

        WebService.getClient(context).verifyTransaction(SharedPreferencesRepository.getTokenWithPrefix(), transaction.getAmount(), transaction.getOur_token(),
                transaction.getBank_token(), transaction.getPlaceID(), transaction.getStatus(), transaction.getBank_type(), transaction.getState(),
                transaction.getCard_number(), transaction.getBank_datetime(), transaction.getTrace_number(), transaction.getResult_message()).enqueue(new Callback<VerifyTransactionResponse>() {
            @Override
            public void onResponse(Call<VerifyTransactionResponse> call, Response<VerifyTransactionResponse> response) {

                if (NewErrorHandler.apiResponseHasError(response, context))
                    return;

                SharedPreferencesRepository.removeFromTransactions02(transaction);
                Toast.makeText(context, response.body().getDescription(), Toast.LENGTH_SHORT).show();
                samanPaymentCallBack.onVerifyFinished();

            }

            @Override
            public void onFailure(Call<VerifyTransactionResponse> call, Throwable t) {
                NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable);
            }
        });

    }

    public void printParkInfo(ViewGroup viewGroupForBindFactor) {

        connection.print(getViewBitmap(viewGroupForBindFactor));

    }

    //------------------------------------------------------------------------------------------------------------------------------

    public static Bitmap getViewBitmap(View v) {

        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    private String getBundleString(Bundle b) {

//        sample
//        amount : 000000001000
//        result : (succeed / unsucceed)
//        pan : 589210***2557
//        rrn : 801663199541
//        date : 23732049000
//        trace : 000015
//        message :    (this will have value if there is an error)
//        res_num : -1 (our_token)
//        charge_pin : null

        Set<String> keys = b.keySet();

        StringBuffer sb = new StringBuffer();

        for (String key : keys) {
            sb.append(key);
            sb.append(" : ");
            if (key.equals("State"))
                sb.append(b.getInt(key));
            else
                sb.append(b.getString(key));
            sb.append("\n");
        }

        return sb.toString();

    }

    //------------------------------------------------------------------------------------------------------------------------------

    public interface SamanPaymentCallBack {

        public void verifyTransaction(Transaction transaction);

        public void getScannerData(int placeID);

        public void onVerifyFinished();
    }

}
